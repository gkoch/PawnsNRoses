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
public class MirrorEvalTest extends TestCase {

    public void testAll() throws IOException {
        final List<String> testFiles = new ArrayList<String>();
        testFiles.add("arasan6.perft");
        testFiles.add("arbitrations.perft");
        testFiles.add("bench3.fixed.perft");
        testFiles.add("perftsuite.epd");
        final MirrorEvalTask task = new MirrorEvalTask();
        new EpdProcessor(this.getClass()).process(testFiles, task);
        assertEquals(0, task.getFailureCount());
    }

    private static class MirrorEvalTask implements EpdProcessorTask {
        private int failureCount;
        private final Evaluation eval = new Evaluation();

        public int getFailureCount() {
            return failureCount;
        }

        @Override
        public void run(final String fileName, final Board board, final Map<String, String> commands) {
            eval.clear();
            final int score = eval.evaluate(board);
            final String fen = StringUtils.toFen(board);
            final String mirroredFen = StringUtils.mirrorFen(fen);
            final Board mirroredBoard = StringUtils.fromFen(mirroredFen);
            eval.clear();
            final int mirroredScore = eval.evaluate(mirroredBoard);
            if (score != mirroredScore) {
                System.out.printf("Score %d for FEN '%s', doesn't match the score %d for FEN '%s'\r\n",
                    score, fen, mirroredScore, mirroredFen);
                failureCount++;
            }
        }

        @Override
         public void completed() {
            // do nothing
        }
    }
}