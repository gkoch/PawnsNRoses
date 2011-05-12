package sf.pnr.base;

import sf.pnr.alg.TranspositionTable;

import java.util.Arrays;
import java.util.Random;

import static sf.pnr.alg.TranspositionTable.*;
import static sf.pnr.base.Evaluation.*;
import static sf.pnr.base.Utils.*;

public final class Engine {

    public static final int INITIAL_ALPHA = -Evaluation.VAL_MATE - 1;
    public static final int INITIAL_BETA = Evaluation.VAL_MATE + 1;
    public static final int ASPIRATION_WINDOW = 50;

    private static final int[] NO_MOVE_ARRAY = new int[] {0};
    @Configurable(Configurable.Key.ENGINE_MOVE_ORDER_CHECK_BONUS)
    private static int MOVE_ORDER_CHECK_BONUS = 400;
    @Configurable(Configurable.Key.ENGINE_MOVE_ORDER_BLOCKED_CHECK_BONUS)
    private static int MOVE_ORDER_BLOCKED_CHECK_BONUS = 150;
    @Configurable(Configurable.Key.ENGINE_MOVE_ORDER_7TH_RANK_PAWN_BONUS)
    private static int MOVE_ORDER_7TH_RANK_PAWN_BONUS = 300;
    @Configurable(Configurable.Key.ENGINE_MOVE_ORDER_POSITIONAL_GAIN_SHIFT)
    private static int MOVE_ORDER_POSITIONAL_GAIN_SHIFT = 1;
    @Configurable(Configurable.Key.ENGINE_MOVE_ORDER_HISTORY_MAX_BITS)
    private static int MOVE_ORDER_HISTORY_MAX_BITS = 8;
    @Configurable(Configurable.Key.ENGINE_MOVE_ORDER_RND_MAX)
    private static int MOVE_ORDER_RND_MAX = 1;
    @Configurable(Configurable.Key.ENGINE_NULL_MOVE_MIN_DEPTH)
    private static int NULL_MOVE_MIN_DEPTH = 4 * PLY;
    @Configurable(Configurable.Key.ENGINE_NULL_MOVE_DEPTH_CHANGE_THRESHOLD)
    private static int NULL_MOVE_DEPTH_CHANGE_THRESHOLD = 6 * PLY;
    @Configurable(Configurable.Key.ENGINE_NULL_MOVE_DEPTH_HIGH)
    private static int NULL_MOVE_DEPTH_HIGH = 3 * PLY;
    @Configurable(Configurable.Key.ENGINE_NULL_MOVE_DEPTH_LOW)
    private static int NULL_MOVE_DEPTH_LOW = 2 * PLY;
    @Configurable(Configurable.Key.ENGINE_FUTILITY_THRESHOLD)
    private static int VAL_FUTILITY_THRESHOLD = 300;
    @Configurable(Configurable.Key.ENGINE_DEEP_FUTILITY_THRESHOLD)
    private static int VAL_DEEP_FUTILITY_THRESHOLD = 550;
    @Configurable(Configurable.Key.ENGINE_RAZORING_THRESHOLD)
    private static int VAL_RAZORING_THRESHOLD = 275;
    @Configurable(Configurable.Key.ENGINE_LMR_MIN_DEPTH)
    private static int LATE_MOVE_REDUCTION_MIN_DEPTH = 1 << SHIFT_PLY;
    @Configurable(Configurable.Key.ENGINE_LMR_MIN_MOVE)
    private static int LATE_MOVE_REDUCTION_MIN_MOVE = 2;

    @Configurable(Configurable.Key.ENGINE_DEPTH_EXT_CHECK)
    private static int DEPTH_EXT_CHECK = 16;
    @Configurable(Configurable.Key.ENGINE_DEPTH_EXT_7TH_RANK_PAWN)
    private static int DEPTH_EXT_7TH_RANK_PAWN = 8;
    @Configurable(Configurable.Key.ENGINE_DEPTH_EXT_MATE_THREAT)
    private static int DEPTH_EXT_MATE_THREAT = 8;
    @Configurable(Configurable.Key.ENGINE_DEPTH_EXT_MAX)
    private static int DEPTH_EXT_MAX = 16;
    @Configurable(Configurable.Key.ENGINE_ITERATIVE_DEEPENING_TIME_LIMIT)
    private static double ITERATIVE_DEEPENING_TIME_LIMIT = 0.9;
    @Configurable(Configurable.Key.ENGINE_SEARCH_ROOT_MIN_MOVE)
    private static int SEARCH_ROOT_MIN_MOVE = 5;

    private static final Random RND = new Random(System.currentTimeMillis());
    private final static int[] RND_ARRAY = new int[256];
    private static int RND_INDEX = 0;

    static {
        for (int i = 0; i < RND_ARRAY.length; i++) {
            RND_ARRAY[i] = RND.nextInt(MOVE_ORDER_RND_MAX);
        }
    }

    private enum SearchStage {TRANS_TABLE, CAPTURES_WINNING, PROMOTION, KILLERS, NORMAL, CAPTURES_LOOSING}
    private static final SearchStage[] searchStages = SearchStage.values();

    private final MoveGenerator moveGenerator = new MoveGenerator();
    private final Evaluation evaluation = new Evaluation();
    private final TranspositionTable transpositionTable = new TranspositionTable();
    private final int[][][] history = new int[14][64][64];
    private final int[][] killerMoves = new int[MAX_SEARCH_DEPTH << 2][2]; 
    private long searchStartTime;
    private long searchEndTime;
    private long lastCheckTime;
    private long nodeCount;
    private long nodeCountAtNextTimeCheck;
    private volatile boolean cancelled;
    private int historyMax = 0;
    private int historyMaxGlobal = 0;
    private int historyShift = 0;
    private int historyShiftGlobal = 0;
    private BestMoveListener listener;
    private int age;

    public long search(final Board board, int maxDepth, final long timeLeft) {
        age = (board.getState() & FULL_MOVES) >> SHIFT_FULL_MOVES;
        if (maxDepth == 0) {
            assert timeLeft > 0;
            maxDepth = MAX_SEARCH_DEPTH;
        }
        searchStartTime = System.currentTimeMillis();
        if (timeLeft == 0) {
            searchEndTime = Long.MAX_VALUE;
            nodeCountAtNextTimeCheck = Long.MAX_VALUE;
        } else {
            lastCheckTime = searchStartTime;
            searchEndTime = lastCheckTime + timeLeft;
            // assume we can easily do 5 nodes / ms
            nodeCountAtNextTimeCheck = timeLeft << 2;
        }
        nodeCount = 0;
        cancelled = false;
        for (int[] array: killerMoves) {
            Arrays.fill(array, 0);
        }
        int value = Evaluation.VAL_DRAW;
        long searchResult = getSearchResult(0, value);
        for (int depth = 1; depth <= maxDepth; depth++) {
            final int alpha;
            final int beta;
            if (depth > 1) {
                alpha = value - ASPIRATION_WINDOW;
                beta = value + ASPIRATION_WINDOW;
            } else {
                alpha = INITIAL_ALPHA;
                beta = INITIAL_BETA;
            }
            long result = negascoutRoot(board, depth << SHIFT_PLY, alpha, beta, 0);
            if (cancelled) {
                final int move = getMoveFromSearchResult(result);
                if (move != 0) {
                    searchResult = result;
                }
                break;
            }
            if (result != 0) {
                value = getValueFromSearchResult(result);
                if (value <= alpha) {
                    result = negascoutRoot(board, depth << SHIFT_PLY, INITIAL_ALPHA, alpha, 0);
                    value = getValueFromSearchResult(result);
                } else if (value >= beta) {
                    result = negascoutRoot(board, depth << SHIFT_PLY, beta, INITIAL_BETA, 0);
                    value = getValueFromSearchResult(result);
                }
            } else {
                result = negascoutRoot(board, depth << SHIFT_PLY, INITIAL_ALPHA, INITIAL_BETA, 0);
                value = getValueFromSearchResult(result);
            }
            final int move = getMoveFromSearchResult(result);
            if (move != 0) {
                searchResult = result;
            }
            if (cancelled) {
                break;
            }
            assert value == 0 || move != 0 || value < -VAL_MATE_THRESHOLD:
                "FEN: " + StringUtils.toFen(board) + ", score: " + value + ", depth: " + depth;
            searchResult = result;
            if (listener != null) {
                assert move != 0: StringUtils.toFen(board) + " / depth: " + depth + " / value: " + getValueFromSearchResult(result);
                if (move != 0) {
                    listener.bestMoveChanged(depth, move, value, System.currentTimeMillis() - searchStartTime,
                        getBestLine(board, move), nodeCount);
                }
            }
            if (value > VAL_MATE_THRESHOLD) {
                break;
            }
            if ((System.currentTimeMillis() - searchStartTime) > (searchEndTime - searchStartTime) * ITERATIVE_DEEPENING_TIME_LIMIT) {
                break;
            }
        }
        assert getMoveFromSearchResult(searchResult) != 0 || getValueFromSearchResult(searchResult) == 0;
        return searchResult;
    }

    public long negascoutRoot(final Board board, final int depth, int alpha, final int beta, final int searchedPly) {
        nodeCount++;
        if (board.getRepetitionCount() == 3 || Evaluation.drawByInsufficientMaterial(board)) {
            // three-fold repetition
            return getSearchResult(0, VAL_DRAW);
        }
        
        final long zobristKey = board.getZobristKey();
        final long ttValue = removeThreefoldRepetition(board, transpositionTable.read(zobristKey));
        int ttMove = (int) ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE);
        final int ttDepth = (int) (((ttValue & TT_DEPTH) >> TT_SHIFT_DEPTH) << SHIFT_PLY);
        if (ttValue != 0 && ttDepth >= depth) {
            final int value = (int) ((ttValue & TT_VALUE) >> TT_SHIFT_VALUE) + VAL_MIN;
            final long ttType = ttValue & TT_TYPE;
            if (ttType == TT_TYPE_EXACT) {
                assert ttMove != 0;
                return getSearchResult(ttMove, value);
            } else {
                if (value > VAL_MATE_THRESHOLD) {
                    assert ttMove != 0;
                    return getSearchResult(ttMove, value);
                }
                if (ttType == TT_TYPE_BETA_CUT) {
                    if (value >= beta) {
                        return getSearchResult(ttMove, value);
                    } else if (value > alpha) {
                        alpha = value;
                    }
                } else if (ttType == TT_TYPE_ALPHA_CUT && value <= alpha) {
                    return getSearchResult(0, 0);
                }
            }
        }

        if (depth > 3 * PLY && (ttMove == 0 || ttDepth < depth / 2)) {
            // internal iterative deepening
            final long searchResult = negascoutRoot(board, depth / 2, alpha, beta, searchedPly);
            ttMove = getMoveFromSearchResult(searchResult);
        }

        final int state = board.getState();
        final int toMove = state & WHITE_TO_MOVE;
        final boolean inCheck = board.attacksKing(1 - toMove);

        moveGenerator.pushFrame();
        int b = beta;
        long bestMoveType = TT_TYPE_ALPHA_CUT;
        int bestScore = VAL_MIN;
        int bestMove = 0;
        int legalMoveCount = 0;
        int quietMoveCount = 0;
        for (SearchStage searchStage: searchStages) {
            final boolean highPriorityStage =
                searchStage != SearchStage.NORMAL && searchStage != SearchStage.CAPTURES_LOOSING;
            final int[] moves = getMoves(searchStage, board, ttMove, searchedPly);
            final boolean allowQuiescence = (searchStage == SearchStage.CAPTURES_WINNING ||
                searchStage == SearchStage.PROMOTION || searchStage == SearchStage.CAPTURES_LOOSING ||
                searchStage == SearchStage.TRANS_TABLE && moves[0] == 1 &&
                    (MoveGenerator.isCapture(board, moves[1]) || MoveGenerator.isPromotion(board, moves[1])));
            for (int i = moves[0]; i > 0; i--) {
                final int move = moves[i];
                assert (move & BASE_INFO) != 0;

                // make the move
                final long undo = board.move(move);

                // check if the king remained in check
                // TODO instead: check if isCheck is true and the current move avoids the check or
                // TODO if the move causes discovered check
                if (board.attacksKing(1 - toMove)) {
                    board.takeBack(undo);
                    continue;
                }

                // register that we had a legal move
                legalMoveCount++;

                if (board.getRepetitionCount() == 3) {
                    if (alpha < VAL_DRAW) {
                        alpha = VAL_DRAW;
                        bestMoveType = TT_TYPE_EXACT;
                    }
                    if (bestScore < VAL_DRAW) {
                        bestScore = VAL_DRAW;
                        bestMove = move;
                    }
                    board.takeBack(undo);
                    continue;
                }

                final int signum = (toMove << 1) - 1;
                final boolean opponentInCheck = board.attacksKing(toMove);
                int depthExt = 0;
                if (opponentInCheck) {
                    depthExt += DEPTH_EXT_CHECK;
                }
                final int toPos = getToPosition(move);
                if (getRank(toPos) == 1 || getRank(toPos) == 6) {
                    final int piece = board.getBoard()[toPos];
                    final int absPiece = signum * piece;
                    if (absPiece == PAWN) {
                        depthExt += DEPTH_EXT_7TH_RANK_PAWN;
                    }
                }

                if (depthExt > DEPTH_EXT_MAX) {
                    depthExt = DEPTH_EXT_MAX;
                }

                // razoring
                if (depthExt == 0 && depth <= (3 << SHIFT_PLY) && legalMoveCount > 1 && beta < VAL_MATE_THRESHOLD) {
                    final int value = -board.getMaterialValue();
                    if (value < beta - VAL_RAZORING_THRESHOLD) {
//                        final int qscore = -quiescence(board, -b, -alpha);
                        final int qscore = board.getRepetitionCount() < 3? -quiescence(board, -b, -alpha): 0;
//                        final int qscore = -negascout(board, PLY, -b, -alpha, false, false, searchedPly + 1);
                        if (cancelled) {
                            moveGenerator.popFrame();
                            board.takeBack(undo);
                            return bestMoveType == TT_TYPE_EXACT && (depth <= PLY || legalMoveCount > SEARCH_ROOT_MIN_MOVE)?
                                getSearchResult(bestMove, alpha): getSearchResult(0, 0);
                        }
//                        if (bestScore < qscore) {
//                            bestScore = qscore;
//                            bestMove = move;
//                        }
                        if (qscore < b) {
                            board.takeBack(undo);
                            continue;
                        }
                    }
                }

                int a = alpha + 1;
                if (!highPriorityStage) {
                    if (quietMoveCount >= LATE_MOVE_REDUCTION_MIN_MOVE && !inCheck && depth >= LATE_MOVE_REDUCTION_MIN_DEPTH &&
                            !Utils.isCastling(move) && !opponentInCheck && depthExt == 0) {
                        a = -negascout(board, depth - (2 << SHIFT_PLY), -b, -alpha, false, true, searchedPly + 1);
                        if (cancelled) {
                            board.takeBack(undo);
                            moveGenerator.popFrame();
                            return bestMoveType == TT_TYPE_EXACT && (depth <= PLY || legalMoveCount > SEARCH_ROOT_MIN_MOVE)?
                                getSearchResult(bestMove, alpha): getSearchResult(0, 0);
                        }
                    }
                }

                // evaluate the move
                if (a > alpha && b >= -VAL_MATE_THRESHOLD) {
                    a = -negascout(board, depth - PLY + depthExt, -b, -alpha, allowQuiescence, true, searchedPly + 1);
                    if (cancelled) {
                        board.takeBack(undo);
                        moveGenerator.popFrame();
                        return bestMoveType == TT_TYPE_EXACT && (depth <= PLY || legalMoveCount > SEARCH_ROOT_MIN_MOVE)?
                            getSearchResult(bestMove, alpha): getSearchResult(0, 0);
                    }

                    // the other player has a better option, beta cut off
                    if (a >= beta) {
                        board.takeBack(undo);
                        moveGenerator.popFrame();
                        assert board.getBoard()[getFromPosition(move)] != EMPTY;
                        transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                        addMoveToHistoryTable(board, move);
                        addMoveToKillers(searchedPly, searchStage, move);
                        assert move != 0;
                        return getSearchResult(move, a);
                    }
                }

                if (a >= b) {
                    // null-window was too narrow, try a full search
                    a = -negascout(board, depth - PLY + depthExt, -beta, -a, allowQuiescence, true, searchedPly + 1);
                    if (cancelled) {
                        board.takeBack(undo);
                        moveGenerator.popFrame();
                        return bestMoveType == TT_TYPE_EXACT && (depth <= PLY || legalMoveCount > SEARCH_ROOT_MIN_MOVE)?
                            getSearchResult(bestMove, alpha): getSearchResult(0, 0);
                    }
                    if (a >= beta) {
                        board.takeBack(undo);
                        moveGenerator.popFrame();
                        assert board.getBoard()[getFromPosition(move)] != EMPTY;
                        transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                        addMoveToHistoryTable(board, move);
                        addMoveToKillers(searchedPly, searchStage, move);
                        assert move != 0;
                        return getSearchResult(move, a);
                    }
                }

                board.takeBack(undo);
                if (bestScore < a) {
                    bestScore = a;
                    bestMove = move;
                }
                if (a > alpha) {
                    bestMoveType = TT_TYPE_EXACT;
                    alpha = a;
                    quietMoveCount = 0;
                    addMoveToHistoryTable(board, move);
                    addMoveToKillers(searchedPly, searchStage, move);
                    if (alpha > VAL_MATE_THRESHOLD) {
                        break;
                    }
                } else {
                    quietMoveCount++;
                }

                b = alpha + 1;
            }
            if (legalMoveCount > 0 && alpha > VAL_MATE_THRESHOLD) {
                break;
            }
        }
        moveGenerator.popFrame();
        if (legalMoveCount == 0) {
            if (inCheck) {
                return getSearchResult(0, -Evaluation.VAL_MATE);
            } else {
                return getSearchResult(0, 0);
            }
        }
        final long result;
        transpositionTable.set(zobristKey, bestMoveType, bestMove, depth >> SHIFT_PLY, bestScore - VAL_MIN, age);
        if (bestMove != 0) {
            result = getSearchResult(bestMove, bestScore);
        } else {
            result = getSearchResult(0, 0);
        }
        return result;
    }

    public int negascout(final Board board, final int depth, int alpha, int beta, final boolean quiescence,
                         final boolean allowNull, final int searchedPly) {
        nodeCount++;
        if (board.getRepetitionCount() == 3 || Evaluation.drawByInsufficientMaterial(board)) {
            // three-fold repetition
            return VAL_DRAW;
        }
        if (depth < PLY || searchedPly > Utils.MAX_SEARCH_DEPTH - 15) {
            final int eval;
            if (!quiescence || searchedPly > Utils.MAX_SEARCH_DEPTH - 15) {
                eval = evaluation.evaluate(board);
            } else {
                eval = quiescence(board, alpha, beta);
            }
            return eval;
        }

        // check the time
        if (nodeCount >= nodeCountAtNextTimeCheck) {
            calculateNextTimeCheck();
            if (cancelled) {
                return alpha;
            }
        }

        final long zobristKey = board.getZobristKey();
        final long ttValue = removeThreefoldRepetition(board, transpositionTable.read(zobristKey));
        final int ttDepth = (int) ((ttValue & TT_DEPTH) >> TT_SHIFT_DEPTH);
        if (ttValue != 0 && ttDepth >= (depth >> SHIFT_PLY)) {
            final int value = (int) ((ttValue & TT_VALUE) >> TT_SHIFT_VALUE) + VAL_MIN;
            final long ttType = ttValue & TT_TYPE;
            if (ttType == TT_TYPE_EXACT) {
                assert ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE) != 0;
                return value;
            } else {
                if (value > VAL_MATE_THRESHOLD) {
                    return value;
                }
                if (ttType == TT_TYPE_BETA_CUT) {
                    if (value >= beta) {
                        return value;
                    } else if (value > alpha) {
                        alpha = value;
                    }
                } else if (ttType == TT_TYPE_ALPHA_CUT && value <= alpha) {
                    if (value != VAL_MIN) {
                        return value;
                    } else {
                        return alpha;
                    }
                }
            }
        }

        final int state = board.getState();
        final int toMove = state & WHITE_TO_MOVE;
        final boolean inCheck = board.attacksKing(1 - toMove);

        // null-move pruning
        int initialDepthExt = 0;
        if (depth > NULL_MOVE_MIN_DEPTH && !inCheck && allowNull && beta < VAL_MATE_THRESHOLD &&
                board.getMinorMajorPieceCount(toMove) > 0) {
            final int r = depth > NULL_MOVE_DEPTH_CHANGE_THRESHOLD? NULL_MOVE_DEPTH_HIGH: NULL_MOVE_DEPTH_LOW;
            final int prevState = board.nullMove();
            final int value = -negascout(board, depth - r, -beta, -beta + 1, false, false, searchedPly + 1);
            if (cancelled) {
                board.nullMove(prevState);
                return alpha;
            }
            if (value >= beta) {
                board.nullMove(prevState);
//                transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, 0, depth >> SHIFT_PLY, value - VAL_MIN, age);
                return beta;
            }
            final int value2 = -negascout(board, depth - r, VAL_MATE_THRESHOLD, VAL_MATE_THRESHOLD + 1, false, false, searchedPly + 1);
            board.nullMove(prevState);
            if (cancelled) {
                return alpha;
            }
            if (value2 < -VAL_MATE_THRESHOLD) {
                initialDepthExt += DEPTH_EXT_MATE_THREAT;
            }
        }

        int ttMove = (int) ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE);
        assert (Utils.getFromPosition(ttMove) & 0x88) == 0: Integer.toHexString(ttMove) + "/" + StringUtils.toSimple(ttMove);
        assert (Utils.getToPosition(ttMove) & 0x88) == 0: Integer.toHexString(ttMove) + "/" + StringUtils.toSimple(ttMove);
        if (depth > 3 * PLY && ttMove == 0) {
            // internal iterative deepening
            final long searchResult = negascoutRoot(board, depth / 2, alpha, beta, searchedPly);
            ttMove = getMoveFromSearchResult(searchResult);
            assert (Utils.getFromPosition(ttMove) & 0x88) == 0: Integer.toHexString(ttMove) + "/" + StringUtils.toSimple(ttMove);
            assert (Utils.getToPosition(ttMove) & 0x88) == 0: Integer.toHexString(ttMove) + "/" + StringUtils.toSimple(ttMove);
        }

        // futility pruning
        final boolean futility;
        if (depth < (3 << SHIFT_PLY) && !inCheck) {
            final int value = board.getMaterialValue();
            if (depth < (2 << SHIFT_PLY)) {
                futility = value < alpha - VAL_FUTILITY_THRESHOLD;
            } else {
                futility = value < alpha - VAL_DEEP_FUTILITY_THRESHOLD;
            }
        } else {
            futility = false;
        }

        moveGenerator.pushFrame();
        int b = beta;
        long bestMoveType = TT_TYPE_ALPHA_CUT;
        int bestMove = 0;
        int bestScore = VAL_MIN;
        int legalMoveCount = 0;
        int quietMoveCount = 0;
        for (SearchStage searchStage: searchStages) {
            final boolean highPriorityStage =
                searchStage != SearchStage.NORMAL && searchStage != SearchStage.CAPTURES_LOOSING;
            final int[] moves = getMoves(searchStage, board, ttMove, searchedPly);
            final boolean allowQuiescence = (searchStage == SearchStage.CAPTURES_WINNING ||
                searchStage == SearchStage.PROMOTION || searchStage == SearchStage.CAPTURES_LOOSING ||
                searchStage == SearchStage.TRANS_TABLE && moves[0] == 1 &&
                    (MoveGenerator.isCapture(board, moves[1]) || MoveGenerator.isPromotion(board, moves[1])));
            final boolean allowToRecurseDown = !futility ||
                (searchStage == SearchStage.TRANS_TABLE || searchStage == SearchStage.CAPTURES_WINNING ||
                    searchStage == SearchStage.PROMOTION);

            for (int i = moves[0]; i > 0; i--) {
                final int move = moves[i];
                assert (move & BASE_INFO) != 0;

                // make the move
                final long undo = board.move(move);

                // check if the king remained in check
                // TODO instead: check if isCheck is true and if the current move avoids the check
                if (board.attacksKing(1 - toMove)) {
                    board.takeBack(undo);
                    continue;
                }

                // register that we had a legal move
                legalMoveCount++;

                if (board.getRepetitionCount() == 3) {
                    if (alpha < VAL_DRAW) {
                        alpha = VAL_DRAW;
                        bestMoveType = TT_TYPE_EXACT;
                    }
                    if (bestScore < VAL_DRAW) {
                        bestScore = VAL_DRAW;
                        bestMove = move;
                    }
                    board.takeBack(undo);
                    continue;
                }

                final int signum = (toMove << 1) - 1;
                final boolean opponentInCheck = board.attacksKing(toMove);
                if (!allowToRecurseDown && !opponentInCheck) {
                    board.takeBack(undo);
                    break;
                }

                int depthExt = initialDepthExt;
                if (opponentInCheck) {
                    depthExt += DEPTH_EXT_CHECK;
                }
                final int toPos = getToPosition(move);
                if (getRank(toPos) == 1 || getRank(toPos) == 6) {
                    final int piece = board.getBoard()[toPos];
                    final int absPiece = signum * piece;
                    if (absPiece == PAWN) {
                        depthExt += DEPTH_EXT_7TH_RANK_PAWN;
                    }
                }

                if (depthExt > DEPTH_EXT_MAX) {
                    depthExt = DEPTH_EXT_MAX;
                }

                // razoring
                if (depthExt == 0 && depth <= (3 << SHIFT_PLY) && legalMoveCount > 1 && beta < VAL_MATE_THRESHOLD) {
                    final int value = -board.getMaterialValue();
                    if (value < beta - VAL_RAZORING_THRESHOLD) {
//                        final int qscore = -quiescence(board, -b, -alpha);
                        final int qscore = board.getRepetitionCount() < 3? -quiescence(board, -b, -alpha): 0;
//                        final int qscore = -negascout(board, PLY, -b, -alpha, false, false, searchedPly + 1);
                        if (cancelled) {
                            moveGenerator.popFrame();
                            board.takeBack(undo);
                            return alpha;
                        }
//                        if (bestScore < qscore) {
//                            bestScore = qscore;
//                            bestMove = move;
//                        }
                        if (qscore < b) {
                            board.takeBack(undo);
                            continue;
                        }
                    }
                }

                int a = alpha + 1;
                if (!highPriorityStage) {
                    if (quietMoveCount >= LATE_MOVE_REDUCTION_MIN_MOVE && !inCheck && depth >= LATE_MOVE_REDUCTION_MIN_DEPTH &&
                            !Utils.isCastling(move) && !opponentInCheck && depthExt == 0) {
                        a = -negascout(board, depth - (2 << SHIFT_PLY), -b, -alpha, false, true, searchedPly + 1);
                        if (cancelled) {
                            board.takeBack(undo);
                            moveGenerator.popFrame();
                            return alpha;
                        }
                    }
                }

                // evaluate the move
                if (a > alpha && b >= -VAL_MATE_THRESHOLD) {
                    a = -negascout(board, depth - PLY + depthExt, -b, -alpha, allowQuiescence, true, searchedPly + 1);
                    if (cancelled) {
                        board.takeBack(undo);
                        moveGenerator.popFrame();
                        return alpha;
                    }
                    // the other player has a better option, beta cut off
                    if (a >= beta) {
                        board.takeBack(undo);
                        moveGenerator.popFrame();
                        assert board.getBoard()[getFromPosition(move)] != EMPTY;
                        transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                        addMoveToHistoryTable(board, move);
                        addMoveToKillers(searchedPly, searchStage, move);
                        return a;
                    }
                }

                if (a >= b) {
                    // null-window was too narrow, try a full search
                    a = -negascout(board, depth - PLY + depthExt, -beta, -a, allowQuiescence, true, searchedPly + 1);
                    if (cancelled) {
                        board.takeBack(undo);
                        moveGenerator.popFrame();
                        return alpha;
                    }
                    if (a >= beta) {
                        board.takeBack(undo);
                        moveGenerator.popFrame();
                        assert board.getBoard()[getFromPosition(move)] != EMPTY;
                        transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, depth >> SHIFT_PLY, a - VAL_MIN, age);
                        addMoveToHistoryTable(board, move);
                        addMoveToKillers(searchedPly, searchStage, move);
                        return a;
                    }
                }
                board.takeBack(undo);
                if (bestScore < a) {
                    bestScore = a;
                    bestMove = move;
                }
                if (a > alpha) {
                    bestMoveType = TT_TYPE_EXACT;
                    alpha = a;
                    quietMoveCount = 0;
                    addMoveToHistoryTable(board, move);
                    addMoveToKillers(searchedPly, searchStage, move);
                    if (alpha > VAL_MATE_THRESHOLD) {
                        break;
                    }
                } else {
                    quietMoveCount++;
                }

                b = alpha + 1;
            }
            if (legalMoveCount > 0 && alpha > VAL_MATE_THRESHOLD) {
                break;
            }
        }
        moveGenerator.popFrame();
        if (legalMoveCount == 0) {
            if (inCheck) {
                return -Evaluation.VAL_MATE;
            } else {
                return 0;
            }
        } else if (bestScore == VAL_MIN) {
            final int value = evaluation.evaluate(board);
            if (value > alpha) {
                alpha = value;
            }
            if (alpha > beta) {
                alpha = beta;
            }
            if (value > bestScore) {
                bestScore = value;
            }
        }

        transpositionTable.set(zobristKey, bestMoveType, bestMove, depth >> SHIFT_PLY, bestScore - VAL_MIN, age);
        assert (Utils.getFromPosition(bestMove) & 0x88) == 0: Integer.toHexString(bestMove) + "/" + StringUtils.toSimple(bestMove);
        assert (Utils.getToPosition(bestMove) & 0x88) == 0: Integer.toHexString(bestMove) + "/" + StringUtils.toSimple(bestMove);
        assert (Utils.getFromPosition(ttMove) & 0x88) == 0: Integer.toHexString(ttMove) + "/" + StringUtils.toSimple(ttMove);
        assert (Utils.getToPosition(ttMove) & 0x88) == 0: Integer.toHexString(ttMove) + "/" + StringUtils.toSimple(ttMove);
        return alpha;
    }

    public long removeThreefoldRepetition(final Board board, final long ttValue1) {
        long result = ttValue1;
        final int move1 = (int) ((ttValue1 & TT_MOVE) >> TT_SHIFT_MOVE);
        if (move1 != 0) {
            final long undo1 = board.move(move1);
            if (board.getRepetitionCount() >= 3) {
                result = 0;
            } else {
                final long zobristKey = board.getZobristKey();
                final long ttValue2 = transpositionTable.read(zobristKey);
                final int move2 = (int) ((ttValue2 & TT_MOVE) >> TT_SHIFT_MOVE);
                if (move2 != 0) {
                    final long undo2 = board.move(move2);
                    if (board.getRepetitionCount() >= 3) {
                        final int value = ((int) ((ttValue1 & TT_VALUE) >> TT_SHIFT_VALUE)) + VAL_MIN;
                        if (value > 0) {
                            // opponent can force a three-fold repetition
                            result = 0;
                        }
                    }
                    board.takeBack(undo2);
                }
            }
            board.takeBack(undo1);
        }
        return result;
    }

    public int quiescence(final Board board, int alpha, int beta) {
        nodeCount++;

        if (Evaluation.drawByInsufficientMaterial(board)) {
            return VAL_DRAW;
        }
        
        final int state = board.getState();
        final int toMove = state & WHITE_TO_MOVE;

        final int eval = evaluation.evaluate(board);
        if (eval > alpha) {
            alpha = eval;
            if (alpha >= beta && !board.attacksKing(1 - toMove)) {
                assert alpha > VAL_MIN;
                return alpha;
            }
        }

        // check the time
        if (nodeCount >= nodeCountAtNextTimeCheck) {
            calculateNextTimeCheck();
            if (cancelled) {
                return alpha;
            }
        }

        final long zobristKey = board.getZobristKey();
        final long ttValue = removeThreefoldRepetition(board, transpositionTable.read(zobristKey));
        if (ttValue != 0) {
            final long ttType = ttValue & TT_TYPE;
            final int value = (int) ((ttValue & TT_VALUE) >> TT_SHIFT_VALUE) + VAL_MIN;
            if (ttType == TT_TYPE_EXACT) {
                assert ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE) != 0;
                assert value > VAL_MIN;
                return value;
            } else {
                if (ttType == TT_TYPE_BETA_CUT) {
                    if (value >= beta) {
                        assert value > VAL_MIN;
                        return value;
                    } else if (value > alpha) {
                        alpha = value;
                    }
                } else if (ttType == TT_TYPE_ALPHA_CUT && value <= alpha) {
                    if (value != VAL_MIN) {
                        return value;
                    } else {
                        return alpha;
                    }
                }
            }
        }

        moveGenerator.pushFrame();
        boolean hasLegalMove = false;
        int b = beta;
        int bestMove = 0;
        int bestScore = VAL_MIN;
        for (SearchStage searchStage: searchStages) {
            final int[] moves = getMoves(searchStage, board, 0, MAX_SEARCH_DEPTH - 1);

            final boolean allowToRecurseDown = (searchStage == SearchStage.CAPTURES_WINNING ||
                    searchStage == SearchStage.PROMOTION);

            for (int i = moves[0]; i > 0; i--) {
                final int move = moves[i];

                assert (move & BASE_INFO) != 0;

                // make the move
                final long undo = board.move(move);

                // check if the king remained in check
                if (board.attacksKing(1 - toMove)) {
                    board.takeBack(undo);
                    continue;
                }

                // register that we had a legal move
                hasLegalMove = true;

                if (!allowToRecurseDown) {
                    board.takeBack(undo);
                    continue;
                }

                // evaluate the move
                int a = -quiescence(board, -b, -alpha);

                if (cancelled) {
                    board.takeBack(undo);
                    moveGenerator.popFrame();
                    return alpha;
                }

                // the other player has a better option, beta cut off
                if (a >= beta) {
                    board.takeBack(undo);
                    moveGenerator.popFrame();
                    assert board.getBoard()[getFromPosition(move)] != EMPTY;
                    transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, 0, a - VAL_MIN, age);
                    addMoveToHistoryTable(board, move);
                    assert a > VAL_MIN;
                    return a;
                }
                if (a >= b) {
                    // null-window was too narrow, try a full search
                    a = -quiescence(board, -beta, -a);
                    if (cancelled) {
                        board.takeBack(undo);
                        moveGenerator.popFrame();
                        return alpha;
                    }
                    if (a >= beta) {
                        board.takeBack(undo);
                        moveGenerator.popFrame();
                        assert board.getBoard()[getFromPosition(move)] != EMPTY;
                        transpositionTable.set(zobristKey, TT_TYPE_BETA_CUT, move, 0, a - VAL_MIN, age);
                        addMoveToHistoryTable(board, move);
                        assert a > VAL_MIN;
                        return a;
                    }
                }
                board.takeBack(undo);
                if (a > bestScore) {
                    bestScore = a;
                }
                if (a > alpha) {
                    bestMove = move;
                    alpha = a;
                    addMoveToHistoryTable(board, move);
                    if (alpha > VAL_MATE_THRESHOLD) {
                        break;
                    }
                }

                b = alpha + 1;
            }
            if (hasLegalMove && alpha > VAL_MATE_THRESHOLD) {
                break;
            }
        }
        moveGenerator.popFrame();
        if (!hasLegalMove) {
            final boolean inCheck = board.attacksKing(1 - toMove);
            if (inCheck) {
                return -Evaluation.VAL_MATE;
            }
        }

        if (bestMove != 0) {
            transpositionTable.set(zobristKey, TT_TYPE_EXACT, bestMove, 0, alpha - VAL_MIN, age);
        } else {
            transpositionTable.set(zobristKey, TT_TYPE_ALPHA_CUT, 0, 0, bestScore - VAL_MIN, age);
        }
        assert alpha > VAL_MIN;
        return alpha;
    }

    private void addMoveToHistoryTable(final Board board, final int move) {
        final int fromPos = getFromPosition(move);
        final int toPos = getToPosition(move);
        final int pieceHistoryIdx = board.getBoard()[fromPos] + 7;
        final int fromPos64 = convert0x88To64(fromPos);
        final int toPos64 = convert0x88To64(toPos);
        history[7][fromPos64][toPos64]++;
        if (history[7][fromPos64][toPos64] > historyMaxGlobal) {
            historyMaxGlobal = history[7][fromPos64][toPos64];
            historyShiftGlobal = 0;
            final int leadingZerosGlobal = Integer.numberOfLeadingZeros(historyMaxGlobal);
            if (leadingZerosGlobal < 34 - MOVE_ORDER_HISTORY_MAX_BITS) {
                // normalise history counts
                historyShiftGlobal = 34 - MOVE_ORDER_HISTORY_MAX_BITS - leadingZerosGlobal;
            }
        }
        history[pieceHistoryIdx][fromPos64][toPos64]++;
        if (history[pieceHistoryIdx][fromPos64][toPos64] > historyMax) {
            historyMax = history[pieceHistoryIdx][fromPos64][toPos64];
            historyShift = 0;
            final int leadingZeros = Integer.numberOfLeadingZeros(historyMax);
            if (leadingZeros < 32 - MOVE_ORDER_HISTORY_MAX_BITS) {
                // normalise history counts
                historyShift = 32 - MOVE_ORDER_HISTORY_MAX_BITS - leadingZeros;
            }
        }
    }

    private void addMoveToKillers(final int searchedPly, final SearchStage searchStage, final int move) {
        if (searchStage == SearchStage.NORMAL && (move & MOVE_TYPE) == MT_NORMAL) {
            final int fromTo = move & FROM_TO;
            if (killerMoves[searchedPly][0] != fromTo) {
                assert (Utils.getFromPosition(fromTo) & 0x88) == 0: Integer.toHexString(move) + "/" + StringUtils.toSimple(move);
                assert (Utils.getToPosition(fromTo) & 0x88) == 0: Integer.toHexString(move) + "/" + StringUtils.toSimple(move);
                killerMoves[searchedPly][1] = killerMoves[searchedPly][0];
                killerMoves[searchedPly][0] = fromTo;
            }
        }
    }

    private void calculateNextTimeCheck() {
        final long currentTime = System.currentTimeMillis();
        if (searchEndTime <= currentTime) {
            cancelled = true;
//                System.out.printf("info string Cancelling after %d ms, search time: %d ms (node count: %d)\r\n", currentTime - searchStartTime, searchEndTime - searchStartTime, nodeCount);
            return;
        }
        long timeEllapsed = currentTime - searchStartTime;
//        System.out.printf("info string Processed %d nodes in %d ms\r\n", nodeCount, timeEllapsed);
        if (timeEllapsed < 10) {
            timeEllapsed = 10;
        }
        final long timeLeft = searchEndTime - currentTime;
        long nodesToProcessUntilNextCheck = (timeLeft * nodeCount / timeEllapsed) >>> 1;
        if (nodesToProcessUntilNextCheck < 200) {
            nodesToProcessUntilNextCheck = 200;
        }
        nodeCountAtNextTimeCheck = nodeCount + nodesToProcessUntilNextCheck;
//        System.out.printf("info string Next check at node count %d\r\n", nodeCountAtNextTimeCheck);
        lastCheckTime = currentTime;
    }

    private int[] getMoves(final SearchStage searchStage, final Board board, final int ttMove, final int searchedPly) {
        final int[] moves;
        switch (searchStage) {
            case TRANS_TABLE:
                if ((ttMove & BASE_INFO) > 0) {
                    moves = new int[2];
                    moves[0] = 1;
                    moves[1] = ttMove;
                } else {
                    moves = NO_MOVE_ARRAY;
                }
                return moves;
            case CAPTURES_WINNING:
                moveGenerator.generatePseudoLegalMoves(board);
                moves = moveGenerator.getWinningCaptures();
                break;
            case PROMOTION:
                moveGenerator.generatePseudoLegalMovesNonAttacking(board);
                moves = moveGenerator.getPromotions();
                break;
            case KILLERS:
                moves = new int[3];
                int killerCount = 0;
                for (int move: killerMoves[searchedPly]) {
                    if (isValidKillerMove(board, getFromPosition(move), getToPosition(move))) {
                        moves[++killerCount] = move;
                    }
                }
                moves[0] = killerCount;
                break;
            case NORMAL:
                moves = moveGenerator.getMoves();
                break;
            case CAPTURES_LOOSING:
                moves = moveGenerator.getLoosingCaptures();
                break;
            default:
                throw new IllegalStateException("Unknow move generation stage: " + searchStage.name());
        }
        if (moves[0] > 0) {
            addMoveValuesAndRemoveTTMove(moves, board, ttMove,
                searchStage == SearchStage.NORMAL? killerMoves[searchedPly]: NO_MOVE_ARRAY);
            Arrays.sort(moves, 1, moves[0] + 1);
        }
        return moves;
    }

    private void addMoveValuesAndRemoveTTMove(final int[] moves, final Board board, final int ttMove, final int[] killers) {
        final int toMove = board.getState() & WHITE_TO_MOVE;
        final int shiftPositionBonus = SHIFT_POSITION_BONUS[toMove];
        final int kingPos = board.getKing(1 - toMove);
        final int signum = (toMove << 1) - 1;
        final int stage = board.getStage();
        final int[] squares = board.getBoard();
        for (int i = moves[0]; i > 0; i--) {
            final int move = moves[i];
            if ((move & BASE_INFO) != ttMove && !isKiller(killers, move)) {
                final int fromPos = getFromPosition(move);
                final int toPos = getToPosition(move);
                final int fromPos64 = convert0x88To64(fromPos);
                final int toPos64 = convert0x88To64(toPos);
                final int piece = squares[fromPos];
                final int historyValue = history[piece + 7][fromPos64][toPos64] >>> historyShift;
                final int historyValueGlobal = history[7][fromPos64][toPos64] >>> historyShiftGlobal;
                final int absPiece = piece * signum;
                final int positionalGain =
                    Evaluation.computePositionalGain(absPiece, fromPos, toPos, stage, shiftPositionBonus);
                final int valPositional = ((positionalGain + 100) >> MOVE_ORDER_POSITIONAL_GAIN_SHIFT);
                final int checkBonus;
                if ((ATTACK_ARRAY[kingPos - toPos + 120] & ATTACK_BITS[absPiece]) > 0) {
                    checkBonus = MOVE_ORDER_BLOCKED_CHECK_BONUS;
                } else {
                    checkBonus = 0;
                }
                final int toRank = getRank(toPos);
                final int pawnBonus;
                if (absPiece == PAWN && (toRank == 1 || toRank == 6)) {
                    pawnBonus = MOVE_ORDER_7TH_RANK_PAWN_BONUS;
                } else {
                    pawnBonus = 0;
                }
                final int moveValue = RND_ARRAY[(RND_INDEX++) & 0xFF] +
                    ((move & MOVE_VALUE) >> SHIFT_MOVE_VALUE) + historyValue + historyValueGlobal + checkBonus + valPositional + pawnBonus;
                moves[i] = (move & ~MOVE_VALUE) | (moveValue << SHIFT_MOVE_VALUE);
                assert (moves[i] & (1 << 31)) == 0: Integer.toHexString(moves[i]); 
            } else {
                moves[i] = moves[moves[0]];
                moves[0]--;
            }
        }
    }

    private boolean isKiller(final int[] killers, final int move) {
        final int fromTo = move & FROM_TO;
        for (int killer: killers) {
            if (killer == fromTo) {
                return true;
            }
        }
        return false;
    }

    public void setSearchEndTime(final long searchEndTime) {
        this.searchEndTime = searchEndTime;
    }

    public long getNodeCount() {
        return nodeCount;
    }

    public void clear() {
        transpositionTable.clear();
        evaluation.getEvalHashTable().clear();
        evaluation.getPawnHashTable().clear();
        for (int[][] arrays: history) {
            for (int[] array: arrays) {
                Arrays.fill(array, 0);
            }
        }
        historyMax = 0;
        historyMaxGlobal = 0;
    }

    public TranspositionTable getTranspositionTable() {
        return transpositionTable;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public int[] getBestLine(final Board board) {
        return getBestLine(board, 0);
    }

    public int[] getBestLine(final Board board, final int defaultMove) {
        long zobristKey = board.getZobristKey();
        long ttValue = transpositionTable.read(zobristKey);
        final int depth = (int) ((ttValue & TT_DEPTH) >> TT_SHIFT_DEPTH);
        int move = (int) ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE);
        if (move == 0 || depth == 0 || (defaultMove != 0 && ((defaultMove & BASE_INFO) != (move & BASE_INFO)))) {
            if (defaultMove == 0) {
                throw new IllegalStateException("Failed to extract valid first move");
            } else {
                return new int[] {defaultMove};
            }
        }
        final int[] line = new int[depth];
        final long[] undos = new long[depth];
        int len = 0;
        for (int i = 0; i < (depth - 1) && move != 0; i++, len++) {
            line[i] = move;
            undos[i] = board.move(move);
            if (board.getRepetitionCount() == 3) {
                move = 0;
            } else {
                zobristKey = board.getZobristKey();
                ttValue = transpositionTable.read(zobristKey);
                final long ttType = ttValue & TT_TYPE;
                if (ttType == TT_TYPE_EXACT) {
                    move = (int) ((ttValue & TT_MOVE) >> TT_SHIFT_MOVE);
                } else {
                    move = 0;
                }
            }
        }
        if (move != 0) {
            line[len++] = move;
        }
        for (int i = len - 1; i >= 0; i--) {
            final long undo = undos[i];
            if (undo != 0) {
                board.takeBack(undo);
            }
        }
        final int[] result;
        if (len < depth) {
            result = new int[len];
            System.arraycopy(line, 0, result, 0, len);
        } else {
            result = line;
        }
        return result;
    }

    public void cancel() {
        cancelled = true;
    }

    public void setBestMoveListener(final BestMoveListener listener) {
        this.listener = listener;
    }
 
    public static int getValueFromSearchResult(final long result) {
        return (int) (result & 0xFFFFFFFFL);
    }

    public static int getMoveFromSearchResult(final long result) {
        final int move = (int) (result >> 32);
        assert (Utils.getFromPosition(move) & 0x88) == 0: Long.toHexString(result) + "/" + StringUtils.toSimple(move);
        assert (Utils.getToPosition(move) & 0x88) == 0: Long.toHexString(result) + "/" + StringUtils.toSimple(move);
        return move;
    }

    public static long getSearchResult(final int move, final int value) {
        assert (Utils.getFromPosition(move) & 0x88) == 0: Integer.toHexString(move) + "/" + StringUtils.toSimple(move);
        assert (Utils.getToPosition(move) & 0x88) == 0: Integer.toHexString(move) + "/" + StringUtils.toSimple(move);
        return (((long) (move & BASE_INFO)) << 32) | (((long) value) & 0xFFFFFFFFL);
    }

    public boolean isValidKillerMove(final Board board, final int fromPos, final int toPos) {
        final int[] squares = board.getBoard();
        final int piece = squares[fromPos];
        if (piece == EMPTY || squares[toPos] != EMPTY) {
            return false;
        }
        final int toMove = board.getState() & WHITE_TO_MOVE;
        final int signum = Integer.signum(piece);
        if (((toMove << 1) - 1) != signum) {
            // fromPos is occupied by the opponent's piece
            return false;
        }
        final int absPiece = signum * piece;
        if (SLIDING[absPiece]) {
            return board.isAttackedBySliding(toPos, ATTACK_BITS[absPiece], fromPos);
        } else if (absPiece == PAWN) {
            final int squareInFront = fromPos + signum * UP;
            final int fromRank = getRank(fromPos);
            final int toRank = getRank(toPos);
            return toRank != 0 && toRank != 7 && (toPos == squareInFront ||
                (squares[squareInFront] == EMPTY && toPos == squareInFront + signum * UP && (fromRank == 1 || fromRank == 6)));
        } else {
            return board.isAttackedByNonSliding(toPos, ATTACK_BITS[absPiece], fromPos);
        }
    }
}