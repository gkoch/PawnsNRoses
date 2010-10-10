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
        final int move = fromSimple("c7e6");
        assertEquals("Ne6+", toShort(board, move));
        assertEquals(move, fromShort(board, toShort(board, move)));
    }

    public void testShortMate() {
        final Board board = fromFen("1b6/2p1p3/1q6/1P6/3n4/3B2B1/5N2/K1k5 w - - 0 1");
        final int move = fromSimple("g3f4");
        assertEquals("Bf4#", toShort(board, move));
        assertEquals(move, fromShort(board, toShort(board, move)));
    }

    public void testShortMultipleRooks() {
        final Board board = fromFen("5r1k/2p3pp/3p4/3Q4/3P2R1/1N6/2B3PP/4rRK1 b - - 0 1");
        int move = fromSimple("f8f1");
        assertEquals("Rfxf1#", toShort(board, move));
        assertEquals(move, fromShort(board, toShort(board, move)));
        move = fromSimple("e1f1");
        assertEquals("Rexf1#", toShort(board, move));
        assertEquals(move, fromShort(board, toShort(board, move)));
    }

    public void testShortMateFromCastling() {
        final Board board = fromFen("8/8/8/8/8/5N2/1pr3PP/r1k1K2R w K - 0 1");
        final int move = fromSimple("e1g1") | MT_CASTLING_KINGSIDE;
        assertEquals("O-O#", toShort(board, move));
        assertEquals(move, fromShort(board, toShort(board, move)));
    }

    public void testShortRooksOnSameFile() {
        final Board board = fromFen("2r4R/5pk1/q5p1/p3p1P1/1p2P3/1P6/KPP5/7R w - - 0 1");
        final int move = fromSimple("h1h7");
        assertEquals("R1h7#", toShort(board, move));
        assertEquals(move, fromShort(board, toShort(board, move)));
    }

    public void testShortPromotionTwoPawnsCheck() {
        final Board board = fromFen("1q2k3/PRP2p2/3Qb3/7p/4P3/8/8/R1K5 w - - 8 1");
        final int move = fromSimple("a7b8") | MT_PROMOTION_QUEEN;
        assertEquals("axb8=Q+", toShort(board, move));
        assertEquals(move, fromShort(board, toShort(board, move)));
    }

    public void testShortPawnOpening() {
        final Board board = new Board();
        board.restart();
        int move = fromSimple("e2e4");
        assertEquals("e4", toShort(board, move));
        assertEquals(move, fromShort(board, toShort(board, move)));
        board.move(move);
        move = fromSimple("e7e5");
        assertEquals("e5", toShort(board, move));
        assertEquals(move, fromShort(board, toShort(board, move)));
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

    public void testFromPgn() {
        final Board board = StringUtils.fromPgn("[Event \"112.New York (F.V)\"]\n" +
            "[Site \"New York, NY USA\"]\n" +
            "[Date \"1924.??.??\"]\n" +
            "[Round \"87\"]\n" +
            "[White \"Alekhine, Alexander A\"]\n" +
            "[Black \"Freeman\"]\n" +
            "[Result \"1-0\"]\n" +
            "[ECO \"C21\"]\n" +
            "[EventDate \"1924.??.??\"]\n" +
            "[Annotator \"Alekhine\"]\n" +
            "\n" +
            "1.e4 e5 2.d4 exd4 3.c3 d5 4.exd5 Qxd5 5.cxd4 Bb4+ 6.Nc3 Nc6 7.Nf3 Nf6 8.\n" +
            "Be2 O-O 9.O-O Bxc3 10.bxc3 b6 11.c4 Qd8 12.d5 Ne7 13.Nd4 Bb7 14.Bb2 c6 15.\n" +
            "Bf3 cxd5 16.Re1 Re8 17.Qc1 Rb8 18.Qg5 Ng6 19.Nf5 Rxe1+ 20.Rxe1 dxc4 21.\n" +
            "Bxb7 Rxb7 22.Bxf6 Qxf6 23.Re8+ Nf8 24.Nh6+ Qxh6 25.Rxf8+ Kxf8 26.Qd8# 1-0");
    }
}