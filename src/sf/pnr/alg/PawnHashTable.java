package sf.pnr.alg;

import sf.pnr.base.Configurable;

import java.util.Arrays;

/**
 */
public class PawnHashTable {
    public static final int VALUE_MASK = 0x0FFF;
    private static final int STAGE_MASK = 0x3F;
    private static final int STAGE_SHIFT = 12;
    public static final int UNSTOPPABLE_PAWN_MASK = 0x07;
    public static final int UNSTOPPABLE_PAWN_WHITE_SHIFT = 18;
    public static final int UNSTOPPABLE_PAWN_IF_NEXT_SHIFT = 3;
    public static final int UNSTOPPABLE_PAWN_BLACK_SHIFT = 24;
    public static final int VALUE_OFFSET = (int) (VALUE_MASK >> 1);
    private static final int STAGE_IN_FRONT_OF_LIMIT = 5;
    private static final int STAGE_BEHIND_LIMIT = 5;

    @Configurable(Configurable.Key.EVAL_PAWNTABLE_SIZE)
    private static int TABLE_SIZE = 1;

    private final int[] array;

    public PawnHashTable() {
        array = new int[TABLE_SIZE * 1024 * 1024 / 12 * 3];
    }

    public int get(final long zobrist, final int stage) {
        final int hashed = hash(zobrist);
        final int index = hashed / 3 * 3;
        final int zobristHigh = (int) (zobrist >>> 32);
        final int zobristLow = (int) (zobrist & 0xFFFFFFFFL);

        if (zobrist != 0L && array[index] == zobristHigh && array[index + 1] == zobristLow) {
            final int storedValue = array[index + 2];
            final long storedStage = (storedValue >>> STAGE_SHIFT) & STAGE_MASK;
            if (storedStage - stage < STAGE_BEHIND_LIMIT && stage - storedStage < STAGE_IN_FRONT_OF_LIMIT) {
                return storedValue;
            }
        }
        return 0;
    }

    public void set(final long zobrist, final int pawnHashValue) {
        final int hashed = hash(zobrist);
        final int index = hashed / 3 * 3;
        final int zobristHigh = (int) (zobrist >>> 32);
        final int zobristLow = (int) (zobrist & 0xFFFFFFFFL);
        array[index] = zobristHigh;
        array[index + 1] = zobristLow;
        array[index + 2] = pawnHashValue;
    }

    private int hash(final long zobrist) {
        int hash = (int) (zobrist % array.length);
        if (hash < 0) {
            hash += array.length;
        }
        return hash & (~1);
    }

    public void clear() {
        Arrays.fill(array, 0);
    }

    public int scan() {
        int emptySlots = 0;
        for (int i = 0; i < array.length; i += 3) {
            if (array[i] == 0 && array[i + 1] == 0) {
                emptySlots++;
            }
        }
        return emptySlots;
    }

    public static int getValueFromPawnHashValue(final int pawnHashValue) {
        return (pawnHashValue & VALUE_MASK) - VALUE_OFFSET;
    }

    public static int getUnstoppablePawnDistWhite(final int pawnHashValue, final int toMove) {
        final int shift = UNSTOPPABLE_PAWN_WHITE_SHIFT + toMove * UNSTOPPABLE_PAWN_IF_NEXT_SHIFT;
        return (pawnHashValue  >>> shift) & UNSTOPPABLE_PAWN_MASK;
    }

    public static int getUnstoppablePawnDistBlack(final int pawnHashValue, final int toMove) {
        final int shift = UNSTOPPABLE_PAWN_BLACK_SHIFT + (1 - toMove) * UNSTOPPABLE_PAWN_IF_NEXT_SHIFT;
        return (pawnHashValue  >>> shift) & UNSTOPPABLE_PAWN_MASK;
    }

    public static int getPawnHashValue(final int value, final int stage,
                                       final int unstoppablePawnWhite, final int unstoppablePawnIfNextWhite,
                                       final int unstoppablePawnBlack, final int unstoppablePawnIfNextBlack) {
        final int storedValue = value + VALUE_OFFSET;
        assert storedValue > 0;
        assert storedValue <= VALUE_MASK;
        assert stage >= 0;
        assert stage <= STAGE_MASK;
        assert ((stage << STAGE_SHIFT) & VALUE_MASK) == 0;
        return (((unstoppablePawnIfNextWhite << UNSTOPPABLE_PAWN_IF_NEXT_SHIFT) | unstoppablePawnWhite) << UNSTOPPABLE_PAWN_WHITE_SHIFT) |
            (((unstoppablePawnIfNextBlack << UNSTOPPABLE_PAWN_IF_NEXT_SHIFT) | unstoppablePawnBlack) << UNSTOPPABLE_PAWN_BLACK_SHIFT) |
            (stage << STAGE_SHIFT) | storedValue;
    }
}