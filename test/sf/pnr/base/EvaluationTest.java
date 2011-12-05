package sf.pnr.base;

import junit.framework.TestCase;
import sf.pnr.alg.PawnHashTable;

import static sf.pnr.base.Evaluation.*;
import static sf.pnr.base.Utils.*;

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

    public void testDrawByInsufficientMaterialKK() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/3K4/8/8/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKNK() {
        final Board board = StringUtils.fromFen("4k3/8/8/1N6/3K4/8/8/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKBK() {
        final Board board = StringUtils.fromFen("4k3/8/8/1B6/3K4/8/8/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKNKB() {
        final Board board = StringUtils.fromFen("4k3/8/4b3/1N6/3K4/8/8/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKNKN() {
        final Board board = StringUtils.fromFen("4k3/8/4n3/1N6/3K4/8/8/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKBKB() {
        final Board board = StringUtils.fromFen("4k3/8/4b3/8/3K4/8/2B5/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKNNK() {
        final Board board = StringUtils.fromFen("4k3/8/4N3/1N6/3K4/8/8/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKNNKN() {
        final Board board = StringUtils.fromFen("4k3/4n3/4N3/1N6/3K4/8/8/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKNNKB() {
        final Board board = StringUtils.fromFen("4k3/4b3/4N3/1N6/3K4/8/8/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKBNKN() {
        final Board board = StringUtils.fromFen("4k3/4n3/4B3/1N6/3K4/8/8/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKBNKB() {
        final Board board = StringUtils.fromFen("4k3/4b3/4B3/1N6/3K4/8/8/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKBNK() {
        final Board board = StringUtils.fromFen("1k6/4K3/8/1B1N4/8/8/8/8 w - - 0 1");
        assertFalse(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKBBKN() {
            final Board board = StringUtils.fromFen("7k/5n2/7K/8/4B2B/8/8/8 w - - 0 1");
        assertFalse(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKBBKB() {
            final Board board = StringUtils.fromFen("7k/2b5/7K/1B6/7B/8/8/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKNNKNN() {
        final Board board = StringUtils.fromFen("4k3/3nn3/4N3/1N6/3K4/8/8/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialKNNBK() {
        final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 0 1");
        assertFalse(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialBishopsOnSameColors() {
            final Board board = StringUtils.fromFen("7k/7B/7K/7B/8/8/8/8 w - - 0 1");
        assertTrue(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawByInsufficientMaterialBishopsOnOppositeColors() {
            final Board board = StringUtils.fromFen("7k/7B/7K/8/7B/8/8/8 w - - 0 1");
        assertFalse(Evaluation.drawByInsufficientMaterial(board));
    }

    public void testDrawProbabilityKK() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/3K4/8/8/8 w - - 0 1");
        assertEquals(1.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKNK() {
        final Board board = StringUtils.fromFen("4k3/8/8/1N6/3K4/8/8/8 w - - 0 1");
        assertEquals(1.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKBK() {
        final Board board = StringUtils.fromFen("4k3/8/8/1B6/3K4/8/8/8 w - - 0 1");
        assertEquals(1.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKNKB() {
        final Board board = StringUtils.fromFen("4k3/8/4b3/1N6/3K4/8/8/8 w - - 0 1");
        assertEquals(1.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKNKN() {
        final Board board = StringUtils.fromFen("4k3/8/4n3/1N6/3K4/8/8/8 w - - 0 1");
        assertEquals(1.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKBKB() {
        final Board board = StringUtils.fromFen("4k3/8/4b3/8/3K4/8/2B5/8 w - - 0 1");
        assertEquals(1.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKNNK() {
        final Board board = StringUtils.fromFen("4k3/8/4N3/1N6/3K4/8/8/8 w - - 0 1");
        assertEquals(1.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKNNKN() {
        final Board board = StringUtils.fromFen("4k3/4n3/4N3/1N6/3K4/8/8/8 w - - 0 1");
        assertEquals(1.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKNNKB() {
        final Board board = StringUtils.fromFen("4k3/4b3/4N3/1N6/3K4/8/8/8 w - - 0 1");
        assertEquals(1.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKBNKN() {
        final Board board = StringUtils.fromFen("4k3/4n3/4B3/1N6/3K4/8/8/8 w - - 0 1");
        assertEquals(0.75, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKBNKB() {
        final Board board = StringUtils.fromFen("4k3/4b3/4B3/1N6/3K4/8/8/8 w - - 0 1");
        assertEquals(0.75, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKBBKN() {
            final Board board = StringUtils.fromFen("7k/5n2/7K/8/4B2B/8/8/8 w - - 0 1");
        assertEquals(DRAW_PROBABILITY_BISHOPS_ON_OPPOSITE, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKBBKB() {
            final Board board = StringUtils.fromFen("7k/2b5/7K/1B6/7B/8/8/8 w - - 0 1");
        assertEquals(DRAW_PROBABILITY_BISHOPS_ON_OPPOSITE, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKNNKNN() {
        final Board board = StringUtils.fromFen("4k3/3nn3/4N3/1N6/3K4/8/8/8 w - - 0 1");
        assertEquals(1.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityKNNBK() {
        final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 0 1");
        assertEquals(0.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityBishopsOnSameColors() {
            final Board board = StringUtils.fromFen("7k/7B/7K/7B/8/8/8/8 w - - 0 1");
        assertEquals(1.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityBishopsOnOppositeColors() {
            final Board board = StringUtils.fromFen("7k/7B/7K/8/7B/8/8/8 w - - 0 1");
        assertEquals(DRAW_PROBABILITY_BISHOPS_ON_OPPOSITE, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityBishopsOnOppositeColors2() {
        final Board board = StringUtils.fromFen("4bb2/4k3/4B3/3K4/8/8/8/4B3 w - - 0 2");
        assertEquals(DRAW_PROBABILITY_BISHOPS_ON_OPPOSITE, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityFiftyMoves85() {
        final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 85 1");
        assertEquals(0.25, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityFiftyMoves90() {
        final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 90 1");
        assertEquals(0.50, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityFiftyMoves95() {
        final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 95 1");
        assertEquals(0.75, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityFiftyMoves100() {
        final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 100 1");
        assertEquals(1.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityFiftyMoves105() {
        final Board board = StringUtils.fromFen("4k3/8/4N3/4B3/1N1K4/8/8/8 w - - 105 1");
        assertEquals(1.0, Evaluation.drawProbability(board));
    }

    public void testDrawProbabilityFiftyMoves95OppositeBishops() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/1B1KB3/8/8/8 w - - 95 1");
        assertEquals(0.80, Evaluation.drawProbability(board));
    }

    public void testEvalScoreStoredInHash() {
        final Board board = StringUtils.fromFen("7k/5n2/7K/8/4B2B/8/8/8 w - - 0 1");
        final int score = eval.evaluate(board);
        assertEquals(score, eval.evaluate(board));
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
        assertEquals(getPositionalBonusPawnAndKing(board) - 2 * PENALTY_DOUBLE_PAWN - 3 * PENALTY_ISOLATED_PAWN -
            passedPawnBonus(3, board.getStage()) +
            VAL_PIECE_COUNTS[PAWN][board.getPieces(WHITE, PAWN)[0]] - VAL_PIECE_COUNTS[PAWN][board.getPieces(BLACK, PAWN)[0]],
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    private int passedPawnBonus(final int distance, final int stage) {
        return (BONUS_PASSED_PAWN_BONUS_OPENING[distance - 1] * (STAGE_MAX - stage) +
            BONUS_PASSED_PAWN_BONUS_ENDGAME[distance - 1] * stage) / STAGE_MAX;
    }

    public void testPawnEvalBackwardsPawns() {
        final Board board = StringUtils.fromFen("8/Qp4pk/p5b1/5p1p/3B2nP/1P4PK/P1P1r1B1/3r4 b - - 0 1");
        assertEquals(30, PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testIsolatedPawnOnFileA() {
        final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/P7/8/4K3 w - - 0 1");
        final int positionalBonus = getPositionalBonusPawnAndKing(board);
        assertEquals(positionalBonus + PENALTY_ISOLATED_PAWN +
            VAL_PIECE_COUNTS[PAWN][board.getPieces(WHITE, PAWN)[0]] - VAL_PIECE_COUNTS[PAWN][board.getPieces(BLACK, PAWN)[0]]
            - 6 * passedPawnBonus(6, board.getStage()),
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testIsolatedPawnOnFileB() {
        final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/1P6/8/4K3 w - - 0 1");
        assertEquals(getPositionalBonusPawnAndKing(board) + PENALTY_ISOLATED_PAWN +
            VAL_PIECE_COUNTS[PAWN][board.getPieces(WHITE, PAWN)[0]] - VAL_PIECE_COUNTS[PAWN][board.getPieces(BLACK, PAWN)[0]]
            - 5 * passedPawnBonus(6, board.getStage()),
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testIsolatedPawnOnFileH() {
        final Board board = StringUtils.fromFen("4k3/pppppppp/8/8/8/7P/8/4K3 w - - 0 1");
        assertEquals(getPositionalBonusPawnAndKing(board) + PENALTY_ISOLATED_PAWN +
            VAL_PIECE_COUNTS[PAWN][board.getPieces(WHITE, PAWN)[0]] - VAL_PIECE_COUNTS[PAWN][board.getPieces(BLACK, PAWN)[0]]
            - 6 * passedPawnBonus(6, board.getStage()),
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testPassedPawnOnFileA() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/P7/8/4K3 w - - 0 1");
        assertEquals(getPositionalBonusPawnAndKing(board) + passedPawnBonus(5, board.getStage()) +
            PENALTY_ISOLATED_PAWN +
            VAL_PIECE_COUNTS[PAWN][board.getPieces(WHITE, PAWN)[0]] - VAL_PIECE_COUNTS[PAWN][board.getPieces(BLACK, PAWN)[0]],
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testPassedPawnOnFileB() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/1P6/8/4K3 w - - 0 1");
        assertEquals(getPositionalBonusPawnAndKing(board) + passedPawnBonus(5, board.getStage()) +
            PENALTY_ISOLATED_PAWN +
            VAL_PIECE_COUNTS[PAWN][board.getPieces(WHITE, PAWN)[0]] - VAL_PIECE_COUNTS[PAWN][board.getPieces(BLACK, PAWN)[0]],
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testPassedPawnOnB4() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/1P6/8/8/4K3 w - - 0 1");
        assertEquals(getPositionalBonusPawnAndKing(board) + passedPawnBonus(4, board.getStage()) +
            PENALTY_ISOLATED_PAWN +
            VAL_PIECE_COUNTS[PAWN][board.getPieces(WHITE, PAWN)[0]] - VAL_PIECE_COUNTS[PAWN][board.getPieces(BLACK, PAWN)[0]],
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testPassedPawnOnFileH() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/7P/8/4K3 w - - 0 1");
        assertEquals(getPositionalBonusPawnAndKing(board) + passedPawnBonus(5, board.getStage()) +
            PENALTY_ISOLATED_PAWN +
            VAL_PIECE_COUNTS[PAWN][board.getPieces(WHITE, PAWN)[0]] - VAL_PIECE_COUNTS[PAWN][board.getPieces(BLACK, PAWN)[0]],
            PawnHashTable.getValueFromPawnHashValue(eval.pawnEval(board)));
    }

    public void testPromotionDistanceStoppableWhite() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/1P6/4K3 w - - 0 1");
        final int pawnHashValue = eval.pawnEval(board);
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceStoppable2White() {
        final Board board = StringUtils.fromFen("4k3/8/8/1P6/8/8/8/4K3 w - - 0 1");
        final int pawnHashValue = eval.pawnEval(board);
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceUnstoppableWhite() {
        final Board board = StringUtils.fromFen("4k3/8/1P6/8/8/8/8/4K3 w - - 0 1");
        final int pawnHashValue = eval.pawnEval(board);
        assertEquals(2, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, BLACK_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceUnstoppable2White() {
        final Board board = StringUtils.fromFen("4k3/1P6/8/8/8/8/8/4K3 w - - 0 1");
        final int pawnHashValue = eval.pawnEval(board);
        assertEquals(1, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(1, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, BLACK_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceStoppableBlack() {
        final Board board = StringUtils.fromFen("4k3/8/1p6/8/8/8/8/4K3 w - - 0 1");
        final int pawnHashValue = eval.pawnEval(board);
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceStoppable2Black() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/1p6/8/8/4K3 w - - 0 1");
        final int pawnHashValue = eval.pawnEval(board);
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceUnstoppableBlack() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/1p6/8/4K3 w - - 0 1");
        final int pawnHashValue = eval.pawnEval(board);
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(2, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPromotionDistanceUnstoppable2Black() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/1p6/4K3 w - - 0 1");
        final int pawnHashValue = eval.pawnEval(board);
        assertEquals(7, PawnHashTable.getUnstoppablePawnDistWhite(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(1, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, WHITE_TO_MOVE));
        assertEquals(1, PawnHashTable.getUnstoppablePawnDistBlack(pawnHashValue, BLACK_TO_MOVE));
    }

    public void testPositionalBonus2() {
        final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
        final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
        final int[] pawnEndGame = Evaluation.VAL_POSITION_BONUS_PAWN_ENDGAME;
        assertTrue(pawnEndGame[A[6] + shiftWhite] >= pawnEndGame[A[1] + shiftWhite]);
        assertTrue(pawnEndGame[A[6] + shiftBlack] <= pawnEndGame[A[1] + shiftBlack]);
    }

    public void testPositionalBonusSymmetry() throws NoSuchFieldException, IllegalAccessException {
        final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
        final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
        for (int[] arr: Evaluation.VAL_POSITION_BONUS_OPENING) {
            assertEquals(128, arr.length);
            for (int rank = 0; rank < 8; rank++) {
                for (int file = 0; file < 8; file++) {
                    final int whitePos = Utils.getPosition(file, rank);
                    final int blackPos = Utils.getPosition(file, 7 - rank);
                    assertEquals(StringUtils.toString0x88(whitePos), arr[whitePos + shiftWhite], arr[blackPos + shiftBlack]);
                }
            }
        }
        for (int[] arr: Evaluation.VAL_POSITION_BONUS_ENDGAME) {
            assertEquals(128, arr.length);
            for (int rank = 0; rank < 8; rank++) {
                for (int file = 0; file < 8; file++) {
                    final int whitePos = Utils.getPosition(file, rank);
                    final int blackPos = Utils.getPosition(file, 7 - rank);
                    assertEquals(StringUtils.toString0x88(whitePos), arr[whitePos + shiftWhite], arr[blackPos + shiftBlack]);
                }
            }
        }
    }

    public void testPawnMirrorEvalPawnStorm() {
        final Board boardWhite = StringUtils.fromFen("1k6/8/1P6/P1P5/8/8/8/6K1 w - - 0 1");
        final int pawnHashValueWhite = eval.pawnEval(boardWhite);
        final int pawnValueWhite = PawnHashTable.getValueFromPawnHashValue(pawnHashValueWhite);
        final int valueWhite = eval.evaluate(boardWhite);

        final Board boardBlack = StringUtils.fromFen("1k6/8/8/8/5p1p/6p1/8/6K1 b - - 0 1");
        final int pawnHashValueBlack = eval.pawnEval(boardBlack);
        final int pawnValueBlack = PawnHashTable.getValueFromPawnHashValue(pawnHashValueBlack);
        final int valueBlack = eval.evaluate(boardBlack);

        assertEquals(pawnValueWhite, -pawnValueBlack);
        assertEquals(valueWhite, valueBlack);
    }

    public void testPawnMirrorEvalWithKnights() {
        final Board boardWhite = StringUtils.fromFen("5q2/3Q1N1p/r4kp1/4p3/3nb3/1B5P/5PP1/1R4K1 w - - 0 1");
        final int pawnHashValueWhite = eval.pawnEval(boardWhite);
        final int pawnValueWhite = PawnHashTable.getValueFromPawnHashValue(pawnHashValueWhite);
        final int valueWhite = eval.evaluate(boardWhite);

        final Board boardBlack = StringUtils.fromFen(StringUtils.mirrorFen("5q2/3Q1N1p/r4kp1/4p3/3nb3/1B5P/5PP1/1R4K1 w - - 0 1"));
        final int pawnHashValueBlack = eval.pawnEval(boardBlack);
        final int pawnValueBlack = PawnHashTable.getValueFromPawnHashValue(pawnHashValueBlack);
        final int valueBlack = eval.evaluate(boardBlack);

        assertEquals(pawnValueWhite, -pawnValueBlack);
        assertEquals(valueWhite, valueBlack);
    }

    public void testPawnMirrorEvalHalfInitial() {
        final Board boardWhite = StringUtils.fromFen("5k2/8/8/8/8/8/PPPPPPPP/RNBQKBNR w KQ - 0 1");
        final int pawnHashValueWhite = eval.pawnEval(boardWhite);
        final int pawnValueWhite = PawnHashTable.getValueFromPawnHashValue(pawnHashValueWhite);
        final int valueWhite = eval.evaluate(boardWhite);

        final Board boardBlack = StringUtils.fromFen("rnbqkbnr/pppppppp/8/8/8/8/8/2K5 b kq - 0 1");
        final int pawnHashValueBlack = eval.pawnEval(boardBlack);
        final int pawnValueBlack = PawnHashTable.getValueFromPawnHashValue(pawnHashValueBlack);
        final int valueBlack = eval.evaluate(boardBlack);

        assertEquals(pawnValueWhite, -pawnValueBlack);
        assertEquals(valueWhite, valueBlack);
    }

    private static int getPositionalBonusPawnAndKing(final Board board) {
        final int shiftWhite = SHIFT_POSITION_BONUS[WHITE];
        final int shiftBlack = SHIFT_POSITION_BONUS[BLACK];
        int openingBonus = VAL_POSITION_BONUS_KING[board.getKing(WHITE) + shiftWhite] -
            VAL_POSITION_BONUS_KING[board.getKing(BLACK) + shiftBlack];
        int endgameBonus = VAL_POSITION_BONUS_KING_ENDGAME[board.getKing(WHITE) + shiftWhite] -
            VAL_POSITION_BONUS_KING_ENDGAME[board.getKing(BLACK) + shiftBlack];
        final int[] whitePawns = board.getPieces(WHITE, PAWN);
        for (int i = whitePawns[0]; i > 0; i--) {
            final int pawn = whitePawns[i];
            openingBonus += VAL_POSITION_BONUS_PAWN[pawn + shiftWhite];
            endgameBonus += VAL_POSITION_BONUS_PAWN_ENDGAME[pawn + shiftWhite];
        }
        final int[] blackPawns = board.getPieces(BLACK, PAWN);
        for (int i = blackPawns[0]; i > 0; i--) {
            final int pawn = blackPawns[i];
            openingBonus -= VAL_POSITION_BONUS_PAWN[pawn + shiftBlack];
            endgameBonus -= VAL_POSITION_BONUS_PAWN_ENDGAME[pawn + shiftBlack];
        }
        final int stage = board.getStage();
        return (openingBonus * (STAGE_MAX - stage) + endgameBonus * stage) / STAGE_MAX;
    }

    public void testCastlingPenaltyCastling() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/8/4K2R w K - 1 1");
        assertEquals(PENALTY_CASTLING_PENDING - PENALTY_CASTLING_MISSED,
            getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
        board.move(StringUtils.fromSimple("e1g1") | MT_CASTLING_KINGSIDE);
        assertEquals(-PENALTY_CASTLING_MISSED,
            getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
    }

    public void testCastlingPenaltyRookMoves() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        assertEquals(PENALTY_CASTLING_PENDING_BOTH - PENALTY_CASTLING_MISSED,
            getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
        board.move(StringUtils.fromSimple("a1a2"));
        assertEquals(PENALTY_CASTLING_PENDING - PENALTY_CASTLING_MISSED,
            getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
        board.move(StringUtils.fromSimple("e8e7"));
        board.move(StringUtils.fromSimple("h1h2"));
        assertEquals(PENALTY_CASTLING_MISSED - PENALTY_CASTLING_MISSED,
            getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
    }

    public void testCastlingPenaltyKingMove() {
        final Board board = StringUtils.fromFen("4k3/8/8/8/8/8/8/4K2R w K - 1 1");
        assertEquals(PENALTY_CASTLING_PENDING - PENALTY_CASTLING_MISSED,
            getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
        board.move(StringUtils.fromSimple("e1e2"));
        assertEquals(PENALTY_CASTLING_MISSED-PENALTY_CASTLING_MISSED,
            getCastlingPenaltyAsWhite(board.getState(), board.getState2()));
    }

    public void testMobilityBonusArraySizes() {
        assertEquals(8 + 1, BONUS_MOBILITY_KNIGHT.length);
        assertEquals(13 + 1, BONUS_MOBILITY_BISHOP.length);
        assertEquals(14 + 1, BONUS_MOBILITY_ROOK.length);
        assertEquals(27 + 1, BONUS_MOBILITY_QUEEN.length);
        assertEquals(8 + 1, BONUS_MOBILITY_KING.length);
    }

    public void testUnstoppablePawnWithCapture() {
        final Board board = StringUtils.fromFen("8/8/1p1K4/P7/2k5/8/8/8 w - - 0 1");
        final int score = eval.evaluate(board);
        assertTrue(score > 700);
    }

    public void test() {
        final Board board = StringUtils.fromFen("1n2r3/p1pq1kp1/1b1pNpp1/3P4/5RP1/3Q3P/1B3P2/6K1 w - - 0 1");
        eval.evaluate(board);
    }
}