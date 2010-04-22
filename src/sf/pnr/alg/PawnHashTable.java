package sf.pnr.alg;

import java.util.Arrays;

/**
 */
public class PawnHashTable {
    private static final long VALUE_MASK = 0x0FFFL;
    private static final long STAGE_MASK = 0x3F;
    private static final int STAGE_SHIFT = 12;
    private static final int VALUE_OFFSET = (int) (VALUE_MASK >> 1);
    private static final int HASH_MASK = 0x3FFF;
    private static final int STAGE_IN_FRONT_OF_LIMIT = 10;
    private static final int STAGE_BEHIND_LIMIT = 10;

    private final long[] array = new long[(HASH_MASK + 1)<< 1];

    public int get(final long zobrist, final int stage) {
        final int hashed = hash(zobrist);
        if (zobrist != 0L && array[hashed] == zobrist) {
            final long storedValue = array[hashed + 1];
            final long storedStage = (storedValue >> STAGE_SHIFT) & STAGE_MASK;
            if (storedStage - stage < STAGE_BEHIND_LIMIT && stage - storedStage < STAGE_IN_FRONT_OF_LIMIT) {
                return (int) (storedValue & VALUE_MASK) - VALUE_OFFSET;
            }
        }
        return 0;
    }

    public void set(final long zobrist, final int value, final int stage) {
        final long storedValue = value + VALUE_OFFSET;
        assert storedValue > 0;
        assert storedValue <= VALUE_MASK;
        assert stage >= 0;
        assert stage <= STAGE_MASK;
        assert ((stage << STAGE_SHIFT) & VALUE_MASK) == 0;
        final int hashed = hash(zobrist);
        array[hashed] = zobrist;
        array[hashed + 1] = (stage << STAGE_SHIFT) | storedValue;
    }

    private int hash(final long zobrist) {
        return (int) (zobrist & VALUE_MASK);
    }

    public void clear() {
        Arrays.fill(array, 0);
    }
}