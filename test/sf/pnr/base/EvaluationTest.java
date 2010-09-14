package sf.pnr.base;

import junit.framework.TestCase;
import sf.pnr.alg.PawnHashTable;

import static sf.pnr.base.Utils.*;
import static sf.pnr.base.Evaluation.*;

/**
 */
public class EvaluationTest extends TestCase {

    private Evaluation eval;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        eval = new Evaluation();
    }

    @Override
    protected void tearDown() throws Exception {
        eval = null;
        super.tearDown();
    }

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

    public void testUnstoppablePawn() {
        final Board board = StringUtils.fromFen("2k5/8/8/8/8/8/p7/4K3 b - - 0 1");
        final int score = eval.evaluate(board);
        assertTrue("Score: " + score, score > VAL_QUEEN - 200);
        assertTrue("Score: " + score, score < VAL_QUEEN + 200);
    }

    public void testPawnEvalBasic() {
        final Board board = new Board();
        board.clear();
        assertEquals(0, PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));

        eval = new Evaluation();
        board.restart();
        assertEquals(0, PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testPawnEvalDoublePawns() {
        final Board board = StringUtils.fromFen("8/p7/K7/p3p2p/k3p2P/4N3/PP4P1/8 w - - 0 1");
        final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
        final int positionalBonusWhite = VAL_POSITION_BONUS_PAWN[A[1] + shiftWhite] +
            VAL_POSITION_BONUS_PAWN[B[1] + shiftWhite] + VAL_POSITION_BONUS_PAWN[G[1] + shiftWhite] +
            VAL_POSITION_BONUS_PAWN[H[3] + shiftWhite];
        final int positionalBonusBlack = VAL_POSITION_BONUS_PAWN[A[4]] + VAL_POSITION_BONUS_PAWN[A[6]] +
            VAL_POSITION_BONUS_PAWN[E[3]] + VAL_POSITION_BONUS_PAWN[E[4]] + VAL_POSITION_BONUS_PAWN[H[4]] +
            BONUS_PASSED_PAWN_PER_SQUARE * 3;
        assertEquals(positionalBonusWhite - positionalBonusBlack - 2 * PENALTY_DOUBLE_PAWN - 3 * PENALTY_ISOLATED_PAWN,
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testPawnEvalBackwardsPawns() {
        final Board board = StringUtils.fromFen("8/Qp4pk/p5b1/5p1p/3B2nP/1P4PK/P1P1r1B1/3r4 b - - 0 1");
        assertEquals(30, PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testPositionalValuePawn() {
        final Board board = new Board();
        board.restart();
        assertEquals(0, eval.computePositionalBonusNoPawnAsWhite(board));
        assertEquals(40, Evaluation.computePositionalGain(PAWN, WHITE_TO_MOVE, E[1], E[3], board.getStage()));
        board.move(StringUtils.fromSimple("e2e4"));
        assertEquals(0, eval.computePositionalBonusNoPawnAsWhite(board));
        assertEquals(40, eval.computePositionalGain(PAWN, BLACK_TO_MOVE, E[6], E[4], board.getStage()));
        board.move(StringUtils.fromSimple("e7e5"));
        assertEquals(0, eval.computePositionalBonusNoPawnAsWhite(board));
    }

    public void testIsolatedPawnOnFileA() {
        final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/P7/8/4K3 w - - 0 1");
        final int positionalBonusWhite = VAL_POSITION_BONUS_PAWN[A[2] + SHIFT_POSITION_BONUS[WHITE]];
        int positionalBonusBlack = 0;
        for (int i = 0; i < 8; i++) {
            positionalBonusBlack += VAL_POSITION_BONUS_PAWN[getIndex(i, 6)];
        }
        assertEquals(positionalBonusWhite - positionalBonusBlack + PENALTY_ISOLATED_PAWN,
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testIsolatedPawnOnFileB() {
        final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/1P6/8/4K3 w - - 0 1");
        final int positionalBonusWhite = VAL_POSITION_BONUS_PAWN[B[2] + SHIFT_POSITION_BONUS[WHITE]];
        int positionalBonusBlack = 0;
        for (int i = 0; i < 8; i++) {
            positionalBonusBlack += VAL_POSITION_BONUS_PAWN[getIndex(i, 6)];
        }
        assertEquals(positionalBonusWhite - positionalBonusBlack + PENALTY_ISOLATED_PAWN,
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testIsolatedPawnOnFileH() {
        final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/7P/8/4K3 w - - 0 1");
        final int positionalBonusWhite = VAL_POSITION_BONUS_PAWN[H[2] + SHIFT_POSITION_BONUS[WHITE]];
        int positionalBonusBlack = 0;
        for (int i = 0; i < 8; i++) {
            positionalBonusBlack += VAL_POSITION_BONUS_PAWN[getIndex(i, 6)];
        }
        assertEquals(positionalBonusWhite - positionalBonusBlack + PENALTY_ISOLATED_PAWN,
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testPassedPawnOnFileA() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/P7/8/4K3 w - - 0 1");
        assertEquals(VAL_POSITION_BONUS_PAWN[A[2] + SHIFT_POSITION_BONUS[WHITE]] + BONUS_PASSED_PAWN_PER_SQUARE +
            PENALTY_ISOLATED_PAWN,
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testPassedPawnOnFileB() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/1P6/8/4K3 w - - 0 1");
        assertEquals(VAL_POSITION_BONUS_PAWN[B[2] + SHIFT_POSITION_BONUS[WHITE]] + BONUS_PASSED_PAWN_PER_SQUARE +
            PENALTY_ISOLATED_PAWN,
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testPassedPawnOnB4() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/1P6/8/8/4K3 w - - 0 1");
        assertEquals(VAL_POSITION_BONUS_PAWN[B[3] + SHIFT_POSITION_BONUS[WHITE]] + BONUS_PASSED_PAWN_PER_SQUARE * 2 +
            PENALTY_ISOLATED_PAWN,
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testPassedPawnOnFileH() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/7P/8/4K3 w - - 0 1");
        assertEquals(VAL_POSITION_BONUS_PAWN[H[2] + SHIFT_POSITION_BONUS[WHITE]] + BONUS_PASSED_PAWN_PER_SQUARE +
            PENALTY_ISOLATED_PAWN,
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testPromotionDistanceStoppableWhite() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/1P6/4K3 w - - 0 1");
        final long pawnHashValue = eval.pawnEval(board);
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceStoppable2White() {
        final Board board = StringUtils.fromFen("4k3/8/8/1P6/8/8/8/4K3 w - - 0 1");
        final long pawnHashValue = eval.pawnEval(board);
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceUnstoppableWhite() {
        final Board board = StringUtils.fromFen("4k3/8/1P6/8/8/8/8/4K3 w - - 0 1");
        final long pawnHashValue = eval.pawnEval(board);
        assertEquals(2, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, BLACK_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceUnstoppable2White() {
        final Board board = StringUtils.fromFen("4k3/1P6/8/8/8/8/8/4K3 w - - 0 1");
        final long pawnHashValue = eval.pawnEval(board);
        assertEquals(1, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(1, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, BLACK_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceStoppableBlack() {
        final Board board = StringUtils.fromFen("4k3/8/1p6/8/8/8/8/4K3 w - - 0 1");
        final long pawnHashValue = eval.pawnEval(board);
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceStoppable2Black() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/1p6/8/8/4K3 w - - 0 1");
        final long pawnHashValue = eval.pawnEval(board);
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceUnstoppableBlack() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/1p6/8/4K3 w - - 0 1");
        final long pawnHashValue = eval.pawnEval(board);
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(2, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceUnstoppable2Black() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/1p6/4K3 w - - 0 1");
        final long pawnHashValue = eval.pawnEval(board);
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(1, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(1, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }
}