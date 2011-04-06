package sf.pnr.tools;

import sf.pnr.base.Board;
import sf.pnr.base.Engine;
import sf.pnr.base.StringUtils;
import sf.pnr.base.Utils;
import sf.pnr.tests.TestUtils;
import sf.pnr.tests.UciRunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PgnToFenListGenerator {
    private static final int MAX_SCORE_DEPTH = 10;
    private int skipMoves;
    private int maxScore;
    private final Map<String, Integer> fenRoots = new HashMap<String, Integer>();
    private final List<String> fens = new ArrayList<String>(10000);
    private int skippedEarly = 0;
    private int skippedMaxScore = 0;
    private UciRunner[] referenceEngines;

    public static void main(final String[] args) throws IOException {
        final PgnToFenListGenerator generator = new PgnToFenListGenerator();
        final UciRunner[] referenceEngines = TestUtils.getReferenceEngines();
        generator.setSkipMoves(13);
        generator.setMaxScore(800);
        generator.setReferenceEngines(referenceEngines);
        //generator.saveDistinct("med-games.epd");
        generator.convert(new File(args[0]));
        generator.saveDistinct(args[1]);
        //generator.saveDuplicates(args[1]);
    }

    private void setReferenceEngines(final UciRunner[] referenceEngines) {
        this.referenceEngines = referenceEngines;
    }

    public void setSkipMoves(final int skipMoves) {
        this.skipMoves = skipMoves;
    }

    public void setMaxScore(final int maxScore) {
        this.maxScore = maxScore;
    }

    private void saveDistinct(final String arg) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new FileWriter(arg));
        try {
            for (String fen: fens) {
                writer.write(fen);
                writer.newLine();
            }
        } finally {
            writer.close();
        }
        System.out.printf("Written %d FENs\r\n", fens.size());
    }

    private void saveDuplicates(final String arg) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new FileWriter(arg));
        int count = 0;
        try {
            for (Map.Entry<String, Integer> entry: fenRoots.entrySet()) {
                if (entry.getValue() > 2) {
                    writer.write(entry.getKey());
                    writer.write(" 0 1");
                    writer.newLine();
                    count++;
                }
            }
        } finally {
            writer.close();
        }
        System.out.printf("Written %d FENs\r\n", count);
    }

    private void convert(final File file) throws IOException {
        if (!file.getName().startsWith("_")) {
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    convert(child);
                }
            } else {
                System.out.println("Processing file " + file.getAbsolutePath());
                if (file.getName().endsWith(".zip")) {
                    final ZipFile zipFile = new ZipFile(file);
                    for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
                        final ZipEntry zipEntry = entries.nextElement();
                        System.out.println("  processing zip entry " + zipEntry.getName());
                        convert(zipFile.getInputStream(zipEntry));
                        System.out.printf("FEN count: %d, skippedEarly %d, skippedMaxScore %d\r\n",
                            fens.size(), skippedEarly, skippedMaxScore);
                    }
                } else {
                    convert(new FileInputStream(file));
                    System.out.printf("FEN count: %d, skippedEarly %d, skippedMaxScore %d\r\n",
                        fens.size(), skippedEarly, skippedMaxScore);
                }

            }
        }
    }

    private void convert(final InputStream is) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        final StringBuilder currentPgn = new StringBuilder(500);
        boolean inHeader = true;
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.startsWith("[")) {
                if (!inHeader) {
                    convert(currentPgn.toString());
                    currentPgn.delete(0, currentPgn.length());
                    inHeader = true;
                }
            } else {
                inHeader = false;
            }
            currentPgn.append(line).append("\r\n");
        }
        if (currentPgn.length() > 0) {
            convert(currentPgn.toString());
        }
    }

    private void convert(final String pgn) {
        final int[] moves;
        final Map<String, String> headers;
        try {
            headers = new HashMap<String, String>();
            moves = StringUtils.fromPgnToMoves(pgn, headers);
        } catch (RuntimeException e) {
            System.out.println("Failed to process PGN: " + pgn);
            throw e;
        }
        final Board board;
        if (headers.containsKey("FEN")) {
            board = StringUtils.fromFen(headers.get("FEN"));
        } else {
            board = new Board();
            board.restart();
        }
        for (final int move: moves) {
            board.move(move);
            final int fullMoves = (board.getState() & Utils.FULL_MOVES) >> Utils.SHIFT_FULL_MOVES;
            if (fullMoves > skipMoves && !board.isMate()) {
                if (maxScore > 0) {
                    final int score = getScore(board);
                    if (score > maxScore || score < -maxScore) {
                        skippedMaxScore++;
                        continue;
                    }
                }
                final String fen = StringUtils.toFen(board);
                final String fenRoot = getFenWithoutMoveCounts(fen);
                final Integer count = fenRoots.get(fenRoot);
                if (count == null) {
                    fens.add(fen);
                    fenRoots.put(fenRoot, 1);
                } else {
                    fenRoots.put(fenRoot, count + 1);
                }
            } else {
                skippedEarly++;
            }
        }
        System.out.printf("FEN count: %d, skippedEarly %d, skippedMaxScore %d\r\n",
            fens.size(), skippedEarly, skippedMaxScore);
    }

    public static String getFenWithoutMoveCounts(final String fen) {
        final String[] parts = fen.split(" ");
        final StringBuilder builder = new StringBuilder(fen.length() - 4);
        for (int i = 0; i < parts.length - 2; i++) {
            builder.append(parts[i]).append(" ");
        }
        return builder.toString().trim();
    }

    public int getScore(final Board board) {
        int score = 0;
        for (UciRunner engine : referenceEngines) {
            TestUtils.compute(engine, board, MAX_SCORE_DEPTH, 0, false);
            score += engine.getScore();
        }
        return score / referenceEngines.length;

    }
}