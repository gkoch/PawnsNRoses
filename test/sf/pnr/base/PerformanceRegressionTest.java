package sf.pnr.base;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class PerformanceRegressionTest {

    public static void main(String[] args) throws IOException {
        final String engineDir = System.getProperty("searchTest.engineDir");
        final List<String> testFiles = new ArrayList<String>();
//        testFiles.add("pos.epd");
        testFiles.add("best7.epd");
        testFiles.add("wnperm.epd");
        testFiles.add("qtest_easy.epd");
        final UciRunner pnrV0029 = new UciRunner("Pawns N' Roses v0.029b", null,
            new ExternalUciProcess(new String[]{"\"" + engineDir + "/PawnsNRoses/v0.02x/PawnsNRoses v0.029b.bat\""},
                new File(engineDir + "/PawnsNRoses/v0.02x/")));
        final UciRunner pnrLatest = new UciRunner("Pawns N' Roses Latest", null, new PipedUciProcess());
        final UciRunner rybka22 = new UciRunner("Rybka 2.2 - 2 cores", null,
            new ExternalUciProcess(new String[]{"\"" + engineDir + "/Rybka/Rybka v2.2n2.mp.w32.exe\""},
                new File(engineDir)));
        final UciRunner[] runners = new UciRunner[] {rybka22, pnrLatest, pnrV0029};
        try {
            new EpdProcessor().process(testFiles, new MultiEngineSearchTask(runners, 6, 0, 20));
        } finally {
            for (UciRunner runner : runners) {
                runner.close();
            }
        }
    }
}                                                                