package sf.pnr.base;

import junit.framework.TestCase;
import sf.pnr.alg.TranspositionTable;

import static sf.pnr.base.Engine.*;
import static sf.pnr.base.Evaluation.*;
import static sf.pnr.base.StringUtils.*;
import static sf.pnr.base.Utils.*;

/**
 */
public class EngineTest extends TestCase {

    private Engine engine = new Engine();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        engine.clear();
        engine.setSearchEndTime(Long.MAX_VALUE);
    }

    @Override
    protected void tearDown() throws Exception {
        engine = null;
        super.tearDown();
    }

    public void testPositions() {
        final Board board = new Board();
        board.restart();
        int score = Engine.getValueFromSearchResult(engine.search(board, 5, 0));
        assertTrue(Integer.toString(score), Math.abs(score) < VAL_PAWN);

        // ROOK
        score = Engine.getValueFromSearchResult(engine.search(fromFen("k7/8/8/8/q7/8/8/1R3R1K w - - 0 1"), 5, 0));
        assertTrue(Integer.toString(score), score > VAL_ROOK * 0.9 && score < VAL_ROOK * 1.1);
        // MATE
        score = Engine.getValueFromSearchResult(engine.search(fromFen("5rk1/5Npp/8/3Q4/8/8/8/7K w - - 0 1"), 6, 0));
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        // QUEEN
        score = Engine.getValueFromSearchResult(engine.search(fromFen("2k5/8/8/8/p7/8/8/4K3 b - - 0 1"), 5, 0));
        assertTrue(Integer.toString(score), score > VAL_QUEEN * 0.7 && score < VAL_QUEEN * 1.3);
        // MATE
        score = Engine.getValueFromSearchResult(engine.search(fromFen("8/8/8/8/8/8/R7/R3K2k w Q - 0 1"), 5, 0));
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        // -QUEEN
        score = Engine.getValueFromSearchResult(engine.search(fromFen("7k/8/8/8/R2K3q/8/8/8 w - - 0 1"), 5, 0));
        assertTrue(Integer.toString(score), score < -VAL_QUEEN * 0.7 && score > -VAL_QUEEN * 1.3);
    }

    public void testTranspTable() {
        final Board board = fromFen("5rk1/5Npp/8/3Q4/8/8/8/7K w - - 0 1");
        final int score = Engine.getValueFromSearchResult(engine.search(board, 6, 0));
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
    }

    public void testTranspTable2() {
        final Board board = fromFen("1b1k4/2NP4/1P1K4/4P3/8/8/8/8 w - - 0 1");
        final long result = engine.search(board, 5, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int[] bestLine = engine.getBestLine(board);
        assertEquals(fromSimple("e5e6"), bestLine[0]);
        assertEquals(fromSimple("e5e6"), Engine.getMoveFromSearchResult(result));
    }

    public void testNodeCountCheck() {
        final Board board = fromFen("3rk2r/pp1n1pb1/q1pBpn1p/8/2PP4/3Q1N2/PP3PPP/R3R1K1 w - - 0 1");
        final int score = Engine.getValueFromSearchResult(engine.search(board, 5, 0));
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        assertTrue("Too many nodes processed: " + engine.getNodeCount(), engine.getNodeCount() < 100000);
    }

    public void testMateIn1Ply() {
        final Board board = fromFen("r3qk1r/ppp1n2p/3p1p2/8/4P3/1BpP4/PPP3PP/R1B1K2R w KQ - 0 1");
        final int searchDepth = 4 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
    }

    public void testDiscoveredCheck() {
        final Board board = fromFen("1k2r1nr/p1p2ppp/PpQ5/4b3/8/3P1P2/1PPN2PP/4K1NR b - - 0 1");
        final int score = Engine.getValueFromSearchResult(engine.search(board, 4 + 1, 0));
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int[] bestLine = engine.getBestLine(board);
        assertTrue(toSimple(bestLine), MoveGeneratorTest.containsMove(bestLine, fromSimple("e5g3")));
    }

    public void testIsDiscoveredCheck() {
        final Board board = fromFen("1k2r1nr/p1p2ppp/PpQ5/4b3/8/3P1P2/1PPN2PP/4K1NR b - - 0 1");
        board.move(StringUtils.fromSimple("e5b2"));
        assertTrue(board.isDiscoveredCheck(E[0], E[4], -1));
    }

    public void testMateIn5Ply() {
        final Board board = fromFen("k1K5/1p6/1P6/8/8/p7/N7/8 w - - 0 1");
        final int searchDepth = (5 + 1) << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
    }

    public void testMateIn5Ply2() {
        final Board board = fromFen("5k2/p4r2/Ppp3r1/8/7p/7K/1P1Qn2P/8 b - - 0 1");
        final int score = Engine.getValueFromSearchResult(engine.search(board, 6, 0));
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
    }

    public void testMateIn11() {
        final Board board = fromFen("7k/8/6P1/6K1/8/8/8/8 w - - 0 1");
        final int score = Engine.getValueFromSearchResult(engine.search(board, 11 + 1, 0));
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
    }

    public void testMateIn5() {
        final Board board = fromFen("r1b4k/ppqp3p/2n1pPp1/7Q/8/8/PPP2PPP/R4RK1 w - - 0 1");
        final int score = Engine.getValueFromSearchResult(engine.search(board, 5 + 1, 0));
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
    }

    public void testMateIn8() {
        final Board board = fromFen("8/8/8/3B4/4b2p/3N1p1p/5K1p/7k w - - 0 1");
        final int score = Engine.getValueFromSearchResult(engine.search(board, 8 + 1, 0));
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
    }

    public void testMateWithPromotion() {
        final Board board = fromFen("6k1/3P2pp/1p6/6r1/P7/1P2q3/5RPP/6K1 w - - 0 1");
        final int score = Engine.getValueFromSearchResult(engine.search(board, 3 + 1, 0));
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int[] bestLine = engine.getBestLine(board);
        assertTrue(toSimple(bestLine), MoveGeneratorTest.containsMove(bestLine, fromSimple("d7d8Q")) ||
            MoveGeneratorTest.containsMove(bestLine, fromSimple("d7d8R")));
    }

    public void testMateWithPromotion2() {
        final Board board = fromFen("k2r4/3N4/K1P5/8/7b/8/8/8 w - - 0 1");
        final long result = engine.search(board, 5, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        assertEquals("c7", toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testMateWithOpponentPromotion() {
        final Board board = fromFen("k1K5/1p6/1P6/8/1N6/8/p7/8 w - - 0 1");
        final int score = Engine.getValueFromSearchResult(engine.search(board, 4 + 1, 0));
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int[] bestLine = engine.getBestLine(board);
        assertTrue(toSimple(bestLine), MoveGeneratorTest.containsMove(bestLine, fromSimple("b4d5")));
    }

    public void testPromotion() {
        final Board board = fromFen("2k5/8/8/8/8/8/p7/4K3 b - - 0 1");
        final int searchDepth = 6 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_QUEEN * 0.9 && score < VAL_QUEEN * 1.1);
    }

    public void testPromotionAt1Ply() {
        final Board board = fromFen("2k5/8/8/8/8/8/p7/4K3 b - - 0 1");
        final int searchDepth = 1 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_QUEEN * 0.9 && score < VAL_QUEEN * 1.1);
        final int[] bestLine = engine.getBestLine(board);
        assertTrue(toSimple(bestLine), MoveGeneratorTest.containsMove(bestLine, fromSimple("a2a1Q")));
    }

    public void testBestLineWithMateIn1() {
        final Board board = fromFen("1bkr4/1b2R3/8/8/2qp4/8/6BP/7K w - - 0 1");
        final long result = engine.search(board, 5, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int[] bestLine = engine.getBestLine(board);
        assertEquals(1, bestLine.length);
        assertEquals("Bxb7#", toShort(board, bestLine[0]));
        assertEquals("Bxb7#", toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testBestLineMatchesEngineResult() {
        final Board board = fromFen("1B1b4/8/p3Pk2/1ppr2p1/4R1P1/1P3P2/P1K5/8 w - - 0 1");
        final long result = engine.search(board, 5, 0);
        final int[] bestLine = engine.getBestLine(board);
//        assertEquals("f4", toShort(board, bestLine[0]));
        assertEquals(Engine.getMoveFromSearchResult(result), bestLine[0]);
    }

    public void testBestLineWithMateIn1V2() {
        final Board board = fromFen("1n2kb1r/p4ppp/4q3/4p1B1/4P3/8/PPP2PPP/2KR4 w k - 0 1");
        final long result = engine.search(board, 5, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int[] bestLine = engine.getBestLine(board);
        assertEquals(1, bestLine.length);
        assertEquals("Rd8#", toShort(board, bestLine[0]));
        assertEquals("Rd8#", toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testBestMove1() {
        final Board board = fromFen("1r2r1k1/2pq1pb1/4b1pp/nPQnp3/P2pP3/1p1P1NPP/1B3PBK/1R2RN2 b - e3 0 1");
        final long result = engine.search(board, 0, 50);
        final int[] bestLine = engine.getBestLine(board, Engine.getMoveFromSearchResult(result));
        assertEquals("Nb7", toShort(board, bestLine[0]));
        assertEquals("Nb7", toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testBestMove2() {
        final Board board = fromFen("2rqnrk1/p5pp/bp2pp2/n1ppP3/2PP4/P1PBBP1N/4Q1PP/2R2RK1 w - d6 0 1");
        final long result = engine.search(board, 0, 50);
        final int[] bestLine = engine.getBestLine(board, Engine.getMoveFromSearchResult(result));
        assertEquals("Nf4", toShort(board, bestLine[0]));
        assertEquals("Nf4", toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testDiscoveredMate() {
        final Board board = fromFen("1r1rb1k1/2p3p1/p2q1pN1/3PpP2/Pp1bP3/1B5R/1P4PP/2B4K w - - 0 1");
        final int searchDepth = 6 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int[] bestLine = engine.getBestLine(board);
        assertEquals("Rh8+", toShort(board, bestLine[0]));
    }

    public void testRookSacWithPinnedQueen() {
        final Board board = fromFen("k7/8/8/8/q7/8/8/1R3R1K w - - 0 1");
        final int searchDepth = 3 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_ROOK * 0.9 && score < VAL_ROOK * 1.1);
    }

    public void testMateWithoutCapture() {
        final Board board = fromFen("1b1k4/2NP4/1P1K4/4P3/8/8/8/8 w - - 0 1");
        final int searchDepth = 5 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
    }

    public void testQueenMate() {
        final int score = getValueFromSearchResult(engine.search(fromFen("8/8/8/8/7p/5Qbk/8/5K2 w - - 0 1"), 5, 0));
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
    }

    public void testRookSacWithPinnedQueenTimeLimit() {
        final Board board = fromFen("k7/8/8/8/q7/8/8/1R3R1K w - - 0 1");
        final long start = System.currentTimeMillis();
        final int score = Engine.getValueFromSearchResult(engine.search(board, 15, 300));
        final long elapsed = System.currentTimeMillis() - start;
        // we shouldn't exceed the time by more than 0.5 seconds
        assertTrue(elapsed + "ms", elapsed < 800);
        assertTrue(Integer.toString(score), score > VAL_ROOK * 0.9 && score < VAL_ROOK * 1.1);
    }

    public void testMateWithQueenSac1() {
        final Board board = fromFen("5rk1/5Npp/8/3Q4/8/8/8/7K w - - 0 1");
        final int searchDepth = 6 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
    }

    public void testMateWithQueenSac2() {
        final Board board = fromFen("8/p7/8/q2p4/6B1/4KN2/6k1/2Q2n2 w - - 0 1");
        final int searchDepth = 3 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int move = Engine.getMoveFromSearchResult(result);
        assertEquals("Qxf1+", StringUtils.toShort(board, move));
    }
    
    public void testMateWithQueenSac3() {
        final Board board = fromFen("4Qrk1/pp2nppp/8/7q/8/8/PP4PP/3R3K w - - 0 1");
        final int searchDepth = 3 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
    }

    public void testMateWithRookSac() {
        final Board board = fromFen("8/5KBk/6p1/6Pb/7R/8/8/4q3 w - - 0 1");
        final int searchDepth = 3 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
    }

    public void testTranspTableQuiescence() {
        final Board board = fromFen("8/p7/8/q2p4/6B1/4KN1p/6k1/2Q2n2 w - - 0 1");
        final int searchDepth = 1 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int move = Engine.getMoveFromSearchResult(result);
        assertEquals("Qxf1+", StringUtils.toShort(board, move));
    }

    public void testMateIn2WithTwoRooks() {
        final Board board = fromFen("8/8/8/8/8/8/R7/R3K2k w Q - 0 1");
        final int searchDepth = 4 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
    }

    public void testQuickestMate1() {
        final Board board = fromFen("8/8/8/8/6p1/5PK1/8/3q1Qbk w - - 0 1");
        final long result = engine.search(board, 5, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int[] bestLine = engine.getBestLine(board);
        assertEquals("Qg2#", toShort(board, bestLine[0]));
        assertEquals("Qg2#", toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testQuickestMate2() {
        final Board board = fromFen("6rk/4rpp1/p2qpN1p/1p4Q1/4pP2/P5R1/1PP3PP/5R1K w - - 0 1");
        final long result = engine.search(board, 5, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int[] bestLine = engine.getBestLine(board);
        assertEquals("Qxh6+", toShort(board, bestLine[0]));
        assertEquals("Qxh6+", toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testQuickestMate3() {
        final Board board = fromFen("4b1k1/8/5PP1/4B3/8/1pp5/2q5/K6R w - - 0 1");
        final long result = engine.search(board, 5, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int[] bestLine = engine.getBestLine(board);
        assertEquals("Rh8+", toShort(board, bestLine[0]));
        assertEquals("Rh8+", toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testQuickestMate4() {
        final Board board = fromFen("8/8/8/8/8/7K/2p4p/2R2Rrk w - - 0 1");
        final long result = engine.search(board, 5, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int[] bestLine = engine.getBestLine(board);
        assertTrue("Rce1,Rfe1".contains(toShort(board, bestLine[0])));
        assertTrue("Rce1,Rfe1".contains(toShort(board, Engine.getMoveFromSearchResult(result))));
    }

    public void testQuickestMate5() {
        final Board board = fromFen("3K1k2/5P2/5RPb/8/8/8/8/5Q2 w - - 0 1");
        final long result = engine.search(board, 5, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int[] bestLine = engine.getBestLine(board);
        assertEquals("Qh3", toShort(board, bestLine[0]));
        assertEquals("Qh3", toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testQuickestMate6() {
        final Board board = fromFen("8/8/8/8/N1K5/k1N5/p7/R2q4 w - - 0 1");
        final long result = engine.search(board, 5, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        final int[] bestLine = engine.getBestLine(board);
        assertEquals("Rxa2#", toShort(board, bestLine[0]));
        assertEquals("Rxa2#", toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testStalemate() {
        final Board board1 = fromFen("1R2bk2/7Q/P4P2/N6p/4p2P/4K3/p7/B7 b - - 0 1");
        int score = Engine.getValueFromSearchResult(engine.search(board1, 0, 100));
        assertTrue(Integer.toString(score), (score < VAL_PAWN * 0.2) && (score > -VAL_PAWN * 0.2));
        final Board board2 = fromFen("1R3bk1/8/P1P3P1/8/4KpP1/2B2P2/8/8 b - - 0 1");
        score = Engine.getValueFromSearchResult(engine.search(board2, 0, 100));
        assertTrue(Integer.toString(score), (score < VAL_PAWN * 0.2) && (score > -VAL_PAWN * 0.2));
    }

    public void testKingInCheck() {
        final Board board = fromFen("7k/8/8/8/R2K3q/8/8/8 w - - 0 1");
        final int searchDepth = 3 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score < -VAL_QUEEN * 0.7 && score > -VAL_QUEEN * 1.3);
    }

    public void testMateWithKnightSac() {
        final Board board = fromFen("8/6p1/5pPk/5P2/6K1/2pN4/2Pn4/2B5 w - - 0 1");
        final long result = engine.search(board, 4, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        assertEquals("Nb2", StringUtils.toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testPawnMate() {
        final Board board = fromFen("8/5N1p/6p1/4Bn1k/8/7K/6P1/2r1q3 w - - 0 1");
        final long result = engine.search(board, 2, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        assertEquals("g4#", StringUtils.toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testPawnSacMate() {
        final Board board = fromFen("6bk/4Np2/5K2/4P1N1/8/8/8/8 w - - 0 1");
        final long result = engine.search(board, 3, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score > VAL_MATE_THRESHOLD);
        assertEquals("e6", StringUtils.toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testIsValidKillerMovePawnDoubleSquareMove() {
        final Board board = fromFen("8/5N1p/6p1/4Bn1k/8/7K/6P1/2r1q3 w - - 0 1");
        assertTrue(isValidKillerMove(board, G[1], G[3]));
    }

    public void testIsValidKillerMovePawnInvalidDoubleSquareMove() {
        final Board board = fromFen("7R/8/4K3/7P/4n1p1/7k/8/8 w - - 1 3");
        assertFalse(isValidKillerMove(board, H[4], H[6]));
    }

    public void testIsValidKillerMovePromotion() {
        final Board board = fromFen("8/4k2P/p7/8/8/8/6K1/q7 w - - 0 3");
        assertFalse(isValidKillerMove(board, H[6], H[7]));
    }

    public void testAttackedKnight() {
        final Board board = fromFen("1r3rk1/3bppbp/1q1p1np1/1N6/P3P3/3Q2PP/1P1B1PB1/2R2RK1 b - - 0 1");
        final long result = engine.search(board, 6, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score < VAL_PAWN);
        assertTrue(Integer.toString(score), score > -VAL_PAWN);
        assertTrue("Bxb5, Qa6".contains(StringUtils.toShort(board, Engine.getMoveFromSearchResult(result))));
    }

    public void testBishopMove() {
        final Board board = fromFen("r1bqk2r/2ppbppp/p7/1p2P3/4n3/1B6/PPP2PPP/RNBQ1RK1 b kq - 0 1");
        final long result = engine.search(board, 6, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score < VAL_PAWN);
        assertTrue(Integer.toString(score), score > -VAL_PAWN);
        assertEquals("Bb7", StringUtils.toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testStalemate2() {
        final Board board = fromFen("1k6/8/p5p1/6p1/6P1/5P1P/6PK/8 w - - 0 1");
        final long result = engine.search(board, 8, 0);
        final int score = Engine.getValueFromSearchResult(result);
        assertTrue(Integer.toString(score), score < VAL_PAWN / 2);
        assertTrue(Integer.toString(score), score > -VAL_PAWN / 2);
        assertEquals("f4", StringUtils.toShort(board, Engine.getMoveFromSearchResult(result)));
    }

    public void testAvoidMate() {
        final Board board = fromFen("3k1r2/pQ6/3q1p2/8/2Bb4/1P1R4/P6P/7K w - - 0 1");
        final long result = engine.search(board, 7, 0);
        final int move = Engine.getMoveFromSearchResult(result);
        assertTrue(!"b7a7".equals(StringUtils.toSimple(move)));
    }

    public void testConsistency() {
        final Board board = fromFen("6qk/7p/p1pp1Q2/4p3/4P1b1/8/2P3PP/6NK b - - 6 32");
        final long result1 = engine.search(board, 8, 0);
        final int score1 = Engine.getValueFromSearchResult(result1);
        final int move = Engine.getMoveFromSearchResult(result1);
        assertEquals(StringUtils.fromSimple("g8g7"), move);
        board.move(move);
        final int score2 = Engine.getValueFromSearchResult(engine.search(board, 7, 0));
        assertTrue(score1 + " vs " + score2, Math.abs(Math.abs(score2) - Math.abs(score1)) < 50);
    }

    public void no_testDebugCheckedMoves() {
        final Board board = fromFen("1b5k/7P/p1p2np1/2P2p2/PP3P2/4RQ1R/q2r3P/6K1 w - - 0 1");
        final int searchDepth = 4 << SHIFT_PLY;
        final long result = engine.negascoutRoot(board, searchDepth, INITIAL_ALPHA, INITIAL_BETA, 0);
        final int move = Engine.getMoveFromSearchResult(result);
        assertEquals("Re8+", StringUtils.toShort(board, move));
    }

    public void testThreefoldRepetition() {
        final Board board = StringUtils.fromPgn("[Date \"2010.10.13\"]\n" +
            "[Round \"0\"]\n" +
            "[Game \"1\"]\n" +
            "[White \"Pawns N' Roses Latest\"]\n" +
            "[Black \"Pawns N' Roses v0.054\"]\n" +
            "[Result \"1/2-1/2\"]\n" +
            "[Time \"19:26:48\"]\n" +
            "[PlyCount \"153\"]\n" +
            "[Termination \"normal\"]\n" +
            "[RemainingTimeWhite \"00:01.872\"]\n" +
            "[RemainingTimeBlack \"00:01.842\"]\n" +
            "\n" +
            "1. Nf3 Nf6 2. Nc3 d5 3. d3 Nc6 4. e4 d4 5. Na4 e5 6. c3 Bd6 7. cxd4 exd4 8. Qc2 a6 \n" +
            "9. Be2 Bb4+ 10. Kf1 Qd6 11. a3 Ba5 12. b4 Nxb4 13. axb4 Bxb4 14. e5 Qd8 15. exf6 Qxf6 16. Qxc7 O-O \n" +
            "17. Qc4 Bc3 18. Nxc3 dxc3 19. d4 Qc6 20. Qxc6 bxc6 21. Ba3 Rd8 22. Rc1 Bg4 23. Rxc3 Bxf3 24. Bxf3 Rxd4 \n" +
            "25. Rxc6 Rb8 26. Rxa6 Rd2 27. Ke1 Ra2 28. Ra7 Re8+ 29. Re7 Ra1+ 30. Bd1 Rd8 31. Bc1 Rxc1 32. Ke2 Rb1 \n" +
            "33. Rc7 Re8+ 34. Kd2 Reb8 35. Re1 h6 36. h3 Kf8 37. Ree7 Rd8+ 38. Red7 Rxd7+ 39. Rxd7 Rb2+ 40. Bc2 g5 \n" +
            "41. Kc3 Rb8 42. Bb3 Ke8 43. Rd6 h5 44. Rh6 Ke7 45. Rxh5 Rg8 46. Rh7 Rf8 47. Kb2 Kf6 48. Kc3 Kg6 \n" +
            "49. Bc2+ f5 50. Rb7 Kf6 51. Kb2 Ke5 52. Bb3 Kd6 53. Rg7 g4 54. hxg4 fxg4 55. Bf7 Kc7 56. Bb3+ Kb8 \n" +
            "57. Rg8 Rxg8 58. Bxg8 Kc7 59. Be6 g3 60. fxg3 Kd6 61. Bc4 Kc5 62. Bd3 Kd4 63. g4 Kxd3 64. g5 Ke4 \n" +
            "65. g6 Ke5 66. g7 Kd6 67. Kb1 Kc7 68. g8=Q Kb7 69. Qf7+ Kb8 70. Qd7 Ka8 71. Qd5+ Ka7 72. Qa5+ Kb7 \n" +
            "73. Qc5 Kb8 74. Kc2 Kb7 75. Kb1 Kb8 76. Kc2 Kb7");
        engine.getTranspositionTable().set(board.getZobristKey(), TranspositionTable.TT_TYPE_EXACT,
            StringUtils.fromShort(board, "Kb1"), 10, 1000, (board.getState() & FULL_MOVES) >> SHIFT_FULL_MOVES);
        assertEquals(0, engine.removeThreefoldRepetition(board, engine.getTranspositionTable().read(board.getZobristKey())));
    }

    public void testThreefoldRepetition2() {
        final Board board = StringUtils.fromPgn("[Date \"2010.10.16\"]\n" +
            "[Round \"22\"]\n" +
            "[Game \"45\"]\n" +
            "[White \"Pawns N' Roses Latest\"]\n" +
            "[Black \"Pawns N' Roses v0.054\"]\n" +
            "[Result \"1/2-1/2\"]\n" +
            "[Time \"21:37:50\"]\n" +
            "[PlyCount \"103\"]\n" +
            "[Termination \"normal\"]\n" +
            "[RemainingTimeWhite \"00:02.025\"]\n" +
            "[RemainingTimeBlack \"00:02.063\"]\n" +
            "\n" +
            "1. Nf3 Nf6 2. Nc3 d5 3. e3 Nbd7 4. Bb5 e6 5. d4 Bd6 6. O-O O-O 7. Qe2 Qe7 8. a3 e5 \n" +
            "9. Ng5 e4 10. Bd2 h6 11. Nh3 a6 12. Ba4 c5 13. Bb3 Qe6 14. Rfd1 cxd4 15. exd4 Nb6 16. Nf4 Qg4 \n" +
            "17. Qxg4 Bxg4 18. Re1 Nc4 19. Bxc4 dxc4 20. Ncd5 Nxd5 21. Nxd5 Rae8 22. Nb6 Bc7 23. Nxc4 Be6 24. Ne5 Rc8 \n" +
            "25. Bc3 Bd5 26. h3 Bd6 27. Nd7 Rxc3 28. bxc3 Rc8 29. Nb6 Rxc3 30. Nxd5 Rxc2 31. Rxe4 Kh7 32. Rb1 Rd2 \n" +
            "33. a4 h5 34. g3 f5 35. Rh4 g5 36. Rxb7+ Kg6 37. Rb6 gxh4 38. Rxd6+ Kf7 39. Rxa6 hxg3 40. fxg3 Rd1+ \n" +
            "41. Kg2 Rxd4 42. Rf6+ Kg7 43. Rxf5 h4 44. gxh4 Rxa4 45. h5 Rd4 46. Rg5+ Kh6 47. Re5 Rd2+ 48. Kg1 Rd1+ \n" +
            "49. Kf2 Rd2+ 50. Kg1 Rd1+ 51. Kf2 Rd2+");
        engine.getTranspositionTable().set(board.getZobristKey(), TranspositionTable.TT_TYPE_EXACT,
            StringUtils.fromShort(board, "Kg1"), 1, 1000, (board.getState() & FULL_MOVES) >> SHIFT_FULL_MOVES);
        assertEquals(0, engine.removeThreefoldRepetition(board, engine.getTranspositionTable().read(board.getZobristKey())));
    }

    public void testThreefoldRepetition3() {
        final Board board = StringUtils.fromPgn("[Date \"2010.10.16\"]\n" +
            "[Round \"3\"]\n" +
            "[Game \"8\"]\n" +
            "[White \"Pawns N' Roses v0.054\"]\n" +
            "[Black \"Pawns N' Roses Latest\"]\n" +
            "[Result \"1/2-1/2\"]\n" +
            "[Time \"22:11:38\"]\n" +
            "[PlyCount \"172\"]\n" +
            "[Termination \"normal\"]\n" +
            "[RemainingTimeWhite \"00:01.904\"]\n" +
            "[RemainingTimeBlack \"00:01.884\"]\n" +
            "\n" +
            "1. Nf3 Nf6 2. Nc3 Nc6 3. e4 e5 4. Bd3 d5 5. O-O Bb4 6. Bb5 d4 7. Ne2 Bd7 8. d3 O-O \n" +
            "9. Bxc6 Bxc6 10. Nxe5 Be8 11. c3 dxc3 12. Nxc3 Qe7 13. Qb3 Qxe5 14. Qxb4 Bc6 15. d4 Qd6 16. Qxd6 cxd6 \n" +
            "17. d5 Bd7 18. Bf4 Ne8 19. Be3 Nf6 20. Bf4 Ne8 21. Be3 f5 22. f3 f4 23. Bd2 Nc7 24. a4 b6 \n" +
            "25. Ne2 Bc8 26. Rac1 Ne8 27. Nxf4 Rf7 28. Be3 Ba6 29. Rf2 Bb7 30. Rc4 Nc7 31. h4 Rc8 32. h5 h6 \n" +
            "33. g4 Re7 34. Bxb6 axb6 35. Ne2 Rce8 36. Nd4 Rf7 37. Rfc2 Na6 38. b4 Ree7 39. Nf5 Rd7 40. Kg2 Rf8 \n" +
            "41. Kg1 Ra8 42. Rg2 Rad8 43. Kf1 Kh7 44. Kg1 Rf7 45. Rf2 Nc7 46. Rfc2 Ne8 47. Nd4 Rb8 48. Nf5 Rd8 \n" +
            "49. b5 Kg8 50. Rf2 Nf6 51. Rg2 Ba8 52. f4 Nxd5 53. exd5 Bxd5 54. Rcc2 Bxg2 55. Kxg2 Ra8 56. Ra2 Kh7 \n" +
            "57. Kg1 Rfa7 58. Nxd6 Rxa4 59. Rf2 Rd8 60. Nf5 Rb4 61. Ne7 Rxb5 62. Nc6 Rd1+ 63. Rf1 Rd2 64. f5 Rc5 \n" +
            "65. f6 gxf6 66. Ne7 Rd4 67. Rxf6 Rxg4+ 68. Kf2 Rxh5 69. Rxb6 Rh2+ 70. Kf1 Rh1+ 71. Kf2 Rh2+ 72. Kf1 Ra4 \n" +
            "73. Kg1 Re2 74. Rb1 Rg4+ 75. Kh1 Rxe7 76. Rb2 Rh4+ 77. Kg1 Re1+ 78. Kg2 Reh1 79. Rb7+ Kg8 80. Rb8+ Kg7 \n" +
            "81. Rb7+ Kf8 82. Rb8+ Kg7 83. Rb7+ Kf8 84. Rb8+ Kf7 85. Rb7+ Kg8 86. Rb8+");
        engine.getTranspositionTable().set(board.getZobristKey(), TranspositionTable.TT_TYPE_EXACT,
            StringUtils.fromShort(board, "Kg7"), 3, 1000, (board.getState() & FULL_MOVES) >> SHIFT_FULL_MOVES);
        assertEquals(0, engine.removeThreefoldRepetition(board, engine.getTranspositionTable().read(board.getZobristKey())));
    }

    public void testAvoidThreefoldRepetition() {
        final Board board = StringUtils.fromPgn("[Date \"2010.11.22\"]\n" +
            "[Round \"1\"]\n" +
            "[Game \"3\"]\n" +
            "[White \"Pawns N' Roses Latest\"]\n" +
            "[Black \"PawnsNRoses v0.063\"]\n" +
            "[Result \"1/2-1/2\"]\n" +
            "[Time \"19:47:38\"]\n" +
            "[PlyCount \"74\"]\n" +
            "[Termination \"normal\"]\n" +
            "[RemainingTimeWhite \"00:03.110\"]\n" +
            "[RemainingTimeBlack \"00:03.063\"]\n" +
            "\n" +
            "1. Nf3 Nf6 2. Nc3 d5 3. e3 Nc6 4. Bb5 Qd6 5. d4 Bf5 6. Ne5 a6 7. Bxc6+ bxc6 8. O-O c5 \n" +
            "9. Bd2 cxd4 10. exd4 Qb6 11. Bc1 Rd8 12. Na4 Qd6 13. Be3 Ne4 14. Re1 Qb4 15. Qf3 g6 16. b3 Qb5 \n" +
            "17. Nc5 Nxc5 18. dxc5 d4 19. g4 Be6 20. Bf4 Qxc5 21. Qc6+ Qxc6 22. Nxc6 Ra8 23. Be5 Rg8 24. g5 Bd5 \n" +
            "25. Nxd4 c5 26. Ne2 Be4 27. Rac1 e6 28. Ng3 Bf3 29. Ne4 Be7 30. Nf6+ Bxf6 31. Bxf6 Rb8 32. Re3 Bg4 \n" +
            "33. Re4 Rb4 34. Re5 Rb5 35. Re4 Rb4 36. Re5 Rb5"); // 37. Re4 Rb4
        final long result = engine.search(board, 6, 0);
        assertTrue(!"Re5".equals(StringUtils.toShort(board, Engine.getMoveFromSearchResult(result))));
    }

    public void testNotStayingInCheck() {
        final Board board = StringUtils.fromFen("8/2b5/5p2/1k6/8/p4K2/8/7q w - - 0 73");
        final long result = engine.search(board, 7, 0);
        assertFalse(StringUtils.toSimple(Engine.getMoveFromSearchResult(result)).equals("f3g2"));
    }
}