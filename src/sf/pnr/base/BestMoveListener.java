package sf.pnr.base;

/**
 */
public interface BestMoveListener {

    public void bestMoveChanged(int depth, int bestMove, int value, long time, int[] bestLine, long nodes);
}
