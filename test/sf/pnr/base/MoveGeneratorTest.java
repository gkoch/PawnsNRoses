package sf.pnr.base;

import junit.framework.TestCase;

import static sf.pnr.base.StringUtils.*;
import static sf.pnr.base.Utils.*;
import static sf.pnr.base.Evaluation.*;

public class MoveGeneratorTest extends TestCase {

    private MoveGenerator moveGenerator = new MoveGenerator();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        moveGenerator.pushFrame();
    }

    @Override
    protected void tearDown() throws Exception {
        moveGenerator.popFrame();
        super.tearDown();
    }

    public void testPseudoLegalMoveGenerationSimple() {
		final Board board = new Board();
		board.restart();
		
		moveGenerator.generatePseudoLegalMoves(board);
        int[] moves = moveGenerator.getMoves();
        assertEquals(4, moves[0]);
        assertTrue(containsMoves(moves, fromSimpleList("b1a3, b1c3, g1f3, g1h3")));
        moves[0] = 0;
        moveGenerator.generatePseudoLegalMovesNonAttacking(board);
        moves = moveGenerator.getMoves();
        assertEquals(16, moves[0]);
        assertTrue(containsMoves(moves, fromSimpleList(
            "a2a3, a2a4, b2b3, b2b4, c2c3, c2c4, d2d3, d2d4, e2e3, e2e4, f2f3, f2f4, g2g3, g2g4, h2h3, h2h4")));

		board.move(fromSimple("a2a3"));
		moveGenerator.generatePseudoLegalMoves(board);
        moves = moveGenerator.getMoves();
        assertEquals(4, moves[0]);
        assertTrue(containsMoves(moves, fromSimpleList("b8a6, b8c6, g8f6, g8h6")));
        moves[0] = 0;
        moveGenerator.generatePseudoLegalMovesNonAttacking(board);
        moves = moveGenerator.getMoves();
        assertEquals(16, moves[0]);
        assertTrue(containsMoves(moves, fromSimpleList(
            "a7a6, a7a5, b7b6, b7b5, c7c6, c7c5, d7d6, d7d5, e7e6, e7e5, f7f6, f7f5, g7g6, g7g5, h7h6, h7h5")));

		board.move(fromSimple("e7e5"));
		moveGenerator.generatePseudoLegalMoves(board);
		moves = moveGenerator.getMoves();
        assertEquals(4, moves[0]);
        assertTrue(containsMoves(moves, fromSimpleList("a1a2, b1c3, g1f3, g1h3")));
        moves[0] = 0;
        moveGenerator.generatePseudoLegalMovesNonAttacking(board);
        moves = moveGenerator.getMoves();
        assertEquals(15, moves[0]);
        assertTrue(containsMoves(moves,
            fromSimpleList("a3a4, b2b3, b2b4, c2c3, c2c4, d2d3, d2d4, e2e3, e2e4, f2f3, f2f4, g2g3, g2g4, h2h3, h2h4")));
	}

    public void testPseudoLegalMoveGenerationPositions() {
        final Board board = fromFen("k7/8/8/8/q7/8/8/1R3R1K w - - 0 1");
        moveGenerator.generatePseudoLegalMoves(board);
        int[] moves = moveGenerator.getMoves();
        assertEquals(25, moves[0]);
        assertTrue(containsMove(moves, fromSimple("b1a1")));
        moves[0] = 0;
        moveGenerator.generatePseudoLegalMovesNonAttacking(board);
        moves = moveGenerator.getMoves();
        assertEquals(0, moves[0]);
    }

    public void testBugRd1d1() {
        final Board board = fromFen("k7/8/8/8/q7/8/8/1R3R1K w - - 0 1");
        board.move(fromSimple("b1d1"));
        board.move(fromSimple("a4d7"));

        moveGenerator.generatePseudoLegalMoves(board);
        int captures[] = moveGenerator.getWinningCaptures();
        assertFalse(containsMove(captures, fromSimple("d1d1")));
    }

    public void testBlockingCaptures() {
        final Board board = fromFen("k7/8/8/8/q7/8/8/1R3R1K w - - 0 1");
        board.move(fromSimple("b1a1"));
        board.move(fromSimple("a4a1"));
        board.move(fromSimple("f1e1"));

        moveGenerator.generatePseudoLegalMoves(board);
        int captures[] = moveGenerator.getWinningCaptures();
        assertFalse(containsMove(captures, fromSimple("a1h1")));
    }

    public void testBugKh1h1() {
        final Board board = fromFen("k7/8/8/8/q7/8/8/1R3R1K w - - 0 1");
        board.move(fromSimple("f1f4"));
        board.move(fromSimple("a4f4"));
        board.move(fromSimple("b1c1"));
        board.move(fromSimple("f4g3"));
        board.move(fromSimple("c1b1"));
        board.move(fromSimple("g3h2"));

        moveGenerator.generatePseudoLegalMoves(board);
        int captures[] = moveGenerator.getWinningCaptures();
        assertFalse(containsMove(captures, fromSimple("h1h1")));
    }

    public void testGenerateEnPassant() {
        Board board = fromFen("5rk1/5N1p/8/5Qp1/8/8/8/7K w - g6 0 2");
        final int[] captures = new int[3];
        captures[0] = 0;
        MoveGenerator.generateEnPassantMoves(board, captures);
        assertEquals(0, captures[0]);

        captures[0] = 0;
        board = fromFen("5rk1/5N1p/8/5Pp1/8/8/8/7K w - g6 0 2");
        MoveGenerator.generateEnPassantMoves(board, captures);
        assertEquals(1, captures[0]);
        assertEquals(fromSimple("f5g6 e.p."), captures[1] & BASE_INFO);

        captures[0] = 0;
        board = fromFen("5rk1/5N1p/8/6pP/8/8/8/7K w - g6 0 2");
        MoveGenerator.generateEnPassantMoves(board, captures);
        assertEquals(1, captures[0]);
        assertEquals(fromSimple("h5g6 e.p."), captures[1] & BASE_INFO);

        captures[0] = 0;
        board = fromFen("5rk1/5N1p/8/5PpP/8/8/8/7K w - g6 0 2");
        MoveGenerator.generateEnPassantMoves(board, captures);
        assertEquals(2, captures[0]);
        assertTrue(containsMove(captures, fromSimple("f5g6 e.p.")));
        assertTrue(containsMove(captures, fromSimple("h5g6 e.p.")));

        captures[0] = 0;
        board = fromFen("5rk1/5N1p/8/6Pp/8/8/8/7K w - h6 0 2");
        MoveGenerator.generateEnPassantMoves(board, captures);
        assertEquals(1, captures[0]);
        assertEquals(fromSimple("g5h6 e.p."), captures[1] & BASE_INFO);
    }

    public void testGeneratePromotion() {
        final Board board = fromFen("2k5/8/8/8/8/8/p7/4K3 b - - 0 1");
        final int[] moves = new int[5];
        moves[0] = 0;
        final int[] promotions = new int[5];
        promotions[0] = 0;
        MoveGenerator.generatePseudoLegalMovesPawn(board, moves, promotions);
        assertEquals(0, moves[0]);
        assertEquals(4, promotions[0]);
        assertTrue(containsMove(promotions, fromSimple("a2a1Q")));
        assertTrue(containsMove(promotions, fromSimple("a2a1R")));
        assertTrue(containsMove(promotions, fromSimple("a2a1B")));
        assertTrue(containsMove(promotions, fromSimple("a2a1N")));
    }

    public void testGeneratePawnMoves() {
        final Board board = fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        final int[] moves = new int[10];
        moves[0] = 0;
        final int[] promotions = new int[1];
        promotions[0] = 0;
        MoveGenerator.generatePseudoLegalMovesPawn(board, moves, promotions);
        assertEquals(6, moves[0]);
        assertEquals(0, promotions[0]);
    }

    public void testGenerateCastling() {
        final Board board = fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        final int[] moves = new int[10];
        moves[0] = 0;
        MoveGenerator.generateCastling(board, moves);
        assertEquals(2, moves[0]);
    }

    public void testNoCapturesAtTheBeginning() {
        final Board board = fromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
        moveGenerator.generatePseudoLegalMoves(board);
        final int[] moves = moveGenerator.getWinningCaptures();
        assertEquals(0, moves[0]);
    }

    public void testStaticExchangeEvaluation() {
        final Board board = fromFen("r3r1k1/6p1/1pqp2Np/2p1nP1P/N1PbPQ2/1P3PR1/P2K4/7R w - - 0 1");
        final int value = new MoveGenerator().staticExchangeEvaluation(board, fromString0x88("f4"), fromString0x88("e5"));
        assertEquals(VAL_KNIGHT - VAL_QUEEN, value);
    }

    public void testStaticExchangeEvaluation2() {
        final Board board = fromFen("3rk2r/pp1n1pb1/2pBpn1p/5Q2/2qP4/5N2/PP3PPP/R3R1K1 w - - 0 2");
        final int value = new MoveGenerator().staticExchangeEvaluation(board, fromString0x88("e1"), fromString0x88("e6"));
        assertEquals(VAL_PAWN - VAL_ROOK, value);
    }

    public void testStaticExchangeEvaluation3() {
        final Board board = fromFen("1B2kb1r/1R2p2p/5p2/3N2p1/2nPn3/4P3/2r3PP/2N1R1K1 w k - 0 1");
        assertEquals(VAL_PAWN - VAL_KNIGHT,
            new MoveGenerator().staticExchangeEvaluation(board, fromString0x88("d5"), fromString0x88("e7")));
    }

    public void testStaticExchangeEvaluation4() {
        final Board board = fromFen("1B2kb1r/1R2p2p/5p2/3N2p1/2nPn3/4P3/2r3PP/2N1R1K1 b k - 0 2");
        assertEquals(VAL_KNIGHT - VAL_ROOK,
            new MoveGenerator().staticExchangeEvaluation(board, fromString0x88("c2"), fromString0x88("c1")));
    }

    public void testStaticExchangeEvaluation5() {
        final Board board = fromFen("1n2r1k1/q2p1ppp/8/1P6/8/5PKN/P3B1PP/R3R3 b - - 0 2");
        assertEquals(VAL_BISHOP - VAL_ROOK,
            new MoveGenerator().staticExchangeEvaluation(board, fromString0x88("e8"), fromString0x88("e2")));
    }

    public void testStaticExchangeEvaluation6() {
        final Board board = fromFen("r1b1r1k1/1p1nqpbp/p1pp2p1/4p3/2PPP1n1/1PN2NP1/PBQ2PBP/R2R2K1 w - - 2 3");
        assertEquals(VAL_PAWN - VAL_PAWN,
            new MoveGenerator().staticExchangeEvaluation(board, fromString0x88("d4"), fromString0x88("e5")));
    }

    public void testStaticExchangeEvaluation7() {
        final Board board = fromFen("r1b1r1k1/pp1n1pp1/5n1p/q2p1B2/2pP3B/2Q1PN2/PP3PPP/R1R3K1 b - - 0 14");
        assertEquals(VAL_QUEEN - VAL_QUEEN,
            new MoveGenerator().staticExchangeEvaluation(board, fromString0x88("a5"), fromString0x88("c3")));
    }

    public static boolean containsMoves(final int[] moves, final int... containedMoves) {
        for (int move: containedMoves) {
            if (containsMove(moves, move)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsMove(final int[] moves, final int move) {
		for (int test: moves) {
			if ((test & BASE_INFO) == move) {
				return true;
			}
		}
		return false;
	}
}