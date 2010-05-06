package sf.pnr.alg;

import sf.pnr.base.Configuration;

import java.util.Arrays;

/**
 */
public final class EvalHashTable {
    private static final int MAX_CHECK_COUNT = 20;
    private static final int MAX_CHECK_INDEX = 3 * MAX_CHECK_COUNT;

    private final int[] array;

    public EvalHashTable() {
        array = new int[Configuration.getInstance().getEvalHashTableSizeInMB() * 1024 * 1024 / 12 * 3];
    }

    public int read(final long zobrist) {
        final int hashed = hash(zobrist);
        final int startIndex = hashed / 3 * 3;
        final int zobristHigh = (int) (zobrist >> 32);
        final int zobristLow = (int) (zobrist & 0xFFFFFFFFL);

        for (int i = startIndex; i < array.length; i += 3) {
            if (array[i] == zobristHigh && array[i + 1] == zobristLow) {
                return array[i + 2];
            } else if (array[i] == 0) {
                return 0;
            } else if (i > startIndex + MAX_CHECK_INDEX) {
                return 0;
            }
        }
        final int toCheck = MAX_CHECK_INDEX - (array.length - startIndex);
        for (int i = 0; i < startIndex; i += 3) {
            if (array[i] == zobristHigh && array[i + 1] == zobristLow) {
                return array[i + 2];
            } else if (array[i] == 0) {
                return 0;
            } else if (i > toCheck) {
                return 0;
            }
        }
        return 0;
    }

    public void set(final long zobrist, final int value) {
        final int hashed = hash(zobrist);
        final int startIndex = hashed / 3 * 3;
        final int zobristHigh = (int) (zobrist >> 32);
        final int zobristLow = (int) (zobrist & 0xFFFFFFFFL);

        for (int i = startIndex; i < array.length; i += 3) {
            if (array[i] == zobristHigh && array[i + 1] == zobristLow) {
                array[i + 2] = value;
                return;
            } else if (array[i] == 0) {
                array[i] = zobristHigh;
                array[i + 1] = zobristLow;
                array[i + 2] = value;
                return;
            } else {
                final int distance = (i - startIndex) / 3;
                if (distance >= MAX_CHECK_COUNT) {
                    array[i] = zobristHigh;
                    array[i + 1] = zobristLow;
                    array[i + 2] = value;
                    return;
                }
            }
        }
        final int toCheck = MAX_CHECK_INDEX - (array.length - startIndex);
        for (int i = 0; i < startIndex; i += 3) {
            if (array[i] == zobristHigh && array[i + 1] == zobristLow) {
                array[i + 2] = value;
                return;
            } else if (array[i] == 0) {
                array[i] = zobristHigh;
                array[i + 1] = zobristLow;
                array[i + 2] = value;
                return;
            } else if (i >= toCheck) {
                array[i] = zobristHigh;
                array[i + 1] = zobristLow;
                array[i + 2] = value;
                return;
            }
        }
    }

    private int hash(final long zobrist) {
        int hash = (int) (zobrist % array.length);
        if (hash < 0) {
            hash += array.length;
        }
        return hash;
    }

    public void clear() {
        Arrays.fill(array, 0);
    }
}