package sf.pnr.base;

import junit.framework.TestCase;

import java.io.File;

import static sf.pnr.base.StringUtils.fromFen;

/**
 */
public class PolyglotTest extends TestCase {

    public void testInitialPos() {
        final File book = new File(PolyglotTest.class.getResource("res/performance.bin").getFile());
        final Polyglot polyglot = new Polyglot(book);
        final Board board = new Board();
        board.restart();
        assertTrue(polyglot.readMove(board) != 0);
    }

    public void testAfterE2E4() {
        final File book = new File(PolyglotTest.class.getResource("res/performance.bin").getFile());
        final Polyglot polyglot = new Polyglot(book);
        final Board board = fromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
        assertTrue(polyglot.readMove(board) != 0);
    }
}
