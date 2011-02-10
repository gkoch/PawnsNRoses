package sf.pnr.base;

import sf.pnr.alg.ReinitStack;
import sf.pnr.alg.Reinitialiseable;

import static sf.pnr.base.Evaluation.*;
import static sf.pnr.base.Utils.*;

/**
 */
public class MoveGenerator {
    private static final int MAX_MOVE_COUNT = 200;
    private static final int MAX_CAPTURE_COUNT = 100;
    private static final int MAX_PROMOTION_COUNT = 32;

    private final ReinitStack<Frame> frames = new ReinitStack<Frame>(Frame.class, MAX_SEARCH_DEPTH);
    private final int[] seeDefenders;
    private final int[] seeList;
    private final int[] seeAttackers;

    public MoveGenerator() {
        seeDefenders = new int[32];
        seeList = new int[32];
        seeAttackers = new int[32];
    }

    public void generatePseudoLegalMoves(final Board board) {
        final Frame frame = frames.peek();
        final int[] moves = frame.getMoves();
		moves[0] = 0;
		final int[] winningCaptures = frame.getWinningCaptures();
		final int[] loosingCaptures = frame.getLoosingCaptures();
		winningCaptures[0] = 0;
		loosingCaptures[0] = 0;
		generatePseudoLegalMoves(board, QUEEN, frame);
		generatePseudoLegalMoves(board, ROOK, frame);
		generatePseudoLegalMoves(board, BISHOP, frame);
		generatePseudoLegalMoves(board, KNIGHT, frame);
        generatePseudoLegalMovesKing(board, frame);
        generatePseudoLegalMovesPawnCapture(board, winningCaptures);
        final int state = board.getState();
        final int enPassant = state & EN_PASSANT;
        if (enPassant != 0) {
            generateEnPassantMoves(board, winningCaptures);
        }
	}

    private void generatePseudoLegalMovesKing(final Board board, final Frame frame) {
        final int state = board.getState();
        final int toMove = state & WHITE_TO_MOVE;
        final int kingPos = board.getKing(toMove);
        final int[] squares = board.getBoard();
        final int signum = Integer.signum(squares[kingPos]);
        final int[] moves = frame.getMoves();
        int idx = moves[0];
        final int[] winningCaptures = frame.getWinningCaptures();
        int winningCaptureIdx = winningCaptures[0];
        final int opponent = 1 - toMove;
        for (int delta : DELTA_KING) {
            final int pos = kingPos + delta;
            if ((pos & 0x88) == 0) {
                final int attacked = squares[pos];
                if (attacked == EMPTY) {
                    if (!board.isAttacked(pos, opponent)) {
                        moves[++idx] = (pos << SHIFT_TO) + kingPos;
                    }
                } else if (signum != Integer.signum(attacked)) {
                    if (!board.isAttacked(pos, opponent)) {
                        final int move = (pos << SHIFT_TO) | kingPos |
                            (VAL_PIECES[-signum * attacked] << SHIFT_MOVE_VALUE);
                        winningCaptures[++winningCaptureIdx] = move;
                    }
                }
            }
        }
        moves[0] = idx;
        winningCaptures[0] = winningCaptureIdx;
    }

    public static void generateEnPassantMoves(final Board board, final int[] captures) {
        final int state = board.getState();
        final int enPassant = ((state & EN_PASSANT ) >> SHIFT_EN_PASSANT) - 1;
        final int toMove = state & WHITE_TO_MOVE;
        final int signum = (toMove << 1) - 1;
        final int pawn = signum * PAWN;
        final int delta = (toMove << 5) - 16;
        final int rankStartPos = (3 + toMove) << 4;
        final int toPos = rankStartPos + enPassant + delta;
        final int[] squares = board.getBoard();
        final int leftPos = rankStartPos + enPassant - 1;
        if ((leftPos & 0x88) == 0 && squares[leftPos] == pawn) {
            final int value = VAL_PAWN;
            captures[++captures[0]] = (toPos << SHIFT_TO) | leftPos | MT_EN_PASSANT | (value << SHIFT_MOVE_VALUE);
        }
        final int rightPos = leftPos + 2;
        if ((rightPos & 0x88) == 0 && squares[rightPos] == pawn) {
            final int value = VAL_PAWN;
            captures[++captures[0]] = (toPos << SHIFT_TO) | rightPos | MT_EN_PASSANT | (value << SHIFT_MOVE_VALUE);
        }
    }

    public void generatePseudoLegalMovesNonAttacking(final Board board) {
        final Frame frame = frames.peek();
        final int[] moves = frame.getMoves();
        final int[] promotions = frame.getPromotions();
        promotions[0] = 0;
        generatePseudoLegalMovesPawn(board, moves, promotions);
        generateCastling(board, moves);
    }

    public void generatePseudoLegalMoves(final Board board, final int absPiece, final Frame frame) {
		final int state = board.getState();
		final int toMove = state & WHITE_TO_MOVE;
		final int[] pieces = board.getPieces(toMove, absPiece);
		final int count = pieces[0];
        final int[] deltas = DELTA[absPiece];
		final boolean sliding = isSliding(absPiece);
		if (sliding) {
			for (int i = 1; i <= count; i++) {
				generatePseudoLegalMovesSliding(board, deltas, pieces[i], frame);
			}
		} else {
			for (int i = 1; i <= count; i++) {
				generatePseudoLegalMovesNonSliding(board, deltas, pieces[i], frame);
			}
		}
	}

    public void generatePseudoLegalMovesSliding(final Board board, final int[] deltas, final int startingPos,
                                                final Frame frame) {
        final int[] squares = board.getBoard();
        final int side = squares[startingPos] >>> 31;
        final int[] moves = frame.getMoves();
        int idx = moves[0];
        final int[] winningCaptures = frame.getWinningCaptures();
        final int[] loosingCaptures = frame.getLoosingCaptures();
        for (int delta : deltas) {
            assert delta != 0;
            for (int pos = startingPos + delta; (pos & 0x88) == 0; pos += delta) {
                final int attacked = squares[pos];
                if (attacked == EMPTY) {
                    moves[++idx] = (pos << SHIFT_TO) | startingPos;
                } else if (side != (attacked >>> 31)) {
                    final int value = staticExchangeEvaluation(board, startingPos, pos);
                    if (value >= 0) {
                        final int move = (pos << SHIFT_TO) | startingPos | (value << SHIFT_MOVE_VALUE);
                        winningCaptures[++winningCaptures[0]] = move;
                    } else {
                        final int move = (pos << SHIFT_TO) | startingPos | ((2000 + value) << SHIFT_MOVE_VALUE);
                        loosingCaptures[++loosingCaptures[0]] = move;
                    }
                    break;
                } else {
                    break;
                }
            }
        }
        moves[0] = idx;
    }

    public void generatePseudoLegalMovesNonSliding(final Board board, final int[] deltas, final int startingPos,
                                                   final Frame frame) {
        final int[] squares = board.getBoard();
		final int signum = Integer.signum(squares[startingPos]);
        final int[] moves = frame.getMoves();
        int idx = moves[0];
        final int[] winningCaptures = frame.getWinningCaptures();
        int winningCaptureIdx = winningCaptures[0];
        final int[] loosingCaptures = frame.getLoosingCaptures();
        int loosingCaptureIdx = loosingCaptures[0];
        for (int delta : deltas) {
            final int pos = startingPos + delta;
            if ((pos & 0x88) == 0) {
                final int attacked = squares[pos];
                if (attacked == EMPTY) {
                    moves[++idx] = (pos << SHIFT_TO) + startingPos;
                } else if (signum != Integer.signum(attacked)) {
                    final int value = staticExchangeEvaluation(board, startingPos, pos);
                    if (value >= 0) {
                        final int move = (pos << SHIFT_TO) | startingPos | (value << SHIFT_MOVE_VALUE);
                        winningCaptures[++winningCaptureIdx] = move;
                    } else {
                        final int move = (pos << SHIFT_TO) | startingPos | ((2000 + value) << SHIFT_MOVE_VALUE);
                        loosingCaptures[++loosingCaptureIdx] = move;
                    }
                }
            }
        }
		moves[0] = idx;
        winningCaptures[0] = winningCaptureIdx;
        loosingCaptures[0] = loosingCaptureIdx;
	}

    public void generatePseudoLegalMovesPawnCapture(final Board board, final int[] captures) {
        final int[] squares = board.getBoard();
        final int state = board.getState();
        final int toMove = state & WHITE_TO_MOVE;
        final int signumOpponent = 1 - (toMove << 1);
        final int[] pawns = board.getPieces(toMove, PAWN);
        final int pawnCount = pawns[0];
        assert pawnCount <= 8;
        final int[] deltas = DELTA_PAWN_ATTACK[toMove];
        int capturesIdx = captures[0];
        for (int i = 1; i <= pawnCount; i++) {
            final int fromPos = pawns[i];
            for (int delta: deltas) {
                final int toPos = fromPos + delta;
                if ((toPos & 0x88) == 0 && Integer.signum(squares[toPos]) == signumOpponent) {
                    final int move = (toPos << SHIFT_TO) + fromPos;
                    final int toRank = getRank(toPos);
                    final int captureValue = staticExchangeEvaluation(board, fromPos, toPos);
                    if (toRank == 0 || toRank == 7) {
                        captures[++capturesIdx] = move | MT_PROMOTION_QUEEN |
                            ((VAL_QUEEN - VAL_PAWN + captureValue) << SHIFT_MOVE_VALUE);
                        captures[++capturesIdx] = move | MT_PROMOTION_ROOK |
                            ((VAL_ROOK - VAL_PAWN + captureValue) << SHIFT_MOVE_VALUE);
                        captures[++capturesIdx] = move | MT_PROMOTION_BISHOP |
                            ((VAL_BISHOP - VAL_PAWN + captureValue) << SHIFT_MOVE_VALUE);
                        captures[++capturesIdx] = move | MT_PROMOTION_KNIGHT |
                            ((VAL_KNIGHT - VAL_PAWN + captureValue) << SHIFT_MOVE_VALUE);
                    } else {
                        captures[++capturesIdx] = move | (captureValue << SHIFT_MOVE_VALUE);
                    }
                }
            }
        }
        captures[0] = capturesIdx;
    }

    public static void generatePseudoLegalMovesPawn(final Board board, final int[] moves, final int[] promotions) {
		final int[] boardArray = board.getBoard();
		final int state = board.getState();
		final int toMove = state & WHITE_TO_MOVE;
        final int delta = (toMove << 5) - 16;
		final int[] pieces = board.getPieces(toMove, PAWN);
		final int count = pieces[0];
		for (int i = 1; i <= count; i++) {
			generatePseudoLegalMovesPawn(boardArray, pieces[i], delta, moves, promotions);
		}
	}

    public static void generatePseudoLegalMovesPawn(final int[] board, final int fromPos, final int delta,
                                                    final int[] moves, final int[] promotions) {
        int idx = moves[0];
        int promotionsIdx = promotions[0];
        final int toPos = fromPos + delta;
        if ((toPos & 0x88) == 0) {
            final int attacked = board[toPos];
            if (attacked == EMPTY) {
                final int move = (toPos << SHIFT_TO) + fromPos;
                final int toRank = getRank(toPos);
                if (toRank == 0 || toRank == 7) {
                    promotions[++promotionsIdx] = move | MT_PROMOTION_QUEEN | ((VAL_QUEEN - VAL_PAWN) << SHIFT_MOVE_VALUE);
                    promotions[++promotionsIdx] = move | MT_PROMOTION_ROOK | ((VAL_ROOK - VAL_PAWN) << SHIFT_MOVE_VALUE);
                    promotions[++promotionsIdx] = move | MT_PROMOTION_BISHOP | ((VAL_BISHOP - VAL_PAWN) << SHIFT_MOVE_VALUE);
                    promotions[++promotionsIdx] = move | MT_PROMOTION_KNIGHT | ((VAL_KNIGHT - VAL_PAWN) << SHIFT_MOVE_VALUE);
                } else {
                    moves[++idx] = move;
                }
                final int rank = getRank(fromPos);
                if (rank == 1 && delta == UP || rank == 6 && delta == DN) {
                    final int enPassantPos = toPos + delta;
                    if (board[enPassantPos] == EMPTY) {
                        moves[++idx] = (enPassantPos << SHIFT_TO) + fromPos;
                    }
                }
                moves[0] = idx;
                promotions[0] = promotionsIdx;
            }
        }
    }

    public static void generateCastling(final Board board, final int[] moves) {
        final int state = board.getState();
        final int toMove = state & WHITE_TO_MOVE;
        final int[] squares = board.getBoard();
        if (toMove == WHITE_TO_MOVE) {
            if ((state & CASTLING_WHITE_QUEENSIDE) > 0 && squares[B[0]] == EMPTY && squares[C[0]] == EMPTY &&
                    squares[D[0]] == EMPTY) {
                if (!board.isAttacked(C[0], BLACK_TO_MOVE) && !board.isAttacked(D[0], BLACK_TO_MOVE) &&
                        !board.isAttacked(E[0], BLACK_TO_MOVE)) {
                    moves[++moves[0]] = (C[0] << SHIFT_TO) | E[0] | MT_CASTLING_QUEENSIDE;
                }
            }
            if ((state & CASTLING_WHITE_KINGSIDE) > 0 && squares[F[0]] == EMPTY && squares[G[0]] == EMPTY) {
                if (!board.isAttacked(E[0], BLACK_TO_MOVE) && !board.isAttacked(F[0], BLACK_TO_MOVE) &&
                        !board.isAttacked(G[0], BLACK_TO_MOVE)) {
                    moves[++moves[0]] = (G[0] << SHIFT_TO) | E[0] | MT_CASTLING_KINGSIDE;
                }
            }
        } else {
            if ((state & CASTLING_BLACK_QUEENSIDE) > 0 && squares[B[7]] == EMPTY && squares[C[7]] == EMPTY &&
                    squares[D[7]] == EMPTY) {
                if (!board.isAttacked(C[7], WHITE_TO_MOVE) && !board.isAttacked(D[7], WHITE_TO_MOVE) &&
                        !board.isAttacked(E[7], WHITE_TO_MOVE)) {
                    moves[++moves[0]] = (C[7] << SHIFT_TO) | E[7] | MT_CASTLING_QUEENSIDE;
                }
            }
            if ((state & CASTLING_BLACK_KINGSIDE) > 0 && squares[F[7]] == EMPTY && squares[G[7]] == EMPTY) {
                if (!board.isAttacked(E[7], WHITE_TO_MOVE) && !board.isAttacked(F[7], WHITE_TO_MOVE) &&
                        !board.isAttacked(G[7], WHITE_TO_MOVE)) {
                    moves[++moves[0]] = (G[7] << SHIFT_TO) | E[7] | MT_CASTLING_KINGSIDE;
                }
            }
        }
    }

    public void pushFrame() {
        frames.push();
    }

    public void popFrame() {
        frames.pop();
    }

    public int[] getMoves() {
        final Frame frame = frames.peek();
        return frame.getMoves();
	}

    public int[] getWinningCaptures() {
        final Frame frame = frames.peek();
        return frame.getWinningCaptures();
	}

    public int[] getLoosingCaptures() {
        final Frame frame = frames.peek();
        return frame.getLoosingCaptures();
	}

    public int[] getPromotions() {
        final Frame frame = frames.peek();
        return frame.getPromotions();
	}

    public int staticExchangeEvaluation(final Board board, final int fromPos, final int toPos) {
        final int[] squares = board.getBoard();
        assert squares[fromPos] != EMPTY;
        assert squares[toPos] != EMPTY;
        assert squares[fromPos] * squares[toPos] < 0;
        final int piece = squares[fromPos];
        final int signum = Integer.signum(piece);
        final int absPiece = signum * piece;
        final int captured = squares[toPos];
        final int signumOpponent = -signum;
        final int absCaptured = signumOpponent * captured;
        final int toMove = (signum + 1) >> 1;
        board.getAttackers(toPos, 1 - toMove, seeDefenders);
        if (seeDefenders[0] == 0) {
            return VAL_PIECES[absCaptured];
        }
        int exchangePos = 0;
        seeList[exchangePos++] = VAL_PIECES[absCaptured];
        board.getAttackers(toPos, toMove, seeAttackers);

        // remove first attacker standing on fromPos
        final int count = seeAttackers[0];
        if (seeAttackers[count] != fromPos) {
            int tmp = seeAttackers[count];
            for (int i = count - 1; i > 0 && tmp != fromPos; i--) {
                final int tmp2 = seeAttackers[i];
                seeAttackers[i] = tmp;
                tmp = tmp2;
            }
        }
        seeAttackers[0] = count - 1;

        // populate value arrays
        final int[] valDefenders = new int[32];
        for (int i = seeDefenders[0]; i > 0; i--) {
            assert squares[seeDefenders[i]] != EMPTY;
            valDefenders[i] = VAL_PIECES[signumOpponent * squares[seeDefenders[i]]];
        }
        valDefenders[0] = seeDefenders[0];
        final int[] valAttackers = new int[32];
        for (int i = seeAttackers[0]; i > 0; i--) {
            assert squares[seeAttackers[i]] != EMPTY;
            valAttackers[i] = VAL_PIECES[signum * squares[seeAttackers[i]]];
        }
        valAttackers[0] = seeAttackers[0];

        addHiddenAttackers(
            board, toPos, seeAttackers, valAttackers, seeDefenders, valDefenders, signum, fromPos, absPiece);
        int captureVal = VAL_PIECES[absPiece];
        int[] currentPosArray = seeDefenders;
        int[] currentValArray = valDefenders;
        int[] opponentPosArray = seeAttackers;
        int[] opponentValArray = valAttackers;
        int currentSignum = signumOpponent;
        int valSignum = -1;
        while (currentPosArray[0] > 0) {
            final int currentVal = currentValArray[1];
            if (currentVal == VAL_KING && opponentValArray[0] > 0) {
                // can't capture with king as we'd move into check
                break;
            }
            final int currentPos = currentPosArray[1];
            final int currentAbsPiece = currentSignum * squares[currentPos];
            final int currentLen = currentPosArray[0];

            // remove current attacker
            System.arraycopy(currentPosArray, 2, currentPosArray, 1, currentLen - 1);
            System.arraycopy(currentValArray, 2, currentValArray, 1, currentLen - 1);
            currentPosArray[0] = currentLen - 1;
            currentValArray[0] = currentLen - 1;

            // add discovered attacker
            addHiddenAttackers(board, toPos, currentPosArray, currentValArray, opponentPosArray, opponentValArray,
                currentSignum, currentPos, currentAbsPiece);

            seeList[exchangePos] = captureVal - seeList[exchangePos - 1];
            exchangePos++;

            // swap roles
            int[] tmp = currentPosArray;
            currentPosArray = opponentPosArray;
            opponentPosArray = tmp;
            tmp = currentValArray;
            currentValArray = opponentValArray;
            opponentValArray = tmp;
            currentSignum = -currentSignum;
            valSignum = -valSignum;
            captureVal = currentVal;
        }

        for (exchangePos--; exchangePos > 0; exchangePos--) {
            seeList[exchangePos - 1] = Math.min(-seeList[exchangePos], seeList[exchangePos - 1]);
        }
        return seeList[0];
    }

    private static void addHiddenAttackers(final Board board, final int toPos,
                                           final int[] currentPosArray, final int[] currentValArray,
                                           final int[] opponentPosArray, final int[] opponentValArray,
                                           final int currentSignum, final int currentPos, final int currentAbsPiece) {
        if (currentAbsPiece != KNIGHT) {
            final int[] squares = board.getBoard();
            // from toPos to currentPos
            final int attackValue = ATTACK_ARRAY[currentPos - toPos + 120];
            assert currentAbsPiece == PAWN || (attackValue & ATTACK_BITS[currentAbsPiece]) > 0;
            final int delta = ((attackValue & ATTACK_DELTA) >> SHIFT_ATTACK_DELTA) - 64;
            int foundPos = currentPos + delta;
            while ((foundPos & 0x88) == 0 && squares[foundPos] == EMPTY) {
                foundPos += delta;
            }
            if ((foundPos & 0x88) == 0) {
                // found a piece
                final int foundPiece = squares[foundPos];
                final int foundSignum = Integer.signum(foundPiece);
                final int foundAbsPiece = foundSignum * foundPiece;
                if (!board.isSliding(foundAbsPiece) || (attackValue & ATTACK_BITS[foundAbsPiece]) == 0) {
                    // this piece cannot attack the target position
                    // (sliding pieces so it's OK not to recompute the attackValue)
                    return;
                }
                final int foundVal = VAL_PIECES[foundSignum * foundPiece];
                final int[] insertIdxArray;
                final int[] insertValArray;
                if (foundSignum == currentSignum) {
                    // friend
                    insertIdxArray = currentPosArray;
                    insertValArray = currentValArray;
                } else {
                    // foe
                    insertIdxArray = opponentPosArray;
                    insertValArray = opponentValArray;
                }
                final int insertArrLen = insertValArray[0];
                for (int i = insertArrLen; i > 0; i--) {
                    final int tmpVal = insertValArray[i];
                    if (tmpVal <= foundVal) {
                        insertValArray[i + 1] = foundVal;
                        insertIdxArray[i + 1] = foundPos;
                        break;
                    }
                    insertValArray[i + 1] = insertValArray[i];
                    insertIdxArray[i + 1] = insertIdxArray[i];
                }
                insertValArray[0] = insertArrLen + 1;
                insertIdxArray[0] = insertArrLen + 1;
                if (insertValArray[0] == 1 || insertValArray[1] > foundVal) {
                    // we haven't inserted it yet
                    insertValArray[1] = foundVal;
                    insertIdxArray[1] = foundPos;
                }
            }
        }
    }

    public static boolean isCapture(final Board board, final int move) {
        final int toPos = getToPosition(move);
        final int[] boardArray = board.getBoard();
        final int attacked = boardArray[toPos];
        if (attacked != EMPTY) {
            return true;
        }
        final int fromPos = getFromPosition(move);
        final int piece = boardArray[fromPos];
        final int signum = Integer.signum(piece);
        return piece * signum == PAWN && getFile(fromPos) != getFile(toPos);
    }

    public static boolean isPromotion(final Board board, final int move) {
        final int fromPos = getFromPosition(move);
        final int piece = board.getBoard()[fromPos];
        if (piece == PAWN || piece == -PAWN) {
            final int rank = getRank(getToPosition(move));
            return rank == 0 || rank == 7;
        }
        return false;
    }

    public static final class Frame implements Reinitialiseable {
        private final int[] moves;
        private final int[] winningCaptures;
        private final int[] loosingCaptures;
        private final int[] promotions;

        public Frame() {
            moves = new int[MAX_MOVE_COUNT];
            winningCaptures = new int[MAX_CAPTURE_COUNT];
            loosingCaptures = new int[MAX_CAPTURE_COUNT];
            promotions = new int[MAX_PROMOTION_COUNT];
        }

        public int[] getMoves() {
            return moves;
        }

        public int[] getWinningCaptures() {
            return winningCaptures;
        }

        public int[] getLoosingCaptures() {
            return loosingCaptures;
        }

        public int[] getPromotions() {
            return promotions;
        }

        @Override
        public void reinitialise() {
            moves[0] = 0;
            winningCaptures[0] = 0;
            loosingCaptures[0] = 0;
        }
    }
}