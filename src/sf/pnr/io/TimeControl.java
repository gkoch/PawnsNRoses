package sf.pnr.io;

/**
 */
public interface TimeControl {
    void moved(int ms);

    int getNextMoveTime();
}
