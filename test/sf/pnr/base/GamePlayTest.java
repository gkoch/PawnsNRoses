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
//        final UciRunner[] players = getTestPlayers();
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
            options.put(UCI.toUciOption(Configurable.Key.TRANSP_TABLE_SIZE), "128");
            options.put(UCI.toUciOption(Configurable.Key.EVAL_TABLE_SIZE), "8");
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

    public static UciRunner[] getTestPlayers() throws IOException {
        final File engineDir = getEngineDir();
        final Map<String, String> options = new HashMap<String, String>();
        options.put(UCI.toUciOption(Configurable.Key.TRANSP_TABLE_SIZE), "128");
        options.put(UCI.toUciOption(Configurable.Key.EVAL_TABLE_SIZE), "8");
        final UciRunner pnrLatest = new UciRunner("Pawns N' Roses Latest", options, new PipedUciProcess());
        final UciRunner pnrV0054 = new UciRunner("Pawns N' Roses v0.054",
            new ExternalUciProcess(new File(engineDir, "PawnsNRoses/v0.05x/PawnsNRoses v0.054.bat").getAbsolutePath()));
        final UciRunner pnrV0055 = new UciRunner("Pawns N' Roses v0.055",
            new ExternalUciProcess(new File(engineDir, "/PawnsNRoses/v0.05x/PawnsNRoses v0.055.bat").getAbsolutePath()));
        final UciRunner rybka22 = new UciRunner("Rybka 2.2 - 2 cores",
            new ExternalUciProcess(new File(engineDir, "/Rybka/Rybka v2.2n2.mp.w32.exe").getAbsolutePath()));
        final UciRunner mediocre = new UciRunner("Mediocre 0.34",
            new ExternalUciProcess(new File(engineDir, "/mediocre-0.34/Mediocre.bat").getAbsolutePath()));
        return new UciRunner[]{pnrLatest, pnrV0054, pnrV0055, rybka22, mediocre};
    }
}