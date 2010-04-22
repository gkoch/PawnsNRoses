package sf.pnr.io;

/**
 */
public class ConventionalTimeControl implements TimeControl{

    private final int sessionMoveCount;
    private final int sessionTime;
    private int timeLeft;
    private int moveCount;

    public ConventionalTimeControl(final int sessionTime) {
        this(40, sessionTime);
    }

    public ConventionalTimeControl(final int sessionMoveCount, final int sessionTime) {
        this.sessionMoveCount = sessionMoveCount;
        this.sessionTime = sessionTime;
        timeLeft = sessionTime;
        moveCount = 0;
    }

    @Override
    public void moved(final int ms) {
        moveCount++;
        timeLeft -= ms;
        if (moveCount % sessionMoveCount == 0) {
            timeLeft = sessionTime;
            moveCount = 0;
        }
    }

    @Override
    public int getNextMoveTime() {
        return timeLeft / (sessionMoveCount - moveCount);
    }
}