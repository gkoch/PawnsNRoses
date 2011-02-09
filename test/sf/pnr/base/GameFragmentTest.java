package sf.pnr.base;

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
        final UciRunner[] players = GamePlayTest.getPlayers();
        System.out.println("Running tournament with the following engines:");
        for (UciRunner player: players) {
            System.out.println("  - " + player.getName());
        }
        final UciRunner refEngine = BestMoveTest.getReferenceEngine();
        System.out.println("against reference engine " + refEngine.getName());
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

        new EpdProcessor().process(testFiles, new GameFragmentTask(players, refEngine, moveTime, moveCount), rndSeed);

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
        private final long[] scores;
        private final int[] depths;
        private final long[] nodes;
        private final long[] moveTimes;
        private final UciRunner referenceEngine;
        private final int moveTime;
        private final int moveCount;

        private GameFragmentTask(final UciRunner[] engines, final UciRunner referenceEngine, final int moveTime,
                                 final int moveCount) {
            this.engines = engines;
            scores = new long[engines.length];
            depths = new int[engines.length];
            nodes = new long[engines.length];
            moveTimes = new long[engines.length];
            this.moveTime = moveTime;
            this.moveCount = moveCount;
            this.referenceEngine = referenceEngine;
        }

        @Override
        public void run(final String fileName, final Board board, final Map<String, String> commands) {
            final String fen = StringUtils.toFen(board);
            System.out.println(fen);
            for (int i = 0; i < engines.length; i++) {
                runTest(i, StringUtils.fromFen(fen));
            }
        }

        private int runTest(final int engineIndex, final Board board) {
            final UciRunner engine = engines[engineIndex];
            System.out.print(engine.getName());
            try {
                final List<Integer> moves = new ArrayList<Integer>(moveCount * 2);
                engine.uciNewGame();
                referenceEngine.uciNewGame();
                final int state = board.getState();
                int fullMoves = (state & Utils.FULL_MOVES) >> Utils.SHIFT_FULL_MOVES;
                final boolean whiteToStart = (state & Utils.WHITE_TO_MOVE) == Utils.WHITE_TO_MOVE;
                if (!whiteToStart) {
                    System.out.printf("\r\n%d. ...", fullMoves);
                }
                int score = 0;
                long testNodeCount = 0;
                int testDepth = 0;
                long testMoveTime = 0;
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
                        score = Evaluation.VAL_MATE;
                        break;
                    }
                    if (!whiteToStart) {
                        System.out.printf("\r\n%d.", fullMoves + i + 1);
                    }
                    move(referenceEngine, board, moves);
                    if (board.isMate()) {
                        score = Evaluation.VAL_MATE;
                        break;
                    }
                    score = -referenceEngine.getScore();
                }
                System.out.printf("\r\ndepth: %.2f, nodes: %d, time: %dms, nps: %.1f, cp: %d\r\n",
                    ((double) testDepth) / i, testNodeCount, testMoveTime, ((double) testNodeCount * 1000) / testMoveTime, score);
                return score;
            } catch (IOException e) {
                throw new UndeclaredThrowableException(e, String.format("Engine '%s' failed on test FEN '%s'",
                    engine.getName(), StringUtils.toFen(board)));
            }
        }

        private long move(final UciRunner engine, final Board board, final List<Integer> moves) throws IOException {
            engine.position(board);
            engine.go(0, moveTime);
            final String moveStr = engine.getBestMove();
            int move = StringUtils.fromLong(board, moveStr);
            moves.add(move);
            System.out.printf(" %s (d:%d, n:%d, t:%dms, nps: %.1f, cp: %d)", StringUtils.toShort(board, move),
                engine.getDepth(), engine.getNodeCount(), engine.getMoveTime(),
                ((double) engine.getNodeCount() * 1000) / engine.getMoveTime(), engine.getScore());
            return board.move(move);
        }

        @Override
        public void completed() {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}