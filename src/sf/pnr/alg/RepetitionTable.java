package sf.pnr.alg;

import java.util.Arrays;

/**
 */
public class RepetitionTable {
    private static final int ARRAY_SIZE_SHIFT = 16;
    private static final int ARRAY_SIZE = 1 << ARRAY_SIZE_SHIFT;
    private static final int ARRAY_LENGHT = ARRAY_SIZE >> 3;
    private static final int SHIFT_ENTRIES_PER_HASH = 4;
    private static final int ARRAY_MASK = (ARRAY_LENGHT >> SHIFT_ENTRIES_PER_HASH) - 1;
    private static final int COUNT_MASK = 0x0C;
    private static final int SHIFT_COUNT = 2;
    private static final int COUNT_INC = 1 << SHIFT_COUNT;
    private static final long ZOBRIST_MASK = ~COUNT_MASK;

    private final long[] array = new long[ARRAY_LENGHT];

    public int get(final long zobrist) {
        final int hashed = hash(zobrist);
        final int startIndex = hashed << SHIFT_ENTRIES_PER_HASH;
        final long maskedZobrist = zobrist & ZOBRIST_MASK;
        for (int i = startIndex; i < ARRAY_LENGHT; i++) {
            if ((array[i] & ZOBRIST_MASK) == maskedZobrist) {
                return (int) ((array[i] & COUNT_MASK) >> SHIFT_COUNT);
            } else if (array[i] == 0) {
                return 0;
            }
        }
        for (int i = 0; i < startIndex; i++) {
            if ((array[i] & ZOBRIST_MASK) == maskedZobrist) {
                return (int) ((array[i] & COUNT_MASK) >> SHIFT_COUNT);
            } else if (array[i] == 0) {
                return 0;
            }
        }
        return 0;
    }

    public void increment(final long zobrist) {
        final int hashed = hash(zobrist);
        final int startIndex = hashed << SHIFT_ENTRIES_PER_HASH;
        final long maskedZobrist = zobrist & ZOBRIST_MASK;
        for (int i = startIndex; i < ARRAY_LENGHT; i++) {
            if ((array[i] & ZOBRIST_MASK) == maskedZobrist) {
                array[i] = maskedZobrist | ((array[i] & COUNT_MASK) + COUNT_INC);
                return;
            } else if (array[i] == 0) {
                array[i] = maskedZobrist | COUNT_INC;
                return;
            }
        }
        for (int i = 0; i < startIndex; i++) {
            if ((array[i] & ZOBRIST_MASK) == maskedZobrist) {
                array[i] += COUNT_INC;
                return;
            } else if (array[i] == 0) {
                array[i] = maskedZobrist;
                return;
            }
        }
    }

    public void decrement(final long zobrist) {
        final int hashed = hash(zobrist);
        final int startIndex = hashed << SHIFT_ENTRIES_PER_HASH;
        final long maskedZobrist = zobrist & ZOBRIST_MASK;
        for (int i = startIndex; i < ARRAY_LENGHT; i++) {
            if ((array[i] & ZOBRIST_MASK) == maskedZobrist) {
                final long count = array[i] & COUNT_MASK;
                if (count == COUNT_INC) {
                    array[i] = 0;
                    return;
                } else {
                    array[i] = maskedZobrist | (count - COUNT_INC);
                    return;
                }
            } else if (array[i] == 0) {
                assert false;
                return;
            }
        }
        for (int i = 0; i < startIndex; i++) {
            if ((array[i] & ZOBRIST_MASK) == maskedZobrist) {
                final long count = array[i] & COUNT_MASK;
                if (count == COUNT_INC) {
                    array[i] = 0;
                    return;
                } else {
                    array[i] = maskedZobrist | (count - COUNT_INC);
                    return;
                }
            } else if (array[i] == 0) {
                assert false;
                return;
            }
        }
    }

    private int hash(final long zobrist) {
        return (int) (zobrist & ARRAY_MASK);
    }

    public void clear() {
        Arrays.fill(array, 0);
    }
}