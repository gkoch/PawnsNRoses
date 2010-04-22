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

    private void generatePseudoLegalMovesKing(final Board boardObj, final Frame frame) {
        final int state = boardObj.getState();
        final int toMove = state & WHITE_TO_MOVE;
        final int kingIndex = boardObj.getKing(toMove);
        final int[] board = boardObj.getBoard();
        final int signum = Integer.signum(board[kingIndex]);
        final int[] moves = frame.getMoves();
        int idx = moves[0];
        final int[] winningCaptures = frame.getWinningCaptures();
        int winningCaptureIdx = winningCaptures[0];
        final int opponent = 1 - toMove;
        for (int delta : DELTA_KING) {
            final int pos = kingIndex + delta;
            if ((pos & 0x88) == 0) {
                final int attacked = board[pos];
                if (attacked == EMPTY) {
                    if (!boardObj.isAttacked(pos, opponent)) {
                        moves[++idx] = (pos << SHIFT_TO) + kingIndex;
                    }
                } else if (signum != Integer.signum(attacked)) {
                    if (!boardObj.isAttacked(pos, opponent)) {
                        final int move = (pos << SHIFT_TO) | kingIndex |
                            (VAL_PIECES[-signum * attacked] << SHIFT_MOVE_VALUE);
                        winningCaptures[++winningCaptureIdx] = move;
                    }
                }
            }
        }
        moves[0] = idx;
        winningCaptures[0] = winningCaptureIdx;
    }

    public static void generateEnPassantMoves(final Board boardObj, final int[] captures) {
        final int state = boardObj.getState();
        final int enPassant = ((state & EN_PASSANT ) >> SHIFT_EN_PASSANT) - 1;
        final int toMove = state & WHITE_TO_MOVE;
        final int signum = (toMove << 1) - 1;
        final int pawn = signum * PAWN;
        final int delta = (toMove << 5) - 16;
        final int rankIndex = (3 + toMove) << 4;
        final int toIndex = rankIndex + enPassant + delta;
        final int[] board = boardObj.getBoard();
        final int leftIndex = rankIndex + enPassant - 1;
        if ((leftIndex & 0x88) == 0 && board[leftIndex] == pawn) {
//            final int value = VAL_PAWN;
            final int value = ATTACK_VALUE[PAWN][PAWN];
            captures[++captures[0]] = (toIndex << SHIFT_TO) | leftIndex | MT_EN_PASSANT | (value << SHIFT_MOVE_VALUE);
        }
        final int rightIndex = leftIndex + 2;
        if ((rightIndex & 0x88) == 0 && board[rightIndex] == pawn) {
//            final int value = VAL_PAWN;
            final int value = ATTACK_VALUE[PAWN][PAWN];
            captures[++captures[0]] = (toIndex << SHIFT_TO) | rightIndex | MT_EN_PASSANT | (value << SHIFT_MOVE_VALUE);
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

    public static void generatePseudoLegalMovesSliding(final Board boardObj, final int[] deltas, final int startingPos,
                                                       final Frame frame) {
        final int[] board = boardObj.getBoard();
        final int side = board[startingPos] >> 31;
        final int[] moves = frame.getMoves();
        int idx = moves[0];
        final int[] winningCaptures = frame.getWinningCaptures();
        final int[] loosingCaptures = frame.getLoosingCaptures();
        for (int delta : deltas) {
            assert delta != 0;
            for (int pos = startingPos + delta; (pos & 0x88) == 0; pos += delta) {
                final int attacked = board[pos];
                if (attacked == EMPTY) {
                    moves[++idx] = (pos << SHIFT_TO) | startingPos;
                } else if (side != (attacked >> 31)) {
                    final int value = staticExchangeEvaluation(boardObj, startingPos, pos);
                    if (value >= 0) {
                        final int move = (pos << SHIFT_TO) | startingPos | (value << SHIFT_MOVE_VALUE);
                        winningCaptures[++winningCaptures[0]] = move;
                    } else {
                        final int move = (pos << SHIFT_TO) | startingPos | ((2000 + value) << SHIFT_MOVE_VALUE);
                        loosingCaptures[++loosingCaptures[0]] = move;
                    }
//                    final int absAttacked = attacked * -signum;
//                    final int absPiece = signum * board[startingPos];
//                    final int valPiece = VAL_PIECES[absPiece];
//                    final int move = (pos << SHIFT_TO) | startingPos |
//                        (ATTACK_VALUE[absPiece][absAttacked] << SHIFT_MOVE_VALUE);
//                    if (VAL_PIECES[absAttacked] > valPiece) {
//                        winningCaptures[++winningCaptureIdx] = move;
//                    } else {
//                        loosingCaptures[++loosingCaptureIdx] = move;
//                    }
                    break;
                } else {
                    break;
                }
            }
        }
        moves[0] = idx;
    }

    public static void generatePseudoLegalMovesNonSliding(final Board boardObj, final int[] deltas, final int startingPos,
                                                          final Frame frame) {
        final int[] board = boardObj.getBoard();
		final int signum = Integer.signum(board[startingPos]);
        final int[] moves = frame.getMoves();
        int idx = moves[0];
        final int[] winningCaptures = frame.getWinningCaptures();
        int winningCaptureIdx = winningCaptures[0];
        final int[] loosingCaptures = frame.getLoosingCaptures();
        int loosingCaptureIdx = loosingCaptures[0];
        for (int delta : deltas) {
            final int pos = startingPos + delta;
            if ((pos & 0x88) == 0) {
                final int attacked = board[pos];
                if (attacked == EMPTY) {
                    moves[++idx] = (pos << SHIFT_TO) + startingPos;
                } else if (signum != Integer.signum(attacked)) {
                    final int value = staticExchangeEvaluation(boardObj, startingPos, pos);
                    if (value >= 0) {
                        final int move = (pos << SHIFT_TO) | startingPos | (value << SHIFT_MOVE_VALUE);
                        winningCaptures[++winningCaptureIdx] = move;
                    } else {
                        final int move = (pos << SHIFT_TO) | startingPos | ((2000 + value) << SHIFT_MOVE_VALUE);
                        loosingCaptures[++loosingCaptureIdx] = move;
                    }
//                    final int absAttacked = attacked * -signum;
//                    final int absPiece = signum * board[startingPos];
//                    final int valPiece = VAL_PIECES[absPiece];
//                    final int move = (pos << SHIFT_TO) | startingPos |
//                        (ATTACK_VALUE[absPiece][absAttacked] << SHIFT_MOVE_VALUE);
//                    if (VAL_PIECES[absAttacked] > valPiece) {
//                        winningCaptures[++winningCaptureIdx] = move;
//                    } else {
//                        loosingCaptures[++loosingCaptureIdx] = move;
//                    }
                }
            }
        }
		moves[0] = idx;
        winningCaptures[0] = winningCaptureIdx;
        loosingCaptures[0] = loosingCaptureIdx;
	}

    public static void generatePseudoLegalMovesPawnCapture(final Board boardObj, final int[] captures) {
        final int[] board = boardObj.getBoard();
        final int state = boardObj.getState();
        final int toMove = state & WHITE_TO_MOVE;
        final int signumOpponent = 1 - (toMove << 1);
        final int[] pawns = boardObj.getPieces(toMove, PAWN);
        final int pawnCount = pawns[0];
        assert pawnCount <= 8;
        final int[] deltas = getDeltasPawnAttack(toMove);
        int capturesIdx = captures[0];
        for (int i = 1; i <= pawnCount; i++) {
            final int fromIndex = pawns[i];
            for (int delta: deltas) {
                final int toIndex = fromIndex + delta;
                if ((toIndex & 0x88) == 0 && Integer.signum(board[toIndex]) == signumOpponent) {
                    final int move = (toIndex << SHIFT_TO) + fromIndex;
                    final int toRank = getRank(toIndex);
//                    final int captureValue = staticExchangeEvaluation(boardObj, fromIndex, toIndex);
                    final int captureValue = ATTACK_VALUE[PAWN][signumOpponent * board[toIndex]];
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

    public static void generatePseudoLegalMovesPawn(final int[] board, final int fromIndex, final int delta,
                                                    final int[] moves, final int[] promotions) {
        int idx = moves[0];
        int promotionsIdx = promotions[0];
        final int toIndex = fromIndex + delta;
        if ((toIndex & 0x88) == 0) {
            final int attacked = board[toIndex];
            if (attacked == EMPTY) {
                final int move = (toIndex << SHIFT_TO) + fromIndex;
                final int toRank = getRank(toIndex);
                if (toRank == 0 || toRank == 7) {
                    promotions[++promotionsIdx] = move | MT_PROMOTION_QUEEN | ((VAL_QUEEN - VAL_PAWN) << SHIFT_MOVE_VALUE);
                    promotions[++promotionsIdx] = move | MT_PROMOTION_ROOK | ((VAL_ROOK - VAL_PAWN) << SHIFT_MOVE_VALUE);
                    promotions[++promotionsIdx] = move | MT_PROMOTION_BISHOP | ((VAL_BISHOP - VAL_PAWN) << SHIFT_MOVE_VALUE);
                    promotions[++promotionsIdx] = move | MT_PROMOTION_KNIGHT | ((VAL_KNIGHT - VAL_PAWN) << SHIFT_MOVE_VALUE);
                } else {
                    moves[++idx] = move;
                }
                final int rank = getRank(fromIndex);
                if (rank == 1 && delta == UP || rank == 6 && delta == DN) {
                    final int enPassantIndex = toIndex + delta;
                    if (board[enPassantIndex] == EMPTY) {
                        moves[++idx] = (enPassantIndex << SHIFT_TO) + fromIndex;
                    }
                }
                moves[0] = idx;
                promotions[0] = promotionsIdx;
            }
        }
    }

    public static void generateCastling(final Board boardObj, final int[] moves) {
        final int state = boardObj.getState();
        final int toMove = state & WHITE_TO_MOVE;
        final int[] board = boardObj.getBoard();
        if (toMove == WHITE_TO_MOVE) {
            if ((state & CASTLING_WHITE_QUEENSIDE) > 0 && board[B[0]] == EMPTY && board[C[0]] == EMPTY &&
                    board[D[0]] == EMPTY) {
                if (!boardObj.isAttacked(C[0], BLACK_TO_MOVE) && !boardObj.isAttacked(D[0], BLACK_TO_MOVE) &&
                        !boardObj.isAttacked(E[0], BLACK_TO_MOVE)) {
                    moves[++moves[0]] = (C[0] << SHIFT_TO) | E[0] | MT_CASTLING_QUEENSIDE;
                }
            }
            if ((state & CASTLING_WHITE_KINGSIDE) > 0 && board[F[0]] == EMPTY && board[G[0]] == EMPTY) {
                if (!boardObj.isAttacked(E[0], BLACK_TO_MOVE) && !boardObj.isAttacked(F[0], BLACK_TO_MOVE) &&
                        !boardObj.isAttacked(G[0], BLACK_TO_MOVE)) {
                    moves[++moves[0]] = (G[0] << SHIFT_TO) | E[0] | MT_CASTLING_KINGSIDE;
                }
            }
        } else {
            if ((state & CASTLING_BLACK_QUEENSIDE) > 0 && board[B[7]] == EMPTY && board[C[7]] == EMPTY &&
                    board[D[7]] == EMPTY) {
                if (!boardObj.isAttacked(C[7], WHITE_TO_MOVE) && !boardObj.isAttacked(D[7], WHITE_TO_MOVE) &&
                        !boardObj.isAttacked(E[7], WHITE_TO_MOVE)) {
                    moves[++moves[0]] = (C[7] << SHIFT_TO) | E[7] | MT_CASTLING_QUEENSIDE;
                }
            }
            if ((state & CASTLING_BLACK_KINGSIDE) > 0 && board[F[7]] == EMPTY && board[G[7]] == EMPTY) {
                if (!boardObj.isAttacked(E[7], WHITE_TO_MOVE) && !boardObj.isAttacked(F[7], WHITE_TO_MOVE) &&
                        !boardObj.isAttacked(G[7], WHITE_TO_MOVE)) {
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

    public static int staticExchangeEvaluation(final Board boardObj, final int fromIndex, final int toIndex) {
        final int[] board = boardObj.getBoard();
        assert board[fromIndex] != EMPTY;
        assert board[toIndex] != EMPTY;
        assert board[fromIndex] * board[toIndex] < 0;
        final int piece = board[fromIndex];
        final int signum = Integer.signum(piece);
        final int absPiece = signum * piece;
        final int captured = board[toIndex];
        final int signumOpponent = -signum;
        final int absCaptured = signumOpponent * captured;
        final int[] defenders = new int[32];
        final int toMove = (signum + 1) >> 1;
        boardObj.getAttackers(toIndex, 1 - toMove, defenders);
        if (defenders[0] == 0) {
            return VAL_PIECES[absCaptured];
        }
        int[] seeList = new int[32];
        int exchangeIndex = 0;
        seeList[exchangeIndex++] = VAL_PIECES[absCaptured];
        final int[] attackers = new int[32];
        boardObj.getAttackers(toIndex, toMove, attackers);

        // remove first attacker standing on fromIndex
        final int count = attackers[0];
        if (attackers[count] != fromIndex) {
            int tmp = attackers[count];
            for (int i = count - 1; i > 0 && tmp != fromIndex; i--) {
                final int tmp2 = attackers[i];
                attackers[i] = tmp;
                tmp = tmp2;
            }
        }
        attackers[0] = count - 1;

        // populate value arrays
        final int[] valDefenders = new int[32];
        for (int i = defenders[0]; i > 0; i--) {
            assert board[defenders[i]] != EMPTY;
            valDefenders[i] = VAL_PIECES[signumOpponent * board[defenders[i]]];
        }
        valDefenders[0] = defenders[0];
        final int[] valAttackers = new int[32];
        for (int i = attackers[0]; i > 0; i--) {
            assert board[attackers[i]] != EMPTY;
            valAttackers[i] = VAL_PIECES[signum * board[attackers[i]]];
        }
        valAttackers[0] = attackers[0];

        addHiddenAttackers(
            boardObj, toIndex, attackers, valAttackers, defenders, valDefenders, signum, fromIndex, absPiece);
        int captureVal = VAL_PIECES[absPiece];
        int[] currentIdxArray = defenders;
        int[] currentValArray = valDefenders;
        int[] opponentIdxArray = attackers;
        int[] opponentValArray = valAttackers;
        int currentSignum = signumOpponent;
        int valSignum = -1;
        while (currentIdxArray[0] > 0) {
            final int currentVal = currentValArray[1];
            if (currentVal == VAL_KING && opponentValArray[0] > 0) {
                // can't capture with king as we'd move into check
                break;
            }
            final int currentIndex = currentIdxArray[1];
            final int currentAbsPiece = currentSignum * board[currentIndex];
            final int currentLen = currentIdxArray[0];

            // remove current attacker
            System.arraycopy(currentIdxArray, 2, currentIdxArray, 1, currentLen - 1);
            System.arraycopy(currentValArray, 2, currentValArray, 1, currentLen - 1);
            currentIdxArray[0] = currentLen - 1;
            currentValArray[0] = currentLen - 1;

            // add discovered attacker
            addHiddenAttackers(boardObj, toIndex, currentIdxArray, currentValArray, opponentIdxArray, opponentValArray,
                currentSignum, currentIndex, currentAbsPiece);

            seeList[exchangeIndex] = captureVal - seeList[exchangeIndex - 1];
            exchangeIndex++;

            // swap roles
            int[] tmp = currentIdxArray;
            currentIdxArray = opponentIdxArray;
            opponentIdxArray = tmp;
            tmp = currentValArray;
            currentValArray = opponentValArray;
            opponentValArray = tmp;
            currentSignum = -currentSignum;
            valSignum = -valSignum;
            captureVal = currentVal;
        }

        for (exchangeIndex--; exchangeIndex > 0; exchangeIndex--) {
            seeList[exchangeIndex - 1] = Math.min(-seeList[exchangeIndex], seeList[exchangeIndex - 1]);
        }
        return seeList[0];
    }

    private static void addHiddenAttackers(final Board boardObj, final int toIndex,
                                           final int[] currentIdxArray, final int[] currentValArray,
                                           final int[] opponentIdxArray, final int[] opponentValArray,
                                           final int currentSignum, final int currentIndex, final int currentAbsPiece) {
        if (currentAbsPiece != KNIGHT) {
            final int[] board = boardObj.getBoard();
            // from toIndex to currentIndex
            final int attackValue = ATTACK_ARRAY[currentIndex - toIndex + 120];
            assert currentAbsPiece == PAWN || (attackValue & ATTACK_BITS[currentAbsPiece]) > 0;
            final int delta = ((attackValue & ATTACK_DELTA) >> SHIFT_ATTACK_DELTA) - 64;
            int foundIndex = currentIndex + delta;
            while ((foundIndex & 0x88) == 0 && board[foundIndex] == EMPTY) {
                foundIndex += delta;
            }
            if ((foundIndex & 0x88) == 0) {
                // found a piece
                final int foundPiece = board[foundIndex];
                final int foundSignum = Integer.signum(foundPiece);
                final int foundAbsPiece = foundSignum * foundPiece;
                if (!boardObj.isSliding(foundAbsPiece) || (attackValue & ATTACK_BITS[foundAbsPiece]) == 0) {
                    // this piece cannot attack the target index
                    // (sliding pieces so it's OK not to recompute the attackValue)
                    return;
                }
                final int foundVal = VAL_PIECES[foundSignum * foundPiece];
                final int[] insertIdxArray;
                final int[] insertValArray;
                if (foundSignum == currentSignum) {
                    // friend
                    insertIdxArray = currentIdxArray;
                    insertValArray = currentValArray;
                } else {
                    // foe
                    insertIdxArray = opponentIdxArray;
                    insertValArray = opponentValArray;
                }
                final int insertArrLen = insertValArray[0];
                for (int i = insertArrLen; i > 0; i--) {
                    final int tmpVal = insertValArray[i];
                    if (tmpVal <= foundVal) {
                        insertValArray[i + 1] = foundVal;
                        insertIdxArray[i + 1] = foundIndex;
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
                    insertIdxArray[1] = foundIndex;
                }
            }
        }
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