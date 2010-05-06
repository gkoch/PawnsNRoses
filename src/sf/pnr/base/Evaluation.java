package sf.pnr.base;

import sf.pnr.alg.EvalHashTable;
import sf.pnr.alg.PawnHashTable;

import java.util.Random;

import static sf.pnr.base.BitBoard.*;
import static sf.pnr.base.Utils.*;

/**
 */
public final class Evaluation {

    public static final int VAL_DRAW = 0;
    public static final int VAL_MATE = 20000;
    public static final int VAL_MIN = -30000;
    public static final int VAL_MAX =  30000;

    public static final int VAL_PAWN = 100;
    public static final int VAL_KNIGHT = 325;
    public static final int VAL_BISHOP = 333;
    public static final int VAL_ROOK = 515;
    public static final int VAL_QUEEN = 935;
    public static final int VAL_KING = VAL_MATE;

    public static final int[] VAL_PIECES;

    public static final int[][] VAL_PIECE_COUNTS;
    public static final int[][] VAL_PIECE_INCREMENTS;

    private static final int[] VAL_POSITION_BONUS_PAWN;
    private static final int[] VAL_POSITION_BONUS_PAWN_ENDGAME;
    private static final int[] VAL_POSITION_BONUS_KNIGHT;
    private static final int[] VAL_POSITION_BONUS_BISHOP;
    private static final int[] VAL_POSITION_BONUS_ROOK;
    private static final int[] VAL_POSITION_BONUS_QUEEN;
    private static final int[] VAL_POSITION_BONUS_KING;
    private static final int[] VAL_POSITION_BONUS_KING_ENDGAME;

    private static final int[][] VAL_POSITION_BONUS_OPENING = new int[7][128];
    private static final int[][] VAL_POSITION_BONUS_ENDGAME = new int[7][128];

    public static final int PENALTY_DOUBLE_PAWN = -20;
    public static final int PENALTY_TRIPLE_PAWN = -30;
    public static final int PENALTY_ISOLATED_PAWN = -30;
    public static final int PENALTY_WEAK_PAWN = -30;

    public static final int BONUS_PAWN_SHIELD = 30;
    public static final int BONUS_PAWN_STORM_MAX = 25;
    public static final int BONUS_PAWN_STORM_DEDUCTION_MAIN_FILE = 5;
    public static final int BONUS_PAWN_STORM_DEDUCTION_SIDE_FILE = 4;
    public static final int BONUS_PASSED_PAWN_PER_SQUARE = 5;
    public static final int BONUS_DEFENSE = 2;
    public static final int BONUS_ATTACK = 3;
    public static final int BONUS_HUNG_PIECE = 3;
    public static final int BONUS_MOBILITY = 1;
    public static final int[] BONUS_DISTANCE_KNIGHT = new int[] {0, 1, 2, 3, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] BONUS_DISTANCE_BISHOP = new int[] {0, 1, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] BONUS_DISTANCE_ROOK = new int[] {0, 1, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] BONUS_DISTANCE_QUEEN = new int[] {0, 1, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0};
    public static final int BONUS_KING_IN_SIGHT_NON_SLIDING = 5;
    public static final int BONUS_KING_IN_SIGHT_SLIDING = 3;

    public static final int INITIAL_MATERIAL_VALUE;

    private static final Random RND = new Random(System.currentTimeMillis());

    static {
        VAL_PIECES = new int[7];
        VAL_PIECES[PAWN] = VAL_PAWN;
        VAL_PIECES[KNIGHT] = VAL_KNIGHT;
        VAL_PIECES[BISHOP] = VAL_BISHOP;
        VAL_PIECES[ROOK] = VAL_ROOK;
        VAL_PIECES[QUEEN] = VAL_QUEEN;
        VAL_PIECES[KING] = VAL_KING;

        VAL_PIECE_COUNTS = new int[7][11];
        VAL_PIECE_COUNTS[PAWN] = new int[] {-35, VAL_PAWN, 2 * VAL_PAWN, 3 * VAL_PAWN, 4 * VAL_PAWN, 5 * VAL_PAWN,
            6 * VAL_PAWN, 7 * VAL_PAWN, 8 * VAL_PAWN};
        VAL_PIECE_COUNTS[KNIGHT] = new int[] {0, VAL_KNIGHT, 2 * VAL_KNIGHT - 30, 3 * VAL_KNIGHT - 60,
            4 * VAL_KNIGHT - 150, 5 * VAL_KNIGHT - 300, 6 * VAL_KNIGHT - 450, 7 * VAL_KNIGHT - 600, 8 * VAL_KNIGHT - 750};
        VAL_PIECE_COUNTS[BISHOP] = new int[] {0, VAL_BISHOP, 2 * VAL_BISHOP + 30, 3 * VAL_BISHOP + 15,
            4 * VAL_BISHOP - 50, 5 * VAL_BISHOP - 150, 6 * VAL_BISHOP - 300, 7 * VAL_BISHOP - 450, 8 * VAL_BISHOP - 600};
        VAL_PIECE_COUNTS[ROOK] = new int[] {0, VAL_ROOK, 2 * VAL_ROOK, 3 * VAL_ROOK - 30, 4 * ROOK - 60,
            5 * VAL_ROOK - 90, 6 * VAL_ROOK - 200, 7 * VAL_ROOK - 350, 8 * VAL_ROOK - 500};
        VAL_PIECE_COUNTS[QUEEN] = new int[] {0, VAL_QUEEN, 2 * VAL_QUEEN, 3 * VAL_QUEEN, 4 * VAL_QUEEN, 5 * VAL_QUEEN,
            6 * VAL_QUEEN, 7 * VAL_QUEEN, 8 * VAL_QUEEN, 9 * VAL_QUEEN, 10 * VAL_QUEEN};
        VAL_PIECE_INCREMENTS = new int[7][11];
        for (int i = 0; i < VAL_PIECE_COUNTS.length; i++) {
            for (int j = 1; j < VAL_PIECE_COUNTS[i].length; j++) {
                VAL_PIECE_INCREMENTS[i][j] = VAL_PIECE_COUNTS[i][j] - VAL_PIECE_COUNTS[i][j - 1];
            }
        }
        INITIAL_MATERIAL_VALUE = VAL_PIECE_COUNTS[PAWN][8] + VAL_PIECE_COUNTS[KNIGHT][2] + VAL_PIECE_COUNTS[BISHOP][2] +
            VAL_PIECE_COUNTS[ROOK][2] + VAL_PIECE_COUNTS[QUEEN][1];

        VAL_POSITION_BONUS_PAWN = new int[]
            {
                  0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0,
                 50, 50, 50, 50, 50, 50, 50, 50,      5, 10, 10,-20,-20, 10, 10,  5,
                 10, 10, 20, 30, 30, 20, 10, 10,      5, -5,-10,  0,  0,-10, -5,  5,
                  5,  5, 10, 25, 25, 10,  5,  5,      0,  0,  0, 20, 20,  0,  0,  0,
                  0,  0,  0, 20, 20,  0,  0,  0,      5,  5, 10, 25, 25, 10,  5,  5,
                  5, -5,-10,  0,  0,-10, -5,  5,     10, 10, 20, 30, 30, 20, 10, 10,
                  5, 10, 10,-20,-20, 10, 10,  5,     50, 50, 50, 50, 50, 50, 50, 50,
                  0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0
            };
        VAL_POSITION_BONUS_PAWN_ENDGAME = new int[]
            {
                  0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0,
                 20, 20, 20, 20, 20, 20, 20, 20,      0,  0,  0,-20,-20,  0,  0,  0,
                 16, 16, 17, 18, 18, 17, 16, 16,      4,  4,  4,  0,  0,  4,  4,  4,
                 12, 12, 14, 16, 16, 14,  0, 12,      8,  8, 11, 12, 12, 11,  8,  8,
                  8,  8, 11, 12, 12, 11,  8,  8,     12, 12, 14, 16, 16, 14,  0, 12,
                  4,  4,  4,  0,  0,  4,  4,  4,     16, 16, 17, 18, 18, 17, 16, 16,
                  0,  0,  0,-20,-20,  0,  0,  0,     20, 20, 20, 20, 20, 20, 20, 20,
                  0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0
            };
        VAL_POSITION_BONUS_KNIGHT = new int[]
            {
                -50,-40,-30,-30,-30,-30,-40,-50,    -50,-40,-25,-30,-30,-25,-40,-50,
                -40,-20,  0,  0,  0,  0,-20,-40,    -40,-20,  0,  5,  5,  0,-20,-40,
                -30,  0, 10, 15, 15, 10,  0,-30,    -30,  5, 10, 15, 15, 10,  5,-30,
                -30,  5, 15, 20, 20, 15,  5,-30,    -30,  0, 15, 20, 20, 15,  0,-30,
                -30,  0, 15, 20, 20, 15,  0,-30,    -30,  5, 15, 20, 20, 15,  5,-30,
                -30,  5, 10, 15, 15, 10,  5,-30,    -30,  0, 10, 15, 15, 10,  0,-30,
                -40,-20,  0,  5,  5,  0,-20,-40,    -40,-20,  0,  0,  0,  0,-20,-40,
                -50,-40,-25,-30,-30,-25,-40,-50,    -50,-40,-30,-30,-30,-30,-40,-50
            };
        VAL_POSITION_BONUS_BISHOP = new int[]
            {
                -20,-10,-10,-10,-10,-10,-10,-20,    -20,-10,-10,-10,-10,-10,-10,-20,
                -10,  0,  0,  0,  0,  0,  0,-10,    -10,  5,  0,  0,  0,  0,  5,-10,
                -10,  0,  5, 10, 10,  5,  0,-10,    -10, 10, 10, 10, 10, 10, 10,-10,
                -10,  5,  5, 10, 10,  5,  5,-10,    -10,  0, 10, 10, 10, 10,  0,-10,
                -10,  0, 10, 10, 10, 10,  0,-10,    -10,  5,  5, 10, 10,  5,  5,-10,
                -10, 10, 10, 10, 10, 10, 10,-10,    -10,  0,  5, 10, 10,  5,  0,-10,
                -10,  5,  0,  0,  0,  0,  5,-10,    -10,  0,  0,  0,  0,  0,  0,-10,
                -20,-10,-10,-10,-10,-10,-10,-20,    -20,-10,-10,-10,-10,-10,-10,-20
            };
        VAL_POSITION_BONUS_ROOK = new int[]
            {
                  0,  0,  0,  0,  0,  0,  0,  0,      0,  0,  0,  5,  5,  0,  0,  0,
                  5, 10, 10, 10, 10, 10, 10,  5,     -5,  0,  0,  0,  0,  0,  0, -5,
                 -5,  0,  0,  0,  0,  0,  0, -5,     -5,  0,  0,  0,  0,  0,  0, -5,
                 -5,  0,  0,  0,  0,  0,  0, -5,     -5,  0,  0,  0,  0,  0,  0, -5,
                 -5,  0,  0,  0,  0,  0,  0, -5,     -5,  0,  0,  0,  0,  0,  0, -5,
                 -5,  0,  0,  0,  0,  0,  0, -5,     -5,  0,  0,  0,  0,  0,  0, -5,
                 -5,  0,  0,  0,  0,  0,  0, -5,      5, 10, 10, 10, 10, 10, 10,  5,
                  0,  0,  0,  5,  5,  0,  0,  0,      0,  0,  0,  0,  0,  0,  0,  0
            };
        VAL_POSITION_BONUS_QUEEN = new int[]
            {
                -20,-10,-10, -5, -5,-10,-10,-20,    -20,-10,-10, -5, -5,-10,-10,-20,
                -10,  0,  0,  0,  0,  0,  0,-10,    -10,  0,  5,  0,  0,  0,  0,-10,
                -10,  0,  5,  5,  5,  5,  0,-10,    -10,  5,  5,  5,  5,  5,  0,-10,
                 -5,  0,  5,  5,  5,  5,  0, -5,      0,  0,  5,  5,  5,  5,  0, -5,
                  0,  0,  5,  5,  5,  5,  0, -5,     -5,  0,  5,  5,  5,  5,  0, -5,
                -10,  5,  5,  5,  5,  5,  0,-10,    -10,  0,  5,  5,  5,  5,  0,-10,
                -10,  0,  5,  0,  0,  0,  0,-10,    -10,  0,  0,  0,  0,  0,  0,-10,
                -20,-10,-10, -5, -5,-10,-10,-20,    -20,-10,-10, -5, -5,-10,-10,-20
            };
        VAL_POSITION_BONUS_KING = new int[]
            {
                -30,-40,-40,-50,-50,-40,-40,-30,     20, 30, 10,  0,  0, 10, 30, 20,
                -30,-40,-40,-50,-50,-40,-40,-30,     20, 20,  0,  0,  0,  0, 20, 20,
                -30,-40,-40,-50,-50,-40,-40,-30,    -10,-20,-20,-20,-20,-20,-20,-10,
                -30,-40,-40,-50,-50,-40,-40,-30,    -20,-30,-30,-40,-40,-30,-30,-20,
                -20,-30,-30,-40,-40,-30,-30,-20,    -30,-40,-40,-50,-50,-40,-40,-30,
                -10,-20,-20,-20,-20,-20,-20,-10,    -30,-40,-40,-50,-50,-40,-40,-30,
                 20, 20,  0,  0,  0,  0, 20, 20,    -30,-40,-40,-50,-50,-40,-40,-30,
                 20, 30, 10,  0,  0, 10, 30, 20,    -30,-40,-40,-50,-50,-40,-40,-30
            };
        VAL_POSITION_BONUS_KING_ENDGAME = new int[]
            {
                -50,-40,-30,-20,-20,-30,-40,-50,   -50,-30,-30,-30,-30,-30,-30,-50,
                -30,-20,-10,  0,  0,-10,-20,-30,   -30,-30,  0,  0,  0,  0,-30,-30,
                -30,-10, 20, 30, 30, 20,-10,-30,   -30,-10, 20, 30, 30, 20,-10,-30,
                -30,-10, 30, 40, 40, 30,-10,-30,   -30,-10, 30, 40, 40, 30,-10,-30,
                -30,-10, 30, 40, 40, 30,-10,-30,   -30,-10, 30, 40, 40, 30,-10,-30,
                -30,-10, 20, 30, 30, 20,-10,-30,   -30,-10, 20, 30, 30, 20,-10,-30,
                -30,-30,  0,  0,  0,  0,-30,-30,   -30,-20,-10,  0,  0,-10,-20,-30,
                -50,-30,-30,-30,-30,-30,-30,-50,   -50,-40,-30,-20,-20,-30,-40,-50
            };

        VAL_POSITION_BONUS_OPENING[PAWN] = VAL_POSITION_BONUS_PAWN;
        VAL_POSITION_BONUS_OPENING[KNIGHT] = VAL_POSITION_BONUS_KNIGHT;
        VAL_POSITION_BONUS_OPENING[BISHOP] = VAL_POSITION_BONUS_BISHOP;
        VAL_POSITION_BONUS_OPENING[ROOK] = VAL_POSITION_BONUS_ROOK;
        VAL_POSITION_BONUS_OPENING[QUEEN] = VAL_POSITION_BONUS_QUEEN;
        VAL_POSITION_BONUS_OPENING[KING] = VAL_POSITION_BONUS_KING;

        VAL_POSITION_BONUS_ENDGAME[PAWN] = VAL_POSITION_BONUS_PAWN_ENDGAME;
        VAL_POSITION_BONUS_ENDGAME[KNIGHT] = VAL_POSITION_BONUS_KNIGHT;
        VAL_POSITION_BONUS_ENDGAME[BISHOP] = VAL_POSITION_BONUS_BISHOP;
        VAL_POSITION_BONUS_ENDGAME[ROOK] = VAL_POSITION_BONUS_ROOK;
        VAL_POSITION_BONUS_ENDGAME[QUEEN] = VAL_POSITION_BONUS_QUEEN;
        VAL_POSITION_BONUS_ENDGAME[KING] = VAL_POSITION_BONUS_KING_ENDGAME;
    }

    private boolean random;
    private PawnHashTable pawnHashTable = new PawnHashTable();
    private EvalHashTable evalHashTable = new EvalHashTable();

    public int evaluate(final Board board) {
        final int state = board.getState();
        final int halfMoves = (state & HALF_MOVES) >> SHIFT_HALF_MOVES;
        if (halfMoves >= 50) {
            return VAL_DRAW;
        }
        final long zobrist = board.getZobristKey();
        final int value = evalHashTable.read(zobrist);
        if (value != 0) {
            return value + VAL_MIN;
        }
        if (drawByInsufficientMaterial(board)) {
            return VAL_DRAW;
        }
        int score = computeMaterialValueNoPawn(board);
        score += computePositionalBonus(board);
        score += computeMobilityBonus(board);
        score += pawnEval(board);
        if (random) {
            score += RND.nextInt(20);
        }
        evalHashTable.set(zobrist, score - VAL_MIN);
        return score;
    }

    public static int computeMaterialValue(final Board board) {
        final int state = board.getState();
        final int toMove = state & WHITE_TO_MOVE;
        return computeMaterialValueNoPawn(board) + VAL_PIECE_COUNTS[PAWN][board.getPieces(toMove, PAWN)[0]] -
            VAL_PIECE_COUNTS[PAWN][board.getPieces(1 - toMove, PAWN)[0]];
    }

    private static int computeMaterialValueNoPawn(final Board board) {
        final int state = board.getState();
        final int toMove = state & WHITE_TO_MOVE;
        final int opponent = 1 - toMove;
        int score = VAL_PIECE_COUNTS[KNIGHT][board.getPieces(toMove, KNIGHT)[0]] -
            VAL_PIECE_COUNTS[KNIGHT][board.getPieces(opponent, KNIGHT)[0]];
        score += VAL_PIECE_COUNTS[BISHOP][board.getPieces(toMove, BISHOP)[0]] -
            VAL_PIECE_COUNTS[BISHOP][board.getPieces(opponent, BISHOP)[0]];
        score += VAL_PIECE_COUNTS[ROOK][board.getPieces(toMove, ROOK)[0]] -
            VAL_PIECE_COUNTS[ROOK][board.getPieces(opponent, ROOK)[0]];
        score += VAL_PIECE_COUNTS[QUEEN][board.getPieces(toMove, QUEEN)[0]] -
            VAL_PIECE_COUNTS[QUEEN][board.getPieces(opponent, QUEEN)[0]];
        return score;
    }

    public static int computeMaterialValueOneSide(final Board board, final int side) {
        int score = VAL_PIECE_COUNTS[PAWN][board.getPieces(side, PAWN)[0]];
        score += VAL_PIECE_COUNTS[KNIGHT][board.getPieces(side, KNIGHT)[0]];
        score += VAL_PIECE_COUNTS[BISHOP][board.getPieces(side, BISHOP)[0]];
        score += VAL_PIECE_COUNTS[ROOK][board.getPieces(side, ROOK)[0]];
        score += VAL_PIECE_COUNTS[QUEEN][board.getPieces(side, QUEEN)[0]];
        return score;
    }

    public int computePositionalBonus(final Board board) {
        int bonus = 0;
        final int toMove = board.getState() & WHITE_TO_MOVE;
        final int shift = toMove << 3;
        final int shiftOpponent = 8 - shift;
        final int opponent = 1 - toMove;
        final int stage = board.getStage();
        for (int type: TYPES) {
            int typeBonusOpening = 0;
            int typeBonusEndGame = 0;
            final int[] positionalBonusOpening = VAL_POSITION_BONUS_OPENING[type];
            final int[] positionalBonusEndGame = VAL_POSITION_BONUS_OPENING[type];
            final int[] pieces = board.getPieces(toMove, type);
            for (int i = pieces[0]; i > 0; i--) {
                typeBonusOpening += positionalBonusOpening[pieces[i] + shift];
                typeBonusEndGame += positionalBonusEndGame[pieces[i] + shift];
            }
            final int[] piecesOpponent = board.getPieces(opponent, type);
            for (int i = piecesOpponent[0]; i > 0; i--) {
                typeBonusOpening -= positionalBonusOpening[piecesOpponent[i] + shiftOpponent];
                typeBonusEndGame -= positionalBonusEndGame[piecesOpponent[i] + shiftOpponent];
            }
            bonus += (typeBonusOpening * (STAGE_MAX - stage) + typeBonusEndGame * stage) / STAGE_MAX;
        }
        return bonus;
    }

    public int computePositionalGain(final int absPiece, final int toMove, final int fromIndex, final int toIndex, final int stage) {
        final int shift = toMove << 3;
        final int[] typeBonusOpening = VAL_POSITION_BONUS_OPENING[absPiece];
        final int[] typeBonusEndGame = VAL_POSITION_BONUS_ENDGAME[absPiece];
        return ((typeBonusOpening[toIndex + shift] - typeBonusOpening[fromIndex + shift]) * (STAGE_MAX - stage) +
            (typeBonusEndGame[toIndex + shift] - typeBonusEndGame[fromIndex + shift]) * stage) / STAGE_MAX;
    }

    public int computeMobilityBonus(final Board board) {
        int score = computeMobilityBonusPawn(board, WHITE) - computeMobilityBonusPawn(board, BLACK);
        score += computeMobilityBonusKnight(board, WHITE) - computeMobilityBonusKnight(board, BLACK);
        score += computeMobilityBonusSliding(board, WHITE, BISHOP, BONUS_DISTANCE_BISHOP) -
            computeMobilityBonusSliding(board, WHITE, BISHOP, BONUS_DISTANCE_BISHOP);
        score += computeMobilityBonusSliding(board, WHITE, ROOK, BONUS_DISTANCE_ROOK) -
            computeMobilityBonusSliding(board, WHITE, ROOK, BONUS_DISTANCE_ROOK);
        score += computeMobilityBonusSliding(board, WHITE, QUEEN, BONUS_DISTANCE_QUEEN) -
            computeMobilityBonusSliding(board, WHITE, QUEEN, BONUS_DISTANCE_QUEEN);
        score += computeMobilityBonusKing(board, WHITE) - computeMobilityBonusKing(board, WHITE);
        return score;
    }

    private int computeMobilityBonusPawn(final Board boardObj, final int side) {
        final int[] board = boardObj.getBoard();
        int score = 0;
        final int signum = (side << 1) - 1;
        final int move = signum * UP;
        final int[] pieces = boardObj.getPieces(side, PAWN);
        for (int i = pieces[0]; i > 0; i--) {
            int pawn = pieces[i];
            for (int delta: DELTA_PAWN_ATTACK[side]) {
                int pos = pawn + delta;
                if ((pos & 0x88) == 0 && board[pos] != EMPTY) {
                    if (side == side(board[pos])) {
                        score += BONUS_DEFENSE;
                    } else if (board[pos] * (-signum) > PAWN) {
                        score += BONUS_ATTACK + BONUS_HUNG_PIECE;
                    }
                }
            }
            final int toIndex = pawn + move;
            if ((toIndex & 0x88) == 0) {
                final int attacked = board[toIndex];
                if (attacked == EMPTY) {
                    score += BONUS_MOBILITY;
                    final int rank = getRank(pawn);
                    if (rank == 1 && move == UP || rank == 6 && move == DN) {
                        if (board[(toIndex + move)] == EMPTY) {
                            score += BONUS_MOBILITY;
                        }
                    }
                }
            }
        }
        // TODO: enpassant?
        return score;
    }

    private int computeMobilityBonusKnight(final Board board, final int side) {
        final long piecesMask = board.getBitboard(side);
        final long opponentPiecesMask = board.getBitboard(1 - side);
        final int opponentKing = board.getKing(1 - side);
        final int opponentKing64 = convert0x88To64(opponentKing);
        final long opponentKingMask = 1L << opponentKing64;
        int score = 0;
        final int[] pieces = board.getPieces(side, KNIGHT);
        for (int i = pieces[0]; i > 0; i--) {
            final int knight = pieces[i];
            final int knight64 = convert0x88To64(knight);
            final long knightMask = KNIGHT_MOVES[knight64];
            final long defended = knightMask & piecesMask;
            score += Long.bitCount(defended) * BONUS_DEFENSE;
            final long attacked = knightMask & opponentPiecesMask;
            score += Long.bitCount(attacked) * BONUS_ATTACK;
            score += Long.bitCount(knightMask ^ defended ^ attacked) * BONUS_MOBILITY;
            score += Long.bitCount(knightMask & opponentKingMask) * BONUS_KING_IN_SIGHT_NON_SLIDING;
            score += BONUS_DISTANCE_KNIGHT[distance(knight, opponentKing)];
        }
        return score;
    }

    public static int distance(final int from0x88, final int to0x88) {
        final int distance = (ATTACK_ARRAY[from0x88 - to0x88 + 120] & ATTACK_DISTANCE) >> SHIFT_ATTACK_DISTANCE;
        assert distance == Math.max(Math.abs(getRank(from0x88) - getRank(to0x88)), Math.abs(getFile(from0x88) - getFile(to0x88))):
            StringUtils.toString0x88(from0x88) + " -> " + StringUtils.toString0x88(to0x88) + " @ " + distance;
        return distance;
    }

    private int computeMobilityBonusSliding(final Board boardObj, final int side, final int type, final int[] distanceBonus) {
        final int opponentKing = boardObj.getKing(1 - side);
        final int[] board = boardObj.getBoard();
        int score = 0;
        int distance = 0;
        final int[] pieces = boardObj.getPieces(side, type);
        for (int i = pieces[0]; i > 0; i--) {
            final int piecePos = pieces[i];
            for (int delta: DELTA[type]) {
                for (int pos = piecePos + delta; (pos & 0x88) == 0; pos += delta) {
                    if (board[pos] == EMPTY) {
                        score += BONUS_MOBILITY;
                    } else if (side == side(board[pos])) {
                        score += BONUS_DEFENSE;
                    } else {
                        score += BONUS_ATTACK;
                    }
                }
            }
            distance += distanceBonus[distance(piecePos, opponentKing)];
        }
        return score + distance * boardObj.getStage() / STAGE_MAX;
    }

    private int computeMobilityBonusKing(final Board boardObj, final int side) {
        final int[] board = boardObj.getBoard();
        int score = 0;
        final int kingIndex = boardObj.getKing(side);
        for (int delta: DELTA_KING) {
            int pos = kingIndex + delta;
            if ((pos & 0x88) == 0) {
                if (board[pos] == EMPTY) {
                    score += BONUS_MOBILITY;
                } else if (side == side(board[pos])) {
                    score += BONUS_DEFENSE;
                } else {
                    score += BONUS_ATTACK;
                }
            }
        }
        return score;
    }

    public static boolean drawByInsufficientMaterial(final Board board) {
        if (board.getPieces(WHITE_TO_MOVE, PAWN)[0] > 0 ||  board.getPieces(BLACK_TO_MOVE, PAWN)[0] > 0 ||
            board.getPieces(WHITE_TO_MOVE, ROOK)[0] > 0 ||  board.getPieces(BLACK_TO_MOVE, ROOK)[0] > 0 ||
            board.getPieces(WHITE_TO_MOVE, QUEEN)[0] > 0 ||  board.getPieces(BLACK_TO_MOVE, QUEEN)[0] > 0) {
            return false;
        }
        final int whiteKnightCount = board.getPieces(WHITE_TO_MOVE, KNIGHT)[0];
        final int blackKnightCount = board.getPieces(BLACK_TO_MOVE, KNIGHT)[0];
        if (whiteKnightCount + blackKnightCount > 1) {
            return false;
        }
        final int[] whiteBishops = board.getPieces(WHITE_TO_MOVE, BISHOP);
        final int whiteBishopCount = whiteBishops[0];
        final int[] blackBishops = board.getPieces(BLACK_TO_MOVE, BISHOP);
        final int blackBishopCount = blackBishops[0];
        if (whiteKnightCount + blackKnightCount == 1 && whiteBishopCount + blackBishopCount == 0) {
            return true;
        }
        if (whiteKnightCount + blackKnightCount == 1) {
            return false;
        }
        boolean bishopOnWhite = false;
        boolean bishopOnBlack = false;
        for (int i = 1; i <= whiteBishopCount; i++) {
            final int index = whiteBishops[i];
            final int color = (getRank(index) + getFile(index)) & 0x01;
            bishopOnWhite |= color == 1;
            bishopOnBlack |= color == 0;
        }
        for (int i = 1; i <= blackBishopCount; i++) {
            final int index = blackBishops[i];
            final int color = (getRank(index) + getFile(index)) & 0x01;
            bishopOnWhite |= color == 1;
            bishopOnBlack |= color == 0;
        }
        return !(bishopOnWhite && bishopOnBlack);
    }

    public int pawnEval(final Board board) {
        final int stage = board.getStage();
        final long zobristPawn = board.getZobristPawn();
        int eval = pawnHashTable.get(zobristPawn, stage);
        if (eval == 0) {
            eval = pawnEval(board, stage);
            pawnHashTable.set(zobristPawn, eval, stage);
        }
        final int toMove = board.getState() & WHITE_TO_MOVE;
        final int signum = ((toMove << 1) - 1);
        return eval * signum;
    }

    public int pawnEval(final Board board, final int stage) {

        int score = 0;

        // pawn storm
        final int whiteKing = board.getKing(WHITE);
        final int whiteKingFile = getFile(whiteKing);
        final int whiteKingRank = getRank(whiteKing);
        final int whiteKing64 = convert0x88To64(whiteKing);
        final long whiteKingMask = 1L << whiteKing64;
        final int blackKing = board.getKing(BLACK);
        final int blackKingFile = getFile(blackKing);
        final int blackKingRank = getRank(blackKing);
        final int blackKing64 = convert0x88To64(blackKing);
        final long blackKingMask = 1L << blackKing64;
        final boolean pawnStorm = (whiteKingFile <= FILE_D && blackKingFile >= FILE_E) ||
            (whiteKingFile >= FILE_E && blackKingFile <= FILE_D);

        long[] pawnMask = new long[2];
        long[] pawnAttackMask = new long[2];
        final int[] pawnsWhite = board.getPieces(WHITE, PAWN);
        int pawnStormBonus = 0;
        for (int i = pawnsWhite[0]; i > 0; i--) {
            final int pawn = pawnsWhite[i];
            final int pawn64 = convert0x88To64(pawn);
            pawnMask[WHITE] |= 1L << pawn64;
            final long pawnAttack = PAWN_ATTACK[WHITE][pawn64];
            pawnAttackMask[WHITE] |= pawnAttack;
            score += (blackKingMask & pawnAttack) > 0? BONUS_KING_IN_SIGHT_NON_SLIDING: 0;
            if (pawnStorm) {
                final int pawnRank = getRank(pawn);
                if (pawnRank < blackKingRank) {
                    final int pawnFile = getFile(pawn);
                    int pawnStormBonusDeduction = BONUS_PAWN_STORM_MAX;
                    if (pawnFile == blackKingFile) {
                        pawnStormBonusDeduction = BONUS_PAWN_STORM_DEDUCTION_MAIN_FILE * (blackKingRank - pawnRank);
                    } else if (pawnFile == blackKingFile + 1 || pawnFile == blackKingFile - 1) {
                        pawnStormBonusDeduction = BONUS_PAWN_STORM_DEDUCTION_SIDE_FILE * (blackKingRank - pawnRank);
                    }
                    if (pawnStormBonusDeduction < BONUS_PAWN_STORM_MAX) {
                        pawnStormBonus += BONUS_PAWN_STORM_MAX - pawnStormBonusDeduction;
                    }
                }
            }
        }
        final int[] pawnsBlack = board.getPieces(BLACK, PAWN);
        for (int i = pawnsBlack[0]; i > 0; i--) {
            final int pawn = pawnsBlack[i];
            final int pawn64 = convert0x88To64(pawn);
            pawnMask[BLACK] |= 1L << pawn64;
            final long pawnAttack = PAWN_ATTACK[BLACK][pawn64];
            pawnAttackMask[BLACK] |= pawnAttack;
            score -= (whiteKingMask & pawnAttack) > 0? BONUS_KING_IN_SIGHT_NON_SLIDING: 0;
            if (pawnStorm) {
                final int pawnRank = getRank(pawn);
                if (pawnRank > whiteKingRank) {
                    final int pawnFile = getFile(pawn);
                    int pawnStormBonusDeduction = BONUS_PAWN_STORM_MAX;
                    if (pawnFile == whiteKingFile) {
                        pawnStormBonusDeduction = BONUS_PAWN_STORM_DEDUCTION_MAIN_FILE * (pawnRank - whiteKingFile);
                    } else if (pawnFile == whiteKingFile + 1 || pawnFile == whiteKingFile - 1) {
                        pawnStormBonusDeduction = BONUS_PAWN_STORM_DEDUCTION_SIDE_FILE * (pawnRank - whiteKingFile);
                    }
                    if (pawnStormBonusDeduction < BONUS_PAWN_STORM_MAX) {
                        pawnStormBonus += BONUS_PAWN_STORM_MAX - pawnStormBonusDeduction;
                    }
                }
            }
        }
        score += pawnStormBonus * stage / STAGE_MAX;

        score += VAL_PIECE_COUNTS[PAWN][pawnsWhite[0]] - VAL_PIECE_COUNTS[PAWN][pawnsBlack[0]];

        final long attackedWhitePawns = pawnMask[WHITE] & pawnAttackMask[BLACK];
        final long attackedBlackPawns = pawnMask[BLACK] & pawnAttackMask[WHITE];
        score += (Long.bitCount((attackedWhitePawns ^ pawnAttackMask[WHITE]) & attackedWhitePawns) -
            Long.bitCount((attackedBlackPawns ^ pawnAttackMask[BLACK]) & attackedBlackPawns)) * PENALTY_WEAK_PAWN;

        long prevFileWhite = 0L;
        long midFileWhite = 0L;
        long prevFileBlack = 0L;
        long midFileBlack = 0L;
        for (int i = 0; i < 8; i++) {
            final long fileMask = BITBOARD_FILE[i];
            final long nextFileWhite = pawnMask[WHITE] & fileMask;
            final int whiteCount = Long.bitCount(nextFileWhite);
            if (whiteCount >= 2) {
                if (whiteCount == 2) {
                    score += PENALTY_DOUBLE_PAWN;
                } else {
                    score += PENALTY_TRIPLE_PAWN;
                }
            }
            if (prevFileWhite == 0L && midFileWhite > 0L && nextFileWhite == 0L) {
                score += PENALTY_ISOLATED_PAWN;
            }

            final long nextFileBlack = pawnMask[BLACK] & fileMask;
            final int blackCount = Long.bitCount(nextFileBlack);
            if (blackCount >= 2) {
                if (blackCount == 2) {
                    score -= PENALTY_DOUBLE_PAWN;
                } else {
                    score -= PENALTY_TRIPLE_PAWN;
                }
            }
            if (prevFileBlack == 0L && midFileBlack > 0L && nextFileBlack == 0L) {
                score -= PENALTY_ISOLATED_PAWN;
            }

            final long highestWhiteBit = Long.highestOneBit(midFileWhite);
            if (highestWhiteBit >= Long.highestOneBit(prevFileBlack) &&
                    highestWhiteBit > Long.highestOneBit(midFileBlack) &&
                    highestWhiteBit >= Long.highestOneBit(nextFileBlack)) {
                score += BONUS_PASSED_PAWN_PER_SQUARE * (Long.numberOfLeadingZeros(highestWhiteBit) / 8);
            }

            final long lowestBlackBit = Long.lowestOneBit(midFileBlack);
            if (lowestBlackBit != 0 && lowestBlackBit <= Long.lowestOneBit(prevFileWhite) &&
                    lowestBlackBit < Long.lowestOneBit(midFileWhite) &&
                    lowestBlackBit <= Long.lowestOneBit(nextFileWhite)) {
                score -= BONUS_PASSED_PAWN_PER_SQUARE * (Long.numberOfTrailingZeros(lowestBlackBit) / 8);
            }

            prevFileWhite = midFileWhite;
            midFileWhite = nextFileWhite;
            prevFileBlack = midFileBlack;
            midFileBlack = nextFileBlack;
        }
        if (prevFileWhite == 0L && midFileWhite > 0L) {
            score += PENALTY_ISOLATED_PAWN;
        }
        if (prevFileBlack == 0L && midFileBlack > 0L) {
            score -= PENALTY_ISOLATED_PAWN;
        }
        
        final long highestWhiteBit = Long.highestOneBit(midFileWhite);
        if (highestWhiteBit >= Long.highestOneBit(prevFileBlack) &&
                highestWhiteBit > Long.highestOneBit(midFileBlack)) {
            score += BONUS_PASSED_PAWN_PER_SQUARE * (Long.numberOfLeadingZeros(highestWhiteBit) / 8);
        }

        final long lowestBlackBit = Long.lowestOneBit(midFileBlack);
        if (lowestBlackBit != 0 && lowestBlackBit <= Long.lowestOneBit(prevFileWhite) &&
                lowestBlackBit < Long.lowestOneBit(midFileWhite)) {
            score -= BONUS_PASSED_PAWN_PER_SQUARE * (Long.numberOfTrailingZeros(lowestBlackBit) / 8);
        }

        int pawnShield = 0;
        if ((PAWN_SHIELD_KING_SIDE_KING[WHITE] & whiteKingMask) > 0) {
            for (long shieldMask : PAWN_SHIELD_KING_SIDE[WHITE]) {
                if ((pawnMask[WHITE] & shieldMask) == shieldMask) {
                    pawnShield = BONUS_PAWN_SHIELD;
                    break;
                }
            }
        } else if ((PAWN_SHIELD_QUEEN_SIDE_KING[WHITE] & whiteKingMask) > 0) {
            for (long shieldMask : PAWN_SHIELD_QUEEN_SIDE[WHITE]) {
                if ((pawnMask[WHITE] & shieldMask) == shieldMask) {
                    pawnShield = BONUS_PAWN_SHIELD;
                    break;
                }
            }
        }
        if ((PAWN_SHIELD_KING_SIDE_KING[BLACK] & blackKingMask) > 0) {
            for (long shieldMask: PAWN_SHIELD_KING_SIDE[BLACK]) {
                if ((pawnMask[BLACK] & shieldMask) == shieldMask) {
                    pawnShield -= BONUS_PAWN_SHIELD;
                    break;
                }
            }
        } else if ((PAWN_SHIELD_QUEEN_SIDE_KING[BLACK] & blackKingMask) > 0) {
            for (long shieldMask: PAWN_SHIELD_QUEEN_SIDE[BLACK]) {
                if ((pawnMask[BLACK] & shieldMask) == shieldMask) {
                    pawnShield -= BONUS_PAWN_SHIELD;
                    break;
                }
            }
        }
        score += pawnShield * (STAGE_MAX - stage) / STAGE_MAX;

        return score;
    }

    public void setRandom(final boolean random) {
        this.random = random;
    }
}