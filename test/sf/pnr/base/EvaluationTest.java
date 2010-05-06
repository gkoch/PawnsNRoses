package sf.pnr.base;

import junit.framework.TestCase;

import static sf.pnr.base.Utils.*;

/**
 */
public class EvaluationTest extends TestCase {

    public void testDrawByInsufficientMaterialKnights() {
        final Board board = StringUtils.fromFen("4k3/3nn3/4N3/1N6/3K4/8/8/8 w - - 0 1");
        assertFalse(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialBishopsOfOppositeColors() {
        final Board board = StringUtils.fromFen("7k/7B/7K/8/7B/8/8/8 w - - 0 1");
        assertFalse(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialBishopsOfOppositeColors2() {
        final Board board = StringUtils.fromFen("4bb2/4k3/4B3/3K4/8/8/8/4B3 w - - 0 2");
        assertFalse(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testPawnEvalBasic() {
        final Board board = new Board();
        board.clear();
        assertEquals(0, new Evaluation().pawnEval(board));
        board.restart();
        assertEquals(0, new Evaluation().pawnEval(board));
    }

    public void testPawnEvalDoublePawns() {
        final Board board = StringUtils.fromFen("8/p7/K7/p3p2p/k3p2P/4N3/PP4P1/8 w - - 0 1");
        assertEquals(30, new Evaluation().pawnEval(board));
    }

    public void testPawnEvalBackwardsPawns() {
        final Board board = StringUtils.fromFen("8/Qp4pk/p5b1/5p1p/3B2nP/1P4PK/P1P1r1B1/3r4 b - - 0 1");
        assertEquals(30, new Evaluation().pawnEval(board));
    }

    public void testPositionalValuePawn() {
        final Board board = new Board();
        board.restart();
        assertEquals(0, new Evaluation().computePositionalBonus(board));
        assertEquals(40, new Evaluation().computePositionalGain(PAWN, WHITE_TO_MOVE, E[1], E[3], board.getStage()));
        board.move(StringUtils.fromSimple("e2e4"));
        assertEquals(40, new Evaluation().computePositionalBonus(board));
        assertEquals(40, new Evaluation().computePositionalGain(PAWN, BLACK_TO_MOVE, E[6], E[4], board.getStage()));
        board.move(StringUtils.fromSimple("e7e5"));
        assertEquals(0, new Evaluation().computePositionalBonus(board));
    }

    public void testIsolatedPawnOnFileA() {
        final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/P7/8/4K3 w - - 0 1");
        assertEquals(Evaluation.PENALTY_ISOLATED_PAWN - Evaluation.VAL_PIECE_COUNTS[PAWN][7],
            new Evaluation().pawnEval(board));
    }

    public void testIsolatedPawnOnFileB() {
        final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/1P6/8/4K3 w - - 0 1");
        assertEquals(Evaluation.PENALTY_ISOLATED_PAWN - Evaluation.VAL_PIECE_COUNTS[PAWN][7],
            new Evaluation().pawnEval(board));
    }

    public void testIsolatedPawnOnFileH() {
        final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/7P/8/4K3 w - - 0 1");
        assertEquals(Evaluation.PENALTY_ISOLATED_PAWN - Evaluation.VAL_PIECE_COUNTS[PAWN][7],
            new Evaluation().pawnEval(board));
    }

    public void testPassedPawnOnFileA() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/P7/8/4K3 w - - 0 1");
        assertEquals(Evaluation.BONUS_PASSED_PAWN_PER_SQUARE * 5 + Evaluation.PENALTY_ISOLATED_PAWN +
                Evaluation.VAL_PIECE_COUNTS[PAWN][1] - Evaluation.VAL_PIECE_COUNTS[PAWN][0],
            new Evaluation().pawnEval(board));
    }

    public void testPassedPawnOnFileB() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/1P6/8/4K3 w - - 0 1");
        assertEquals(Evaluation.BONUS_PASSED_PAWN_PER_SQUARE * 5 + Evaluation.PENALTY_ISOLATED_PAWN +
                Evaluation.VAL_PIECE_COUNTS[PAWN][1] - Evaluation.VAL_PIECE_COUNTS[PAWN][0],
            new Evaluation().pawnEval(board));
    }

    public void testPassedPawnOnFileH() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/7P/8/4K3 w - - 0 1");
        assertEquals(Evaluation.BONUS_PASSED_PAWN_PER_SQUARE * 5 + Evaluation.PENALTY_ISOLATED_PAWN +
                Evaluation.VAL_PIECE_COUNTS[PAWN][1] - Evaluation.VAL_PIECE_COUNTS[PAWN][0],
            new Evaluation().pawnEval(board));
    }
}