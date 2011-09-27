package sf.pnr.tests;

import sf.pnr.base.Configurable;
import sf.pnr.io.UCI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 */
public class PerformanceRegressionTest {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final List<String> testFiles = new ArrayList<String>();
        testFiles.add("pos.epd");
        testFiles.add("best7.epd");
        testFiles.add("wnperm.epd");
        testFiles.add("qtest_easy.epd");
        final UciRunner[] engines = TestUtils.getEngines();
        try {
            new EpdProcessor().process(testFiles, new MultiEngineSearchTask(engines, 10, 0, 10));
        } finally {
            for (UciRunner engine: engines) {
                engine.close();
            }
        }
    }
}                                                                