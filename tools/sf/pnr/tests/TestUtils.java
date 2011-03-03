package sf.pnr.tests;

import sf.pnr.base.Configurable;
import sf.pnr.base.Configuration;
import sf.pnr.io.UCI;
import sf.pnr.io.UncloseableOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TestUtils {

    public static UciRunner[] getEngines() throws IOException {
        UciRunner[] engines = getEngines(System.getProperty("searchTask.engines"));
        final boolean includeLatest = Boolean.parseBoolean(System.getProperty("searchTask.includeLatest", "false"));
        if (includeLatest) {
            final UciRunner[] enginesWithLatest = new UciRunner[engines.length + 1];
            System.arraycopy(engines, 0, enginesWithLatest, 1, engines.length);
            engines = enginesWithLatest;
            final Map<String, String> options = new HashMap<String, String>();
            options.put(UCI.toUciOption(Configurable.Key.TRANSP_TABLE_SIZE), "128");
            options.put(UCI.toUciOption(Configurable.Key.EVAL_TABLE_SIZE), "8");
            final Map<Configurable.Key, String> systemProps = Configuration.preprocess(System.getProperties(), true);
            for (Map.Entry<Configurable.Key, String> entry: systemProps.entrySet()) {
                options.put(UCI.toUciOption(entry.getKey()), entry.getValue());
            }
            engines[0] = new UciRunner("Pawns N' Roses Latest", options, new PipedUciProcess());
        }
        final String debugFile = System.getProperty("searchTask.debugFile");
        if (debugFile != null) {
            final UncloseableOutputStream os = new UncloseableOutputStream(new FileOutputStream(debugFile));
            for (UciRunner player: engines) {
                player.setDebugOutputStream(os, player.getName() + " ");
                os.incCounter();
            }
        }

        return engines;
    }

    public static UciRunner[] getReferenceEngines() throws IOException {
        return getEngines(System.getProperty("searchTask.referenceEngines"));
    }

    public static UciRunner[] getEngines(final String patternStr) throws IOException {
        final File parent = getRootDir(patternStr);
        final List<File> allFiles = collectAllFiles(parent);
        final List<File> matches = new ArrayList<File>();
        final Pattern pattern = Pattern.compile(patternStr.replace("\\", "\\\\"));
        for (File child: allFiles) {
            if (pattern.matcher(child.getAbsolutePath()).matches()) {
                matches.add(child);
            }
        }
        final UciRunner[] engines = new UciRunner[matches.size()];
        for (int i = 0; i < matches.size(); i++) {
            final File file = matches.get(i);
            engines[i] = new UciRunner(getPlayerName(file), new ExternalUciProcess(file.getAbsolutePath()));
        }
        return engines;
    }

    private static File getRootDir(final String patternStr) {
        File parent = new File(patternStr);
        while (!parent.exists()) {
            parent = parent.getParentFile();
        }
        return parent;
    }

    static List<File> collectAllFiles(final File file) {
        final List<File> files = new ArrayList<File>();
        if (file.isDirectory()) {
            final File[] children = file.listFiles();
            for (File child: children) {
                files.addAll(collectAllFiles(child));
            }
        } else {
            files.addAll(Collections.singletonList(file));
        }
        return files;
    }

    public static File getEngineDir() {
        return getRootDir(System.getProperty("searchTask.engines"));
    }

    public static String getPlayerName(final File executable) {
        final String fileName = executable.getName();
        final int pos = fileName.lastIndexOf('.');
        final String name;
        if (pos != -1 && pos >= fileName.length() - 4) {
            name = fileName.substring(0, pos);
        } else {
            name = fileName;
        }
        return name;
    }
}
