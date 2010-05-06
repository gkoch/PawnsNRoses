package sf.pnr.base;

import sf.pnr.alg.TranspositionTable;

public class SelfPlayTest {
    private static final int TIME_TO_MOVE = 6000;

    public static void main(final String[] args) {
        final Configuration config = Configuration.getInstance();
        config.setTranspositionTableSizeInMB(128);
        config.setEvalHashTableSizeInMB(8);
        while (true) {
            final Board board = new Board();
            board.restart();
            final Engine white = new Engine();
            final Engine black = new Engine();
            final Engine[] engines = new Engine[] {black, white};
            while (!isDraw(board) && !isMate(board)) {
                System.out.print("FEN: " + StringUtils.toFen(board));
                final Engine engine = engines[board.getState() & Utils.WHITE_TO_MOVE];
                final long startTime = System.currentTimeMillis();
                final long result = engine.search(board, 0, TIME_TO_MOVE);
                final int move = Engine.getMoveFromSearchResult(result);
                board.move(move);
                final long endTime = System.currentTimeMillis();
                System.out.printf(", move: %s, value: %d, time: %d ms\r\n",
                    StringUtils.toSimple(move), Engine.getValueFromSearchResult(result), endTime - startTime);
            }
        }
    }

    private static boolean isDraw(final Board board) {
        return board.getRepetitionCount() == 3 || Evaluation.drawByInsufficientMaterial(board);
    }

    private static boolean isMate(final Board board) {
        final int toMove = board.getState() & Utils.WHITE_TO_MOVE;
        final int kingIndex = board.getKing(toMove);
        return board.isAttacked(kingIndex, 1 - toMove) &&
            PerftTest.perft(board, 1, new MoveGenerator(), false, false) == 0;
    }
}