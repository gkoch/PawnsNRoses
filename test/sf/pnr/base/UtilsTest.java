package sf.pnr.base;

import junit.framework.TestCase;

import java.util.Set;

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

    public void testAttackArray() {
        for (int i = 1; i < 8; i++) {
            assertEquals(1, (ATTACK_ARRAY[A[0] - A[i] + 120] & ATTACK_DISTANCE_ROOK) >> SHIFT_ATTACK_DISTANCE_ROOK);
            assertEquals(1, (ATTACK_ARRAY[A[i] - A[0] + 120] & ATTACK_DISTANCE_ROOK) >> SHIFT_ATTACK_DISTANCE_ROOK);
            assertEquals(2, (ATTACK_ARRAY[A[0] - B[i] + 120] & ATTACK_DISTANCE_ROOK) >> SHIFT_ATTACK_DISTANCE_ROOK);
            assertEquals(2, (ATTACK_ARRAY[B[i] - A[0] + 120] & ATTACK_DISTANCE_ROOK) >> SHIFT_ATTACK_DISTANCE_ROOK);

            final int diag = getPosition(i, i);
            assertEquals(1, (ATTACK_ARRAY[A[0] - diag + 120] & ATTACK_DISTANCE_BISHOP) >> SHIFT_ATTACK_DISTANCE_BISHOP);
            assertEquals(1, (ATTACK_ARRAY[diag - A[0] + 120] & ATTACK_DISTANCE_BISHOP) >> SHIFT_ATTACK_DISTANCE_BISHOP);
            assertEquals(7, (ATTACK_ARRAY[A[0] - (diag - 1) + 120] & ATTACK_DISTANCE_BISHOP) >> SHIFT_ATTACK_DISTANCE_BISHOP);
            assertEquals(7, (ATTACK_ARRAY[(diag - 1) - A[0] + 120] & ATTACK_DISTANCE_BISHOP) >> SHIFT_ATTACK_DISTANCE_BISHOP);

            assertEquals(1, (ATTACK_ARRAY[A[0] - A[i] + 120] & ATTACK_DISTANCE_QUEEN) >> SHIFT_ATTACK_DISTANCE_QUEEN);
            assertEquals(1, (ATTACK_ARRAY[A[i] - A[0] + 120] & ATTACK_DISTANCE_QUEEN) >> SHIFT_ATTACK_DISTANCE_QUEEN);
            assertEquals(1, (ATTACK_ARRAY[A[0] - diag + 120] & ATTACK_DISTANCE_QUEEN) >> SHIFT_ATTACK_DISTANCE_QUEEN);
            assertEquals(1, (ATTACK_ARRAY[diag - A[0] + 120] & ATTACK_DISTANCE_QUEEN) >> SHIFT_ATTACK_DISTANCE_QUEEN);
        }
        assertEquals(2, (ATTACK_ARRAY[E[0] - E[2] + 120] & ATTACK_DISTANCE_KNIGHT) >> SHIFT_ATTACK_DISTANCE_KNIGHT);
        assertEquals(3, (ATTACK_ARRAY[F[4] - F[3] + 120] & ATTACK_DISTANCE_KNIGHT) >> SHIFT_ATTACK_DISTANCE_KNIGHT);
        assertEquals(2, (ATTACK_ARRAY[E[0] - E[2] + 120] & ATTACK_DISTANCE_BISHOP) >> SHIFT_ATTACK_DISTANCE_BISHOP);
        assertEquals(2, (ATTACK_ARRAY[D[7] - A[0] + 120] & ATTACK_DISTANCE_BISHOP) >> SHIFT_ATTACK_DISTANCE_BISHOP);
        assertEquals(2, (ATTACK_ARRAY[H[7] - E[0] + 120] & ATTACK_DISTANCE_BISHOP) >> SHIFT_ATTACK_DISTANCE_BISHOP);
        assertEquals(2, (ATTACK_ARRAY[E[0] - H[7] + 120] & ATTACK_DISTANCE_BISHOP) >> SHIFT_ATTACK_DISTANCE_BISHOP);
        assertEquals(2, (ATTACK_ARRAY[E[0] - B[7] + 120] & ATTACK_DISTANCE_BISHOP) >> SHIFT_ATTACK_DISTANCE_BISHOP);
    }

    public void testCheckMove() {
        final Board board = StringUtils.fromFen("8/2b5/5p2/1k6/8/p4K2/8/7q w - - 0 73");
        final Set<String> problems = Utils.checkMove(board, StringUtils.fromSimple("f3g2"));
        assertFalse(problems.isEmpty());
    }

    public void testConvert0x88to64() {
        assertEquals(0, convert0x88To64(A[0]));
        assertEquals(1, convert0x88To64(B[0]));
        assertEquals(2, convert0x88To64(C[0]));
    }
}