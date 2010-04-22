package sf.pnr.base;

import junit.framework.TestCase;

import static sf.pnr.base.StringUtils.*;
import static sf.pnr.base.Utils.*;

/**
 */
public class StringUtilsTest extends TestCase {

    public void testFromFenPieceArrays() {
        final Board board = fromFen("k7/8/8/8/q7/8/8/1R3R1K w - - 0 1");
        final int[] blackQueens = board.getPieces(BLACK_TO_MOVE, QUEEN);
        assertEquals(1, blackQueens[0]);
        assertEquals(A[3], blackQueens[1]);
        final int[] whiteRooks = board.getPieces(WHITE_TO_MOVE, ROOK);
        assertEquals(2, whiteRooks[0]);
        final int blackKing = board.getKing(BLACK_TO_MOVE);
        assertEquals(A[7], blackKing);
        final int whiteKing = board.getKing(WHITE_TO_MOVE);
        assertEquals(H[0], whiteKing);
    }

    public void testSimple() {
        assertEquals(fromSimple("a4b3") | MT_EN_PASSANT, fromSimple("a4b3 e.p."));
        assertEquals(fromSimple("a2a1") | MT_PROMOTION_KNIGHT, fromSimple("a2a1N"));
        assertEquals(fromSimple("a2a1") | MT_PROMOTION_BISHOP, fromSimple("a2a1B"));
        assertEquals(fromSimple("a2a1") | MT_PROMOTION_ROOK, fromSimple("a2a1R"));
        assertEquals(fromSimple("a2a1") | MT_PROMOTION_QUEEN, fromSimple("a2a1Q"));
    }

    public void testShortCheck() {
        final Board board = fromFen("1B2K2n/1QNp1p2/2Pk1P1N/1bp3P1/8/3P1p2/1b6/4qn2 w - - 0 1");
        assertEquals("Ne6+", toShort(board, fromSimple("c7e6")));
    }

    public void testShortMate() {
        final Board board = fromFen("1b6/2p1p3/1q6/1P6/3n4/3B2B1/5N2/K1k5 w - - 0 1");
        assertEquals("Bf4#", toShort(board, fromSimple("g3f4")));
    }

    public void testMultipleRooks() {
        final Board board = fromFen("5r1k/2p3pp/3p4/3Q4/3P2R1/1N6/2B3PP/4rRK1 b - - 0 1");
        assertEquals("Rfxf1#", toShort(board, fromSimple("f8f1")));
        assertEquals("Rexf1#", toShort(board, fromSimple("e1f1")));
    }

    public void testMateFromCastling() {
        final Board board = fromFen("8/8/8/8/8/5N2/1pr3PP/r1k1K2R w K - 0 1");
        assertEquals("O-O#", toShort(board, fromSimple("e1g1") | MT_CASTLING_KINGSIDE));
    }

    public void testRooksOnSameFile() {
        final Board board = fromFen("2r4R/5pk1/q5p1/p3p1P1/1p2P3/1P6/KPP5/7R w - - 0 1");
        assertEquals("R1h7#", toShort(board, fromSimple("h1h7")));
    }

    public void testLong() {
        final Board board = fromFen("8/8/8/8/8/5N2/1pr3PP/r1k1K2R w K - 0 1");
        final int castlingKingside = fromSimple("e1g1") | MT_CASTLING_KINGSIDE;
        assertEquals("e1g1", toLong(castlingKingside));
        assertEquals(castlingKingside, fromLong(board, toLong(castlingKingside)));
    }

    public void testLongPawnMovesTwo() {
        final Board board = new Board();
        board.restart();
        assertEquals(fromSimple("d2d4"), fromLong(board, "d2d4"));
    }

    public void testFenRoundTrip() {
        final String fen = "2rqkb1r/3n1p1p/p3p1pn/1p1pP1N1/5P2/2N5/PPP3PP/R1BQ1R1K w kq - 0 1";
        final Board board = fromFen(fen);
        assertEquals(fen, toFen(board));
    }
}