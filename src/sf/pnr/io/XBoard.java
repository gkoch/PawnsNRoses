package sf.pnr.io;

import sf.pnr.base.BestMoveListener;
import sf.pnr.base.StringUtils;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class XBoard {

    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
    private static final Pattern TIME_PATTERN = Pattern.compile("([0-9]+)(:([0-9][0-9]))?.*");

    private final BufferedReader in;
    private final PrintStream out;
    private final PawnsNRoses chess;
    private Future<String> future;
    private boolean random;
    private TimeControl timeControl;
    private static final int CANCEL_THRESHOLD = 50;

    public XBoard(final BufferedReader in, final PrintStream out) {
        this.in = in;
        this.out = out;
        chess = new PawnsNRoses(null, 4); // TODO: fixme
        chess.setBestMoveListener(new BestMoveListener() {
            @Override
            public void bestMoveChanged(final int depth, final int bestMove, final int value, final long time,
                                        final int[] bestLine, final long nodes) {
                final String message = String.format("%d\t%s\t%.2f\t%.1f\t%s", depth, StringUtils.toLong(bestMove),
                    ((double) value) / 100, ((double) time) / 1000, StringUtils.toLong(bestLine, " "));
                out.println(message);
            }
        });
    }

    public static void main(final String[] args) throws IOException, ExecutionException, InterruptedException {
        final PrintStream outputStream;
        if (args.length > 0) {
            outputStream = new PrintStream(new TeeOutputStream(System.out, new FileOutputStream(args[0])));
        } else {
            outputStream = System.out;
        }
        final XBoard protocol =
            new XBoard(new BufferedReader(new InputStreamReader(System.in)), outputStream);
        protocol.run();
    }

    public void run() throws IOException, ExecutionException, InterruptedException {
        boolean analyzeMode = false;
        while (true) {
            final String line = in.readLine().trim();
            if ("quit".equals(line)) {
                break;
            } else if (line.startsWith("setboard ")) {
                ensureReady();
                chess.setBoard(line.substring(9).trim());
            } else if ("new".equals(line)) {
                ensureReady();
                chess.restart();
            } else if ("go".equals(line) || "analyze".equals(line)) {
                if ("analyze".equals(line)) {
                    analyzeMode = true;
                }
                move(analyzeMode);
            } else if ("exit".equals(line)) {
                analyzeMode = false;
            } else if (line.startsWith("st ")) {
                timeControl = new FixedPerMoveTimeControl(Integer.parseInt(line.substring(3).trim()) * 1000);
            } else if (line.startsWith("sd ")) {
                chess.setDepth(Integer.parseInt(line.substring(3).trim()));
            } else if ("force".equals(line)) {
                ensureReady();
            } else if (line.startsWith("protover ")) {
                print("feature myname=\"Pawns N' Roses 0.1\" usermove=1 setboard=1 colors=0 analyze=1 done=1");
            } else if (line.startsWith("accepted ") || line.startsWith("rejected ")) {
                // do nothing
            } else if (line.equals("random")) {
                random = !random;
                chess.setRandom(random);
            } else if (line.startsWith("level ")) {
                final String[] parts = line.substring(6).split(" ");
                if (parts.length == 3) {
                    final int moves = Integer.parseInt(parts[0]);
                    final String timeStr = parts[1];
                    final int time = parseTimeStr(timeStr);
                    if (moves > 0) {
                        timeControl = new ConventionalTimeControl(moves, time);
                    } else {
                        timeControl = new IncrementalTimeControl(time, Integer.parseInt(parts[0]));
                    }
                }
            } else if ("?".equals(line)) {
                ensureReady();
                chess.moveNow();
            } else if (line.startsWith("usermove")) {
                final String moveStr = line.substring(9).trim();
                final int move = StringUtils.fromSimple(moveStr);
                chess.moveNow(move);
                move(false);
            }
        }
    }

    private void move(final boolean analyzeMode) throws InterruptedException, ExecutionException {
        ensureReady();
        final int nextMoveTime = timeControl.getNextMoveTime();
        chess.setTime(nextMoveTime - CANCEL_THRESHOLD);
        future = THREAD_POOL.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                out.printf("# depth: %d, time: %d\r\n", chess.getDepth(), chess.getTime());
                return StringUtils.toLong(chess.move());
            }
        });
        if (!analyzeMode) {
            final String moveStr = future.get();
            final String reply = "move " + moveStr;
            out.println(reply);
        }
    }

    private void print(final String message) {
        out.println(message);
    }

    private void ensureReady() throws InterruptedException, ExecutionException {
        if (future != null) {
            chess.cancel();
            future.get();
            future = null;
        }
    }

    private int parseTimeStr(final String timeStr) {
        final Matcher matcher = TIME_PATTERN.matcher(timeStr);
        if (!matcher.matches()) {
            // TODO: report error
        }
        final int mins = Integer.parseInt(matcher.group(1));
        final int seconds;
        if (matcher.groupCount() > 2) {
            final String secondsStr = matcher.group(3);
            if (secondsStr != null) {
                seconds = Integer.parseInt(secondsStr);
            } else {
                seconds = 0;
            }
        } else {
            seconds = 0;
        }
        return (mins * 60 + seconds) * 1000;
    }
}