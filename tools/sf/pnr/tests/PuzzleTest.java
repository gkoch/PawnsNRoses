package sf.pnr.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class PuzzleTest {

    public static void main(String[] args) throws IOException {
        final List<String> testFiles = new ArrayList<String>();
        testFiles.add("pos.epd");
        testFiles.add("best7.epd");
        testFiles.add("wnperm.epd");
        testFiles.add("qtest_easy.epd");
        final SearchTask task = new SearchTask(100);
        new EpdProcessor().process(testFiles, task);
    }
}