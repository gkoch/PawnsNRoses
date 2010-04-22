package sf.pnr.io;

/**
 */
public class FixedPerMoveTimeControl implements TimeControl {

    private final int timePerMove;

    public FixedPerMoveTimeControl(final int timePerMove) {
        this.timePerMove = timePerMove;
    }

    @Override
    public void moved(final int ms) {
        // do nothing
    }

    @Override
    public int getNextMoveTime() {
        return timePerMove;
    }
}