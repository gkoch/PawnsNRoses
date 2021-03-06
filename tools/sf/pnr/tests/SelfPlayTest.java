package sf.pnr.tests;

import sf.pnr.base.Board;
import sf.pnr.base.Configurable;
import sf.pnr.base.Configuration;
import sf.pnr.base.Engine;
import sf.pnr.base.Evaluation;
import sf.pnr.base.MoveGenerator;
import sf.pnr.base.PerftTest;
import sf.pnr.base.StringUtils;
import sf.pnr.base.Utils;

public class SelfPlayTest {
    private static final int TIME_TO_MOVE = 6000;

    public static void main(final String[] args) {
        final Configuration config = Configuration.getInstance();
        config.setProperty(Configurable.Key.TRANSP_TABLE_SIZE, "128");
        config.setProperty(Configurable.Key.EVAL_TABLE_SIZE, "8");
        while (true) {
            final Board board = new Board();
            board.restart();
            final Engine white = new Engine();
            final Engine black = new Engine();
            final Engine[] engines = new Engine[] {black, white};
            long totalNodeCount = 0;
            int moveCount = 0;
            while (!isDraw(board) && !isMate(board)) {
                System.out.print("FEN: " + StringUtils.toFen(board));
                final Engine engine = engines[board.getState() & Utils.WHITE_TO_MOVE];
                final long startTime = System.currentTimeMillis();
                final long result = engine.search(board, 0, TIME_TO_MOVE);
                final int move = Engine.getMoveFromSearchResult(result);
                board.move(move);
                final long endTime = System.currentTimeMillis();
                final long nodeCount = engine.getNodeCount();
                totalNodeCount += nodeCount;
                moveCount++;
                System.out.printf(", move: %s, value: %d, time: %d ms, node count: %d (avg: %.1f)\r\n",
                    StringUtils.toSimple(move), Engine.getValueFromSearchResult(result), endTime - startTime, nodeCount,
                    ((double) totalNodeCount) / moveCount);
            }
        }
    }

    private static boolean isDraw(final Board board) {
        return board.getRepetitionCount() == 3 || Evaluation.drawByInsufficientMaterial(board);
    }

    private static boolean isMate(final Board board) {
        final int toMove = board.getState() & Utils.WHITE_TO_MOVE;
        final int kingPos = board.getKing(toMove);
        return board.isAttacked(kingPos, 1 - toMove) &&
            PerftTest.perft(board, 1, new MoveGenerator(), false, false) == 0;
    }
}