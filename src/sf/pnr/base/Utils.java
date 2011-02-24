package sf.pnr.base;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Utils {
	public static final int FULL_INT = 0xFFFFFFFF;
    public static final int MAX_SEARCH_DEPTH = 150;

    // pieces
	public static final int EMPTY = 0;
	public static final int KING = 1;
	public static final int QUEEN = 2;
	public static final int ROOK = 3;
	public static final int BISHOP = 4;
	public static final int KNIGHT = 5;
	public static final int PAWN = 6;
    public static final int[] TYPES = new int[] {KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN};
    public static final int[] TYPES_NOPAWN = new int[] {KING, QUEEN, ROOK, BISHOP, KNIGHT};

	// state
	public static final int BLACK_TO_MOVE = 0x00000000;
    public static final int BLACK = BLACK_TO_MOVE;
    public static final int WHITE_TO_MOVE = 0x00000001;
	public static final int WHITE = WHITE_TO_MOVE;
	public static final int CASTLING_WHITE_KINGSIDE =  0x00000002;
	public static final int CASTLING_WHITE_QUEENSIDE = 0x00000004;
	public static final int CASTLING_BLACK_KINGSIDE =  0x00000008;
	public static final int CASTLING_BLACK_QUEENSIDE = 0x00000010;
	public static final int CASTLING_WHITE = CASTLING_WHITE_KINGSIDE | CASTLING_WHITE_QUEENSIDE;
	public static final int CASTLING_BLACK = CASTLING_BLACK_KINGSIDE | CASTLING_BLACK_QUEENSIDE;
	public static final int CASTLING_ALL = CASTLING_WHITE | CASTLING_BLACK;
	public static final int EN_PASSANT = 0x000001E0;
	public static final int HALF_MOVES = 0x000FFE00;
	public static final int FULL_MOVES = 0xFFF00000;
	public static final int SHIFT_CASTLING = 1;
	public static final int SHIFT_EN_PASSANT = 5;
	public static final int SHIFT_HALF_MOVES = 9;
	public static final int SHIFT_FULL_MOVES = 20;
	public static final int CLEAR_CASTLING_WHITE_KINGSIDE = ~CASTLING_WHITE_KINGSIDE;
	public static final int CLEAR_CASTLING_WHITE_QUEENSIDE = ~CASTLING_WHITE_QUEENSIDE;
	public static final int CLEAR_CASTLING_BLACK_KINGSIDE = ~CASTLING_BLACK_KINGSIDE;
	public static final int CLEAR_CASTLING_BLACK_QUEENSIDE = ~CASTLING_BLACK_QUEENSIDE;
	public static final int[] CLEAR_CASTLING = new int[128];
	public static final int CLEAR_EN_PASSANT = ~EN_PASSANT;
	public static final int CLEAR_HALF_MOVES = ~HALF_MOVES;
	public static final int CLEAR_FULL_MOVES = ~FULL_MOVES;
	public static final int UNIT_HALF_MOVES = 0x00000200;
	public static final int UNIT_FULL_MOVES = 0x00100000;

    // state2
    public static final int CASTLED_BLACK = 0x00000001;
    public static final int CASTLED_WHITE = 0x00000002;

	public static final int FILE = 0x07;
	public static final int RANK = 0x70;
	public static final int SHIFT_RANK = 4;
    public static final int[] FILE_RANK_BITS = new int[]{0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80};

	// move
	public static final int FROM       = 0x0000007F;
	public static final int TO         = 0x00003F80;
	public static final int MOVE_TYPE  = 0x0001C000;
    //public static final int CHECKING   = 0x00020000;
    public static final int CAPTURED   = 0x000E0000;
    public static final int MOVE_VALUE = 0x7FFC0000;
    public static final int FROM_TO  = FROM | TO;
    public static final int BASE_INFO  = FROM_TO | MOVE_TYPE;
    public static final int SHIFT_TO = 7;
	public static final int SHIFT_MOVE_TYPE = 14;
    public static final int SHIFT_CHECKING = 17;
    public static final int SHIFT_CAPTURED = 17;
	public static final int SHIFT_MOVE_VALUE = 18;
	public static final int MT_NORMAL = 0 << SHIFT_MOVE_TYPE;
	public static final int MT_CASTLING_KINGSIDE = 1 << SHIFT_MOVE_TYPE;
	public static final int MT_CASTLING_QUEENSIDE = 2 << SHIFT_MOVE_TYPE;
	public static final int MT_CASTLING = MT_CASTLING_KINGSIDE | MT_CASTLING_QUEENSIDE;
	public static final int MT_EN_PASSANT = 3 << SHIFT_MOVE_TYPE;
	public static final int MT_PROMOTION_QUEEN = 4 << SHIFT_MOVE_TYPE;
	public static final int MT_PROMOTION_ROOK = 5 << SHIFT_MOVE_TYPE;
	public static final int MT_PROMOTION_BISHOP = 6 << SHIFT_MOVE_TYPE;
	public static final int MT_PROMOTION_KNIGHT = 7 << SHIFT_MOVE_TYPE;
	public static final int MT_PROMOTION = 4 << SHIFT_MOVE_TYPE;
    public static final int PLY = 16;
    public static final int SHIFT_PLY = 4;
    public static final int STAGE_MAX = 63;
    public static final int[] PROMOTION_TO_PIECE = new int[] {EMPTY, EMPTY, EMPTY, EMPTY, QUEEN, ROOK, BISHOP, KNIGHT}; 
    public static final int[] CASTLING_TO_ROOK_FROM_DELTA = new int[] {0, 1, -2};
    public static final int[] CASTLING_TO_ROOK_TO_DELTA = new int[] {0, -1, 1}; 

	// deltas
	public static final int[] DELTA_KING = {-0x0F, -0x11, 0x0F, 0x11, -0x01, -0x10, 0x01, 0x10};
	public static final int[] DELTA_QUEEN = {-0x0F, -0x11, 0x0F, 0x11, -0x01, -0x10, 0x01, 0x10};
	public static final int[] DELTA_ROOK = {-0x01, -0x10, 0x01, 0x10};
	public static final int[] DELTA_BISHOP = {-0x0F, -0x11, 0x0F, 0x11};
	public static final int[] DELTA_KNIGHT = {-0x21, -0x12, 0x0E, 0x1F, 0x21, 0x12, -0x0E, -0x1F};
	public static final int[][] DELTA_PAWN_ATTACK = {{-0x11, -0x0F}, {0x11, 0x0F}};
	public static final int[][] DELTA = new int[7][8];

    // attack
    public static final int ATTACK_K = 1;
    public static final int ATTACK_R = 2;
    public static final int ATTACK_B = 4;
    public static final int ATTACK_N = 8;
    public static final int ATTACK_Q = ATTACK_R | ATTACK_B;
    public static final int SHIFT_ATTACK_DELTA = 4;
    public static final int ATTACK_DELTA = 0xFF << SHIFT_ATTACK_DELTA;
    public static final int SHIFT_ATTACK_DISTANCE_KING = 12;
    public static final int ATTACK_DISTANCE_KING = 0x07 << SHIFT_ATTACK_DISTANCE_KING;
    public static final int SHIFT_ATTACK_DISTANCE_KNIGHT = 15;
    public static final int ATTACK_DISTANCE_KNIGHT = 0x07 << SHIFT_ATTACK_DISTANCE_KNIGHT;
    public static final int SHIFT_ATTACK_DISTANCE_BISHOP = 18;
    public static final int ATTACK_DISTANCE_BISHOP = 0x07 << SHIFT_ATTACK_DISTANCE_BISHOP;
    public static final int SHIFT_ATTACK_DISTANCE_ROOK = 21;
    public static final int ATTACK_DISTANCE_ROOK = 0x07 << SHIFT_ATTACK_DISTANCE_ROOK;
    public static final int SHIFT_ATTACK_DISTANCE_QUEEN = 24;
    public static final int ATTACK_DISTANCE_QUEEN = 0x07 << SHIFT_ATTACK_DISTANCE_QUEEN;
    public static final int[] ATTACK_DISTANCE_MASKS = new int[]{EMPTY, ATTACK_DISTANCE_KING, ATTACK_DISTANCE_QUEEN,
        ATTACK_DISTANCE_ROOK, ATTACK_DISTANCE_BISHOP, ATTACK_DISTANCE_KNIGHT};
    public static final int[] SHIFT_ATTACK_DISTANCES = new int[]{EMPTY, SHIFT_ATTACK_DISTANCE_KING,
        SHIFT_ATTACK_DISTANCE_QUEEN, SHIFT_ATTACK_DISTANCE_ROOK, SHIFT_ATTACK_DISTANCE_BISHOP, SHIFT_ATTACK_DISTANCE_KNIGHT};
    public static final int[] ATTACK_BITS;

    public static final int[] A = new int[8];
	public static final int[] B = new int[8];
	public static final int[] C = new int[8];
	public static final int[] D = new int[8];
	public static final int[] E = new int[8];
	public static final int[] F = new int[8];
	public static final int[] G = new int[8];
	public static final int[] H = new int[8];
	public static final int FILE_A = 0;
	public static final int FILE_B = 1;
	public static final int FILE_C = 2;
	public static final int FILE_D = 3;
	public static final int FILE_E = 4;
	public static final int FILE_F = 5;
	public static final int FILE_G = 6;
	public static final int FILE_H = 7;
    public static final int DL = -0x11;
    public static final int UL = 0x0F;
    public static final int DR = -0x0F;
    public static final int UR = 0x11;
    public static final int UP = 0x10;
    public static final int DN = -0x10;

    public static final int[] INITIAL_BOARD = new int[]
	   {   ROOK,  KNIGHT,  BISHOP,   QUEEN,    KING,  BISHOP,  KNIGHT,    ROOK,
               EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
		   PAWN,    PAWN,    PAWN,    PAWN,    PAWN,    PAWN,    PAWN,    PAWN,
               EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
		  EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
               EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
		  EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
               EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
		  EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
               EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
		  EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
               EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
		  -PAWN,   -PAWN,   -PAWN,   -PAWN,   -PAWN,   -PAWN,   -PAWN,   -PAWN,
               EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,
		  -ROOK, -KNIGHT, -BISHOP,  -QUEEN,   -KING, -BISHOP, -KNIGHT,   -ROOK,
               EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY,   EMPTY};

	public static final int INITIAL_STATE = WHITE_TO_MOVE |
		CASTLING_WHITE_KINGSIDE | CASTLING_WHITE_QUEENSIDE | CASTLING_BLACK_KINGSIDE | CASTLING_BLACK_QUEENSIDE |
		(1 << SHIFT_FULL_MOVES);
	public static final int[] INITIAL_PIECE_ARRAY_POS = new int[]
   	   {1, 1, 1, 1, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0,
   		1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0, 0, 0, 0, 0, 0,
   		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
   		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
   		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
   		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
   		1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0, 0, 0, 0, 0, 0,
   		1, 1, 1, 1, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0};

    public static final int[] ATTACK_ARRAY = new int[]{
        0x00000400, 0x014772F4, 0x025EF400, 0x024A7400, 0x025EF400, 0x024A7400, 0x025EF400, 0x024A7400,
        0x013EF302, 0x024A7400, 0x025EF400, 0x024A7400, 0x025EF400, 0x024A7400, 0x025EF400, 0x01477314,
        0x00000400, 0x025EF400, 0x014662F4, 0x025EE400, 0x024A6400, 0x025EE400, 0x024A6400, 0x025EE400,
        0x012A6302, 0x025EE400, 0x024A6400, 0x025EE400, 0x024A6400, 0x025EE400, 0x01466314, 0x025EF400,
        0x00000400, 0x024A7400, 0x025EE400, 0x014652F4, 0x025DD400, 0x024A5400, 0x025DD400, 0x024A5400,
        0x013DD302, 0x024A5400, 0x025DD400, 0x024A5400, 0x025DD400, 0x01465314, 0x025EE400, 0x024A7400,
        0x00000400, 0x025EF400, 0x024A6400, 0x025DD400, 0x014642F4, 0x025DC400, 0x024A4400, 0x025DC400,
        0x012A4302, 0x025DC400, 0x024A4400, 0x025DC400, 0x01464314, 0x025DD400, 0x024A6400, 0x025EF400,
        0x00000400, 0x024A7400, 0x025EE400, 0x024A5400, 0x025DC400, 0x014532F4, 0x025DB400, 0x02493400,
        0x013EB302, 0x02493400, 0x025DB400, 0x01453314, 0x025DC400, 0x024A5400, 0x025EE400, 0x024A7400,
        0x00000400, 0x025EF400, 0x024A6400, 0x025DD400, 0x024A4400, 0x025DB400, 0x014622F4, 0x025CA1F8,
        0x01292302, 0x025CA218, 0x01462314, 0x025DB400, 0x024A4400, 0x025DD400, 0x024A6400, 0x025EF400,
        0x00000400, 0x024A7400, 0x025EE400, 0x024A5400, 0x025DC400, 0x02493400, 0x025CA2E8, 0x014612F5,
        0x013D9303, 0x01461315, 0x025CA328, 0x02493400, 0x025DC400, 0x024A5400, 0x025EE400, 0x024A7400,
        0x00000400, 0x013EF3F2, 0x012A63F2, 0x013DD3F2, 0x012A43F2, 0x013EB3F2, 0x012923F2, 0x013D93F3,
        0x00000400, 0x013D9413, 0x01292412, 0x013EB412, 0x012A4412, 0x013DD412, 0x012A6412, 0x013EF412,
        0x00000400, 0x024A7400, 0x025EE400, 0x024A5400, 0x025DC400, 0x02493400, 0x025CA4E8, 0x014614F5,
        0x013D9503, 0x01461515, 0x025CA528, 0x02493400, 0x025DC400, 0x024A5400, 0x025EE400, 0x024A7400,
        0x00000400, 0x025EF400, 0x024A6400, 0x025DD400, 0x024A4400, 0x025DB400, 0x014624F4, 0x025CA5F8,
        0x01292502, 0x025CA618, 0x01462514, 0x025DB400, 0x024A4400, 0x025DD400, 0x024A6400, 0x025EF400,
        0x00000400, 0x024A7400, 0x025EE400, 0x024A5400, 0x025DC400, 0x014534F4, 0x025DB400, 0x02493400,
        0x013EB502, 0x02493400, 0x025DB400, 0x01453514, 0x025DC400, 0x024A5400, 0x025EE400, 0x024A7400,
        0x00000400, 0x025EF400, 0x024A6400, 0x025DD400, 0x014644F4, 0x025DC400, 0x024A4400, 0x025DC400,
        0x012A4502, 0x025DC400, 0x024A4400, 0x025DC400, 0x01464514, 0x025DD400, 0x024A6400, 0x025EF400,
        0x00000400, 0x024A7400, 0x025EE400, 0x014654F4, 0x025DD400, 0x024A5400, 0x025DD400, 0x024A5400,
        0x013DD502, 0x024A5400, 0x025DD400, 0x024A5400, 0x025DD400, 0x01465514, 0x025EE400, 0x024A7400,
        0x00000400, 0x025EF400, 0x014664F4, 0x025EE400, 0x024A6400, 0x025EE400, 0x024A6400, 0x025EE400,
        0x012A6502, 0x025EE400, 0x024A6400, 0x025EE400, 0x024A6400, 0x025EE400, 0x01466514, 0x025EF400,
        0x00000400, 0x014774F4, 0x025EF400, 0x024A7400, 0x025EF400, 0x024A7400, 0x025EF400, 0x024A7400,
        0x013EF502, 0x024A7400, 0x025EF400, 0x024A7400, 0x025EF400, 0x024A7400, 0x025EF400, 0x01477514};

    public static final long[][][] ZOBRIST_PIECES = new long[7][2][64];
    public static final long[] ZOBRIST_CASTLING = new long[16];
    public static final long[] ZOBRIST_EN_PASSANT = new long[9];
    public static final long ZOBRIST_WHITE_TO_MOVE;
    public static final long[] ZOBRIST_TO_MOVE = new long[2];

    public static final boolean[] SLIDING = new boolean[7];

    static {
        ATTACK_BITS = new int[6];
        ATTACK_BITS[KING] = ATTACK_K; ATTACK_BITS[QUEEN] = ATTACK_Q; ATTACK_BITS[ROOK] = ATTACK_R;
        ATTACK_BITS[BISHOP] = ATTACK_B; ATTACK_BITS[KNIGHT] = ATTACK_N;

        DELTA[KNIGHT] = DELTA_KNIGHT;
        DELTA[BISHOP] = DELTA_BISHOP;
        DELTA[ROOK] = DELTA_ROOK;
        DELTA[QUEEN] = DELTA_QUEEN;
        DELTA[KING] = DELTA_KING;

		for (int i = 0; i < 8; i++) {
			A[i] = i << 4;
			B[i] = A[i] + 1;
			C[i] = A[i] + 2;
			D[i] = A[i] + 3;
			E[i] = A[i] + 4;
			F[i] = A[i] + 5;
			G[i] = A[i] + 6;
			H[i] = A[i] + 7;
		}
		Arrays.fill(CLEAR_CASTLING, FULL_INT);
		CLEAR_CASTLING[A[0]] = CLEAR_CASTLING_WHITE_QUEENSIDE;
		CLEAR_CASTLING[E[0]] = CLEAR_CASTLING_WHITE_QUEENSIDE & CLEAR_CASTLING_WHITE_KINGSIDE;
		CLEAR_CASTLING[H[0]] = CLEAR_CASTLING_WHITE_KINGSIDE;
		CLEAR_CASTLING[A[7]] = CLEAR_CASTLING_BLACK_QUEENSIDE;
		CLEAR_CASTLING[E[7]] = CLEAR_CASTLING_BLACK_QUEENSIDE & CLEAR_CASTLING_BLACK_KINGSIDE;
		CLEAR_CASTLING[H[7]] = CLEAR_CASTLING_BLACK_KINGSIDE;

        System.arraycopy(Polyglot.ZOBRIST, 0 * 64, ZOBRIST_PIECES[PAWN][BLACK_TO_MOVE], 0, 64);
        System.arraycopy(Polyglot.ZOBRIST, 1 * 64, ZOBRIST_PIECES[PAWN][WHITE_TO_MOVE], 0, 64);
        System.arraycopy(Polyglot.ZOBRIST, 2 * 64, ZOBRIST_PIECES[KNIGHT][BLACK_TO_MOVE], 0, 64);
        System.arraycopy(Polyglot.ZOBRIST, 3 * 64, ZOBRIST_PIECES[KNIGHT][WHITE_TO_MOVE], 0, 64);
        System.arraycopy(Polyglot.ZOBRIST, 4 * 64, ZOBRIST_PIECES[BISHOP][BLACK_TO_MOVE], 0, 64);
        System.arraycopy(Polyglot.ZOBRIST, 5 * 64, ZOBRIST_PIECES[BISHOP][WHITE_TO_MOVE], 0, 64);
        System.arraycopy(Polyglot.ZOBRIST, 6 * 64, ZOBRIST_PIECES[ROOK][BLACK_TO_MOVE], 0, 64);
        System.arraycopy(Polyglot.ZOBRIST, 7 * 64, ZOBRIST_PIECES[ROOK][WHITE_TO_MOVE], 0, 64);
        System.arraycopy(Polyglot.ZOBRIST, 8 * 64, ZOBRIST_PIECES[QUEEN][BLACK_TO_MOVE], 0, 64);
        System.arraycopy(Polyglot.ZOBRIST, 9 * 64, ZOBRIST_PIECES[QUEEN][WHITE_TO_MOVE], 0, 64);
        System.arraycopy(Polyglot.ZOBRIST, 10 * 64, ZOBRIST_PIECES[KING][BLACK_TO_MOVE], 0, 64);
        System.arraycopy(Polyglot.ZOBRIST, 11 * 64, ZOBRIST_PIECES[KING][WHITE_TO_MOVE], 0, 64);

        for (int i = 0; i < 16; i++) {
            long zobrist = 0;
            final int castling = i << SHIFT_CASTLING;
            if ((castling & CASTLING_WHITE_KINGSIDE) > 0) {
                zobrist ^= Polyglot.ZOBRIST[768];
            }
            if ((castling & CASTLING_WHITE_QUEENSIDE) > 0) {
                zobrist ^= Polyglot.ZOBRIST[769];
            }
            if ((castling & CASTLING_BLACK_KINGSIDE) > 0) {
                zobrist ^= Polyglot.ZOBRIST[770];
            }
            if ((castling & CASTLING_BLACK_QUEENSIDE) > 0) {
                zobrist ^= Polyglot.ZOBRIST[771];
            }
            ZOBRIST_CASTLING[i] = zobrist;
        }

        System.arraycopy(Polyglot.ZOBRIST, 772, ZOBRIST_EN_PASSANT, 1, 8);
        ZOBRIST_WHITE_TO_MOVE = Polyglot.ZOBRIST[780];
        ZOBRIST_TO_MOVE[BLACK_TO_MOVE] = 0;
        ZOBRIST_TO_MOVE[WHITE_TO_MOVE] = ZOBRIST_WHITE_TO_MOVE;

        Arrays.fill(SLIDING, true);
        SLIDING[PAWN] = false;
        SLIDING[KNIGHT] = false;
        SLIDING[KING] = false;
    }
	
	private Utils() {
		// hide
	}

    public static int getFromPosition(final int move) {
		return move & FROM;
	}

	public static int getToPosition(final int move) {
		return (move & TO) >> SHIFT_TO;
	}
	
	public static int getFile(final int position) {
		return position & 0x0F;
	}

	public static int getRank(final int position) {
		return position >> SHIFT_RANK;
	}

    public static int getPosition(final int file, final int rank) {
        return (rank << 4) + file;
    }

    public static boolean isSliding(final int piece) {
		return piece == QUEEN || piece == ROOK || piece == BISHOP;
	}

    public static long computeZobristIncremental(final Board board) {
        final int state = board.getState();
        long zobrist = ZOBRIST_TO_MOVE[state & WHITE_TO_MOVE];
        zobrist ^= computeZobrist(board, KING);
        zobrist ^= computeZobrist(board, QUEEN);
        zobrist ^= computeZobrist(board, ROOK);
        zobrist ^= computeZobrist(board, BISHOP);
        zobrist ^= computeZobrist(board, KNIGHT);
        zobrist ^= computeZobrist(board, PAWN);
        return zobrist;
    }

    public static long computeZobristNonIncremental(final int state) {
        long zobrist = ZOBRIST_CASTLING[(state & CASTLING_ALL) >> SHIFT_CASTLING];
        zobrist ^= ZOBRIST_EN_PASSANT[(state & EN_PASSANT) >> SHIFT_EN_PASSANT];
        return zobrist;
    }

    public static long computeZobrist(final Board board, final int piece) {
        return computeZobrist(board, WHITE_TO_MOVE, piece) ^ computeZobrist(board, BLACK_TO_MOVE, piece);
    }

    private static long computeZobrist(final Board board, final int toMove, final int piece) {
        final int[] pieces = board.getPieces(toMove, piece);
        long zobrist = 0;
        for (int i = pieces[0]; i > 0; i--) {
            final int pos0x88 = pieces[i];
            final int pos64 = convert0x88To64(pos0x88);
            zobrist ^= ZOBRIST_PIECES[piece][toMove][pos64];
        }
        return zobrist;
    }

    public static int convert0x88To64(final int pos0x88) {
        return ((pos0x88 & RANK) >> 1) | (pos0x88 & FILE);
    }

    public static int side(final int piece) {
        return 1 - (piece >>> 31);
    }

    public static boolean isCastling(final int move) {
        final int moveType = move & MOVE_TYPE;
        return moveType == MT_CASTLING_KINGSIDE || moveType == MT_CASTLING_QUEENSIDE;
    }

    public static Set<String> checkBoard(final Board board) {
        final Set<String> problems = new HashSet<String>();
        final int state = board.getState();
        final int[] squares = board.getBoard();
        if ((state & CASTLING_WHITE_KINGSIDE) > 0) {
            if (squares[H[0]] != ROOK) {
                problems.add("King-side castling for white is allowed, but no white rook on h1");
            }
            if (squares[E[0]] != KING) {
                problems.add("King-side castling for white is allowed, but white king is not on e1");
            }
        }
        if ((state & CASTLING_WHITE_QUEENSIDE) > 0) {
            if (squares[A[0]] != ROOK) {
                problems.add("Queen-side castling for white is allowed, but no white rook on a1");
            }
            if (squares[E[0]] != KING) {
                problems.add("King-side castling for white is allowed, but white king is not on e1");
            }
        }
        if ((state & CASTLING_BLACK_KINGSIDE) > 0) {
            if (squares[H[7]] != -ROOK) {
                problems.add("King-side castling for white is allowed, but no black rook on h8");
            }
            if (squares[E[7]] != -KING) {
                problems.add("King-side castling for white is allowed, but black king is not on e8");
            }
        }
        if ((state & CASTLING_BLACK_QUEENSIDE) > 0) {
            if (squares[A[7]] != -ROOK) {
                problems.add("Queen-side castling for white is allowed, but no black rook on a8");
            }
            if (squares[E[7]] != -KING) {
                problems.add("King-side castling for white is allowed, but black king is not on e8");
            }
        }
        if ((state & EN_PASSANT) > 0) {
            final int file = ((state & EN_PASSANT) >> SHIFT_EN_PASSANT) - 1;
            if ((state & WHITE_TO_MOVE) > 0) {
                if (squares[getPosition(file, 6)] != EMPTY) {
                    problems.add(String.format(
                        "En passant file '%1$s' is defined, but '%1$s7' is not empty", StringUtils.FILE[file]));
                }
                if (squares[getPosition(file, 5)] != EMPTY) {
                    problems.add(String.format(
                        "En passant file '%1$s' is defined, but '%1$s6' is not empty", StringUtils.FILE[file]));
                }
                if (squares[getPosition(file, 4)] != -PAWN) {
                    problems.add(String.format(
                        "En passant file '%1$s' is defined, but no black pawn is on '%1$s6'", StringUtils.FILE[file]));
                }
            } else {
                if (squares[getPosition(file, 1)] != EMPTY) {
                    problems.add(String.format(
                        "En passant file '%1$s' is defined, but '%1$s2' is not empty", StringUtils.FILE[file]));
                }
                if (squares[getPosition(file, 2)] != EMPTY) {
                    problems.add(String.format(
                        "En passant file '%1$s' is defined, but '%1$s3' is not empty", StringUtils.FILE[file]));
                }
                if (squares[getPosition(file, 3)] != PAWN) {
                    problems.add(String.format(
                        "En passant file '%1$s' is defined, but no black pawn is on '%1$s4'", StringUtils.FILE[file]));
                }
            }
        }
        return problems;
    }

    public static Set<String> checkMove(final Board board, final int move) {
        final Set<String> problems = new HashSet<String>();
        final int[] squares = board.getBoard();
        final int fromPos = getFromPosition(move);
        final int piece = squares[fromPos];
        final int absPiece = Math.abs(piece);
        if (piece == EMPTY) {
            problems.add(String.format("From position %s is empty", StringUtils.toString0x88(fromPos)));
        }
        final int state = board.getState();
        final int toMove = state & WHITE_TO_MOVE;
        if (toMove == WHITE && piece < 0) {
            problems.add(String.format("It's white to move, but %s has a black %s on it",
                StringUtils.toString0x88(fromPos), StringUtils.PIECES[absPiece]));
        }
        final int signum = (toMove << 1) - 1;
        if (toMove == BLACK && piece > 0) {
            problems.add(String.format("It's black to move, but %s has a white %s on it",
                StringUtils.toString0x88(fromPos), StringUtils.PIECES[absPiece]));
        }
        final int fromRank = getRank(fromPos);
        final int toPos = getToPosition(move);
        final int moveType = move & MOVE_TYPE;
        if (absPiece == PAWN) {
            if (fromRank == 0 || fromRank == 7) {
                problems.add(String.format("Pawn moving from 1st/8th rank (%s)", StringUtils.toString0x88(fromPos)));
            }

            final int fromFile = getFile(fromPos);
            final int toFile = getFile(toPos);
            if (fromFile == toFile && squares[toPos] != EMPTY) {
                problems.add(String.format("Pawn moving to non-empty square (%s).", StringUtils.toString0x88(toPos)));
            }
        }
        final int capturePos;
        if (moveType == MT_EN_PASSANT) {
            capturePos = toPos - signum * UP;
            if (absPiece != PAWN) {
                problems.add(String.format("%s on %s is trying to make an en-passant move",
                    StringUtils.PIECES[absPiece], StringUtils.toString0x88(fromPos)));
            }
        } else {
            capturePos = toPos;
        }
        final int capturedPiece = squares[capturePos];
        if (piece * capturedPiece > 0) {
            problems.add(String.format("The %s on %s is trying to capture a piece with the same color on %s",
                StringUtils.PIECES[absPiece], StringUtils.toString0x88(fromPos), StringUtils.toString0x88(toPos)));
        }
        if (moveType == MT_NORMAL) {
            if (board.isSliding(absPiece)) {
                if (!board.isAttackedBySliding(toPos, ATTACK_BITS[absPiece], fromPos)) {
                    problems.add(String.format("The %s on %s is trying to move to %s which is not possible",
                        StringUtils.PIECES[absPiece], StringUtils.toString0x88(fromPos), StringUtils.toString0x88(toPos)));
                }
            } else if (absPiece != PAWN) {
                if (!board.isAttackedByNonSliding(toPos, ATTACK_BITS[absPiece], fromPos)) {
                    problems.add(String.format("The %s on %s is trying to move to %s which is not possible",
                        StringUtils.PIECES[absPiece], StringUtils.toString0x88(fromPos), StringUtils.toString0x88(toPos)));
                }
            }
        }
        return problems;
    }
}