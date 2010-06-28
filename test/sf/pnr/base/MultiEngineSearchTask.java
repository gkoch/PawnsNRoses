package sf.pnr.base;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;

/**
*/
public class MultiEngineSearchTask implements EpdProcessorTask {

    private final UciRunner[] runners;
    private final int fixedDepth;
    private final int timeToSolve;
    private final int debugPrintInterval;
    private int testCount = 0;
    private final long[] totalNodeCount;
    private final int[] totalDepth;
    private final long[] totalMoveTime;
    private final int[] failureCount;

    public MultiEngineSearchTask(final UciRunner[] runners, final int fixedDepth, final int timeToSolve,
                                 final int debugPrintInterval) {
        this.runners = runners;
        this.fixedDepth = fixedDepth;
        this.timeToSolve = timeToSolve;
        this.debugPrintInterval = debugPrintInterval;
        failureCount = new int[runners.length];
        totalNodeCount = new long[runners.length];
        totalDepth = new int[runners.length];
        totalMoveTime = new long[runners.length];
    }

    @Override
    public void run(final String fileName, final Board board, final Map<String, String> commands) {
        testCount++;

        if (commands.containsKey("fmvn")) {
            final int fullMoveCount = Integer.parseInt(commands.get("fmvn"));
            board.setState((board.getState() & Utils.CLEAR_FULL_MOVES) | (fullMoveCount << Utils.SHIFT_FULL_MOVES));
        }
        if (commands.containsKey("hmvc")) {
            final int halfMoveCount = Integer.parseInt(commands.get("hmvc"));
            board.setState((board.getState() & Utils.CLEAR_HALF_MOVES) | (halfMoveCount << Utils.SHIFT_HALF_MOVES));
        }

        final int depth;
        final int time;
        if (timeToSolve <= 0) {
            if (fixedDepth > 0) {
                depth = fixedDepth;
            } else if (commands.containsKey("acd")) {
                depth = Integer.parseInt(commands.get("acd"));
            } else {
                depth = Integer.parseInt(commands.get("dm")) * 2;
            }
            time = 0;
        } else {
            depth = 0;
            time = timeToSolve;
        }
        try {
            for (int i = 0; i < runners.length; i++) {
                final UciRunner runner = runners[i];
                runner.uciNewGame();
                runner.position(board);
                runner.go(depth, time);
                boolean passed = true;
                try {
                    if (commands.containsKey("bm")) {
                        final String engineBestMove = runner.getBestMove();
                        final String[] bestMoves = commands.get("bm").split("/");
                        if (engineBestMove == null || !StringUtils.containsString(bestMoves, engineBestMove)) {
                            passed = false;
                        }
                    } else if (commands.containsKey("am")) {
                        final String engineBestMove = runner.getBestMove();
                        final String[] avoidMoves = commands.get("am").split("/");
                        if (engineBestMove == null || StringUtils.containsString(avoidMoves, engineBestMove)) {
                            passed = false;
                        }
                    }
                } catch (IllegalStateException e) {
                    System.out.printf("Failed to extract best move for FEN: %s\r\n", StringUtils.toFen(board));
                    throw e;
                }

                if (!passed) {
                    failureCount[i]++;
                }

                totalNodeCount[i] += runner.getNodeCount();
                totalDepth[i] += runner.getDepth();
                totalMoveTime[i] += runner.getMoveTime();
                System.out.printf("%s - nodes: %d, depth: %d, time: %d\r\n",
                    runner.getName(), runner.getNodeCount(), runner.getDepth(), runner.getMoveTime());
            }
        } catch (IOException e) {
            throw new UndeclaredThrowableException(e,
                String.format("Error at test #%d (FEN: %s)", testCount, StringUtils.toFen(board)));
        }

        if (testCount % debugPrintInterval == 0) {
            printStats();
        }
    }

    private void printStats() {
        System.out.printf("Statistics after %d tests:\r\n", testCount);
        System.out.printf("%25s\t%5s\t%5s\t%9s\t%5s\t%7s\r\n", "Engine name", "%", "ply", "nodes", "msec", "nodes/sec");
        for (int i = 0; i < runners.length; i++) {
            final UciRunner runner = runners[i];
            System.out.printf("%25s\t%5.1f\t%5.1f\t%9.1f\t%3.2f\t%9.1f\r\n", runner.getName(),
                ((double) (testCount - failureCount[i]) * 100) / testCount, ((double) totalDepth[i]) / testCount,
                ((double) totalNodeCount[i]) / testCount, ((double) totalMoveTime[i]) / testCount,
                ((double) totalNodeCount[i]) / totalMoveTime[i] * 1000);
        }
    }
}