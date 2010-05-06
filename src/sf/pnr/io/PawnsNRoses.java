package sf.pnr.io;

import sf.pnr.alg.TranspositionTable;
import sf.pnr.base.BestMoveListener;
import sf.pnr.base.Board;
import sf.pnr.base.Engine;
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

    public PawnsNRoses(final File polyglotBook) {
        polyglot = new Polyglot(polyglotBook);
        useBook = polyglotBook != null && polyglotBook.exists();
        board = new Board();
        board.restart();
        engine = new Engine();
        depth = 0;
        time = 0;
    }

    public void restart() {
        board.restart();
        engine.clear();
        final File book = polyglot.getBook();
        useBook = book != null && book.exists();
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
            final long result = engine.search(board, depth, time);
            move = Engine.getMoveFromSearchResult(result);
        }
        moveNow(move);
        return move;
    }

    public void moveNow() {
        final int[] bestLine = engine.getBestLine(board);
        moveNow(bestLine[0]);
    }

    public void moveNow(int move) {
        board.move(move);
    }

    public void cancel() {
        engine.cancel();
    }

    public void setRandom(final boolean random) {
        engine.setRandomEval(random);
    }

    public void setBestMoveListener(final BestMoveListener listener) {
        engine.setBestMoveListener(listener);
    }

    public boolean isWhiteToMove() {
        return (board.getState() & Utils.WHITE_TO_MOVE) == Utils.WHITE_TO_MOVE;
    }

    public boolean useBook() {
        return useBook;
    }

    public File getBook() {
        return polyglot.getBook();
    }
}