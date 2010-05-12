package sf.pnr.base;

import junit.framework.TestCase;
import static sf.pnr.base.Utils.*;
import static sf.pnr.base.StringUtils.*;

/**
 */
public class UtilsTest extends TestCase {

    public void testDelta() {
        assertEquals(ATTACK_R, ATTACK_ARRAY[A[7] - A[3] + 120] & ATTACK_Q);
        assertEquals(UP, ((ATTACK_ARRAY[A[7] - A[3] + 120] & ATTACK_DELTA) >> SHIFT_ATTACK_DELTA) - 64);
    }

    public void testPolyglotZobrist() {
        Board board = fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        assertEquals(0x463B96181691FC9CL, board.getPolyglotZobristKey());

        board = fromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
        assertEquals(0x823C9B50FD114196L, board.getPolyglotZobristKey());

        board = fromFen("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 2");
        assertEquals(0x0756B94461C50FB0L, board.getPolyglotZobristKey());

        board = fromFen("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2");
        assertEquals(0x662FAFB965DB29D4L, board.getPolyglotZobristKey());

        board = fromFen("rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3");
        assertEquals(0x22A48B5A8E47FF78L, board.getPolyglotZobristKey());

        board = fromFen("rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPPKPPP/RNBQ1BNR b kq - 0 3");
        assertEquals(0x652A607CA3F242C1L, board.getPolyglotZobristKey());

        board = fromFen("rnbq1bnr/ppp1pkpp/8/3pPp2/8/8/PPPPKPPP/RNBQ1BNR w - - 0 4");
        assertEquals(0x00FDD303C946BDD9L, board.getPolyglotZobristKey());

        board = fromFen("rnbqkbnr/p1pppppp/8/8/PpP4P/8/1P1PPPP1/RNBQKBNR b KQkq c3 0 3");
        assertEquals(0x3C8123EA7B067637L, board.getPolyglotZobristKey());

        board = fromFen("rnbqkbnr/p1pppppp/8/8/P6P/R1p5/1P1PPPP1/1NBQKBNR b Kkq - 0 4");
        assertEquals(0x5C3F9B829B279560L, board.getPolyglotZobristKey());
    }

    public void testSide() {
        final Board board = new Board();
        board.restart();
        assertEquals(WHITE, side(board.getBoard()[A[0]]));
        assertEquals(BLACK, side(board.getBoard()[A[7]]));
    }
}