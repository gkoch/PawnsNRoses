package sf.pnr.tests;

import sf.pnr.base.Board;
import sf.pnr.base.StringUtils;
import sf.pnr.base.Utils;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
*/
public class MultiEngineSearchTask implements EpdProcessorTask {

    private static final Comparator<Stats> FAILURE_COUNT_COMPARATOR = new Comparator<Stats>() {
        @Override
        public int compare(final Stats s1, final Stats s2) {
            if (s1.getElimatedAt() != s2.getElimatedAt()) {
                return s2.isAlive()? 1: (s1.isAlive()? -1: s2.getElimatedAt() - s1.getElimatedAt());
            }
            int result = s1.getFailureCount() - s2.getFailureCount();
            if (result == 0) {
                result = (int) (s1.getTotalNodeCount() - s2.getTotalNodeCount());
            }
            if (result == 0) {
                result = (int) (s1.getTotalMoveTime() - s2.getTotalMoveTime());
            }
            return result;
        }
    };

    private final List<UciRunner> runners;
    private final int fixedDepth;
    private final int timeToSolve;
    private final int debugPrintInterval;
    private int testCount = 0;
    private final Stats[] stats;
    private final long startTime;
    private int eliminateInterval = 1000;
    private int eliminateCount = 0;
    private int eliminateMinRemaining = 10;
    private double eliminateMinPercentageDiff = 1.0;
    private int maxNameLen;
    private boolean verbose = false;

    public MultiEngineSearchTask(final List<UciRunner> runners, final int fixedDepth, final int timeToSolve,
                                 final int debugPrintInterval) {
        this.runners = runners;
        this.fixedDepth = fixedDepth;
        this.timeToSolve = timeToSolve;
        this.debugPrintInterval = debugPrintInterval;
        maxNameLen = 0;
        for (UciRunner runner: runners) {
            final int nameLen = runner.getName().length();
            if (nameLen > maxNameLen) {
                maxNameLen = nameLen;
            }
        }
        stats = new Stats[runners.size()];
        final Iterator<UciRunner> iter = runners.iterator();
        for (int i = 0; i < stats.length; i++) {
            stats[i] = new Stats(iter.next().getName());
        }
        startTime = System.currentTimeMillis();
    }

    public int getEliminateInterval() {
        return eliminateInterval;
    }

    public void setEliminateInterval(final int eliminateInterval) {
        this.eliminateInterval = eliminateInterval;
    }

    public int getEliminateCount() {
        return eliminateCount;
    }

    public void setEliminateCount(final int eliminateCount) {
        this.eliminateCount = eliminateCount;
    }

    public int getEliminateMinRemaining() {
        return eliminateMinRemaining;
    }

    public void setEliminateMinRemaining(final int eliminateMinRemaining) {
        this.eliminateMinRemaining = eliminateMinRemaining;
    }

    public double getEliminateMinPercentageDiff() {
        return eliminateMinPercentageDiff;
    }

    public void setEliminateMinPercentageDiff(final double eliminateMinPercentageDiff) {
        this.eliminateMinPercentageDiff = eliminateMinPercentageDiff;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
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
                if (!stats[i].isAlive()) {
                    continue;
                }
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
                            if (verbose) {
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
                            if (verbose) {
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
                if (verbose) {
                    System.out.printf("%s - nodes: %d, depth: %d, time: %d\r\n",
                        runner.getName(), runner.getNodeCount(), runner.getDepth(), runner.getMoveTime());
                }
            }

            if (testCount % eliminateInterval == 0) {
                eliminate();
            }
        } catch (IOException e) {
            throw new UndeclaredThrowableException(e,
                String.format("Error at test #%d (FEN: %s)", testCount, StringUtils.toFen(board)));
        }

        if (testCount % debugPrintInterval == 0) {
            printStats();
        }
    }

    @Override
    public void completed() {
        if (testCount % debugPrintInterval != 0) {
            printStats();
        }
    }

    private void eliminate() throws IOException {
        final TreeMap<Stats, UciRunner> map = new TreeMap<Stats, UciRunner>(FAILURE_COUNT_COMPARATOR);
        int remaining = 0;
        for (int i = 0; i < runners.size(); i++) {
            map.put(stats[i], runners.get(i));
            if (stats[i].isAlive()) {
                remaining++;
            }
        }
        final double bestPct = map.keySet().iterator().next().getPassRate() * 100;
        int eliminated = 0;
        for (Stats stat: map.descendingKeySet()) {
            if (eliminated >= eliminateCount) {
                break;
            }
            if (remaining <= eliminateMinRemaining) {
                break;
            }
            if (!stat.isAlive()) {
                continue;
            }
            final double pct = stat.getPassRate() * 100;
            if (pct > bestPct - eliminateMinPercentageDiff) {
                break;
            }
            stat.setElimatedAt(testCount);
            final UciRunner runner = map.get(stat);
            runner.close();
            eliminated++;
            remaining--;
        }
    }

    private void printStats() {
        System.out.printf("Statistics after %d tests (elapsed time: %2.1fs):\r\n",
            testCount, ((double) System.currentTimeMillis() - startTime) / 1000);
        System.out.println(getHeader());
        final Stats[] sorted = new Stats[stats.length];
        System.arraycopy(stats, 0, sorted, 0, stats.length);
        Arrays.sort(sorted, FAILURE_COUNT_COMPARATOR);
        for (Stats stats: sorted) {
            System.out.println(toString(stats));
        }
    }

    public String getHeader() {
        return String.format("%" + (maxNameLen + 2) + "s\t%6s\t%5s\t%8s\t%6s\t%9s\t%10s",
            "Engine name", "%", "ply", "nodes", "msec", "nodes/sec", "eliminated");
    }

    public String toString(final Stats stats) {
        return String.format("%" + (maxNameLen + 2) + "s\t%6.2f\t%5.2f\t%8.0f\t%6.1f\t%9.0f\t%s",
            stats.getName(), stats.getPassRate() * 100, stats.getAvgDepth(), stats.getAvgNodeCount(),
            stats.getAvgMoveTime(), stats.getNodesPerSec(), stats.isAlive()? "alive": "@" + stats.getElimatedAt());
    }

    private static class Stats {
        private final String name;
        private long totalNodeCount;
        private int totalDepth;
        private long totalMoveTime;
        private int failureCount;
        private int testCount;
        private int elimatedAt;

        public Stats(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public long getTotalNodeCount() {
            return totalNodeCount;
        }

        public long getTotalMoveTime() {
            return totalMoveTime;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public boolean isAlive() {
            return elimatedAt == 0;
        }

        public int getElimatedAt() {
            return elimatedAt;
        }

        public void setElimatedAt(final int elimatedAt) {
            this.elimatedAt = elimatedAt;
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