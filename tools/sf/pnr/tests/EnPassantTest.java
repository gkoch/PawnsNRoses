package sf.pnr.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class EnPassantTest {

    public static void main(final String[] args) throws IOException {
        final List<String> testFiles = new ArrayList<String>();
        testFiles.add("en passant.epd");
        final SearchTask task = new SearchTask(50);
        final long startTime = System.currentTimeMillis();
        new EpdProcessor().process(testFiles, task);
        final long endTime = System.currentTimeMillis();
        final long totalNodeCount = task.getTotalNodeCount();
        final long testCount = task.getTestCount();
        final long failureCount = task.getFailureCount();

        final double processTime = ((double) (endTime - startTime)) / 1000;
        System.out.printf(
            "Solved %d en passant puzzles, in %.2f seconds. Pass ratio: %.2f%%, total node count: %d (%.1f nodes/s)\r\n",
            testCount, processTime, (((double) (testCount - failureCount)) * 100)/ testCount,
            totalNodeCount, ((double) totalNodeCount) / processTime);
    }
}