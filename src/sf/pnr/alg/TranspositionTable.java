package sf.pnr.alg;

import java.util.Arrays;

import static sf.pnr.base.Utils.BASE_INFO;

/**
 */
public class TranspositionTable {
    private static final int ARRAY_SIZE_SHIFT = 20;
    private static final int ARRAY_SIZE = 1 << ARRAY_SIZE_SHIFT;
    private static final int ARRAY_LENGHT = ARRAY_SIZE >> 3;
    private static final int ARRAY_MASK = (ARRAY_LENGHT >> 1) - 1;

    public static final long TT_VALUE = 0x000000000000FFFFL;
    public static final long TT_DEPTH = 0x0000000000FF0000L;
    public static final int TT_SHIFT_VALUE = 0;
    public static final int TT_SHIFT_DEPTH = 16;
    public static final int TT_SHIFT_MOVE  = 32;
    public static final long TT_MOVE  = ((long) BASE_INFO) << TT_SHIFT_MOVE;
    public static final int TT_SHIFT_TYPE  = 32 + Integer.bitCount(BASE_INFO); // 32 + 17
    public static final long TT_TYPE = 0x03L << TT_SHIFT_TYPE;
    public static final long TT_TYPE_EXACT = 0x00L << TT_SHIFT_TYPE;
    public static final long TT_TYPE_ALPHA_CUT = 0x01L << TT_SHIFT_TYPE;
    public static final long TT_TYPE_BETA_CUT = 0x02L << TT_SHIFT_TYPE;
    public static final int TT_SHIFT_AGE = 2 + TT_SHIFT_TYPE; // 2 + 49
    public static final long TT_AGE = 0x0FFFL << TT_SHIFT_AGE;

    private final long[][] arrays;
    private final int[] entryCounts;

    public TranspositionTable(final int size) {
        final int arrayCount = size >> ARRAY_SIZE_SHIFT;
        arrays = new long[arrayCount][ARRAY_LENGHT];
        entryCounts = new int[arrayCount];
    }

    public long read(final long zobrist) {
        final int hashed = hash(zobrist);
        final int startIndex = hashed << 1;
        int arrayIndex = (int) (zobrist % arrays.length);
        if (arrayIndex < 0) {
            arrayIndex += arrays.length;
        }
        final long[] array = arrays[arrayIndex];

        for (int i = startIndex; i < ARRAY_LENGHT; i += 2) {
            if (array[i] == zobrist) {
                return array[i + 1];
            } else if (array[i] == 0) {
                return 0;
            } else if (i > startIndex + 40) {
                return 0;
            }
        }
        final int toCheck = 40 - (ARRAY_LENGHT - startIndex);
        for (int i = 0; i < startIndex; i += 2) {
            if (array[i] == zobrist) {
                return array[i + 1];
            } else if (array[i] == 0) {
                return 0;
            } else if (i > toCheck) {
                return 0;
            }
        }
        return 0;
    }

    public void set(final long zobrist, final long type, final int move, final int depth, final int value,
                    final int age) {
        final int hashed = hash(zobrist);
        final int startIndex = hashed << 1;
        int arrayIndex = (int) (zobrist % arrays.length);
        if (arrayIndex < 0) {
            arrayIndex += arrays.length;
        }
        final long[] array = arrays[arrayIndex];
        final long ttValue = type | (((long) (move & BASE_INFO)) << TT_SHIFT_MOVE) | (depth << TT_SHIFT_DEPTH) |
            (value << TT_SHIFT_VALUE) | (((long) age) << TT_SHIFT_AGE);
        for (int i = startIndex; i < ARRAY_LENGHT; i += 2) {
            if (array[i] == zobrist) {
                array[i + 1] = ttValue;
                return;
            } else if (array[i] == 0) {
                array[i] = zobrist;
                array[i + 1] = ttValue;
                entryCounts[arrayIndex]++;
                return;
            } else {
                final int distance = (i - startIndex) >> 1;
                final long ttStored = array[i + 1];
                // if it's too old or we've tried at least 20 and it's not an exact match then overwrite it
                final long ttAge = (ttStored & TT_AGE) >> TT_SHIFT_AGE;
                if ((ttAge < age - 15 + (distance >> 1)) || distance > 10 && (ttStored & TT_TYPE) != TT_TYPE_EXACT ||
                        distance >= 20) {
                    array[i] = zobrist;
                    array[i + 1] = ttValue;
                    return;
                }
            }
        }
        final int checked = ARRAY_LENGHT - startIndex;
        for (int i = 0; i < startIndex; i += 2) {
            if (array[i] == zobrist) {
                array[i + 1] = ttValue;
                return;
            } else if (array[i] == 0) {
                array[i] = zobrist;
                array[i + 1] = ttValue;
                entryCounts[arrayIndex]++;
                return;
            } else if (i > 10 && (array[i + 1] & TT_TYPE) != TT_TYPE_EXACT) {
                array[i] = zobrist;
                array[i + 1] = ttValue;
                return;
            } else {
                final int distance = (i + checked) >> 1;
                final long ttStored = array[i + 1];
                // if it's too old or we've tried at least 20 and it's not an exact match then overwrite it
                final long ttAge = (ttStored & TT_AGE) >> TT_SHIFT_AGE;
                if ((ttAge < age - 15 + (distance >> 1)) || distance > 10 && (ttStored & TT_TYPE) != TT_TYPE_EXACT ||
                        distance > 20) {
                    array[i] = zobrist;
                    array[i + 1] = ttValue;
                    return;
                }
            }
        }
    }

    private int hash(final long zobrist) {
        return (int) (zobrist & ARRAY_MASK);
    }

    public void clear() {
        for (long[] array : arrays) {
            Arrays.fill(array, 0);
        }
        Arrays.fill(entryCounts, 0);
    }

    public int[] getEntryCounts() {
        return entryCounts;
    }
}