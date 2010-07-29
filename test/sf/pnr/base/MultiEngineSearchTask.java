package sf.pnr.base;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
*/
public class MultiEngineSearchTask implements EpdProcessorTask {

    private static final Comparator<Stats> FAILURE_COUNT_COMPARATOR = new Comparator<Stats>() {
        @Override
        public int compare(final Stats s1, final Stats s2) {
            return s1.getFailureCount() - s2.getFailureCount();
        }
    };

    private final List<UciRunner> runners;
    private final int fixedDepth;
    private final int timeToSolve;
    private final int debugPrintInterval;
    private int testCount = 0;
    private final Stats[] stats;
    private final boolean debugStats = false;
    private final long startTime;

    public MultiEngineSearchTask(final List<UciRunner> runners, final int fixedDepth, final int timeToSolve,
                                 final int debugPrintInterval) {
        this.runners = runners;
        this.fixedDepth = fixedDepth;
        this.timeToSolve = timeToSolve;
        this.debugPrintInterval = debugPrintInterval;
        stats = new Stats[runners.size()];
        final Iterator<UciRunner> iter = runners.iterator();
        for (int i = 0; i < stats.length; i++) {
            stats[i] = new Stats(iter.next().getName());
        }
        startTime = System.currentTimeMillis();
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
            for (int i = 0; i < runners.size(); i++) {
                final UciRunner runner = runners.get(i);
                runner.uciNewGame();
                runner.position(board);
                runner.go(depth, time);
                boolean passed = true;
                try {
                    if (commands.containsKey("bm") || commands.containsKey("pm")) {
                        final String engineBestMove =
                            StringUtils.toShort(board, StringUtils.fromLong(board, runner.getBestMove()));
                        final String[] bestMoves =
                            (commands.containsKey("bm")? commands.get("bm"): commands.get("pm")).split("/");
                        if (engineBestMove == null || !StringUtils.containsString(bestMoves, engineBestMove)) {
                            passed = false;
                            if (debugStats) {
                                System.out.printf(
                                    "Engine '%s' failed on test '%s'. Best moves: %s, engine recommended: %s\r\n",
                                    runner.getName(), StringUtils.toFen(board), commands.get("bm"), engineBestMove);
                            }
                        }
                    } else if (commands.containsKey("am")) {
                        final String engineBestMove =
                            StringUtils.toShort(board, StringUtils.fromLong(board, runner.getBestMove()));
                        final String[] avoidMoves = commands.get("am").split("/");
                        if (engineBestMove == null || StringUtils.containsString(avoidMoves, engineBestMove)) {
                            passed = false;
                            if (debugStats) {
                                System.out.printf(
                                    "Engine '%s' failed on test '%s'. Avoid moves: %s, engine recommended: %s\r\n",
                                    runner.getName(), StringUtils.toFen(board), commands.get("am"), engineBestMove);
                            }
                        }
                    }
                } catch (IllegalStateException e) {
                    System.out.printf("Failed to extract best move for FEN: %s\r\n", StringUtils.toFen(board));
                    throw e;
                }

                stats[i].addResult(runner.getNodeCount(), runner.getDepth(), runner.getMoveTime(), passed);
                if (debugStats) {
                    System.out.printf("%s - nodes: %d, depth: %d, time: %d\r\n",
                        runner.getName(), runner.getNodeCount(), runner.getDepth(), runner.getMoveTime());
                }
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
        System.out.printf("Statistics after %d tests (elapsed time: %2.1fs):\r\n",
            testCount, ((double) System.currentTimeMillis() - startTime) / 1000);
        System.out.printf("%40s\t%5s\t%5s\t%9s\t%5s\t%7s\r\n", "Engine name", "%", "ply", "nodes", "msec", "nodes/sec");
        final Stats[] sorted = new Stats[stats.length];
        System.arraycopy(stats, 0, sorted, 0, stats.length);
        Arrays.sort(sorted, FAILURE_COUNT_COMPARATOR);
        for (Stats stats: sorted) {
            System.out.printf("%40s\t%5.1f\t%5.1f\t%9.1f\t%3.2f\t%9.1f\r\n", stats.getName(), stats.getPassRate() * 100,
                stats.getAvgDepth(), stats.getAvgNodeCount(), stats.getAvgMoveTime(), stats.getNodesPerSec());
        }
    }

    private static class Stats {
        private final String name;
        private long totalNodeCount;
        private int totalDepth;
        private long totalMoveTime;
        private int failureCount;
        private int testCount;

        public Stats(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void addResult(final long nodeCount, final int depth, final long moveTime, final boolean passed) {
            totalNodeCount += nodeCount;
            totalDepth += depth;
            totalMoveTime += moveTime;
            if (!passed) {
                failureCount++;
            }
            testCount++;
        }

        public double getAvgNodeCount() {
            return ((double) totalNodeCount) / testCount;
        }

        public double getAvgDepth() {
            return ((double) totalDepth) / testCount;
        }

        public double getAvgMoveTime() {
            return ((double) totalMoveTime) / testCount;
        }

        public double getPassRate() {
            return ((double) (testCount - failureCount)) / testCount;
        }

        public double getNodesPerSec() {
            return ((double) totalNodeCount * 1000) / totalMoveTime;
        }
    }
}