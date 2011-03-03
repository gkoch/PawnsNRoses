package sf.pnr.tests;

import sf.pnr.base.Board;
import sf.pnr.base.Evaluation;
import sf.pnr.base.StringUtils;
import sf.pnr.io.PrefixOutputStream;
import sf.pnr.io.TeeInputStream;
import sf.pnr.io.TeeOutputStream;
import sf.pnr.io.UciProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UciRunner {

    private static final ScheduledExecutorService THREAD_POOL = Executors.newScheduledThreadPool(2);

    private final String name;
    private final Map<String, String> uciOptions;
    private final Map<String, String> postSearchOptions;
    private final UciProcess process;
    private BufferedWriter writer;
    private BufferedReader reader;
    private int depth;
    private long nodeCount;
    private int score;
    private String bestMove;
    private long moveTime;
    private String bestMoveLine;
    private OutputStream debugOs;
    private String prefix;

    public UciRunner(final String name, final UciProcess process) {
        this(name, null, null, process);
    }

    public UciRunner(final String name, final Map<String, String> uciOptions, final UciProcess process) {
        this(name, uciOptions, null, process);
    }

    public UciRunner(final String name, final Map<String, String> uciOptions, final Map<String, String> postSearchOptions,
                     final UciProcess process) {
        this.name = name;
        this.uciOptions = uciOptions;
        this.postSearchOptions = postSearchOptions;
        this.process = process;
        reader = null;
        writer = null;
    }

    public void setDebugOutputStream(final OutputStream debugOs, final String prefix) {
        this.debugOs = debugOs;
        this.prefix = prefix;
    }

    public OutputStream getDebugOutputStream() {
        return debugOs;
    }

    private void initializeProcess() throws IOException {
        if (reader == null) {
            final OutputStream os;
            final InputStream is;
            if (debugOs != null) {
                final String outPrefix;
                final String inPrefix;
                if (prefix != null) {
                    outPrefix = prefix + ">>> ";
                    inPrefix = prefix + "<<< ";
                } else {
                    outPrefix = ">>> ";
                    inPrefix = "<<< ";
                }
                os = new TeeOutputStream(process.getOutputStream(), new PrefixOutputStream(debugOs, '\n', outPrefix));
                is = new TeeInputStream(process.getInputStream(), new PrefixOutputStream(debugOs, '\n', inPrefix));
            } else {
                os = process.getOutputStream();
                is = process.getInputStream();
            }
            writer = new BufferedWriter(new OutputStreamWriter(os));
            reader = new BufferedReader(new InputStreamReader(is));
            emptyInputStream(is);
            sendCommand("uci");
            if (!waitResponse("uciok")) {
                throw new IllegalStateException(String.format("%s is not a UCI engine", name));
            }
            ensureReady();
        }
    }

    private void emptyInputStream(final InputStream is) throws IOException {
        while (is.available() > 0) {
            is.read();
        }
    }

    public String getName() {
        return name;
    }

    public void uciNewGame() throws IOException {
        sendCommand("ucinewgame");
        ensureReady();
        setOptions(uciOptions);
    }

    public void position(final Board board) throws IOException {
        position(board, Collections.<Integer>emptyList());
    }

    public void position(final List<Integer> moves) throws IOException {
        position(null, moves);
    }

    public void position(final Board board, final List<Integer> moves) throws IOException {
        initializeProcess();
        final StringBuilder builder = new StringBuilder();
        if (board == null) {
            builder.append("position startpos");
        } else {
            sendCommand("position fen " + StringUtils.toFen(board));
        }
        if (!moves.isEmpty()) {
            builder.append(" moves");
            for (int move: moves) {
                builder.append(' ').append(StringUtils.toLong(move));
            }
        }
        sendCommand(builder.toString());
        ensureReady();
    }

    public void go(final int depth, final int time) throws IOException {
        if (depth == 0 && time == 0) {
            throw new IllegalArgumentException("Cannot set both depth and time to 0");
        }
        final StringBuilder command = new StringBuilder();
        command.append("go");
        if (depth > 0) {
            command.append(" depth ");
            command.append(depth);
        }
        if (time > 0) {
            command.append(" movetime ");
            command.append(time);
        }
        go(command.toString(), -1);
    }

    public void go(final int wtime, final int btime, final int winc, final int binc, final int timeout) throws IOException {
        final StringBuilder command = new StringBuilder();
        command.append("go");
        command.append(" wtime ").append(wtime);
        command.append(" btime ").append(btime);
        command.append(" winc ").append(winc);
        command.append(" binc ").append(binc);
        go(command.toString(), timeout);
    }

    private void go(final String command, final int timeout) throws IOException {
        final ScheduledFuture<Boolean> future;
        if (timeout > 0) {
            future = THREAD_POOL.schedule(new TimeBomb(process), timeout + 5000, TimeUnit.MILLISECONDS);
        } else {
            future = null;
        }
        sendCommand(command);
        final long startTime = System.currentTimeMillis();
        waitBestMove();
        final long endTime = System.currentTimeMillis();
        moveTime = endTime - startTime;
        ensureReady();
        setOptions(postSearchOptions);
        if (future != null) {
            if (!future.cancel(false)) {
                throw new IOException("Timeout!");
            }
        }
    }


    private void setOptions(final Map<String, String> options) throws IOException {
        if (options != null) {
            for (Map.Entry<String, String> entry: options.entrySet()) {
                sendCommand(String.format("setoption name %s value %s", entry.getKey(), entry.getValue()));
            }
            ensureReady();
        }
    }

    public int getDepth() {
        return depth;
    }

    public long getNodeCount() {
        return nodeCount;
    }

    public int getScore() {
        int value = score;
        if (score < -20000) {
            value = -20000;
        }
        if (score > 20000) {
            value = 20000;
        }
        return value;
    }

    public String getBestMove() {
        return bestMove;
    }

    public String getBestMoveLine() {
        return bestMoveLine;
    }

    public long getMoveTime() {
        return moveTime;
    }

    private void waitBestMove() throws IOException {
        initializeProcess();
        bestMove = "";
        bestMoveLine = "";
        depth = 0;
        String line = reader.readLine();
        for (; line != null && !line.startsWith("bestmove "); line = reader.readLine()) {
            if (line.startsWith("info string")) {
                continue;
            }
            if (line.startsWith("info")) {
                final String[] parts = line.split(" ");
                final Map<String, Number> params = parseInfoLine(parts);
                final Integer newDepth = (Integer) params.get("depth");
                if (newDepth != null) {
                    if (depth != newDepth) {
                        resetFields();
                    }
                    depth = newDepth;
                }
                final Long newNodeCount = (Long) params.get("nodes");
                if (newNodeCount != null) {
                    nodeCount = newNodeCount;
                }
                final Integer newScore = (Integer) params.get("cp");
                if (newScore != null) {
                    score = newScore;
                }
            }
        }
        if (line != null && line.startsWith("bestmove ")) {
            bestMoveLine = line;
            bestMove = line.substring("bestmove ".length()).split(" ")[0].trim();
        }
    }

    private Map<String, Number> parseInfoLine(final String[] parts) {
        final Map<String, Number> params = new HashMap<String, Number>();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("depth")) {
                params.put("depth", Integer.parseInt(parts[++i]));
            } else if (parts[i].equals("nodes")) {
                params.put("nodes", Long.parseLong(parts[++i]));
            } else if (parts[i].equals("cp")) {
                params.put("cp", Integer.parseInt(parts[++i]));
            } else if (parts[i].equals("mate")) {
                final int moves = Integer.parseInt(parts[++i]);
                params.put("cp", moves > 0? Evaluation.VAL_MATE: -Evaluation.VAL_MATE);
                params.put("mate", moves);
            }
        }
        return params;
    }

    private void resetFields() {
        depth = 0;
        nodeCount = 0L;
        score = 0;
    }

    public void close() throws IOException {
        process.destroy();
        writer.close();
        reader.close();
    }

    private void sendCommand(final String command) throws IOException {
        initializeProcess();
        writer.write(command);
        writer.newLine();
        writer.flush();
    }

    private boolean ensureReady() throws IOException {
        sendCommand("isready");
        return waitResponse("readyok");
    }

    private boolean waitResponse(final String expected) throws IOException {
        initializeProcess();
        String line = reader.readLine();
        while (line != null && !expected.equals(line)) {
            line = reader.readLine();
        }
        return line != null;
    }

    public void restart() throws IOException {
        process.restart();
        reader = null;
        writer = null;
        initializeProcess();
    }

    public UciRunner duplicate() {
        return new UciRunner(name, uciOptions, postSearchOptions, process.duplicate());
    }

    private static class TimeBomb implements Callable<Boolean> {
        private final UciProcess process;

        public TimeBomb(final UciProcess process) {
            this.process = process;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                process.destroy();
            } catch (IOException e) {
                throw new UndeclaredThrowableException(e);
            }
            return Boolean.TRUE;
        }
    }
}