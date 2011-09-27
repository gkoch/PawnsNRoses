package sf.pnr.tests;

import sf.pnr.base.Board;
import sf.pnr.base.Configurable;
import sf.pnr.base.Configuration;
import sf.pnr.base.Evaluation;
import sf.pnr.io.UCI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class EvaluationPerformanceTest {

    public static void main(final String[] args) throws IOException, ExecutionException, InterruptedException {
        final Map<String, String> options = new HashMap<String, String>();
        options.put(UCI.toUciOption(Configurable.Key.TRANSP_TABLE_SIZE), "128");
        options.put(UCI.toUciOption(Configurable.Key.EVAL_TABLE_SIZE), "8");
        options.putAll(TestUtils.getDefaultConfigs(true));
        final Map<Configurable.Key, String> systemProps = Configuration.preprocess(System.getProperties(), true);
        for (Map.Entry<Configurable.Key, String> entry: systemProps.entrySet()) {
            options.put(UCI.toUciOption(entry.getKey()), entry.getValue());
        }
        for (Map.Entry<String, String> entry : options.entrySet()) {
            final Configurable.Key key = Configuration.getKey(UCI.fromUciOption(entry.getKey()));
            Configuration.getInstance().setProperty(key, entry.getValue());
        }
        final long rndSeed = Long.parseLong(System.getProperty("searchTask.rndSeed", "-1"));

        final List<String> testFiles = new ArrayList<String>();
        testFiles.add("pos.epd");
        testFiles.add("best7.epd");
        testFiles.add("wnperm.epd");
        testFiles.add("qtest_easy.epd");
        testFiles.add("en passant.epd");
        testFiles.add("ans.epd");

        final long start = System.currentTimeMillis();
        new EpdProcessor().process(testFiles, new EvaluationTask(), rndSeed);
        final long end = System.currentTimeMillis();
        System.out.printf("Completed in %d ms\r\n", end - start);
    }
    
    private static class EvaluationTask implements EpdProcessorTask {

        private int testCount;

        @Override
        public void run(final String fileName, final Board board, final Map<String, String> commands) {
            testCount++;
            new Evaluation().evaluate(board);
            if (testCount % 1000 == 0) {
                System.out.printf("Evaluated %d positions\r\n", testCount);
            }
        }

        @Override
        public void completed() {
        }
    }
}