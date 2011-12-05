package sf.pnr.tests;

import sf.pnr.base.Board;
import sf.pnr.base.Configurable;
import sf.pnr.base.Configuration;
import sf.pnr.base.Evaluation;
import sf.pnr.base.StringUtils;
import sf.pnr.tools.ConfigGenerator;
import sf.pnr.tools.ConfigIdGenerator;
import sf.pnr.tools.ConfigVisitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static sf.pnr.base.StringUtils.formatter;
import static sf.pnr.base.StringUtils.prettyPrint;

public class EvaluationParamTuning {

    private static final String REF_ENGINE_SCORE_PREFIX = "e";

    public static void main(final String[] args) throws IOException {
        final String inputFile = System.getProperty("pnr.inputFile");
        final String outputFile = System.getProperty("pnr.outputFile");
        if (outputFile != null) {
            writeTestFile(inputFile, outputFile);
        } else {
            final Properties template = new Properties();
            //template.setProperty(Configurable.Key.EVAL_BONUS_PAWN_SHIELD.getKey(), "[-50,200,5]");
            template.setProperty(Configurable.Key.EVAL_BONUS_ATTACK.getKey(), "[-5,15,1]");
            //template.setProperty(Configurable.Key.EVAL_BONUS_DEFENSE.getKey(), "[-5,15,1]");
            //template.setProperty(Configurable.Key.EVAL_BONUS_HUNG_PIECE.getKey(), "[-50,100,5]");
            //template.setProperty(Configurable.Key.EVAL_BONUS_PASSED_PAWN_PER_SQUARE.getKey(), "[-50,100,5]");
            playTest(inputFile, template);
        }
    }

    private static void playTest(final String inputFile, final Properties template) throws IOException {
        final long rndSeed = Long.parseLong(System.getProperty("searchTask.rndSeed", "-1"));
        final List<String> testFiles = Collections.singletonList(inputFile);
        final Map<String, Properties> configs = new LinkedHashMap<String, Properties>();
        new ConfigGenerator().generateConfigs(template, new ConfigVisitor() {
            @Override
            public void visit(final ConfigIdGenerator idGenerator, final Properties config) {
                configs.put(idGenerator.getId(), config);
            }
        });
        boolean currentPrinted = false;
        for (Properties config: configs.values()) {
            if (!currentPrinted) {
                System.out.println("*** Current values:");
                final Enumeration<String> keys = (Enumeration<String>) config.propertyNames();
                while (keys.hasMoreElements()) {
                    final String keyStr = keys.nextElement();
                    System.out.printf("%s=%s\r\n", keyStr, Configuration.getInstance().getString(Configuration.getKey(keyStr)));
                }
                System.out.println("*** Configs:");
                currentPrinted = true;
            }
            System.out.println(config);
        }

        new EpdProcessor().process(testFiles, new EvaluationParamTuningTask(configs), rndSeed);
    }

    private static class EvaluationParamTuningTask implements EpdProcessorTask {

        private int testCount;
        private final Map<String, Properties> configs;
        private final int[] fails;
        private final double[] diffs;
        private final double[] diffsNorm;
        private final Evaluation eval;
        private double maxStdDev = 200.0;
        private double maxStdDevNorm = 0.2;
        private int minScore = -800;
        private int maxScore = 800;
        private int totalTestCount = 0;

        public EvaluationParamTuningTask(final Map<String, Properties> configs) {
            this.configs = configs;
            eval = new Evaluation();
            final int engineCount = configs.size();
            fails = new int[engineCount];
            diffs = new double[engineCount];
            diffsNorm = new double[engineCount];
        }

        @Override
        public void run(final String fileName, final Board board, final Map<String, String> commands) {
            totalTestCount++;
            int count = 0;
            while (commands.get(REF_ENGINE_SCORE_PREFIX + count) != null) {
                count++;
            }
            if (count == 0) {
                return;
            }
            final int[] scores = new int[count];
            int sum = 0;
            for (int i = 0; i < count; i++) {
                scores[i] = Integer.parseInt(commands.get(REF_ENGINE_SCORE_PREFIX + i));
                sum += scores[i];
            }
            final double avg = ((double) sum) / scores.length;
            double stdDev = 0.0;
            for (final int score : scores) {
                final double diff = avg - score;
                stdDev += diff * diff;
            }
            stdDev /= scores.length;
            stdDev = Math.sqrt(stdDev);
            if (stdDev > maxStdDev) {
                return;
            }
            final double stdDevNorm = stdDev / avg;
            if (stdDevNorm > maxStdDevNorm) {
                return;
            }
            if (avg < minScore) {
                return;
            }
            if (avg > maxScore) {
                return;
            }

            testCount++;
            final Configuration configuration = Configuration.getInstance();
            int idx = 0;
            for (Map.Entry<String, Properties> entry : configs.entrySet()) {
                final Properties config = entry.getValue();
                for (Map.Entry<Object, Object> configEntry : config.entrySet()) {
                    final String keyStr = (String) configEntry.getKey();
                    final String value = (String) configEntry.getValue();
                    configuration.setProperty(Configuration.getKey(keyStr), value);
                }
                eval.clear();
                final int score = eval.evaluate(board);
                if (score < avg - stdDev) {
                    fails[idx]++;
                    final double diff = (avg - stdDev) - score;
                    diffs[idx] += diff;
                    diffsNorm[idx] += diff / Math.max(1, Math.abs(avg));
                } else if (score > avg + stdDev) {
                    fails[idx]++;
                    final double diff = score - (avg + stdDev);
                    diffs[idx] += diff;
                    diffsNorm[idx] += diff / Math.max(1, Math.abs(avg));
                }
                idx++;
            }
            if (testCount % 100 == 0) {
                printStats();
            }
        }

        private void printStats() {
            System.out.printf("At test count %d (%d)\r\n", testCount, totalTestCount);
            System.out.printf("fails: %s\r\n", Arrays.toString(fails));
            System.out.printf("diffs: %s\r\n", printArray(diffs));
            System.out.printf("diffsNorm: %s\r\n", printArray(diffsNorm));
        }

        private void printDetailedStats() {
            System.out.print(prettyPrint(formatter("id", configs.keySet().toArray(new String[configs.size()])),
                formatter("Fails", fails), formatter("Diffs", diffs, 2), formatter("DiffNorms", diffsNorm, 2)));
        }

        private String printArray(final double[] values) {
            final StringBuilder builder = new StringBuilder(values.length * 12);
            builder.append('[');
            boolean first = true;
            for (double value : values) {
                if (!first) {
                    builder.append(", ");
                } else {
                    first = false;
                }
                builder.append(String.format("%.2f", value));
            }
            builder.append(']');
            return builder.toString();
        }

        @Override
        public void completed() {
            printStats();
            printDetailedStats();
        }
    }

    private static void writeTestFile(final String inputFile, final String outputFile) throws IOException {
        final UciRunner[] refEngines = TestUtils.getReferenceEngines();

        final int depth = Integer.parseInt(System.getProperty("pnr.depth", "5"));
        final int time = Integer.parseInt(System.getProperty("pnr.moveTime", "0"));
        final int skipFirst = Integer.parseInt(System.getProperty("pnr.skipFirst", "0"));

        final BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile), 512);
        try {
            for (UciRunner engine: refEngines) {
                writer.write("# " + engine.getName());
                writer.newLine();
            }
            writer.write("# Depth: " + depth);
            writer.newLine();
            writer.write("# Time: " + time + "ms");
            writer.newLine();
            writer.write("# Starting from FEN: " + (skipFirst + 1));
            writer.newLine();
            int count = 0;
            final int[] refScores = new int[refEngines.length];
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                if (line.startsWith("#") || line.length() == 0) {
                    continue;
                }
                count++;
                if (count <= skipFirst) {
                    continue;
                }
                final Board board = StringUtils.fromFen(line);
                final StringBuilder result = new StringBuilder(line);
                Arrays.fill(refScores, 0);
                for (int i = 0; i < refEngines.length; i++) {
                    final UciRunner engine = refEngines[i];
                    TestUtils.compute(engine, board, depth, time, true);
                    final int score = engine.getScore();
                    result.append(String.format("; %s%d %d", REF_ENGINE_SCORE_PREFIX, i, score));
                    refScores[i] = score;
                }
                writer.write(result.toString());
                writer.newLine();
                System.out.printf("%5d. FEN: %s\r\n", count, result.toString());
            }
        } finally {
            reader.close();
            writer.close();
        }
    }
}