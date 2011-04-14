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

    public void testShortPromotionCheck() {
        final Board board = fromFen("8/2b5/5p2/1k6/8/p4K2/7p/8 b - - 1 72");
        final int move = fromSimple("h2h1") | MT_PROMOTION_QUEEN;
        assertEquals("h1=Q+", toShort(board, move));
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
        assertEquals("3Q1k2/pr3ppp/1p5q/8/2p5/8/P4PPP/6K1 b - - 1 26", StringUtils.toFen(board));
    }

    public void testFromPgn2() {
        final Board board = StringUtils.fromPgn("[Date \"2010.10.11\"]\n" +
            "[Round \"2\"]\n" +
            "[Game \"5\"]\n" +
            "[White \"Pawns N' Roses Latest\"]\n" +
            "[Black \"Pawns N' Roses v0.054\"]\n" +
            "[Result \"1/2-1/2\"]\n" +
            "[Time \"09:20:51\"]\n" +
            "[PlyCount \"185\"]\n" +
            "[Termination \"normal\"]\n" +
            "[RemainingTimeWhite \"00:01.897\"]\n" +
            "[RemainingTimeBlack \"00:01.915\"]\n" +
            "\n" +
            "1. Nf3 Nf6 2. Nc3 d5 3. d3 d4 4. Ne4 e6 5. e3 Nxe4 6. dxe4 Nc6 7. Bb5 Bb4+ 8. Bd2 Bc5 \n" +
            "9. Bxc6+ bxc6 10. exd4 Bxd4 11. Bg5 Bxf2+ 12. Kxf2 Qxd1 13. Rhxd1 O-O 14. Rd8 Rxd8 15. Bxd8 Rb8 16. b3 Rb7 \n" +
            "17. Ke3 f5 18. e5 c5 19. Kd2 c4 20. bxc4 a6 21. h3 h6 22. Rg1 Bd7 23. Nd4 Rb4 24. Kc3 c5 \n" +
            "25. Nb3 Rb8 26. Be7 Rc8 27. Nxc5 Re8 28. Bd6 Bc8 29. Nxa6 Rd8 30. Nc5 Kf7 31. Kd2 g6 32. Rf1 Bd7 \n" +
            "33. Rf2 Bc6 34. a3 Re8 35. Nd3 Rd8 36. Re2 Ra8 37. Kc3 Ra4 38. Nb2 Ra7 39. Nd3 Ra4 40. Nb2 Ra8 \n" +
            "41. Rf2 g5 42. Kd2 f4 43. Nd3 Rd8 44. c5 Rd7 45. g3 Kg8 46. Nxf4 Rf7 47. g4 Be4 48. Nd3 Rxf2+ \n" +
            "49. Nxf2 Bg2 50. Kc1 Kf7 51. Kb1 Bc6 52. Kc1 Bg2 53. Kb1 Bc6 54. Kc1 Kg8 55. Ne4 Bxe4 56. a4 Kf7 \n" +
            "57. a5 Ke8 58. a6 Bc6 59. a7 Kd7 60. Bf8 Kc8 61. Bxh6 Bb7 62. Bxg5 Kd7 63. h4 Ke8 64. Kb1 Bc6 \n" +
            "65. h5 Kf7 66. h6 Kg6 67. Be3 Kh7 68. g5 Be4 69. Bd2 Bg2 70. Bc3 Ba8 71. Kb2 Bf3 72. Bd4 Ba8 \n" +
            "73. Kb1 Kg6 74. Bc3 Be4 75. Bd2 Ba8 76. Be3 Bd5 77. Kb2 Kh7 78. Bd4 Bh1 79. Kb3 Bg2 80. Bc3 Bd5+ \n" +
            "81. Kb2 Bf3 82. Bd4 Ba8 83. Bc3 Kg6 84. Kb1 Be4 85. Bd2 Ba8 86. Be3 Be4 87. Kb2 Ba8 88. Bf2 Bc6 \n" +
            "89. Kb1 Bd5 90. Be3 Be4 91. Kb2 Kh7 92. Bd4 Kg6 93. Be3");
        assertEquals("8/P7/4p1kP/2P1P1P1/4b3/4B3/1KP5/8 b - - 50 93", StringUtils.toFen(board));
    }
}