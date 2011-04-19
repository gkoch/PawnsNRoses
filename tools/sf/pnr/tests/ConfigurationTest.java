package sf.pnr.tests;

import sf.pnr.base.Configuration;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 */
public class ConfigurationTest {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Config directory expected");
        }
        Configuration.getInstance().loadFromSystemProperties();

        final List<String> testFiles = new ArrayList<String>();
        testFiles.add("pos.epd");
        testFiles.add("best7.epd");
        testFiles.add("wnperm.epd");
        testFiles.add("qtest_easy.epd");
        testFiles.add("en passant.epd");
        testFiles.add("ans.epd");

        final File[] files = new File(args[0]).listFiles(new FileFilter() {
            @Override
            public boolean accept(final File file) {
                return file.isFile() && file.getName().endsWith(".ini");
            }
        });

        final Map<String, String> defaults = TestUtils.getDefaultConfigs();
        final Map<String, String> postSearchOptions = new LinkedHashMap<String, String>();
//        postSearchOptions.put("Command", "releaseEngine");

//        final Properties defaultValues = new Properties();
//        for (Configurable.Key key: Configurable.Key.values()) {
//            final String value = Configuration.getInstance().getString(key);
//            defaultValues.setProperty(key.getKey(), value);
//        }
//        defaultValues.store(new FileWriter("defaults.ini"), "Default values");

        final List<UciRunner> runners = new ArrayList<UciRunner>(files.length);
        for (File file: files) {
            final Map<String, String> uciOptions = TestUtils.getConfigs(file, defaults);
            runners.add(new UciRunner(
                "Pawns N' Roses - " + file.getName(), uciOptions, postSearchOptions, new PipedUciProcess()));
        }
        try {
            final int depth = Integer.parseInt(System.getProperty("searchTest.maxDepth", "0"));
            final int time = Integer.parseInt(System.getProperty("searchTest.maxTime", "100"));
            final int printInterval = Integer.parseInt(System.getProperty("searchTest.printInterval", "20"));
            final MultiEngineSearchTask searchTask = new MultiEngineSearchTask(runners, depth, time, printInterval);
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
            for (UciRunner runner : runners) {
                runner.close();
            }
        }
    }
}