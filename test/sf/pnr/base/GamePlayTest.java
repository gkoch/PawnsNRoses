package sf.pnr.base;

import sf.pnr.io.UCI;
import sf.pnr.io.UncloseableOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GamePlayTest {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final String engineDir = System.getProperty("searchTest.engineDir");
        final Map<String, String> options = new HashMap<String, String>();
        options.put(UCI.toUciOption(Configurable.Key.TRANSP_TABLE_SIZE), "128");
        options.put(UCI.toUciOption(Configurable.Key.EVAL_TABLE_SIZE), "8");
        final UciRunner pnrLatest = new UciRunner("Pawns N' Roses Latest", options, new PipedUciProcess());
        final UciRunner pnrV0054 = new UciRunner("Pawns N' Roses v0.054", null,
            new ExternalUciProcess(new String[]{"\"" + engineDir + "/PawnsNRoses/v0.05x/PawnsNRoses v0.054.bat\""},
                new File(engineDir + "/PawnsNRoses/v0.05x/")));
        final UciRunner pnrV0055 = new UciRunner("Pawns N' Roses v0.055", null,
            new ExternalUciProcess(new String[]{"\"" + engineDir + "/PawnsNRoses/v0.05x/PawnsNRoses v0.055.bat\""},
                new File(engineDir + "/PawnsNRoses/v0.05x/")));
        final UciRunner rybka22 = new UciRunner("Rybka 2.2 - 2 cores", null,
            new ExternalUciProcess(new String[]{"\"" + engineDir + "/Rybka/Rybka v2.2n2.mp.w32.exe\""},
                new File(engineDir)));
        final UciRunner mediocre = new UciRunner("Mediocre 0.34", null,
            new ExternalUciProcess(new String[]{"\"" + engineDir + "/mediocre-0.34/Mediocre.bat\""},
                new File(engineDir, "mediocre-0.34")));
        FileOutputStream debugOs = null;
        final String debugFile = System.getProperty("searchTest.debugFile");
        if (debugFile != null) {
            debugOs = new FileOutputStream(debugFile);
            final UncloseableOutputStream os = new UncloseableOutputStream(debugOs);
            pnrLatest.setDebugOutputStream(os, "pnr ");
            pnrV0054.setDebugOutputStream(os, "v54 ");
            pnrV0055.setDebugOutputStream(os, "v55 ");
            mediocre.setDebugOutputStream(os, "med ");
            rybka22.setDebugOutputStream(os, "ryb ");
        }
        final GameManager manager = new GameManager(1000, 100, 30);
        manager.play(pnrLatest, pnrV0055, pnrV0054, rybka22);
        if (debugOs != null) {
            debugOs.close();
        }
        pnrLatest.close();
        pnrV0054.close();
        pnrV0055.close();
        mediocre.close();
        rybka22.close();
    }
}
