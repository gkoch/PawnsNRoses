package sf.pnr.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static sf.pnr.base.Utils.*;

/**
 */
public class StringUtils {
    // helpers
    public static final char[] FILE = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
    public static final char[] FEN_CHARS = new char[6 * 2];
    public static final String[] PIECES = new String[7];
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

        PIECES[KING] = "king";
        PIECES[QUEEN] = "queen";
        PIECES[ROOK] = "rook";
        PIECES[BISHOP] = "bishop";
        PIECES[KNIGHT] = "knight";
        PIECES[PAWN] = "pawn";

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
        final Board board = new Board();
        board.clear();
        final int[] squares = board.getBoard();

        int i = 0;
        for (int idx = 112; idx >= 0; idx++, i++) {
            final char ch = fen.charAt(i);
            switch (ch) {
                case 'K':
                    squares[idx] = KING;
                    break;
                case 'Q':
                    squares[idx] = QUEEN;
                    break;
                case 'R':
                    squares[idx] = ROOK;
                    break;
                case 'B':
                    squares[idx] = BISHOP;
                    break;
                case 'N':
                    squares[idx] = KNIGHT;
                    break;
                case 'P':
                    squares[idx] = PAWN;
                    break;
                case 'k':
                    squares[idx] = -KING;
                    break;
                case 'q':
                    squares[idx] = -QUEEN;
                    break;
                case 'r':
                    squares[idx] = -ROOK;
                    break;
                case 'b':
                    squares[idx] = -BISHOP;
                    break;
                case 'n':
                    squares[idx] = -KNIGHT;
                    break;
                case 'p':
                    squares[idx] = -PAWN;
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
                if (squares[idx] > 0) {
                    side = WHITE;
                    absPiece = squares[idx];
                } else {
                    side = BLACK;
                    absPiece = -squares[idx];
                }
                if (absPiece == KING) {
                    board.setKing(side, idx);
                    board.getPieceArrayPositions()[idx] = 1;
                } else {
                    final int[] pieces = board.getPieces(side, absPiece);
                    pieces[0]++;
                    pieces[pieces[0]] = idx;
                    board.getPieceArrayPositions()[idx] = pieces[0];
                }
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

        board.setState(state);
        board.recompute();
        return board;
    }

    public static String mirrorFen(final String fen) {
        final String[] parts = fen.split(" ");
        final StringBuilder mirrored = new StringBuilder(fen.length());
        // position
        final String position = parts[0];
        final String[] ranks = position.split("/");
        for (int i = ranks.length - 1; i >= 0; i--) {
            addCaseSwapped(mirrored, ranks[i]);
            if (i > 0) {
                mirrored.append("/");
            }
        }
        mirrored.append(' ');
        // who is next
        final String next = parts[1];
        if (next.equals("w")) {
            mirrored.append('b');
        } else {
            assert next.equals("b");
            mirrored.append('w');
        }
        mirrored.append(' ');
        addCaseSwapped(mirrored, parts[2]);
        for (int i = 3; i < parts.length; i++) {
            mirrored.append(' ');
            mirrored.append(parts[i]);
        }
        return mirrored.toString();
    }

    private static void addCaseSwapped(final StringBuilder builder, final String str) {
        for (int j = 0; j < str.length(); j++) {
            final char ch = str.charAt(j);
            final char transformed;
            if (Character.isUpperCase(ch)) {
                transformed = Character.toLowerCase(ch);
            } else if (Character.isLowerCase(ch)) {
                transformed = Character.toUpperCase(ch);
            } else {
                transformed = ch;
            }
            builder.append(transformed);
        }
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
        int move = (getPosition(toFile - 'a', toRank - '1') << SHIFT_TO) + getPosition(fromFile - 'a', fromRank - '1');
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
        final int fromPos = move & FROM;
        final int toPos = (move & TO) >> SHIFT_TO;
        final StringBuilder builder = new StringBuilder();
        builder.append((char) ('a' + getFile(fromPos)));
        builder.append((char) ('1' + getRank(fromPos)));
        builder.append((char) ('a' + getFile(toPos)));
        builder.append((char) ('1' + getRank(toPos)));
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
        final int fromPos = getPosition(fromFile - 'a', fromRank - '1');
        final int toPos = getPosition(toFile - 'a', toRank - '1');
        int move = (toPos << SHIFT_TO) + fromPos;
        if (moveStr.length() > 4) {
            final String suffix = moveStr.substring(4).trim().toLowerCase();
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
            final int absPiece = Math.abs(boardArray[fromPos]);
            if (absPiece == PAWN && boardArray[toPos] == EMPTY && ((fromPos - toPos) & 0x0F) > 0) {
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
        final int fromPos = move & FROM;
        final int toPos = (move & TO) >> SHIFT_TO;
        final StringBuilder builder = new StringBuilder();
        builder.append((char) ('a' + getFile(fromPos)));
        builder.append((char) ('1' + getRank(fromPos)));
        builder.append((char) ('a' + getFile(toPos)));
        builder.append((char) ('1' + getRank(toPos)));
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

    public static String toShort(final Board board, final int move) {
        final StringBuilder builder = new StringBuilder();
        final int toMove = board.getState() & WHITE_TO_MOVE;
        final int moveType = move & MOVE_TYPE;
        if (moveType == MT_CASTLING_KINGSIDE) {
            builder.append("O-O");
        } else if (moveType == MT_CASTLING_QUEENSIDE) {
            builder.append("O-O-O");
        } else {
            final int fromPos = getFromPosition(move);
            final int toPos = getToPosition(move);
            final int[] squares = board.getBoard();
            final int piece = squares[fromPos];
            assert piece != EMPTY: StringUtils.toFen(board) + "/" + StringUtils.toSimple(move);
            final int absPiece = Integer.signum(piece) * piece;
            if (absPiece != PAWN) {
                builder.append(FEN_CHARS[absPiece * 2 - 2]);
                final int fromFile = getFile(fromPos);
                final int fromRank = getRank(fromPos);
                boolean needsExtraInfo = false;
                boolean needsFromFile = false;
                boolean needsFromRank = false;
                if (absPiece != KING) {
                    final int[] pieces = board.getPieces(toMove, absPiece);
                    if (absPiece == KNIGHT) {
                        for (int i = pieces[0]; i > 0; i--) {
                            final int pos = pieces[i];
                            if (pos != fromPos) {
                                if ((ATTACK_ARRAY[toPos - pos + 120] & ATTACK_N) == ATTACK_N) {
                                    needsExtraInfo = true;
                                    needsFromRank |= getFile(pos) == fromFile;
                                    needsFromFile |= getRank(pos) == fromRank;
                                }
                            }
                        }
                    } else {
                        final int attackBits = ATTACK_BITS[absPiece];
                        for (int i = pieces[0]; i > 0; i--) {
                            final int pos = pieces[i];
                            if (pos != fromPos) {
                                if (isAttackedBySliding(board, toPos, attackBits, pos)) {
                                    needsExtraInfo = true;
                                    needsFromRank |= getFile(pos) == fromFile;
                                    needsFromFile |= getRank(pos) == fromRank;
                                }
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

            assert fromPos != toPos;
            final boolean capture = (moveType == MT_EN_PASSANT) || (squares[toPos] != EMPTY);
            if (capture) {
                if (absPiece == PAWN) {
                    builder.append((char) ('a' + getFile(fromPos)));
                }
                builder.append('x');
            }
            builder.append((char) ('a' + getFile(toPos)));
            builder.append((char) ('1' + getRank(toPos)));
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
        final long undo = board.move(move);
        final int kingPos = board.getKing(1 - toMove);
        if (board.isAttacked(kingPos, toMove)) {
            final MoveGenerator moveGenerator = new MoveGenerator();
            moveGenerator.pushFrame();
            moveGenerator.generatePseudoLegalMoves(board);
            boolean hasLegalMove = board.hasLegalMove(moveGenerator.getCaptures());
            if (!hasLegalMove) {
                moveGenerator.generatePseudoLegalMovesNonAttacking(board);
                hasLegalMove = board.hasLegalMove(moveGenerator.getMoves()) ||
                    board.hasLegalMove(moveGenerator.getPromotions());
            }
            if (hasLegalMove) {
                builder.append('+');
            } else {
                builder.append('#');
            }
        }
        board.takeBack(undo);
        return builder.toString();
    }

    public static int fromShort(final Board board, final String moveStr) {
        final int state = board.getState();
        final int toMove = state & WHITE_TO_MOVE;
        if (moveStr.startsWith("0-0-0") || moveStr.startsWith("O-O-O")) {
            if (toMove > 0) {
                return (C[0] << SHIFT_TO) | E[0] | MT_CASTLING_QUEENSIDE;
            } else {
                return (C[7] << SHIFT_TO) | E[7] | MT_CASTLING_QUEENSIDE;
            }
        } else if (moveStr.startsWith("0-0") || moveStr.startsWith("O-O")) {
            if (toMove > 0) {
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

        final int toPos = getPosition(toFile, toRank);
        if (pieceType == KING) {
            final int king = board.getKing(toMove);
            fromFile = getFile(king);
            fromRank = getRank(king);
        } else {
            final int[] pieces = board.getPieces(toMove, pieceType);
            if (pieceType == PAWN) {
                final int signum = (toMove << 1) - 1;
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
                    if (toPos == squareInFront ||
                        (squares[squareInFront] == EMPTY && toPos == squareInFront + delta && (pieceRank == 1 || pieceRank == 6))) {
                        fromFile = pieceFile;
                        fromRank = pieceRank;
                        break;
                    } else if (toPos == squareInFront - 1 || toPos == squareInFront + 1) {
                        if (squares[toPos] != EMPTY) {
                            fromFile = pieceFile;
                            fromRank = pieceRank;
                            break;
                        } else if (((state & EN_PASSANT) >> SHIFT_EN_PASSANT) - 1 == toFile &&
                                toRank == 2 + toMove * 3) {
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
                    if (SLIDING[pieceType]) {
                        if (isAttackedBySliding(board, toPos, ATTACK_BITS[pieceType], piecePos) &&
                                !isPinned(board, toPos << SHIFT_TO | getPosition(pieceFile, pieceRank) | moveType)) {
                            fromFile = pieceFile;
                            fromRank = pieceRank;
                            break;
                        }
                    } else {
                        if (board.isAttackedByNonSliding(toPos, ATTACK_BITS[pieceType], piecePos) &&
                                !isPinned(board, toPos << SHIFT_TO | getPosition(pieceFile, pieceRank) | moveType)) {
                            fromFile = pieceFile;
                            fromRank = pieceRank;
                            break;
                        }
                    }
                }
            }
        }
        return toPos << SHIFT_TO | getPosition(fromFile, fromRank) | moveType;
    }

    private static boolean isPinned(final Board board, final int move) {
        final long undo = board.move(move);
        final boolean result = board.attacksKing(board.getState() & WHITE_TO_MOVE);
        board.takeBack(undo);
        return  result;
    }

    public static String toString0x88(final int position) {
        final StringBuilder builder = new StringBuilder();
        builder.append((char) ('a' + getFile(position)));
        builder.append((char) ('1' + getRank(position)));
        return builder.toString();
    }

    public static String toString64(long bitboard) {
        final StringBuilder builder = new StringBuilder();
        final StringBuilder rowBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            rowBuilder.delete(0, rowBuilder.length());
            for (int j = 0; j < 8; j++) {
                rowBuilder.append((bitboard & 0x01L) == 0? '.': '1');
                bitboard >>>= 1;
            }
            rowBuilder.append("\r\n");
            builder.insert(0, rowBuilder);
        }
        return builder.toString();
    }

    public static int fromString0x88(final String posStr) {
        final char file = posStr.charAt(0);
        final char rank = posStr.charAt(1);
        return getPosition(file - 'a', rank - '1');
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
        final int[] moves = fromPgnToMoves(pgn, null);
        for (int move: moves) {
            board.move(move);
        }
        return board;
    }

    public static int[] fromPgnToMoves(final String pgn, final Map<String, String> properties) {
        Board board = null;
        boolean inHeader = false;
        boolean inQuotes = false;
        final StringBuilder builder = new StringBuilder(5);
        final List<Integer> moveList = new ArrayList<Integer>(150);
        final StringBuilder propertyName = new StringBuilder();
        final StringBuilder propertyValue = new StringBuilder();
        StringBuilder propertyBuilder = propertyName;
        for (int i = 0; i < pgn.length(); i++) {
            final char ch = pgn.charAt(i);
            if (ch == '"') {
                if (inHeader) {
                    propertyBuilder = propertyValue;
                    propertyBuilder.append(ch);
                    inQuotes = !inQuotes;
                } else {
                    throw new IllegalStateException("Quote outside PGN header: " + pgn);
                }
            } else if (inQuotes) {
                propertyBuilder.append(ch);
                // ignore
            } else if (ch == ']') {
                if (properties != null) {
                    if (propertyValue.charAt(0) == '"' && propertyValue.charAt(propertyValue.length() - 1) == '"') {
                        propertyValue.delete(0, 1);
                        propertyValue.delete(propertyValue.length() - 1, propertyValue.length());
                    }
                    properties.put(propertyName.toString().trim(), propertyValue.toString());
                }
                propertyName.delete(0, propertyName.length());
                propertyValue.delete(0, propertyValue.length());
                inHeader = false;
            } else if (inHeader) {
                propertyBuilder.append(ch);
            } else if (ch == '[') {
                propertyBuilder = propertyName;
                inHeader = true;
            } else if (Character.isWhitespace(ch)) {
                if (builder.length() > 0) {
                    if (moveList.isEmpty()) {
                        if (properties != null && properties.containsKey("FEN")) {
                            board = StringUtils.fromFen(properties.get("FEN"));
                        } else {
                            board = new Board();
                            board.restart();
                        }
                    }
                    final int move;
                    try {
                        move = fromShort(board, builder.toString());
                    } catch (RuntimeException e) {
                        System.out.printf("Failed to parse move #%d: %s (current FEN: %s)\r\n",
                            moveList.size(), builder.toString(), StringUtils.toFen(board));
                        throw e;
                    }
                    moveList.add(move);
                    board.move(move);
                    builder.delete(0, builder.length());
                }
            } else if (builder.length() == 0 && (Character.isDigit(ch) || ch == '.' || ch == '-' || ch == '/' || ch == '*')) {
                // ignore
            } else {
                builder.append(ch);
            }
        }
        if (builder.length() > 0) {
            if (moveList.isEmpty()) {
                if (properties != null && properties.containsKey("FEN")) {
                    board = StringUtils.fromFen(properties.get("FEN"));
                } else {
                    board = new Board();
                    board.restart();
                }
            }
            final int move = fromShort(board, builder.toString());
            moveList.add(move);
        }
        final int[] moves = new int[moveList.size()];
        for (int i = 0; i < moves.length; i++){
            moves[i] = moveList.get(i);
        }
        return moves;
    }

    public static String trimTailing(final String str) {
        for (int i = str.length() - 1; i >= 0; i--) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return str.substring(0, i + 1);
            }
        }
        return "";
    }

    public static boolean isAttackedBySliding(final Board board, final int targetPos, final int attackBits, final int piecePos) {
        final int attackValue = ATTACK_ARRAY[(targetPos - piecePos + 120)];
        return (attackValue & attackBits) > 0 && board.isAttackedBySlidingInSight(targetPos, piecePos, attackValue);
    }

    public static String prettyPrint(final ColumnFormatter... formatters) {
        String padding = " ";
        String horizontalSeparator = "|";
        String verticalSeparator = "-";
        int rowCount = 0;
        boolean printTitle = false;
        for (ColumnFormatter formatter : formatters) {
            rowCount = Math.max(rowCount, formatter.getRowCount());
            printTitle |= formatter.hasTitle();
        }

        final StringBuilder builder = new StringBuilder(formatters.length * formatters[0].getRowCount() * 10);
        if (printTitle) {
            boolean firstCol = true;
            for (ColumnFormatter formatter : formatters) {
                if (!firstCol) {
                    builder.append(padding).append(horizontalSeparator).append(padding);
                } else {
                    firstCol = false;
                }
                builder.append(formatter.getTitle());
            }
            final int width = builder.length();
            builder.append("\r\n");
            for (int i = 0; i < width / verticalSeparator.length(); i++) {
                builder.append(verticalSeparator);
            }
            builder.append("\r\n");
        }

        for (int i = 0; i < rowCount; i++) {
            boolean firstCol = true;
            for (ColumnFormatter formatter : formatters) {
                if (!firstCol) {
                    builder.append(padding).append(horizontalSeparator).append(padding);
                } else {
                    firstCol = false;
                }
                builder.append(formatter.format(i));
            }
            builder.append("\r\n");
        }

        return builder.toString();
    }

    public static ColumnFormatter formatter(final String title, final String[] values) {
        return new StringColumnFormatter(title, values);
    }

    public static ColumnFormatter formatter(final String title, final int[] values) {
        return new IntColumnFormatter(title, values);
    }

    public static ColumnFormatter formatter(final String title, final double[] values, final int precision) {
        return new DoubleColumnFormatter(title, values, precision);
    }

    public static interface ColumnFormatter {
        public boolean hasTitle();
        public String getTitle();
        public String format(final int index);
        public int getRowCount();
    }

    public static class StringColumnFormatter implements ColumnFormatter {
        private final String title;
        private final String[] values;
        private int width;

        public StringColumnFormatter(final String title, final String[] values) {
            this.title = title;
            this.values = values;
            width = title == null? 0: title.length();
            if (values != null && values.length > 0) {
                for (String value : values) {
                    width = Math.max(width, value.length());
                }
            }
        }

        @Override
        public boolean hasTitle() {
            return title != null;
        }

        @Override
        public String getTitle() {
            final String titleStr = title == null? "": title;
            return String.format("%" + width + "s", titleStr);
        }

        @Override
        public String format(final int index) {
            return index < values.length? String.format("%" + width + "s", values[index]):
                String.format("%" + width + "s", "");
        }

        @Override
        public int getRowCount() {
            return values.length;
        }
    }

    public static class IntColumnFormatter implements ColumnFormatter {
        private final String title;
        private final int[] values;
        private int width;
        private int min;
        private int max;

        public IntColumnFormatter(final String title, final int[] values) {
            this.title = title;
            this.values = values;
            width = title == null? 0: title.length();
            if (values != null && values.length > 0) {
                min = values[0];
                max = values[0];
                for (int value : values) {
                    if (value < min) {
                        min = value;
                        width = Math.max(width, Integer.toString(value).length());
                    } else if (value > max) {
                        max = value;
                        width = Math.max(width, Integer.toString(value).length());
                    }
                }
            }
        }

        @Override
        public boolean hasTitle() {
            return title != null;
        }

        @Override
        public String getTitle() {
            final String titleStr = title == null? "": title;
            return String.format("%" + width + "s", titleStr);
        }

        @Override
        public String format(final int index) {
            return index < values.length? String.format("%" + width + "d", values[index]):
                String.format("%" + width + "s", "");
        }

        @Override
        public int getRowCount() {
            return values.length;
        }
    }

    public static class DoubleColumnFormatter implements ColumnFormatter {
        private final String title;
        private final double[] values;
        private final int precision;
        private int width;
        private double min;
        private double max;

        public DoubleColumnFormatter(final String title, final double[] values, final int precision) {
            this.title = title;
            this.values = values;
            this.precision = precision;
            width = title == null? 0: title.length();
            if (values != null && values.length > 0) {
                min = values[0];
                max = values[0];
                for (double value : values) {
                    if (value < min) {
                        min = value;
                        width = Math.max(width, String.format("%." + precision + "f", value).length());
                    } else if (value > max) {
                        max = value;
                        width = Math.max(width, String.format("%." + precision + "f", value).length());
                    }
                }
            }
        }

        @Override
        public boolean hasTitle() {
            return title != null;
        }

        @Override
        public String getTitle() {
            final String titleStr = title == null? "": title;
            return String.format("%" + width + "s", titleStr);
        }

        @Override
        public String format(final int index) {
            return index < values.length? String.format("%" + width + "." + precision + "f", values[index]):
                String.format("%" + width + "s", "");
        }

        @Override
        public int getRowCount() {
            return values.length;
        }
    }
}