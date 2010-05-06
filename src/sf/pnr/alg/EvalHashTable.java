package sf.pnr.alg;

import sf.pnr.base.Configuration;

import java.util.Arrays;

/**
 */
public final class EvalHashTable {
    private static final long VALUE_SHIFT = 1;
    private static final long VALUE_MASK = 0x1FFFE;
    private static final long HASH_MASK = 0x1FFFF;
    private static final long ZOBRIST_MASK = 0xFFFFFFFFFFFFFFFFL ^ VALUE_MASK;

    private final int sizeInMB;
    private final long[] array;

    public EvalHashTable() {
        sizeInMB = Configuration.getInstance().getEvalHashTableSizeInMB();
        array = new long[sizeInMB * 1024 * 1024 / 8];
    }

    public int read(final long zobrist) {
        final int hashed = hash(zobrist);
        final long zobristMasked = zobrist & ZOBRIST_MASK;
        if ((array[hashed] & ZOBRIST_MASK) == zobristMasked) {
            return (int) ((array[hashed] & VALUE_MASK) >> VALUE_SHIFT);
        }
        if ((array[hashed + 1] & ZOBRIST_MASK) == zobristMasked) {
            return (int) ((array[hashed + 1] & VALUE_MASK) >> VALUE_SHIFT);
        }
        return 0;
    }

    public void set(final long zobrist, final int value) {
        final int hashed = hash(zobrist);
        final long zobristMasked = zobrist & ZOBRIST_MASK;
        if (array[hashed] == 0L) {
            array[hashed] = zobristMasked | (value << VALUE_SHIFT);
        } else {
            array[hashed + 1] = zobristMasked | (value << VALUE_SHIFT);
        }
    }

    private int hash(final long zobrist) {
        final long offset = zobrist & HASH_MASK;
        final long segment = (zobrist & (~HASH_MASK)) % sizeInMB;
        return (int) (segment * (HASH_MASK + 1) + offset);
    }

    public void clear() {
        Arrays.fill(array, 0L);
    }
}