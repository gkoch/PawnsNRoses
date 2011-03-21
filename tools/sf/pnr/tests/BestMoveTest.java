package sf.pnr.tests;

import sf.pnr.base.Board;
import sf.pnr.base.StringUtils;
import sf.pnr.io.UncloseableOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class BestMoveTest {
    public static void main(final String[] args) throws IOException, ExecutionException, InterruptedException {
        final UciRunner[] players = TestUtils.getEngines();
        System.out.println("Running best move test with the following engines:");
        for (UciRunner player: players) {
            System.out.println("  - " + player.getName());
        }
        final UciRunner[] refEngines = TestUtils.getReferenceEngines();
        System.out.print("against reference engines ");
        for (int i = 0; i < refEngines.length; i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.print(refEngines[i].getName());
        }
        System.out.println();
        FileOutputStream debugOs = null;
        final String debugFile = System.getProperty("searchTask.debugFile");
        if (debugFile != null) {
            debugOs = new FileOutputStream(debugFile);
            final UncloseableOutputStream os = new UncloseableOutputStream(debugOs);
            for (UciRunner player: players) {
                player.setDebugOutputStream(os, player.getName() + " ");
            }
        }
        final int depth = Integer.parseInt(System.getProperty("searchTask.depth", "8"));
        final int time = Integer.parseInt(System.getProperty("searchTask.moveTime", "0"));
        final int printInterval = Integer.parseInt(System.getProperty("searchTask.printInterval", "10"));
        final int maxScore = Integer.parseInt(System.getProperty("searchTask.maxScore", "20000"));
        final long rndSeed = Long.parseLong(System.getProperty("searchTask.rndSeed", "-1"));

        final List<String> testFiles = new ArrayList<String>();
        //testFiles.add("pos.epd");
        //testFiles.add("best7.epd");
        //testFiles.add("wnperm.epd");
        //testFiles.add("qtest_easy.epd");
        //testFiles.add("en passant.epd");
        //testFiles.add("ans.epd");
        testFiles.add("gmgames-min3.epd");

        new EpdProcessor().process(testFiles, new BestMoveTask(players, refEngines, depth, time, printInterval, maxScore), rndSeed);

        if (debugOs != null) {
            debugOs.close();
        }
        for (UciRunner player: players) {
            player.close();
        }
        for (UciRunner player: refEngines) {
            player.close();
        }
    }

    private static class BestMoveTask implements EpdProcessorTask {

        private final UciRunner[] engines;
        private final long[] scores;
        private final long[] nodeCounts;
        private final long[] moveTimes;
        private final int[] depths;
        private final int[][] allScores;
        private final int depth;
        private final int time;
        private final int printInterval;
        private final int maxScore;
        private int testCount;
        private long startTime;
        private int maxNameLen;
        private int referenceEngineCount;

        private BestMoveTask(final UciRunner[] engines, final UciRunner[] referenceEngines, final int depth, final int time,
                             final int printInterval, final int maxScore) {
            this.maxScore = maxScore;
            this.engines = new UciRunner[referenceEngines.length + engines.length];
            this.referenceEngineCount = referenceEngines.length;
            System.arraycopy(referenceEngines, 0, this.engines, 0, referenceEngines.length);
            System.arraycopy(engines, 0, this.engines, referenceEngines.length, engines.length);
            this.printInterval = printInterval;
            scores = new long[this.engines.length];
            nodeCounts = new long[this.engines.length];
            moveTimes = new long[this.engines.length];
            depths = new int[this.engines.length];
            allScores = new int[this.engines.length][1000];
            this.depth = depth;
            this.time = time;
            startTime = System.currentTimeMillis();
            maxNameLen = 0;
            for (UciRunner engine: engines) {
                final int nameLen = engine.getName().length();
                if (nameLen > maxNameLen) {
                    maxNameLen = nameLen;
                }
            }
        }

        @Override
        public void run(final String fileName, final Board board, final Map<String, String> commands) {
            if (allScores[0].length <= testCount) {
                expandAllScores();
            }
            testCount++;
            System.out.println("Running test on FEN: " + StringUtils.toFen(board));
            final Map<String, int[]> scoresMap = new HashMap<String, int[]>();
            final int[] engineScores = new int[engines.length];
            int maxScore = Integer.MIN_VALUE;
            for (int i = 0; i < engines.length; i++) {
                final UciRunner engine = engines[i];
                compute(engine, board, depth, time);
                final String bestMoveStr = engine.getBestMove();
                final int[] scores;
                if (scoresMap.containsKey(bestMoveStr)) {
                    scores = scoresMap.get(bestMoveStr);
                } else {
                    scores = getScores(board, bestMoveStr);
                    scoresMap.put(bestMoveStr, scores);
                }
                int score = 0;
                for (int j = 0; j < referenceEngineCount; j++) {
                    score += scores[j];
                }
                score = limitScore(score / referenceEngineCount);
                if (score > maxScore) {
                    maxScore = score;
                }
                System.out.printf("  - engine '%s' moved '%s' (depth %d, time: %dms) score: %d %s\r\n",
                    engine.getName(), bestMoveStr, engine.getDepth(), engine.getMoveTime(), score, Arrays.toString(scores));
                engineScores[i] = score;
                nodeCounts[i] += engine.getNodeCount();
                moveTimes[i] += engine.getMoveTime();
                depths[i] += engine.getDepth();
            }

            for (int i = 0; i < engineScores.length; i++) {
                final int score = engineScores[i] - maxScore;
                scores[i] += score;
                allScores[i][testCount - 1] = score;
                Arrays.sort(allScores[i]);
            }

            if (testCount % printInterval == 0) {
                printStats();
            }
        }

        private void expandAllScores() {
            for (int i = 0; i < engines.length; i++) {
                int[] newScores = new int[allScores[i].length + 1000];
                System.arraycopy(allScores[i], 0, newScores, 0, allScores[i].length);
                allScores[i] = newScores;
            }
        }

        private void printStats() {
            System.out.printf("Statistics after %d tests (elapsed time: %2.1fs):\r\n",
                testCount, ((double) System.currentTimeMillis() - startTime) / 1000);
            System.out.printf("%" + (maxNameLen + 4) + "s\t%7s\t%7s\t%9s\t%6s\t%9s\t%7s\r\n",
                    "Engine name", "cp(avg)", "cp(mid)", "nodes", "ms", "nodes/sec", "depth");
            for (int i = 0; i < engines.length; i++) {
                final UciRunner engine = engines[i];
                final double avgScore = ((double) scores[i]) / testCount;
                final int from = testCount / 100 + 1;
                final int end = testCount - from;
                double midScore = 0;
                for (int j = from; j < end; j++) {
                    midScore += allScores[i][j];
                }
                if (from < end) {
                    midScore /= testCount - from * 2;
                }
                final double avgNodeCount = ((double) nodeCounts[i]) / testCount;
                final double avgMoveTime = ((double) moveTimes[i]) / testCount;
                final double avgDepth = ((double) depths[i]) / testCount;
                System.out.printf("%" + (maxNameLen + 4) + "s\t%7.1f\t%7.1f\t%9.1f\t%6.0f\t%9.1f\t%7.2f\r\n",
                    engine.getName(), avgScore, midScore, avgNodeCount, avgMoveTime, (avgNodeCount * 1000) / avgMoveTime,
                    avgDepth);
            }
        }

        private void compute(final UciRunner engine, final Board board, final int depth, final int time) {
            try {
                engine.uciNewGame();
                engine.position(board);
                if (time == 0) {
                    engine.go(depth, 0);
                } else {
                    engine.go(0, time);
                }
            } catch (IOException e) {
                try {
                    engine.close();
                } catch (IOException e1) {
                    // do nothing, propagate the outer exception
                    e1.printStackTrace(System.out);
                }
                throw new UndeclaredThrowableException(e, String.format("Engine '%s' failed on test FEN '%s'",
                    engine.getName(), StringUtils.toFen(board)));
            }
        }

        private int[] getScores(final Board board, final String moveStr) {
            final int move = StringUtils.fromLong(board, moveStr);
            final long undo = board.move(move);
            final int[] scores = new int[referenceEngineCount];
            try {
                for (int i = 0; i < referenceEngineCount; i++) {
                    compute(engines[i], board, depth + 1, time);
                    scores[i] = -engines[i].getScore();
                }
            } finally {
                board.takeBack(undo);
            }
            return scores;
        }

        private int limitScore(final int score) {
            if (score < -maxScore) {
                return -maxScore;
            } else if (score > maxScore) {
                return maxScore;
            } else {
                return score;
            }
        }

        @Override
        public void completed() {
            if (testCount % printInterval != 0) {
                printStats();
            }
        }
    }
}