package sf.pnr.base;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 */
public class ConfigurationTest {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Config directory expected");
        }

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

        final Properties defaults = new Properties();
        for (Configurable.Key key: Configurable.Key.values()) {
            final String value = Configuration.getInstance().getString(key);
            defaults.setProperty(StringUtils.toUciOption(key), value);
        }

//        final Properties defaultValues = new Properties();
//        for (Configurable.Key key: Configurable.Key.values()) {
//            final String value = Configuration.getInstance().getString(key);
//            defaultValues.setProperty(key.getKey(), value);
//        }
//        defaultValues.store(new FileWriter("defaults.ini"), "Default values");

        final List<UciRunner> runners = new ArrayList<UciRunner>(files.length);
        for (File file: files) {
            final Properties properties = new Properties();
            final FileReader reader = new FileReader(file.getName());
            try {
                properties.load(reader);
            } finally {
                reader.close();
            }
            final Map<Configurable.Key, String> configs = Configuration.preprocess(properties);
            final Properties uciOptions = new Properties(defaults);
            for (Map.Entry<Configurable.Key, String> entry: configs.entrySet()) {
                uciOptions.setProperty(StringUtils.toUciOption(entry.getKey()), entry.getValue());
            }
            runners.add(new UciRunner("Pawns N' Roses - " + file.getName(), uciOptions, new PipedUciProcess()));
        }
        try {
            new EpdProcessor().process(testFiles, new MultiEngineSearchTask(runners, 6, 0, 20));
        } finally {
            for (UciRunner runner : runners) {
                runner.close();
            }
        }
    }
}