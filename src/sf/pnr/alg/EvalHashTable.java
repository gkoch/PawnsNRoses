package sf.pnr.alg;

import sf.pnr.base.Configurable;

import java.util.Arrays;

/**
 */
public final class EvalHashTable {
    private static final int MAX_CHECK_INDEX = 10;
    private static final long VALUE_MASK = 0xFFFFL;
    private static final long ZOBRIST_MASK = ~VALUE_MASK;
    @Configurable(Configurable.Key.EVAL_TABLE_SIZE)
    private static int TABLE_SIZE = 1;

    private final long[] array;

    public EvalHashTable() {
        array = new long[TABLE_SIZE * 1024 * 1024 / 8];
    }

    public int read(final long zobrist) {
        final int startIndex = hash(zobrist);
        final long maskedZobrist = zobrist & ZOBRIST_MASK;
        final int toCheck1 = startIndex + MAX_CHECK_INDEX;
        for (int i = startIndex; i < array.length; i++) {
            if ((array[i] & ZOBRIST_MASK) == maskedZobrist) {
                return (int) (array[i] & VALUE_MASK);
            } else if (array[i] == 0) {
                return 0;
            } else if (i > toCheck1) {
                return 0;
            }
        }
        final int toCheck2 = MAX_CHECK_INDEX - (array.length - startIndex);
        for (int i = 0; i < startIndex; i += 3) {
            if ((array[i] & ZOBRIST_MASK) == maskedZobrist) {
                return (int) (array[i] & VALUE_MASK);
            } else if (array[i] == 0) {
                return 0;
            } else if (i > toCheck2) {
                return 0;
            }
        }
        return 0;
    }

    public void set(final long zobrist, final int value) {
        assert value > 0;
        final int startIndex = hash(zobrist);
        final long maskedZobrist = zobrist & ZOBRIST_MASK;

        final int toCheck1 = startIndex + MAX_CHECK_INDEX;
        for (int i = startIndex; i < array.length; i++) {
            if (array[i] == 0) {
                array[i] = maskedZobrist + value;
                return;
            } else if (i >= toCheck1) {
                array[startIndex] = maskedZobrist + value;
                return;
            }
        }
        final int toCheck2 = MAX_CHECK_INDEX - (array.length - startIndex);
        for (int i = 0; i < startIndex; i += 3) {
            if (array[i] == 0) {
                array[i] = maskedZobrist + value;
                return;
            } else if (i >= toCheck2) {
                array[startIndex] = maskedZobrist + value;
                return;
            }
        }
    }

    private int hash(final long zobrist) {
        return (int) ((zobrist & 0x0FFFFFFF) % array.length);
    }

    public void clear() {
        Arrays.fill(array, 0);
    }

    public int scan() {
        int emptySlots = 0;
        for (long entry : array) {
            if (entry == 0) {
                emptySlots++;
            }
        }
        return emptySlots;
    }
}