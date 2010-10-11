package sf.pnr.base;

import static sf.pnr.base.Utils.*;

/**
 */
public class StringUtils {
    // helpers
    public static final char[] FILE = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
    public static final char[] FEN_CHARS = new char[6 * 2];
    public static final int[] CODE_TO_TYPE = new int[20];
    public static final int[] CODE_TO_PROMOTION_TYPE = new int[20];

    static {
		FEN_CHARS[(KING * 2) - 2] = 'K';
		FEN_CHARS[(KING * 2) - 1] = 'k';
		FEN_CHARS[(QUEEN * 2) - 2] = 'Q';
		FEN_CHARS[(QUEEN * 2) - 1] = 'q';
		FEN_CHARS[(ROOK * 2) - 2] = 'R';
		FEN_CHARS[(ROOK * 2) - 1] = 'r';
		FEN_CHARS[(BISHOP * 2) - 2] = 'B';
		FEN_CHARS[(BISHOP * 2) - 1] = 'b';
		FEN_CHARS[(KNIGHT * 2) - 2] = 'N';
		FEN_CHARS[(KNIGHT * 2) - 1] = 'n';
		FEN_CHARS[(PAWN * 2) - 2] = 'P';
		FEN_CHARS[(PAWN * 2) - 1] = 'p';

        CODE_TO_TYPE['B' - 'A'] = BISHOP;
        CODE_TO_TYPE['K' - 'A'] = KING;
        CODE_TO_TYPE['N' - 'A'] = KNIGHT;
        CODE_TO_TYPE['Q' - 'A'] = QUEEN;
        CODE_TO_TYPE['R' - 'A'] = ROOK;

        CODE_TO_PROMOTION_TYPE['B' - 'A'] = MT_PROMOTION_BISHOP;
        CODE_TO_PROMOTION_TYPE['N' - 'A'] = MT_PROMOTION_KNIGHT;
        CODE_TO_PROMOTION_TYPE['Q' - 'A'] = MT_PROMOTION_QUEEN;
        CODE_TO_PROMOTION_TYPE['R' - 'A'] = MT_PROMOTION_ROOK;
    }

    public static String toFen(final Board board) {
        final int[] pieces = board.getBoard();
        final int state = board.getState();
        final StringBuilder builder = new StringBuilder(80);
        int empty = 0;
        for (int idx = 112; idx >= 0; idx++) {
            final int current = pieces[idx];
            if ((idx & 0x88) > 0) {
                if (empty > 0) {
                    builder.append(empty);
                    empty = 0;
                }
                if (idx > 16) {
                    builder.append('/');
                }
                idx -= 25;
            } else if (current == EMPTY) {
                empty++;
            } else {
                if (empty > 0) {
                    builder.append(empty);
                    empty = 0;
                }
                final char ch;
                if (current < 0) {
                    ch = FEN_CHARS[2 * (-current) - 1];
                } else {
                    ch = FEN_CHARS[2 * (current - 1)];
                }
                builder.append(ch);
            }
        }
        builder.append(' ');
        appendState(state, builder);

        return builder.toString();
    }

    public static Board fromFen(final String fen) {
        final Board boardObj = new Board();
        boardObj.clear();
        final int[] board = boardObj.getBoard();

        int i = 0;
        for (int idx = 112; idx >= 0; idx++, i++) {
            final char ch = fen.charAt(i);
            switch (ch) {
                case 'K':
                    board[idx] = KING;
                    break;
                case 'Q':
                    board[idx] = QUEEN;
                    break;
                case 'R':
                    board[idx] = ROOK;
                    break;
                case 'B':
                    board[idx] = BISHOP;
                    break;
                case 'N':
                    board[idx] = KNIGHT;
                    break;
                case 'P':
                    board[idx] = PAWN;
                    break;
                case 'k':
                    board[idx] = -KING;
                    break;
                case 'q':
                    board[idx] = -QUEEN;
                    break;
                case 'r':
                    board[idx] = -ROOK;
                    break;
                case 'b':
                    board[idx] = -BISHOP;
                    break;
                case 'n':
                    board[idx] = -KNIGHT;
                    break;
                case 'p':
                    board[idx] = -PAWN;
                    break;
                case ' ':
                case '/':
                    idx -= 25;
                    break;
                default:
                    idx += (ch - '1');
            }
            if ("KQRBNPkqrbnp".indexOf(ch) >= 0) {
                final int side;
                final int absPiece;
                if (board[idx] > 0) {
                    side = WHITE_TO_MOVE;
                    absPiece = board[idx];
                } else {
                    side = BLACK_TO_MOVE;
                    absPiece = -board[idx];
                }
                final int[] pieces = boardObj.getPieces(side, absPiece);
                pieces[0]++;
                pieces[pieces[0]] = idx;
                boardObj.getPieceArrayPositions()[idx] = pieces[0];
            }
        }

        int state = 0;
        if (fen.charAt(i++) == 'w') {
            state |= WHITE_TO_MOVE;
        }
        i++;

        for (char ch = fen.charAt(i++); ch != ' '; ch = fen.charAt(i++)) {
            switch (ch) {
                case 'K':
                    state |= CASTLING_WHITE_KINGSIDE;
                    break;
                case 'Q':
                    state |= CASTLING_WHITE_QUEENSIDE;
                    break;
                case 'k':
                    state |= CASTLING_BLACK_KINGSIDE;
                    break;
                case 'q':
                    state |= CASTLING_BLACK_QUEENSIDE;
                    break;
            }
        }

        char ch = fen.charAt(i++);
        if (ch != '-') {
            state |= (ch - 'a' + 1) << SHIFT_EN_PASSANT;
            i++;
        }
        i++;

        final String[] moves = fen.substring(i).split(" ");
        state |= Integer.parseInt(moves[0]) << SHIFT_HALF_MOVES;
        state |= Integer.parseInt(moves[1]) << SHIFT_FULL_MOVES;

        boardObj.setState(state);
        boardObj.recompute();
        return boardObj;
    }

    public static String toString(final Board board) {
        final int[] pieces = board.getBoard();
        final int state = board.getState();
        final StringBuilder builder = new StringBuilder(80);
        for (int i = 112; i >= 0; i++) {
            if ((i & 0x88) > 0) {
                builder.append("\r\n");
                i -= 25;
            } else {
                if ((i & 0x0F) != 0) {
                    builder.append(' ');
                }
                final int current = pieces[i];
                if (current == EMPTY) {
                    builder.append('0');
                } else {
                    final char ch;
                    if (current < 0) {
                        ch = FEN_CHARS[2 * (-current) - 1];
                    } else {
                        ch = FEN_CHARS[2 * (current - 1)];
                    }
                    builder.append(ch);
                }
            }
        }
        builder.append("\r\nState: ");
        appendState(state, builder);

        return builder.toString();
    }

    public static void appendState(final int state, final StringBuilder builder) {
        final boolean whiteToMove = (state & WHITE_TO_MOVE) > 0;
        builder.append(whiteToMove ? 'w' : 'b');
        builder.append(' ');
        if ((state & CASTLING_WHITE_KINGSIDE) > 0) {
            builder.append('K');
        }
        if ((state & CASTLING_WHITE_QUEENSIDE) > 0) {
            builder.append('Q');
        }
        if ((state & CASTLING_BLACK_KINGSIDE) > 0) {
            builder.append('k');
        }
        if ((state & CASTLING_BLACK_QUEENSIDE) > 0) {
            builder.append('q');
        }
        if ((state & CASTLING_ALL) == 0) {
            builder.append('-');
        }
        builder.append(' ');
        final int enPassant = state & EN_PASSANT;
        if (enPassant > 0) {
            builder.append(FILE[(enPassant >> SHIFT_EN_PASSANT) - 1]);
            builder.append(whiteToMove ? '6' : '3');
        } else {
            builder.append('-');
        }
        builder.append(' ');
        builder.append((state & HALF_MOVES) >> SHIFT_HALF_MOVES);
        builder.append(' ');
        builder.append((state & FULL_MOVES) >> SHIFT_FULL_MOVES);
    }

    public static int[] fromSimpleList(final String moveListStr) {
        final String[] parts = moveListStr.split(",");
        final int[] moves = new int[parts.length];
        for (int i = 0, partsLength = parts.length; i < partsLength; i++) {
            moves[i] = fromSimple(parts[i].trim());
        }
        return moves;
    }

    public static int fromSimple(final String moveStr) {
        final char fromFile = moveStr.charAt(0);
        final char fromRank = moveStr.charAt(1);
        final char toFile = moveStr.charAt(2);
        final char toRank = moveStr.charAt(3);
        int move = (getIndex(toFile - 'a', toRank - '1') << SHIFT_TO) + getIndex(fromFile - 'a', fromRank - '1');
        if (moveStr.length() > 4) {
            final String suffix = moveStr.substring(4).trim();
            if ("e.p.".equals(suffix)) {
                move |= MT_EN_PASSANT;
            } else if ("N".equals(suffix)) {
                move |= MT_PROMOTION_KNIGHT;
            } else if ("B".equals(suffix)) {
                move |= MT_PROMOTION_BISHOP;
            } else if ("R".equals(suffix)) {
                move |= MT_PROMOTION_ROOK;
            } else if ("Q".equals(suffix)) {
                move |= MT_PROMOTION_QUEEN;
            } else {
                throw new IllegalStateException("Unexpected suffix: " + suffix);
            }
        }
        return move;
    }

    public static String toSimple(final int[] moves) {
        final StringBuilder builder = new StringBuilder(moves.length * 6);
        for (int move: moves) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(toSimple(move));
        }
        return builder.toString();
    }

    public static String toSimple(final int move) {
        final int fromIndex = move & FROM;
        final int toIndex = (move & TO) >> SHIFT_TO;
        final StringBuilder builder = new StringBuilder();
        builder.append((char) ('a' + getFile(fromIndex)));
        builder.append((char) ('1' + getRank(fromIndex)));
        builder.append((char) ('a' + getFile(toIndex)));
        builder.append((char) ('1' + getRank(toIndex)));
        final int moveType = move & MOVE_TYPE;
        switch (moveType) {
            case MT_EN_PASSANT:
                builder.append(" e.p.");
                break;
            case MT_PROMOTION_KNIGHT:
                builder.append("N");
                break;
            case MT_PROMOTION_BISHOP:
                builder.append("B");
                break;
            case MT_PROMOTION_ROOK:
                builder.append("R");
                break;
            case MT_PROMOTION_QUEEN:
                builder.append("Q");
                break;
        }
        return builder.toString();
    }
    public static int fromLong(final Board board, final String moveStr) {
        final char fromFile = moveStr.charAt(0);
        final char fromRank = moveStr.charAt(1);
        final char toFile = moveStr.charAt(2);
        final char toRank = moveStr.charAt(3);
        final int fromIndex = getIndex(fromFile - 'a', fromRank - '1');
        final int toIndex = getIndex(toFile - 'a', toRank - '1');
        int move = (toIndex << SHIFT_TO) + fromIndex;
        if (moveStr.length() > 4) {
            final String suffix = moveStr.substring(4).trim();
            if ("n".equals(suffix)) {
                move |= MT_PROMOTION_KNIGHT;
            } else if ("b".equals(suffix)) {
                move |= MT_PROMOTION_BISHOP;
            } else if ("r".equals(suffix)) {
                move |= MT_PROMOTION_ROOK;
            } else if ("q".equals(suffix)) {
                move |= MT_PROMOTION_QUEEN;
            } else {
                throw new IllegalStateException("Unexpected suffix: " + suffix);
            }
        } else {
            final int[] boardArray = board.getBoard();
            final int absPiece = Math.abs(boardArray[fromIndex]);
            if (absPiece == PAWN && boardArray[toIndex] == EMPTY && ((fromIndex - toIndex) & 0x0F) > 0) {
                move |= MT_EN_PASSANT;
            } else if (absPiece == KING && (fromFile - toFile == 2)) {
                move |= MT_CASTLING_QUEENSIDE;
            } else if (absPiece == KING && (fromFile - toFile == -2)) {
                move |= MT_CASTLING_KINGSIDE;
            }
        }
        return move;
    }

    public static String toLong(final int[] moves, final String separator) {
        final StringBuilder builder = new StringBuilder(moves.length * 6);
        for (int move: moves) {
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(toLong(move));
        }
        return builder.toString();
    }

    public static String toLong(final int move) {
        final int fromIndex = move & FROM;
        final int toIndex = (move & TO) >> SHIFT_TO;
        final StringBuilder builder = new StringBuilder();
        builder.append((char) ('a' + getFile(fromIndex)));
        builder.append((char) ('1' + getRank(fromIndex)));
        builder.append((char) ('a' + getFile(toIndex)));
        builder.append((char) ('1' + getRank(toIndex)));
        final int moveType = move & MOVE_TYPE;
        switch (moveType) {
            case MT_PROMOTION_KNIGHT:
                builder.append("n");
                break;
            case MT_PROMOTION_BISHOP:
                builder.append("b");
                break;
            case MT_PROMOTION_ROOK:
                builder.append("r");
                break;
            case MT_PROMOTION_QUEEN:
                builder.append("q");
                break;
        }
        return builder.toString();
    }

    public static String toShort(final Board boardObj, final int move) {
        final StringBuilder builder = new StringBuilder();
        final int toMove = boardObj.getState() & WHITE_TO_MOVE;
        final int moveType = move & MOVE_TYPE;
        if (moveType == MT_CASTLING_KINGSIDE) {
            builder.append("O-O");
        } else if (moveType == MT_CASTLING_QUEENSIDE) {
            builder.append("O-O-O");
        } else {
            final int fromIndex = getMoveFromIndex(move);
            final int toIndex = getMoveToIndex(move);
            final int[] board = boardObj.getBoard();
            final int piece = board[fromIndex];
            assert piece != EMPTY: StringUtils.toFen(boardObj) + "/" + StringUtils.toSimple(move);
            final int absPiece = Integer.signum(piece) * piece;
            if (absPiece != PAWN) {
                builder.append(FEN_CHARS[absPiece * 2 - 2]);
                final int fromFile = getFile(fromIndex);
                final int fromRank = getRank(fromIndex);
                boolean needsExtraInfo = false;
                boolean needsFromFile = false;
                boolean needsFromRank = false;
                final int[] pieces = boardObj.getPieces(toMove, absPiece);
                if (absPiece == KNIGHT) {
                    for (int i = pieces[0]; i > 0; i--) {
                        final int index = pieces[i];
                        if (index != fromIndex) {
                            if ((ATTACK_ARRAY[toIndex - index + 120] & ATTACK_N) == ATTACK_N) {
                                needsExtraInfo = true;
                                needsFromRank |= getFile(index) == fromFile;
                                needsFromFile |= getRank(index) == fromRank;
                            }
                        }
                    }
                } else if (absPiece != KING) {
                    final int attackBits = ATTACK_BITS[absPiece];
                    for (int i = pieces[0]; i > 0; i--) {
                        final int index = pieces[i];
                        if (index != fromIndex) {
                            if (boardObj.isAttackedBySliding(toIndex, attackBits, index)) {
                                needsExtraInfo = true;
                                needsFromRank |= getFile(index) == fromFile;
                                needsFromFile |= getRank(index) == fromRank;
                            }
                        }
                    }
                }
                if (needsExtraInfo && (needsFromFile || !needsFromRank)) {
                    builder.append((char) ('a' + fromFile));
                }
                if (needsFromRank) {
                    builder.append((char) ('1' + fromRank));
                }
            }

            assert fromIndex != toIndex;
            final boolean capture = (moveType == MT_EN_PASSANT) || (board[toIndex] != EMPTY);
            if (capture) {
                if (absPiece == PAWN) {
                    builder.append((char) ('a' + getFile(fromIndex)));
                }
                builder.append('x');
            }
            builder.append((char) ('a' + getFile(toIndex)));
            builder.append((char) ('1' + getRank(toIndex)));
            switch (moveType) {
                case MT_PROMOTION_KNIGHT:
                    builder.append("=N");
                    break;
                case MT_PROMOTION_BISHOP:
                    builder.append("=B");
                    break;
                case MT_PROMOTION_ROOK:
                    builder.append("=R");
                    break;
                case MT_PROMOTION_QUEEN:
                    builder.append("=Q");
                    break;
            }
        }

        // check if it is a check or mate
        final long undo = boardObj.move(move);
        final int kingIndex = boardObj.getKing(1 - toMove);
        if (boardObj.isAttacked(kingIndex, toMove)) {
            final MoveGenerator moveGenerator = new MoveGenerator();
            moveGenerator.pushFrame();
            moveGenerator.generatePseudoLegalMoves(boardObj);
            boolean hasLegalMove = boardObj.hasLegalMove(moveGenerator.getWinningCaptures()) ||
                boardObj.hasLegalMove(moveGenerator.getLoosingCaptures());
            if (!hasLegalMove) {
                moveGenerator.generatePseudoLegalMovesNonAttacking(boardObj);
                hasLegalMove = boardObj.hasLegalMove(moveGenerator.getMoves()) ||
                    boardObj.hasLegalMove(moveGenerator.getPromotions());
            }
            if (hasLegalMove) {
                builder.append('+');
            } else {
                builder.append('#');
            }
        }
        boardObj.takeBack(undo);
        return builder.toString();
    }

    public static int fromShort(final Board board, final String moveStr) {
        final int state = board.getState();
        final int whiteToMove = state & WHITE_TO_MOVE;
        if (moveStr.startsWith("0-0-0") || moveStr.startsWith("O-O-O")) {
            if (whiteToMove > 0) {
                return (C[0] << SHIFT_TO) | E[0] | MT_CASTLING_QUEENSIDE;
            } else {
                return (C[7] << SHIFT_TO) | E[7] | MT_CASTLING_QUEENSIDE;
            }
        } else if (moveStr.startsWith("0-0") || moveStr.startsWith("O-O")) {
            if (whiteToMove > 0) {
                return (G[0] << SHIFT_TO) | E[0] | MT_CASTLING_KINGSIDE;
            } else {
                return (G[7] << SHIFT_TO) | E[7] | MT_CASTLING_KINGSIDE;
            }
        }

        int fromFile = -1;
        int fromRank = -1;
        int toFile = -1;
        int toRank = -1;
        int pieceType = PAWN;
        int moveType = MT_NORMAL;
        boolean capture = false;
        for (int i = 0; i < moveStr.length(); i++) {
            final char ch = moveStr.charAt(i);
            if (ch >= 'B' && ch <= 'R') {
                pieceType = CODE_TO_TYPE[ch - 'A'];
                assert pieceType != 0;
            } else if (ch >= 'a' && ch <= 'h') {
                if (toFile >= 0) {
                    fromFile = toFile;
                }
                toFile = ch - 'a';
            } else if (ch >= '1' && ch <= '8') {
                if (toRank >= 0) {
                    fromRank = toRank;
                }
                toRank = ch - '1';
            } else if (ch == 'x') {
                capture = true;
            } else if (ch == '=') {
                final char promotedTo = moveStr.charAt(++i);
                moveType |= CODE_TO_PROMOTION_TYPE[promotedTo - 'A'];
            } else if (ch == '+' || ch == '#') {
                // ignore
            } else {
                throw new IllegalStateException(String.format("Unexpected character '%s' in %s", ch, moveStr));
            }
        }
        assert toFile != -1;
        assert toRank != -1;

        final int toIndex = getIndex(toFile, toRank);
        final int[] pieces = board.getPieces(whiteToMove, pieceType);
        if (pieceType == PAWN) {
            final int signum = (whiteToMove << 1) - 1;
            final int[] squares = board.getBoard();
            for (int i = 1; i <= pieces[0]; i++) {
                final int piecePos = pieces[i];
                final int pieceFile = getFile(piecePos);
                final int pieceRank = getRank(piecePos);
                if (fromFile >= 0 && pieceFile != fromFile) {
                    continue;
                }
                if (fromRank >= 0 && pieceRank != fromRank) {
                    continue;
                }
                final int delta = signum * UP;
                final int squareInFront = piecePos + delta;
                if (toIndex == squareInFront ||
                    (squares[squareInFront] == EMPTY && toIndex == squareInFront + delta && (pieceRank == 1 || pieceRank == 6))) {
                    fromFile = pieceFile;
                    fromRank = pieceRank;
                    break;
                } else if (toIndex == squareInFront - 1 || toIndex == squareInFront + 1) {
                    if (squares[toIndex] != EMPTY) {
                        fromFile = pieceFile;
                        fromRank = pieceRank;
                        break;
                    } else if (((state & EN_PASSANT) >> SHIFT_EN_PASSANT) - 1 == toFile &&
                            toRank == 3 + whiteToMove * 3) {
                        fromFile = pieceFile;
                        fromRank = pieceRank;
                        moveType = MT_EN_PASSANT;
                        break;
                    }
                }
            }
        } else {
            for (int i = 1; i <= pieces[0]; i++) {
                final int piecePos = pieces[i];
                final int pieceFile = getFile(piecePos);
                final int pieceRank = getRank(piecePos);
                if (fromFile >= 0 && pieceFile != fromFile) {
                    continue;
                }
                if (fromRank >= 0 && pieceRank != fromRank) {
                    continue;
                }
                if (board.isSliding(pieceType)) {
                    if (board.isAttackedBySliding(toIndex, ATTACK_BITS[pieceType], piecePos)) {
                        fromFile = pieceFile;
                        fromRank = pieceRank;
                        break;
                    }
                } else {
                    if (board.isAttackedByNonSliding(toIndex, ATTACK_BITS[pieceType], piecePos)) {
                        fromFile = pieceFile;
                        fromRank = pieceRank;
                        break;
                    }
                }
            }
        }
        return toIndex << SHIFT_TO | getIndex(fromFile, fromRank) | moveType;
    }

    public static String toString0x88(final int pos) {
        final StringBuilder builder = new StringBuilder();
        builder.append((char) ('a' + getFile(pos)));
        builder.append((char) ('1' + getRank(pos)));
        return builder.toString();
    }

    public static int fromString0x88(final String posStr) {
        final char file = posStr.charAt(0);
        final char rank = posStr.charAt(1);
        return getIndex(file - 'a', rank - '1');
    }

    public static boolean containsString(final String[] strings, final String str) {
        for (String string: strings) {
            if (str.equals(string)) {
                return true;
            }
        }
        return false;
    }

    public static String createPgnEntry(final String key, final String value) {
        return String.format("[%s \"%s\"]\r\n", key, value);
    }

    public static Board fromPgn(final String pgn) {
        final Board board = new Board();
        board.restart();
        boolean inHeader = false;
        boolean inQuotes = false;
        final StringBuilder builder = new StringBuilder(5);
        for (int i = 0; i < pgn.length(); i++) {
            final char ch = pgn.charAt(i);
            if (ch == '"') {
                if (inHeader) {
                    inQuotes = !inQuotes;
                } else {
                    throw new IllegalStateException("Quote outside PGN header: " + pgn);
                }
            } else if (inQuotes) {
                // ignore
            } else if (ch == ']') {
                inHeader = false;
            } else if (inHeader) {
                // ignore
            } else if (ch == '[') {
                inHeader = true;
            } else if (Character.isWhitespace(ch)) {
                if (builder.length() > 0) {
                    board.move(fromShort(board, builder.toString()));
                    builder.delete(0, builder.length());
                }
            } else if (builder.length() == 0 && (Character.isDigit(ch) || ch == '.' || ch == '-' || ch == '/')) {
                // ignore
            } else {
                builder.append(ch);
            }
        }
        if (builder.length() > 0) {
            board.move(fromShort(board, builder.toString()));
        }
        return board;
    }
}