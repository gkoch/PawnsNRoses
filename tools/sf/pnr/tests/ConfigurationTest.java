package sf.pnr.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 */
public class ConfigurationTest {

    public static void main(final String[] args) throws IOException, ExecutionException, InterruptedException {

        final List<String> testFiles = new ArrayList<String>();
        testFiles.add("pos.epd");
        testFiles.add("best7.epd");
        testFiles.add("wnperm.epd");
        testFiles.add("qtest_easy.epd");
        testFiles.add("en passant.epd");
        testFiles.add("ans.epd");

        final UciRunner[] engines = TestUtils.getEngines();
        try {
            final int depth = Integer.parseInt(System.getProperty("searchTest.maxDepth", "0"));
            final int time = Integer.parseInt(System.getProperty("searchTest.maxTime", "100"));
            final int printInterval = Integer.parseInt(System.getProperty("searchTest.printInterval", "20"));

            final MultiEngineSearchTask searchTask = new MultiEngineSearchTask(engines, depth, time, printInterval);
            searchTask.setEliminateInterval(Integer.parseInt(System.getProperty("searchTask.eliminateInterval",
                Integer.toString(searchTask.getEliminateInterval()))));
            searchTask.setEliminateCount(Integer.parseInt(System.getProperty("searchTask.eliminateCount",
                Integer.toString(searchTask.getEliminateCount()))));
            searchTask.setEliminateMinRemaining(Integer.parseInt(System.getProperty("searchTask.eliminateMinRemaining",
                Integer.toString(searchTask.getEliminateMinRemaining()))));
            searchTask.setEliminateMinPercentageDiff(Double.parseDouble(System.getProperty("searchTask.eliminateMinPercentageDiff",
                Double.toString(searchTask.getEliminateMinPercentageDiff()))));

            final long rndSeed = Long.parseLong(System.getProperty("searchTask.rndSeed", "-1"));
            new EpdProcessor().process(testFiles, searchTask, rndSeed);
        } finally {
            for (UciRunner engine: engines) {
                engine.close();
            }
        }
    }
}