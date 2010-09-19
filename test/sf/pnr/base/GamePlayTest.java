package sf.pnr.base;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GamePlayTest {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final String engineDir = System.getProperty("searchTest.engineDir");
        final Map<String, String> options = new HashMap<String, String>();
        options.put(StringUtils.toUciOption(Configurable.Key.TRANSP_TABLE_SIZE), "128");
        options.put(StringUtils.toUciOption(Configurable.Key.EVAL_TABLE_SIZE), "8");
        final UciRunner pnrLatest = new UciRunner("Pawns N' Roses Latest", options, new PipedUciProcess());
        final UciRunner rybka22 = new UciRunner("Rybka 2.2 - 2 cores", null,
            new ExternalUciProcess(new String[]{"\"" + engineDir + "/Rybka/Rybka v2.2n2.mp.w32.exe\""},
                new File(engineDir)));
        final GameManager manager = new GameManager(pnrLatest, rybka22, 120000, 6000);
        manager.play(10);
    }
}
