package sf.pnr.base;

import junit.framework.TestCase;
import sf.pnr.tests.EpdProcessor;
import sf.pnr.tests.EpdProcessorTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 */
public class BigPerftTest extends TestCase {

    public void testAll() throws IOException {
        final List<String> testFiles = new ArrayList<String>();
        testFiles.add("arasan6.perft");
        testFiles.add("arbitrations.perft");
        testFiles.add("bench3.fixed.perft");
        testFiles.add("perftsuite.epd");
        final int maxDepth = Integer.parseInt(System.getProperty("perftBig.maxDepth", "100"));
        final boolean failOnFirstError = Boolean.parseBoolean(System.getProperty("perftBig.failOnFirstError", "true"));
        final PerftTask task = new PerftTask(maxDepth, failOnFirstError);
        new EpdProcessor(this.getClass()).process(testFiles, task);
        assertTrue(task.getFailures().isEmpty());
    }

    private static class PerftTask implements EpdProcessorTask {
        private final MoveGenerator moveGenerator;
        private final List<PerftFailure> failures;
        private final double maxDepth;
        private final boolean failOnFirstError;

        public PerftTask(final double maxDepth, final boolean failOnFirstError) {
            moveGenerator = new MoveGenerator();
            failures = new ArrayList<PerftFailure>(100);
            this.maxDepth = maxDepth;
            this.failOnFirstError = failOnFirstError;
        }

        @Override
        public void run(final String fileName, final Board board, final Map<String, String> commands) {
            for (Map.Entry<String, String> entry: commands.entrySet()) {
                final String command = entry.getKey();
                final String parameter = entry.getValue();
                final String depthStr = command.substring(1);
                final int depth = Integer.parseInt(depthStr);
                if (depth > maxDepth) {
                    continue;
                }
                final long expected = Long.parseLong(parameter);
                final long actual = PerftTest.perft(board, depth, moveGenerator);
                if (failOnFirstError) {
                    assertEquals("FEN: " + StringUtils.toFen(board), expected, actual);
                } else {
                    if (expected != actual) {
                        final PerftFailure failure = new PerftFailure(StringUtils.toFen(board), depth, expected, actual);
                        failures.add(failure);
                        System.out.println(failure);
                    }
                }
            }
        }

        @Override
         public void completed() {
            // do nothing
        }

        public List<PerftFailure> getFailures() {
            return failures;
        }

        public static class PerftFailure {
            final String fen;
            final int depth;
            final long expected;
            final long actual;

            private PerftFailure(final String fen, final int depth, final long expected, final long actual) {
                this.fen = fen;
                this.depth = depth;
                this.expected = expected;
                this.actual = actual;
            }

            public String getFen() {
                return fen;
            }

            public int getDepth() {
                return depth;
            }

            public long getExpected() {
                return expected;
            }

            public long getActual() {
                return actual;
            }

            @Override
            public String toString() {
                return
                    "PerftFailure {fen='" + fen + "', depth=" + depth + ", expected=" + expected + ", actual=" + actual + '}';
            }
        }
    }
}