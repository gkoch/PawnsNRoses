package sf.pnr.alg;

import java.util.Arrays;

/**
 */
public final class RepetitionTable {
    private static final int ARRAY_LENGHT_BITS = 16 - 3;
    private static final int USABLE_ARRAY_LENGHT = 1 << ARRAY_LENGHT_BITS;
    private static final long ARRAY_MASK = USABLE_ARRAY_LENGHT - 1;
    private static final int ARRAY_LENGHT = USABLE_ARRAY_LENGHT + 128;
    private static final int SHIFT_COUNT = ARRAY_LENGHT_BITS - 2;
    private static final long COUNT_MASK = 0x03L << SHIFT_COUNT;
    private static final long ZOBRIST_MASK = ~COUNT_MASK;
    private static final long ZOBRIST_MASK_IN_HASH = ARRAY_MASK & ~COUNT_MASK;
    private static final long COUNT_INC = 1L << SHIFT_COUNT;

    private final long[] array = new long[ARRAY_LENGHT];

    public int get(final long zobrist) {
        final int startIndex = hash(zobrist);
        final long maskedZobrist = zobrist & ZOBRIST_MASK;
        for (int i = startIndex; i < ARRAY_LENGHT; i++) {
            if ((array[i] & ZOBRIST_MASK) == maskedZobrist) {
                return (int) ((array[i] & COUNT_MASK) >> SHIFT_COUNT);
            } else if (array[i] == 0) {
                return 0;
            }
        }
        return 0;
    }

    public void increment(final long zobrist) {
        final int startIndex = hash(zobrist);
        final long maskedZobrist = zobrist & ZOBRIST_MASK;
        for (int i = startIndex; i < ARRAY_LENGHT; i++) {
            if ((array[i] & ZOBRIST_MASK) == maskedZobrist) {
                array[i] += COUNT_INC;
                return;
            } else if (array[i] == 0) {
                array[i] = maskedZobrist | COUNT_INC;
                return;
            }
        }
    }

    public void decrement(final long zobrist) {
        final int startIndex = hash(zobrist);
        final long maskedZobrist = zobrist & ZOBRIST_MASK;
        for (int i = startIndex; i < ARRAY_LENGHT; i++) {
            if ((array[i] & ZOBRIST_MASK) == maskedZobrist) {
                final long count = array[i] & COUNT_MASK;
                if (count == COUNT_INC) {
                    for (int j = i; j < ARRAY_LENGHT; j++) {
                        final int next = j + 1;
                        if ((array[next] != 0) && (array[next] & ZOBRIST_MASK_IN_HASH) != (next & ZOBRIST_MASK_IN_HASH)) {
                            array[j] = array[next];
                        } else {
                            array[j] = 0;
                            break;
                        }
                    }
                    return;
                } else {
                    array[i] -= COUNT_INC;
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

    public int scan() {
        int emptySlots = 0;
        for (long entry: array) {
            if (entry == 0) {
                emptySlots++;
            }
        }
        return emptySlots;
    }
}