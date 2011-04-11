package sf.pnr.tests;

import sf.pnr.base.Configuration;
import sf.pnr.io.UncloseableOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class GamePlayTest {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final UciRunner[] engines = TestUtils.getEngines();
        final UciRunner[] referenceEngines = TestUtils.getReferenceEngines();
        FileOutputStream debugOs = null;
        final String debugFile = System.getProperty("searchTask.debugFile");
        if (debugFile != null) {
            debugOs = new FileOutputStream(debugFile);
            final UncloseableOutputStream os = new UncloseableOutputStream(debugOs);
            for (UciRunner player: engines) {
                player.setDebugOutputStream(os, player.getName() + " ");
            }
        }
        final int initialTime = Integer.parseInt(System.getProperty("searchTask.initialTime", "120000"));
        final int increment = Integer.parseInt(System.getProperty("searchTask.incrementTime", "6000"));
        final int rounds = Integer.parseInt(System.getProperty("searchTask.rounds", "20"));
        final boolean restartBeforeMoves = Boolean.parseBoolean(System.getProperty("searchTask.restartBeforeMoves", "false"));
        final GameManager manager =
            new GameManager(TestUtils.getEngineDir().getName(), initialTime, increment, rounds, restartBeforeMoves);
        final String kibitzerPath = System.getProperty("searchTask.kibitzer");
        if (kibitzerPath != null) {
            final UciRunner kibitzer =
                new UciRunner(new File(kibitzerPath).getName(), new ExternalUciProcess(kibitzerPath));
            manager.setKibitzer(kibitzer);
            System.out.println("Kibitzer: " + kibitzer.getName());
        }
        Configuration.getInstance().loadFromSystemProperties();
        manager.play(referenceEngines, engines);
        if (debugOs != null) {
            debugOs.close();
        }
        for (UciRunner player: engines) {
            player.close();
        }
    }

}