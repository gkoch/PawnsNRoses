package sf.pnr.tests;

import sf.pnr.base.Board;
import sf.pnr.base.StringUtils;
import sf.pnr.base.Utils;
import sf.pnr.io.UncloseableOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GameFragmentTest {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final UciRunner[] players = TestUtils.getEngines();
        final UciRunner refEngine = TestUtils.getReferenceEngines()[0];
        final UciRunner kibitzer = TestUtils.getKibitzer();
        FileOutputStream debugOs = null;
        final String debugFile = System.getProperty("searchTask.debugFile");
        if (debugFile != null) {
            debugOs = new FileOutputStream(debugFile);
            final UncloseableOutputStream os = new UncloseableOutputStream(debugOs);
            for (UciRunner player: players) {
                player.setDebugOutputStream(os, player.getName() + " ");
            }
        }
        final int moveTime = Integer.parseInt(System.getProperty("searchTask.moveTime", "6000"));
        final int moveCount = Integer.parseInt(System.getProperty("searchTask.moveCount", "5"));
        final long rndSeed = Long.parseLong(System.getProperty("searchTask.rndSeed", "-1"));

        final List<String> testFiles = new ArrayList<String>();
        testFiles.add("pos.epd");
        testFiles.add("best7.epd");
        testFiles.add("wnperm.epd");
        testFiles.add("qtest_easy.epd");
        testFiles.add("en passant.epd");
        testFiles.add("ans.epd");

        new EpdProcessor().process(testFiles, new GameFragmentTask(players, refEngine, kibitzer, moveTime, moveCount), rndSeed);

        if (debugOs != null) {
            debugOs.close();
        }
        for (UciRunner player: players) {
            player.close();
        }
        refEngine.close();
    }
    
    private static class GameFragmentTask implements EpdProcessorTask {

        private final UciRunner[] engines;
        private final UciRunner kibitzer;
        private final int[] depths;
        private final long[] nodeCounts;
        private final long[] moveTimes;
        private final int[] moveCounts;
        private final int[] kibitzerScoreDiffs;
        private final UciRunner referenceEngine;
        private final int moveTime;
        private final int moveCount;
        private int testCount;

        private GameFragmentTask(final UciRunner[] engines, final UciRunner referenceEngine, final UciRunner kibitzer,
                                 final int moveTime, final int moveCount) {
            this.engines = engines;
            this.referenceEngine = referenceEngine;
            this.kibitzer = kibitzer;
            depths = new int[engines.length];
            nodeCounts = new long[engines.length];
            moveTimes = new long[engines.length];
            moveCounts = new int[engines.length];
            kibitzerScoreDiffs = new int[engines.length];
            this.moveTime = moveTime;
            this.moveCount = moveCount;
        }

        @Override
        public void run(final String fileName, final Board board, final Map<String, String> commands) {
            testCount++;
            final String fen = StringUtils.toFen(board);
            System.out.println(testCount + ": " + fen);
            for (int i = 0; i < engines.length; i++) {
                runTest(i, StringUtils.fromFen(fen));
            }
            System.out.println();
        }

        private void runTest(final int engineIndex, final Board board) {
            final UciRunner engine = engines[engineIndex];
            System.out.print(engine.getName());
            runTest(engineIndex, board, engine, referenceEngine);
        }

        private void runTest(final int engineIndex, final Board board, final UciRunner engine, final UciRunner opponent) {
            try {
                final int initialKibitzerScore = getKibitzerScore(board);
                final List<Integer> moves = new ArrayList<Integer>(moveCount * 2);
                engine.uciNewGame();
                opponent.uciNewGame();
                final int state = board.getState();
                int fullMoves = (state & Utils.FULL_MOVES) >> Utils.SHIFT_FULL_MOVES;
                final boolean whiteToStart = (state & Utils.WHITE_TO_MOVE) == Utils.WHITE_TO_MOVE;
                if (!whiteToStart) {
                    System.out.printf("\r\n%d. ...", fullMoves);
                }
                long testNodeCount = 0;
                int testDepth = 0;
                long testMoveTime = 0;
                long opponentTestNodeCount = 0;
                int opponentTestDepth = 0;
                long opponentTestMoveTime = 0;
                int i = 0;
                for (; i < moveCount; i++) {
                    if (whiteToStart) {
                        System.out.printf("\r\n%d.", fullMoves + i);
                    }
                    move(engine, board, moves);
                    testDepth += engine.getDepth();
                    testNodeCount += engine.getNodeCount();
                    testMoveTime += engine.getMoveTime();
                    if (board.isMate()) {
                        break;
                    }
                    if (!whiteToStart) {
                        System.out.printf("\r\n%d.", fullMoves + i + 1);
                    }
                    move(opponent, board, moves);
                    opponentTestDepth += opponent.getDepth();
                    opponentTestNodeCount += opponent.getNodeCount();
                    opponentTestMoveTime += opponent.getMoveTime();
                    if (board.isMate()) {
                        break;
                    }
                }
                depths[engineIndex] += testDepth;
                nodeCounts[engineIndex] += testNodeCount;
                moveTimes[engineIndex] += testMoveTime;
                moveCounts[engineIndex] += i;
                final int finalKibitzerScore =
                    (1 - 2 * Math.abs((state & Utils.WHITE_TO_MOVE) - (board.getState() & Utils.WHITE_TO_MOVE))) *
                        getKibitzerScore(board);
                final int kibitzerScoreDiff = finalKibitzerScore - initialKibitzerScore;
                kibitzerScoreDiffs[engineIndex] += kibitzerScoreDiff;
                System.out.printf("\r\ndepth: %.2f (%.2f), nodes: %d (%.1f), time: %dms (%.1f), nps: %.1f (%.1f), k.cp diff: %d (%d -> %d, avg: %.1f)\r\n",
                    ((double) testDepth) / i, ((double) depths[engineIndex]) / moveCounts[engineIndex],
                    testNodeCount, ((double) nodeCounts[engineIndex]) / moveCounts[engineIndex],
                    testMoveTime, ((double) moveTimes[engineIndex]) / moveCounts[engineIndex],
                    ((double) testNodeCount * 1000) / testMoveTime, ((double) nodeCounts[engineIndex] * 1000) / moveTimes[engineIndex],
                    kibitzerScoreDiff, initialKibitzerScore, finalKibitzerScore, ((double) kibitzerScoreDiffs[engineIndex]) / testCount);
                System.out.printf("depth: %.2f, nodes: %d, time: %dms, nps: %.1f\r\n",
                    ((double) opponentTestDepth) / i, opponentTestNodeCount, opponentTestMoveTime,
                    ((double) opponentTestNodeCount * 1000) / testMoveTime);
            } catch (IOException e) {
                throw new UndeclaredThrowableException(e, String.format("Engine '%s' failed on test FEN '%s'",
                    engine.getName(), StringUtils.toFen(board)));
            }
        }

        private int getKibitzerScore(final Board board) throws IOException {
            final int kibitzerScore;
            if (kibitzer != null) {
                System.out.printf("\r\nKibitzer (%s) move: ", kibitzer);
                kibitzer.uciNewGame();
                move(kibitzer, board, moveTime * 2);
                kibitzerScore = kibitzer.getScore();
            } else {
                kibitzerScore = 0;
            }
            return kibitzerScore;
        }

        private long move(final UciRunner engine, final Board board, final List<Integer> moves) throws IOException {
            final int move = move(engine, board, moveTime);
            moves.add(move);
            return board.move(move);
        }

        private int move(final UciRunner engine, final Board board, final int moveTime) throws IOException {
            engine.position(board);
            engine.go(0, moveTime);
            final String moveStr = engine.getBestMove();
            final int move = StringUtils.fromLong(board, moveStr);
            System.out.printf(" %s (d:%d, n:%d, t:%dms, nps: %.1f, cp: %d)", StringUtils.toShort(board, move),
                engine.getDepth(), engine.getNodeCount(), engine.getMoveTime(),
                ((double) engine.getNodeCount() * 1000) / engine.getMoveTime(), engine.getScore());
            return move;
        }

        @Override
        public void completed() {
        }
    }
}