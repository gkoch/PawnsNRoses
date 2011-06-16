package sf.pnr.tools;

import static sf.pnr.base.Utils.*;

/**
 */
public class BitBoardBuilder {

    private static final String[] LONG_LEADING_ZEROS = new String[17];

    static {
        final StringBuilder builder = new StringBuilder(16);
        for (int i = 16; i >= 0; i--) {
            LONG_LEADING_ZEROS[i] = builder.toString();
            builder.append('0');
        }
    }

    public static void main(final String[] args) {
//        generateInitialBoard();
//        generatePawnAttack();
//        generatePawnShields();
//        generateFileBitmaps();
        //generateKnightMoves();
        generatePawnMovesAndAttacks();
    }

    private static void generateFileBitmaps() {
        long bitmap = 1L;
        for (int i = 0; i < 7; i++) {
            bitmap <<= 8;
            bitmap |= 1;
        }
        final long[] files = new long[8];
        files[0] = bitmap;
        for (int i = 1; i < 8; i++) {
            bitmap <<= 1;
            files[i] = bitmap;
        }
        print("BITBOARD_FILE", files);
    }

    private static void generateInitialBoard() {
        print("BITBOARD_ROOK_WHITE", 0x0000000000000001L, 0x0000000000000080L);
        print("BITBOARD_KNIGHT_WHITE", 0x0000000000000002L, 0x0000000000000040L);
        print("BITBOARD_BISHOP_WHITE", 0x0000000000000004L, 0x0000000000000020L);
        print("BITBOARD_QUEEN_WHITE", 0x0000000000000008L);
        print("BITBOARD_KING_WHITE", 0x0000000000000010L);
        print("BITBOARD_PAWN_WHITE", 0x0000000000000100L, 0x0000000000000200L, 0x0000000000000400L, 0x0000000000000800L,
            0x0000000000001000L, 0x0000000000002000L, 0x0000000000004000L, 0x0000000000008000L);
    }

    private static void generatePawnAttack() {
        final long[] whitePawnAttacks = new long[64];
        for (int i = 0; i < 120; i++) {
            if ((i & 0x88) > 0) {
                continue;
            }
            long attack = 0;
            if (((i + UL) & 0x88) == 0) {
                final int idx = convert0x88To64(i + UL);
                attack |= 1L << idx;
            }
            if (((i + UR) & 0x88) == 0) {
                final int idx = convert0x88To64(i + UR);
                attack |= 1L << idx;
            }
            whitePawnAttacks[convert0x88To64(i)] = attack;
        }
        final long[] blackPawnAttacks = new long[64];
        for (int i = 0; i < 120; i++) {
            if ((i & 0x88) > 0) {
                continue;
            }
            long attack = 0;
            if (((i + DL) & 0x88) == 0) {
                final int idx = convert0x88To64(i + DL);
                attack |= 1L << idx;
            }
            if (((i + DR) & 0x88) == 0) {
                final int idx = convert0x88To64(i + DR);
                attack |= 1L << idx;
            }
            blackPawnAttacks[convert0x88To64(i)] = attack;
        }
        print("BITBOARD_BLACK_PAWN_ATTACKS", blackPawnAttacks);
        print("BITBOARD_WHITE_PAWN_ATTACKS", whitePawnAttacks);
    }

    private static void generatePawnShields() {
        generatePositionMask("PAWN_SHIELD_KING_SIDE_BLACK", F[6], G[6], H[6]);
        generatePositionMask("PAWN_SHIELD_KING_SIDE_BLACK", F[6], G[5], H[6]);
        generatePositionMask("PAWN_SHIELD_KING_SIDE_BLACK", F[6], G[6], H[5]);
        generatePositionMask("PAWN_SHIELD_KING_SIDE_WHITE", F[1], G[1], H[1]);
        generatePositionMask("PAWN_SHIELD_KING_SIDE_WHITE", F[1], G[2], H[1]);
        generatePositionMask("PAWN_SHIELD_KING_SIDE_WHITE", F[1], G[1], H[2]);
        generatePositionMask("PAWN_SHIELD_QUEEN_SIDE_BLACK", A[6], B[6], C[6]);
        generatePositionMask("PAWN_SHIELD_QUEEN_SIDE_BLACK", A[6], B[5], C[6]);
        generatePositionMask("PAWN_SHIELD_QUEEN_SIDE_BLACK", A[5], B[6], C[6]);
        generatePositionMask("PAWN_SHIELD_QUEEN_SIDE_WHITE", A[1], B[1], C[1]);
        generatePositionMask("PAWN_SHIELD_QUEEN_SIDE_WHITE", A[1], B[2], C[1]);
        generatePositionMask("PAWN_SHIELD_QUEEN_SIDE_WHITE", A[2], B[1], C[1]);

        generatePositionMask("PAWN_SHIELD_KING_SIDE_BLACK_KING", F[7], G[7], H[7]);
        generatePositionMask("PAWN_SHIELD_KING_SIDE_WHITE_KING", F[0], G[0], H[0]);
        generatePositionMask("PAWN_SHIELD_QUEEN_SIDE_BLACK_KING", A[7], B[7], C[7]);
        generatePositionMask("PAWN_SHIELD_QUEEN_SIDE_WHITE_KING", A[0], B[0], C[0]);
    }

    private static void generatePositionMask(final String name, final int... squares) {
        long mask = 0L;
        for (int square: squares) {
            int square64 = convert0x88To64(square);
            mask |= 1L << square64;
        }
        print(name, mask);
    }

    private static void generateKnightMoves() {
        final long[] moveBitBoards = new long[64];
        for (int i = 0; i < 120; i++) {
            if ((i & 0x88) == 0) {
                final int i64 = convert0x88To64(i);
                long bitboard = 0L;
                for (int delta : DELTA_KNIGHT) {
                    final int pos = i + delta;
                    if ((pos & 0x88) == 0) {
                        final int pos64 = convert0x88To64(pos);
                        bitboard |= 1L << pos64;
                    }
                }
                moveBitBoards[i64] = bitboard;
            }
        }
        print("KNIGHT_MOVES", moveBitBoards);
    }

    private static void generatePawnMovesAndAttacks() {
        generatePawnMovesAndAttacks(16, "WHITE");
        generatePawnMovesAndAttacks(-16, "BLACK");
    }

    private static void generatePawnMovesAndAttacks(final int moveDelta, final String sideStr) {
        final long[] moveBitBoards = new long[64];
        final long[] attackBitBoards = new long[64];
        for (int pos = 0; pos < 120; pos++) {
            final int rank = getRank(pos);
            if (rank > 0 && rank < 7 && (pos & 0x88) == 0) {
                final int to = pos + moveDelta;
                long moveBitBoard = 0L;
                long attackBitBoard = 0L;
                if ((to & 0x88) == 0) {
                    moveBitBoard |= 1L << convert0x88To64(to);
                    final int attackQueenSide = to - 1;
                    if ((attackQueenSide & 0x88) == 0) {
                        attackBitBoard |= 1L << convert0x88To64(attackQueenSide);
                    }
                    final int attackKingSide = to + 1;
                    if ((attackKingSide & 0x88) == 0) {
                        attackBitBoard |= 1L << convert0x88To64(attackKingSide);
                    }
                    if (rank == 1 || rank == 6) {
                        final int to2 = to + moveDelta;
                        if ((to2 & 0x88) == 0) {
                            moveBitBoard |= 1L << convert0x88To64(to2);
                        }
                    }
                }
                final int pos64 = convert0x88To64(pos);
                moveBitBoards[pos64] = moveBitBoard;
                attackBitBoards[pos64] = attackBitBoard;
            }
        }
        print("PAWN_MOVES_" + sideStr, moveBitBoards);
        //print("PAWN_ATTACKS_" + sideStr, attackBitBoards);
    }

    public static void print(final String name, final long... bitboards) {
        final int len = bitboards.length;
        final StringBuilder builder = new StringBuilder(len * 10);
        builder.append("    public static final long");
        if (len > 1) {
            builder.append("[]");
        }
        builder.append(' ');
        builder.append(name);
        builder.append(" = ");
        if (len > 1) {
            builder.append("new long[]{");
        }
        if (len > 4) {
            builder.append("\r\n\t\t");
        }
        for (int i = 0; i < len; i++) {
            builder.append(toHexString(bitboards[i]));
            if (i != len - 1) {
                builder.append(",");
                if (i % 4 == 3) {
                    builder.append("\r\n\t\t");
                } else {
                    builder.append(" ");
                }
            }
        }
        if (len > 1) {
            builder.append('}');
        }
        builder.append(';');
        System.out.println(builder.toString());
    }

    public static String toHexString(final long bitboard) {
        final StringBuilder builder = new StringBuilder(20);
        builder.append("0x");
        final String hex = Long.toHexString(bitboard).toUpperCase();
        builder.append(LONG_LEADING_ZEROS[hex.length()]);
        builder.append(hex);
        builder.append("L");
        return builder.toString();
    }
}
