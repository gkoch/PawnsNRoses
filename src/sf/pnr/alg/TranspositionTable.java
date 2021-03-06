package sf.pnr.alg;

import sf.pnr.base.Configurable;
import sf.pnr.base.Evaluation;
import sf.pnr.base.Utils;

import java.util.Arrays;

import static sf.pnr.base.Utils.BASE_INFO;

/**
 */
public final class TranspositionTable {
    private static final int ARRAY_SIZE_SHIFT = 20;
    private static final int ARRAY_SIZE = 1 << ARRAY_SIZE_SHIFT;
    private static final int ARRAY_LENGHT = ARRAY_SIZE >> 3;
    private static final int ARRAY_MASK = (ARRAY_LENGHT >> 1) - 1;

    public static final long TT_VALUE = 0x000000000000FFFFL;
    public static final long TT_DEPTH = 0x0000000000FF0000L;
    public static final int TT_SHIFT_VALUE = 0;
    public static final int TT_SHIFT_DEPTH = 16;
    public static final int TT_SHIFT_MOVE  = 24;
    public static final long TT_MOVE  = ((long) BASE_INFO) << TT_SHIFT_MOVE;
    public static final int TT_SHIFT_TYPE  = TT_SHIFT_MOVE + Integer.bitCount(BASE_INFO); // 24 + 17
    public static final long TT_TYPE = 0x03L << TT_SHIFT_TYPE;
    public static final long TT_TYPE_EXACT = 0x00L << TT_SHIFT_TYPE;
    public static final long TT_TYPE_ALPHA_CUT = 0x01L << TT_SHIFT_TYPE;
    public static final long TT_TYPE_BETA_CUT = 0x02L << TT_SHIFT_TYPE; // 41 + 2 = 43 bits

    public static final int TT_SHIFT_AGE = 6;
    public static final long TT_AGE = 0x03FFL << TT_SHIFT_AGE;
    public static final long TT_ZOBRIST_MASK = ~TT_AGE;

    private static final int MAX_CHECK_COUNT = 16;
    private static final int MAX_CHECK_INDEX = 2 * MAX_CHECK_COUNT;

    @Configurable(Configurable.Key.TRANSP_TABLE_SIZE)
    private static int TABLE_SIZE = 1;

    private final long[][] arrays;
    private long age;

    public TranspositionTable() {
        final int size = TABLE_SIZE * 1024 * 1024;
        final int arrayCount = size >>> ARRAY_SIZE_SHIFT;
        arrays = new long[arrayCount][ARRAY_LENGHT];
    }

    public long read(final long zobrist) {
        final int hashed = hash(zobrist);
        final int startIndex = hashed << 1;
        final long[] array = getArraySegment(zobrist);
        final long maskedZobrist = zobrist & TT_ZOBRIST_MASK;

        for (int i = startIndex; i < ARRAY_LENGHT; i += 2) {
            if ((array[i] & TT_ZOBRIST_MASK) == maskedZobrist) {
                return array[i + 1];
            } else if (array[i] == 0) {
                return 0;
            } else if (i > startIndex + MAX_CHECK_INDEX) {
                return 0;
            }
        }
        final int toCheck = MAX_CHECK_INDEX - (ARRAY_LENGHT - startIndex);
        for (int i = 0; i < startIndex; i += 2) {
            if ((array[i] & TT_ZOBRIST_MASK) == maskedZobrist) {
                return array[i + 1];
            } else if (array[i] == 0) {
                return 0;
            } else if (i > toCheck) {
                return 0;
            }
        }
        return 0;
    }

    public void set(final long zobrist, final long type, final int move, final int depth, final int value) {
        assert (Utils.getFromPosition(move) & 0x88) == 0;
        assert (Utils.getToPosition(move) & 0x88) == 0;
        assert move != 0 || type != TT_TYPE_EXACT;
        assert value < Evaluation.VAL_MIN * -2;
        final int hashed = hash(zobrist);
        final int startIndex = hashed << 1;
        final long[] array = getArraySegment(zobrist);
        final long ttValue = type | (((long) (move & BASE_INFO)) << TT_SHIFT_MOVE) | (depth << TT_SHIFT_DEPTH) |
            (value << TT_SHIFT_VALUE);
        int minAge = Integer.MAX_VALUE;
        int minAgeIndex = startIndex;
        final long maskedZobrist = zobrist & TT_ZOBRIST_MASK;
        for (int i = startIndex; i < ARRAY_LENGHT; i += 2) {
            if ((array[i] & TT_ZOBRIST_MASK) == maskedZobrist) {
                final int ttDepth = (int) ((array[i + 1] & TT_DEPTH) >>> TT_SHIFT_DEPTH);
                if (ttDepth < depth || ttDepth < depth + 2 && (array[i + 1] & TT_TYPE) != TT_TYPE_EXACT && type == TT_TYPE_EXACT) {
                    array[i + 1] = ttValue;
                }
                return;
            } else if (array[i] == 0) {
                array[i] = maskedZobrist | age << TT_SHIFT_AGE;
                array[i + 1] = ttValue;
                return;
            } else if (i > startIndex + MAX_CHECK_INDEX) {
                array[minAgeIndex] = maskedZobrist | age << TT_SHIFT_AGE;
                array[minAgeIndex + 1] = ttValue;
                return;
            } else {
                final int mult = (array[i + 1] & TT_TYPE) > 0? 2: 3;
                final int ttAge = (int) ((array[i] & TT_AGE) >>> TT_SHIFT_AGE) * mult;
                if (ttAge < minAge) {
                    minAge = ttAge;
                    minAgeIndex = i;
                }
            }
        }
        final int toCheck = MAX_CHECK_INDEX - (ARRAY_LENGHT - startIndex);
        for (int i = 0; i < startIndex; i += 2) {
            if ((array[i] & TT_ZOBRIST_MASK) == maskedZobrist) {
                final int ttDepth = (int) ((array[i + 1] & TT_DEPTH) >>> TT_SHIFT_DEPTH);
                if (ttDepth < depth || ttDepth < depth + 2 && (array[i + 1] & TT_TYPE) != TT_TYPE_EXACT && type == TT_TYPE_EXACT) {
                    array[i + 1] = ttValue;
                }
                return;
            } else if (array[i] == 0) {
                array[i] = maskedZobrist | age << TT_SHIFT_AGE;
                array[i + 1] = ttValue;
                return;
            } else if (i > toCheck) {
                array[minAgeIndex] = maskedZobrist | age << TT_SHIFT_AGE;
                array[minAgeIndex + 1] = ttValue;
                return;
            } else {
                final int mult = (array[i + 1] & TT_TYPE) > 0? 2: 3;
                final int ttAge = (int) ((array[i] & TT_AGE) >>> TT_SHIFT_AGE) * mult;
                if (ttAge < minAge) {
                    minAge = ttAge;
                    minAgeIndex = i;
                }
            }
        }
    }

    private long[] getArraySegment(final long zobrist) {
        int arrayIndex = (int) ((zobrist >> 32) % arrays.length);
        if (arrayIndex < 0) {
            arrayIndex += arrays.length;
        }
        return arrays[arrayIndex];
    }

    private int hash(final long zobrist) {
        return (int) (zobrist & ARRAY_MASK);
    }

    public void clear() {
        for (long[] array: arrays) {
            Arrays.fill(array, 0);
        }
    }

    public int scan() {
        int emptySlots = 0;
        for (long[] array: arrays) {
            for (int i = 0; i < array.length; i += 2) {
                if (array[i] == 0) {
                    emptySlots++;
                }
            }
        }
        return emptySlots;
    }

    public void setAge(final long age) {
        this.age = age;
    }
}