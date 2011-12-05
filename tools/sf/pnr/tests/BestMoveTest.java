package sf.pnr.tests;

import sf.pnr.base.Board;
import sf.pnr.base.StringUtils;
import sf.pnr.io.UncloseableOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class BestMoveTest {
    public static void main(final String[] args) throws IOException, ExecutionException, InterruptedException {
        final UciRunner[] players = TestUtils.getEngines();
        final UciRunner[] refEngines = TestUtils.getReferenceEngines();
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
        final boolean includeRefEngines = Boolean.parseBoolean(System.getProperty("searchTask.includeRefEngines", "true"));

        final List<String> testFiles = new ArrayList<String>();
        //testFiles.add("pos.epd");
        //testFiles.add("best7.epd");
        //testFiles.add("wnperm.epd");
        //testFiles.add("qtest_easy.epd");
        //testFiles.add("en passant.epd");
        //testFiles.add("ans.epd");
        testFiles.add("gmgames-min3.epd");
        //testFiles.add("med-games.epd");

        new EpdProcessor().process(testFiles, new BestMoveTask(players, refEngines, depth, time, printInterval, maxScore, includeRefEngines), rndSeed);

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
        private final boolean includeRefEngines;
        private int testCount;
        private long startTime;
        private int maxNameLen;
        private int referenceEngineCount;

        private BestMoveTask(final UciRunner[] engines, final UciRunner[] referenceEngines, final int depth, final int time,
                             final int printInterval, final int maxScore, final boolean includeRefEngines) {
            this.engines = new UciRunner[referenceEngines.length + engines.length];
            this.referenceEngineCount = referenceEngines.length;
            System.arraycopy(referenceEngines, 0, this.engines, 0, referenceEngines.length);
            System.arraycopy(engines, 0, this.engines, referenceEngines.length, engines.length);
            scores = new long[this.engines.length];
            nodeCounts = new long[this.engines.length];
            moveTimes = new long[this.engines.length];
            depths = new int[this.engines.length];
            allScores = new int[this.engines.length][1000];
            this.depth = depth;
            this.time = time;
            this.printInterval = printInterval;
            this.maxScore = maxScore;
            this.includeRefEngines = includeRefEngines;
            maxNameLen = TestUtils.getMaxNameLen(engines);
            startTime = System.currentTimeMillis();
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
            final int startIdx = includeRefEngines? 0: referenceEngineCount;
            for (int i = startIdx; i < engines.length; i++) {
                final UciRunner engine = engines[i];
                TestUtils.compute(engine, board, depth, time);
                final long engineNodeCount = engine.getNodeCount();
                final long engineMoveTime = engine.getMoveTime();
                final int engineDepth = engine.getDepth();
                nodeCounts[i] += engineNodeCount;
                moveTimes[i] += engineMoveTime;
                depths[i] += engineDepth;
                final int engineScore = engine.getScore();
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
                System.out.printf("  - engine '%s' moved '%s' (depth %d, time: %dms, cp:%d, nodes:%d) \tscore: %d %s\r\n",
                    engine.getName(), bestMoveStr, engineDepth, engineMoveTime, engineScore, engine.getNodeCount(), score,
                    Arrays.toString(scores));
                engineScores[i] = score;
            }

            for (int i = startIdx; i < engineScores.length; i++) {
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
            System.out.printf("%" + (maxNameLen + 4) + "s\t%7s\t%7s\t%9s\t%6s\t%7s\t%7s\r\n",
                    "Engine name", "cp(avg)", "cp(mid)", "nodes", "ms", "nodes/sec", "depth");
            final int startIdx = includeRefEngines? 0: referenceEngineCount;
            for (int i = startIdx; i < engines.length; i++) {
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
                System.out.printf("%" + (maxNameLen + 4) + "s\t%7.1f\t%7.1f\t%9.0f\t%6.0f\t%7.0f\t%7.2f\r\n",
                    engine.getName(), avgScore, midScore, avgNodeCount, avgMoveTime, (avgNodeCount * 1000) / avgMoveTime,
                    avgDepth);
            }
        }

        private int[] getScores(final Board board, final String moveStr) {
            final int move = StringUtils.fromLong(board, moveStr);
            final long undo = board.move(move);
            try {
                final int[] scores = new int[referenceEngineCount];
                for (int i = 0; i < referenceEngineCount; i++) {
                    TestUtils.compute(engines[i], board, depth + 1, time);
                    scores[i] = -engines[i].getScore();
                }
                return scores;
            } finally {
                board.takeBack(undo);
            }
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