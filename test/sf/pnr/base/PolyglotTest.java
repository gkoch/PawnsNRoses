package sf.pnr.base;

import junit.framework.TestCase;

import static sf.pnr.base.StringUtils.fromFen;

/**
 */
public class PolyglotTest extends TestCase {

    public void testInitialPos() {
        Configuration.getInstance().setProperty(Configurable.Key.POLYGLOT_BOOK,
            PolyglotTest.class.getResource("res/performance.bin").getFile());
        final Board board = new Board();
        board.restart();
        assertTrue(new Polyglot().readMove(board) != 0);
    }

    public void testAfterE2E4() {
        Configuration.getInstance().setProperty(Configurable.Key.POLYGLOT_BOOK,
            PolyglotTest.class.getResource("res/performance.bin").getFile());
        final Board board = fromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
        assertTrue(new Polyglot().readMove(board) != 0);
    }
}
