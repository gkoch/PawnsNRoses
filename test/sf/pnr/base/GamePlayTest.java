package sf.pnr.base;

import sf.pnr.io.UCI;
import sf.pnr.io.UncloseableOutputStream;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class GamePlayTest {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final UciRunner[] players = getPlayers();
        System.out.println("Running tournament with the following engines:");
        for (UciRunner player: players) {
            System.out.println("  - " + player.getName());
        }
        FileOutputStream debugOs = null;
        final String debugFile = System.getProperty("searchTask.debugFile");
        if (debugFile != null) {
            debugOs = new FileOutputStream(debugFile);
            final UncloseableOutputStream os = new UncloseableOutputStream(debugOs);
            for (UciRunner player: players) {
                player.setDebugOutputStream(os, player.getName() + " ");
            }
        }
        final int initialTime = Integer.parseInt(System.getProperty("searchTask.initialTime", "120000"));
        final int increment = Integer.parseInt(System.getProperty("searchTask.incrementTime", "6000"));
        final int rounds = Integer.parseInt(System.getProperty("searchTask.rounds", "20"));
        final GameManager manager = new GameManager(getEngineDir().getName(), initialTime, increment, rounds);
        final String kibitzerPath = System.getProperty("searchTask.kibitzer");
        if (kibitzerPath != null) {
            final UciRunner kibitzer =
                new UciRunner(new File(kibitzerPath).getName(), new ExternalUciProcess(kibitzerPath));
            manager.setKibitzer(kibitzer);
            System.out.println("Kibitzer: " + kibitzer.getName());
        }
        manager.play(players);
        if (debugOs != null) {
            debugOs.close();
        }
        for (UciRunner player: players) {
            player.close();
        }
    }

    public static UciRunner[] getPlayers() throws IOException {
        final File engineDir = getEngineDir();
        final File[] executables;
        if (engineDir != null) {
            final String patternStr = System.getProperty("searchTask.enginePattern", ".*");
            final Pattern executablePattern = Pattern.compile(patternStr);
            executables = engineDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(final File file) {
                    return file.isFile() && executablePattern.matcher(file.getName()).matches();
                }
            });
        } else {
            executables = new File[0];
        }
        final boolean includeLatest = Boolean.parseBoolean(System.getProperty("searchTask.includeLatest", "false"));
        final int shift = includeLatest? 1: 0;
        final UciRunner[] players = new UciRunner[executables.length + shift];
        if (includeLatest) {
            final Map<String, String> options = new HashMap<String, String>();
            options.put(UCI.toUciOption(Configurable.Key.TRANSP_TABLE_SIZE),
                System.getProperty(Configurable.Key.TRANSP_TABLE_SIZE.getKey(), "128"));
            options.put(UCI.toUciOption(Configurable.Key.EVAL_TABLE_SIZE),
                System.getProperty(Configurable.Key.EVAL_TABLE_SIZE.getKey(), "8"));
            players[0] = new UciRunner("Pawns N' Roses Latest", options, new PipedUciProcess());
        }
        for (int i = 0, executablesLength = executables.length; i < executablesLength; i++) {
            final File executable = executables[i];
            players[i + shift] =
                new UciRunner(getPlayerName(executable), new ExternalUciProcess(executable.getAbsolutePath()));
        }
        return players;
    }

    public static File getEngineDir() {
        final String path = System.getProperty("searchTask.engineDir");
        final File engineDir;
        if (path != null) {
            engineDir = new File(path);
        } else {
            engineDir = null;
        }
        return engineDir;
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