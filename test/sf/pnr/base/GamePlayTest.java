package sf.pnr.base;

import sf.pnr.io.UCI;
import sf.pnr.io.UncloseableOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GamePlayTest {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final UciRunner[] players = getTestPlayers();
        FileOutputStream debugOs = null;
        final String debugFile = System.getProperty("searchTest.debugFile");
        if (debugFile != null) {
            debugOs = new FileOutputStream(debugFile);
            final UncloseableOutputStream os = new UncloseableOutputStream(debugOs);
            for (UciRunner player: players) {
                player.setDebugOutputStream(os, player.getName() + " ");
            }
        }
        final GameManager manager = new GameManager(3000, 300, 30);
        manager.play(players);
        if (debugOs != null) {
            debugOs.close();
        }
        for (UciRunner player: players) {
            player.close();
        }
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