package sf.pnr.base;

import junit.framework.TestCase;

import static sf.pnr.base.StringUtils.*;
import static sf.pnr.base.Utils.WHITE_TO_MOVE;

/**
 */
public class PerftTest extends TestCase {
    private int maxDepth;

    private final MoveGenerator moveGenerator = new MoveGenerator();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        maxDepth = Integer.parseInt(System.getProperty("perft.maxDepth", "100"));
    }

    public void testInitialPosition() {
        final Board board = new Board();
        board.restart();
        runPerftTest(20L, board, 1);
        runPerftTest(400L, board, 2);
        runPerftTest(8902L, board, 3);
        runPerftTest(197281L, board, 4);
        runPerftTest(4865609L, board, 5);
        runPerftTest(119060324L, board, 6);
        runPerftTest(3195901860L, board, 7);
        runPerftTest(84998978956L, board, 8);
        runPerftTest(2439530234167L, board, 9);
        runPerftTest(69352859712417L, board, 10);
    }

    public void test1() {
        final Board board = fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        runPerftTest(48, board, 1);
        runPerftTest(2039, board, 2);
        runPerftTest(97862, board, 3);
        runPerftTest(4085603, board, 4);
        runPerftTest(193690690, board, 5);
        runPerftTest(8031647685L, board, 6);
    }

    public void test2() {
        final Board board = fromFen("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 1 67");
        runPerftTest(50, board, 1);
        runPerftTest(279, board, 2);
        runPerftTest(13310, board, 3);
        runPerftTest(54703, board, 4);
        runPerftTest(2538084, board, 5);
        runPerftTest(10809689, board, 6);
    }

    public void test3() {
        final Board board = fromFen("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28");
        runPerftTest(5, board, 1);
        runPerftTest(117, board, 2);
        runPerftTest(3293, board, 3);
        runPerftTest(67197, board, 4);
        runPerftTest(1881089, board, 5);
        runPerftTest(38633283, board, 6);
    }

    public void test4() {
        final Board board = fromFen("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3");
        runPerftTest(31, board, 1);
        runPerftTest(570, board, 2);
        runPerftTest(17546, board, 3);
        runPerftTest(351806, board, 4);
        runPerftTest(11139762, board, 5);
    }

    public void test5() {
        final Board board = fromFen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1");
        runPerftTest(14, board, 1);
        runPerftTest(191, board, 2);
        runPerftTest(2812, board, 3);
        runPerftTest(43238, board, 4);
        runPerftTest(674624, board, 5);
        runPerftTest(11030083, board, 6);
        runPerftTest(178633661, board, 7);
    }

    public void testPromotion() {
        final Board board = fromFen("8/8/3p4/KPp4r/4P2k/8/1R4p1/8 b - - 1 4");
        runPerftTest(17, board, 1);
        runPerftTest(244, board, 2);
        runPerftTest(4692, board, 3);
        runPerftTest(69680, board, 4);
        runPerftTest(1429665, board, 5);
    }

    public void testPromotionWithCapture() {
        Board board = fromFen("1b3nq1/P1kp4/8/KP6/8/8/8/3R1B2 w - - 0 1");
        runPerftTest(27, board, 1);
        board = fromFen("1rb5/1P3kp1/P7/2p3K1/3b2P1/6PB/4P3/6N1 w - - 0 1");
        runPerftTest(13, board, 1);
    }

    private void runPerftTest(final long expected, final Board board, final int depth) {
        if (depth <= maxDepth) {
            assertEquals(expected, perft(board, depth));
        }
    }

    public long perft(final Board board, final int depth) {
        return perft(board, depth, moveGenerator);
    }

    public static long perft(final Board board, final int depth, final MoveGenerator moveGenerator) {
        final boolean checkConsistency = depth <= 5;
        final boolean printFen = depth <= 0;
        if (printFen) {
            System.out.println("Printing FEN");
        }
        return perft(board, depth, moveGenerator, checkConsistency, printFen);
    }

    public static long perft(final Board board, final int depth, final MoveGenerator moveGenerator,
                             final boolean checkConsistency, final boolean printFen) {
        if (depth == 0) {
            if (printFen) {
                System.out.println(StringUtils.toFen(board));
            }
            return 1;
        }
        long moveCount = 0;
        moveGenerator.pushFrame();
        moveGenerator.generatePseudoLegalMoves(board);
        moveGenerator.generatePseudoLegalMovesNonAttacking(board);
        moveCount += playMoves(board, depth, moveGenerator, moveGenerator.getWinningCaptures(), checkConsistency, printFen);
        moveCount += playMoves(board, depth, moveGenerator, moveGenerator.getLoosingCaptures(), checkConsistency, printFen);
        moveCount += playMoves(board, depth, moveGenerator, moveGenerator.getPromotions(), checkConsistency, printFen);
        moveCount += playMoves(board, depth, moveGenerator, moveGenerator.getMoves(), checkConsistency, printFen);
        moveGenerator.popFrame();
        return moveCount;
    }

    private static int playMoves(final Board board, final int depth, final MoveGenerator moveGenerator,
                                 final int[] moves, final boolean checkConsistency, final boolean printFen) {
        int moveCount = 0;
        final int len = moves[0];
        for (int i = 1; i <= len; i++) {
            int move = moves[i];
            try {
                final long zobrist = board.getZobristKey();
                final long undo = board.move(move);
                if (checkConsistency) {
                    BoardTest.checkPieceListConsistency(board);
                }
                final int state = board.getState();
                final int toMove = state & WHITE_TO_MOVE;
                if (!Engine.attacksKing(board, toMove)) {
                    moveCount += perft(board, depth - 1, moveGenerator, checkConsistency, printFen);
                }
                board.takeBack(undo);
                assertEquals(zobrist, board.getZobristKey());
                if (checkConsistency) {
                    BoardTest.checkPieceListConsistency(board);
                }
            } catch (RuntimeException e) {
                System.out.println(toSimple(move));
                throw e;
            } catch (Error e) {
                System.out.println(toSimple(move));
                throw e;
            }
        }
        return moveCount;
    }
}