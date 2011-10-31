package sf.pnr.base;

import junit.framework.TestCase;

import java.util.Random;

import static sf.pnr.base.Utils.*;

public class RandomPlayTest extends TestCase {
    private static final Random RND = new Random(System.currentTimeMillis());
    private static final int TEST_COUNT = 2000;

    private enum Result {MATE, FIFTY_MOVES, INSUFFICIENT_MATERIAL, THREEFOLD_REPETITION, STALE_MATE}

    private final MoveGenerator moveGenerator = new MoveGenerator();

    public void testDistribution() {
        int[] counts = new int[Result.values().length];
        for (int i = 0; i < TEST_COUNT; i++) {
            counts[playGame().ordinal()]++;
        }
        for (Result result: Result.values()) {
            final int count = counts[result.ordinal()];
            System.out.printf("%s: %d (%.3f%%)\r\n", result, count, 100.0 * count / TEST_COUNT);
        }

        //rgstat 1000000000
        //Summary:
        //  Checkmate 153,023,351 (0.153023)
        //  FiftyMoves 199,868,185 (0.199868)
        //  Insufficient 560,510,846 (0.560511)
        //  Repetition 25,427,172 (0.0254272)
        //  Stalemate 61,170,446 (0.0611704)
        //Count: 1,000,000,000   Elapsed: 159960
        assertCorrectCount(counts, Result.MATE, 0.153);
        assertCorrectCount(counts, Result.FIFTY_MOVES, 0.200);
        assertCorrectCount(counts, Result.INSUFFICIENT_MATERIAL, 0.561);
        assertCorrectCount(counts, Result.THREEFOLD_REPETITION, 0.025);
        assertCorrectCount(counts, Result.STALE_MATE, 0.061);
    }

    private void assertCorrectCount(final int[] counts, final Result result, final double expected) {
        assertTrue(counts[result.ordinal()] >= (expected * 0.8 - 0.01) * TEST_COUNT);
        assertTrue(counts[result.ordinal()] <= (expected * 1.2 + 0.01) * TEST_COUNT);
    }

    public Result playGame() {
        final Board board = new Board();
        board.restart();
        Result result = null;
        while (result == null) {
            final int[] moves = getValidMoves(board);
            boolean keepSearching = true;
            while (keepSearching) {
                if (moves[0] == 0) {
                    result = Result.STALE_MATE;
                    keepSearching = false;
                } else {
                    final int moveIdx = RND.nextInt(moves[0]) + 1;
                    final int move = moves[moveIdx];
                    final long undo = board.move(move);
                    final int state = board.getState();
                    final int toMove = state & Utils.WHITE_TO_MOVE;
                    if (board.attacksKing(toMove)) {
                        moves[moveIdx] = moves[moves[0]];
                        moves[0]--;
                        board.takeBack(undo);
                        continue;
                    }
                    keepSearching = false;

                    // check result
                    if (board.isMate()) {
                        result = Result.MATE;
                    } else if (board.getRepetitionCount() >= 3) {
                        result = Result.THREEFOLD_REPETITION;
                    } else if (Evaluation.drawByInsufficientMaterial(board)) {
                        result = Result.INSUFFICIENT_MATERIAL;
                    } else {
                        final int halfMoves = (state & HALF_MOVES) >> SHIFT_HALF_MOVES;
                        if (halfMoves >= 100) {
                            result = Result.FIFTY_MOVES;
                        }
                    }
                }
            }
        }
        return result;
    }

    public int[] getValidMoves(final Board board) {
        moveGenerator.pushFrame();
        moveGenerator.generatePseudoLegalMoves(board);
        moveGenerator.generatePseudoLegalMovesNonAttacking(board);
        final int[] captures = moveGenerator.getCaptures();
        final int[] promotions = moveGenerator.getPromotions();
        final int[] normalMoves = moveGenerator.getMoves();
        moveGenerator.popFrame();
        final int[] moves = new int[captures.length + promotions.length + normalMoves.length - 3 + 1];
        addMoves(moves, captures);
        addMoves(moves, promotions);
        addMoves(moves, normalMoves);
        //for (int i = moves[0]; i > 0; i--) {
        //    int move = moves[i];
        //    final long undo = board.move(move);
        //    final int state = board.getState();
        //    final int toMove = state & Utils.WHITE_TO_MOVE;
        //    if (board.attacksKing(toMove)) {
        //        moves[i] = moves[moves[0]];
        //        moves[0]--;
        //    }
        //    board.takeBack(undo);
        //}
        return moves;
    }

    private void addMoves(final int[] moves, final int[] movesToAdd) {
        System.arraycopy(movesToAdd, 1, moves, 1 + moves[0], movesToAdd[0]);
        moves[0] += movesToAdd[0];
    }
}
