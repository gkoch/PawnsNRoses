package sf.pnr.io;

import sf.pnr.base.BestMoveListener;
import sf.pnr.base.Board;
import sf.pnr.base.Engine;
import sf.pnr.base.Evaluation;
import sf.pnr.base.Polyglot;
import sf.pnr.base.StringUtils;
import sf.pnr.base.Utils;

import java.io.File;

/**
 */
public class PawnsNRoses {
    private Polyglot polyglot;
    private boolean useBook;
    private Board board;
    private Engine engine;
    private int depth;
    private int time;
    private BestMoveListener listener;
    private boolean random;

    public PawnsNRoses() {
        polyglot = new Polyglot();
        useBook = Polyglot.BOOK != null && Polyglot.BOOK.exists();
        board = new Board();
        board.restart();
        depth = 0;
        time = 0;
    }

    public void restart() {
        board = new Board();
        board.restart();
        final File book = polyglot.getBook();
        useBook = book != null && book.exists();
    }

    private void ensureEngineIsAvailable() {
        if (engine == null) {
            engine = new Engine();
            if (listener != null) {
                engine.setBestMoveListener(listener);
            }
            engine.setRandomEval(random);
        }
    }

    public void restartBoard() {
        board.restart();
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(final String fen) {
        board = StringUtils.fromFen(fen);
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(final int depth) {
        this.depth = depth;
    }

    public int getTime() {
        return time;
    }

    public void setTime(final int time) {
        this.time = time;
    }

    public int move() {
        int move = 0;
        if (useBook) {
            move = polyglot.readMove(board);
            useBook = move != 0;
        }
        if (move == 0) {
            ensureEngineIsAvailable();
            final long result = engine.search(board, depth, time);
            move = Engine.getMoveFromSearchResult(result);
        }
        moveNow(move);
        return move;
    }

    public void moveNow() {
        if (engine != null) {
            final int[] bestLine = engine.getBestLine(board);
            moveNow(bestLine[0]);
        }
    }

    public void moveNow(int move) {
        board.move(move);
    }

    public void cancel() {
        if (engine != null) {
            engine.cancel();
        }
    }

    public void setRandom(final boolean random) {
        this.random = random;
        if (engine != null) {
            engine.setRandomEval(this.random);
        }
    }

    public void setBestMoveListener(final BestMoveListener listener) {
        this.listener = listener;
        if (engine != null) {
            engine.setBestMoveListener(this.listener);
        }
    }

    public boolean isWhiteToMove() {
        return (board.getState() & Utils.WHITE_TO_MOVE) == Utils.WHITE_TO_MOVE;
    }

    @Executable()
    public void releaseEngine() {
        engine = null;
    }

    @Executable()
    public void scanHashTables() {
        if (engine != null) {
            engine.getTranspositionTable().scan();
            final Evaluation evaluation = engine.getEvaluation();
            evaluation.getEvalHashTable().scan();
            evaluation.getPawnHashTable().scan();
        }
    }

    public boolean useBook() {
        return useBook;
    }

    public File getBook() {
        return polyglot.getBook();
    }
}