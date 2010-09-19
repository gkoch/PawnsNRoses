package sf.pnr.base;

import sf.pnr.io.TeeOutputStream;
import sf.pnr.io.UciProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

public class UciRunner {
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
    private boolean debug = false;

    public UciRunner(final String name, final Map<String, String> uciOptions, final UciProcess process) throws IOException {
        this(name, uciOptions, null, process);
    }

    public UciRunner(final String name, final Map<String, String> uciOptions, final Map<String, String> postSearchOptions,
                     final UciProcess process) throws IOException {
        this.name = name;
        this.uciOptions = uciOptions;
        this.postSearchOptions = postSearchOptions;
        this.process = process;
        final OutputStream os;
        if (debug) {
            os = new TeeOutputStream(this.process.getOutputStream(), System.out);
        } else {
            os = this.process.getOutputStream();
        }
        writer = new BufferedWriter(new OutputStreamWriter(os));
        reader = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
        sendCommand("uci");
        if (!waitResponse("uciok")) {
            throw new IllegalStateException(String.format("%s is not a UCI engine", name));
        }
        ensureReady();
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
        final String fen = StringUtils.toFen(board);
        sendCommand("position fen " + fen);
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
        go(command.toString());
    }

    public void go(final int wtime, final int btime, final int winc, final int binc) throws IOException {
        final StringBuilder command = new StringBuilder();
        command.append("go");
        command.append(" wtime ").append(wtime);
        command.append(" btime ").append(btime);
        command.append(" winc ").append(winc);
        command.append(" binc ").append(binc);
        go(command.toString());
    }

    private void go(final String command) throws IOException {
        sendCommand(command);
        final long startTime = System.currentTimeMillis();
        waitBestMove();
        final long endTime = System.currentTimeMillis();
        moveTime = endTime - startTime;
        ensureReady();
        setOptions(postSearchOptions);
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
        return score;
    }

    public String getBestMove() {
        return bestMove;
    }

    public long getMoveTime() {
        return moveTime;
    }

    private void waitBestMove() throws IOException {
        depth = 0;
        String line = reader.readLine();
        for (; line != null && !line.startsWith("bestmove "); line = reader.readLine()) {
            if (line.startsWith("info string")) {
                continue;
            }
            if (line.startsWith("info")) {
                if (debug) {
                    System.out.println(line);
                }
                final String[] parts = line.split(" ");
                boolean resetFields = true;
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals("depth")) {
                        if (resetFields) {
                            resetFields();
                            resetFields = false;
                        }
                        depth = Integer.parseInt(parts[++i]);
                    } else if (parts[i].equals("nodes")) {
                        if (resetFields) {
                            resetFields();
                            resetFields = false;
                        }
                        nodeCount = Long.parseLong(parts[++i]);
                    } else if (parts[i].equals("cp")) {
                        if (resetFields) {
                            resetFields();
                            resetFields = false;
                        }
                        score = Integer.parseInt(parts[++i]);
                    }
                }
            }
        }
        if (line != null && line.startsWith("bestmove ")) {
            if (debug) {
                System.out.println(line);
            }
            bestMove = line.substring("bestmove ".length()).split(" ")[0].trim();
        }
    }

    private void resetFields() {
        depth = 0;
        nodeCount = 0L;
        score = 0;
    }

    public void close() throws IOException {
        try {
            writer.close();
            reader.close();
        } finally {
            process.destroy();
        }
    }

    private void sendCommand(final String command) throws IOException {
        writer.write(command);
        writer.newLine();
        writer.flush();
    }

    private boolean ensureReady() throws IOException {
        sendCommand("isready");
        return waitResponse("readyok");
    }

    private boolean waitResponse(final String expected) throws IOException {
        String line = reader.readLine();
        while (line != null && !expected.equals(line)) {
            if (debug) {
                System.out.println(line);
            }
            line = reader.readLine();
        }
        if (line != null && debug) {
            System.out.println(line);
        }
        return line != null;
    }
}