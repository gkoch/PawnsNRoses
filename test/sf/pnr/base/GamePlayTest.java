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
        final GameManager manager = new GameManager(initialTime, increment, rounds);
        manager.play(players);
        if (debugOs != null) {
            debugOs.close();
        }
        for (UciRunner player: players) {
            player.close();
        }
    }

    private static UciRunner[] getPlayers() throws IOException {
        final String engineDir = System.getProperty("searchTask.engineDir");
        final String patternStr = System.getProperty("searchTask.enginePattern", ".*");
        final Pattern executablePattern = Pattern.compile(patternStr);
        final File[] executables = new File(engineDir).listFiles(new FileFilter() {
            @Override
            public boolean accept(final File file) {
                return file.isFile() && executablePattern.matcher(file.getName()).matches();
            }
        });
        final UciRunner[] players = new UciRunner[executables.length];
        for (int i = 0, executablesLength = executables.length; i < executablesLength; i++) {
            final File executable = executables[i];
            players[i] = new UciRunner(getPlayerName(executable), new ExternalUciProcess(executable.getAbsolutePath()));
        }
        return players;
    }

    private static String getPlayerName(final File executable) {
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

    private static UciRunner[] getTestPlayers() throws IOException {
        final String engineDir = System.getProperty("searchTest.engineDir");
        final Map<String, String> options = new HashMap<String, String>();
        options.put(UCI.toUciOption(Configurable.Key.TRANSP_TABLE_SIZE), "128");
        options.put(UCI.toUciOption(Configurable.Key.EVAL_TABLE_SIZE), "8");
        final UciRunner pnrLatest = new UciRunner("Pawns N' Roses Latest", options, new PipedUciProcess());
        final UciRunner pnrV0054 = new UciRunner("Pawns N' Roses v0.054",
            new ExternalUciProcess(engineDir + "/PawnsNRoses/v0.05x/PawnsNRoses v0.054.bat"));
        final UciRunner pnrV0055 = new UciRunner("Pawns N' Roses v0.055",
            new ExternalUciProcess(engineDir + "/PawnsNRoses/v0.05x/PawnsNRoses v0.055.bat"));
        final UciRunner rybka22 = new UciRunner("Rybka 2.2 - 2 cores",
            new ExternalUciProcess(engineDir + "/Rybka/Rybka v2.2n2.mp.w32.exe"));
        final UciRunner mediocre = new UciRunner("Mediocre 0.34",
            new ExternalUciProcess(engineDir + "/mediocre-0.34/Mediocre.bat"));
        return new UciRunner[]{pnrLatest, pnrV0054, pnrV0055, rybka22, mediocre};
    }
}