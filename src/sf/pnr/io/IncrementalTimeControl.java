package sf.pnr.io;

/**
 */
public class IncrementalTimeControl implements TimeControl{

    private final int baseTime;
    private final int increment;
    private final int movesLeft;
    private int timeLeft;

    public IncrementalTimeControl(final int baseTime, final int increment) {
        this(baseTime, increment, 40);
    }

    public IncrementalTimeControl(final int baseTime, final int increment, final int movesLeft) {
        this.baseTime = baseTime;
        this.increment = increment;
        this.movesLeft = movesLeft;
        timeLeft = baseTime;
    }

    @Override
    public void moved(final int ms) {
        timeLeft -= ms;
    }

    @Override
    public int getNextMoveTime() {
        // assume we have 40 moves left for the base time
        final int baseTimePart = timeLeft / movesLeft;
        timeLeft += increment;
        return baseTimePart + increment;
    }
}