package sf.pnr.base;

import java.util.Map;

/**
*/
public class SearchTask implements EpdProcessorTask {

    private final Engine engine;
    private final int timeToSolve;
    private final boolean stopAtFirstFailure;
    private final int debugPrintInterval;
    private long totalNodeCount = 0;
    private int testCount = 0;
    private int failureCount = 0;

    public SearchTask(final int timeToSolve) {
        this(timeToSolve, false, 50);
    }

    public SearchTask(final int timeToSolve, final boolean stopAtFirstFailure, final int debugPrintInterval) {
        this.timeToSolve = timeToSolve;
        this.stopAtFirstFailure = stopAtFirstFailure;
        this.debugPrintInterval = debugPrintInterval;
        engine = new Engine();
    }

    @Override
    public void run(final String fileName, final Board board, final Map<String, String> commands) {
        testCount++;
        engine.clear();

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
        if (timeToSolve < 0) {
            if (commands.containsKey("acd")) {
                depth = Integer.parseInt(commands.get("acd"));
            } else {
                depth = Integer.parseInt(commands.get("dm")) * 2;
            }
            time = 0;
        } else {
            depth = 0;
            time = timeToSolve;
        }

        final long startTime = System.currentTimeMillis();
        final long result = engine.search(board, depth, time);
        final long endTime = System.currentTimeMillis();

        if (time > 0 && time * 1.5 < (endTime - startTime)) {
            System.out.printf(
                "Used significantly more time than was allowed. FEN: %s, target time: %d ms, time used: %d ms\r\n",
                StringUtils.toFen(board), time, (endTime - startTime));
        }

        boolean passed = true;
//            Assert.assertEquals(Engine.getMoveFromSearchResult(result), bestLine[0]);
        try {
            if (commands.containsKey("bm") || commands.containsKey("pm")) {
                final int[] bestLine = engine.getBestLine(board, Engine.getMoveFromSearchResult(result));
                final String[] bestMoves =
                    (commands.containsKey("bm")? commands.get("bm"): commands.get("pm")).split("/");
                final String engineBestMove = StringUtils.toShort(board, bestLine[0]);
                if (!StringUtils.containsString(bestMoves, engineBestMove)) {
    //                System.out.printf("Failed to solve puzzle %d. (%s). Engine suggested %s, best moves: %s, commands: %s\r\n",
    //                    testCount, StringUtils.toFen(board), engineBestMove, Arrays.toString(bestMoves), commands);
                    passed = false;
                }
            } else if (commands.containsKey("am")) {
                final int[] bestLine = engine.getBestLine(board, Engine.getMoveFromSearchResult(result));
                final String[] avoidMoves = commands.get("am").split("/");
                final String engineBestMove = StringUtils.toShort(board, bestLine[0]);
                if (StringUtils.containsString(avoidMoves, engineBestMove)) {
    //                System.out.printf("Failed to solve puzzle %d. (%s). Engine suggested %s, avoid moves: %s, commands: %s\r\n",
    //                    testCount, StringUtils.toFen(board), engineBestMove, Arrays.toString(avoidMoves), commands);
                    passed = false;

                }
            }
        } catch (IllegalStateException e) {
            System.out.printf("Failed to extract best move for FEN: %s\r\n", StringUtils.toFen(board));
            e.printStackTrace();
        }

        passed = additionalChecks(engine, board, result, commands) && passed;
        if (!passed) {
            failureCount++;
            if (stopAtFirstFailure) {
                throw new IllegalStateException(String.format("Failed to pass test %d (FEN: %s)",
                    testCount, StringUtils.toFen(board)));
            }
        }

        final long engineNodeCount = engine.getNodeCount();
        totalNodeCount += engineNodeCount;
        if (testCount % debugPrintInterval == 0) {
            System.out.printf("Pass ratio after %d tests is %.2f%% with %d nodes processed\r\n", testCount,
                ((double)(testCount - failureCount) * 100) / testCount, totalNodeCount);
        }
    }

    @Override
    public void completed() {
        if (testCount % debugPrintInterval != 0) {
            System.out.printf("Pass ratio after %d tests is %.2f%% with %d nodes processed\r\n", testCount,
                ((double)(testCount - failureCount) * 100) / testCount, totalNodeCount);
        }
    }

    protected boolean additionalChecks(final Engine engine, final Board board, final long result,
                                       final Map<String, String> commands) {
        return true;
    }

    public long getTotalNodeCount() {
        return totalNodeCount;
    }

    public int getTestCount() {
        return testCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void resetCounters() {
        totalNodeCount = 0;
        testCount = 0;
        failureCount = 0;
    }
}
