package tools;

import static sf.pnr.base.Utils.*;

public class AttackArrayGenerator {

    private static final String[] LONG_LEADING_ZEROS = new String[17];

    static {
        final StringBuilder builder = new StringBuilder(16);
        for (int i = 16; i >= 0; i--) {
            LONG_LEADING_ZEROS[i] = builder.toString();
            builder.append('0');
        }
    }

    public static void main(final String[] args) {
        final long[] attackArray = new long[240];
        generateAttacks(attackArray);
        print(int.class, "ATTACK_ARRAY", 4, 12, attackArray);
    }

    private static void generateAttacks(final long[] attackArray) {
        for (int delta: DELTA_KNIGHT) {
            attackArray[delta + 120] |= ((delta + 64) << SHIFT_ATTACK_DELTA) | ATTACK_N;
        }
        for (int delta: DELTA_KING) {
            attackArray[delta + 120] |= ATTACK_K;
        }
        for (int delta: DELTA_BISHOP) {
            for (int i = 1; i < 8; i++) {
                attackArray[delta * i + 120] |= ((delta + 64) << SHIFT_ATTACK_DELTA) | ATTACK_B;
            }
        }
        for (int delta: DELTA_ROOK) {
            for (int i = 1; i < 8; i++) {
                attackArray[delta * i + 120] |= ((delta + 64) << SHIFT_ATTACK_DELTA) | ATTACK_R;
            }
        }
        for (int i = 0; i < attackArray.length; i++) {
            if (attackArray[i] == 0) {
                attackArray[i] = 64 << SHIFT_ATTACK_DELTA;
            }
        }
        // add distance
        for (int pos = 0; pos < 120; pos++) {
            if ((pos & 0x88) == 0) {
                final int file = getFile(pos);
                final int rank = getRank(pos);
                attackArray[pos + 120] |= Math.max(file, rank) << SHIFT_ATTACK_DISTANCE;
                attackArray[pos + 120 - 2 * file] |= Math.max(file, rank) << SHIFT_ATTACK_DISTANCE;
                attackArray[120 - pos] |= Math.max(file, rank) << SHIFT_ATTACK_DISTANCE;
                attackArray[120 - pos + 2 * file] |= Math.max(file, rank) << SHIFT_ATTACK_DISTANCE;
            }
        }
    }

    public static void print(final Class type, final String name, final int maxHexChars, final long... array) {
        int entrySize = 2 + maxHexChars + 2;

        final boolean isLong = long.class.equals(type);
        if (isLong) {
            entrySize++;
        }
        final int valuesOnOneLine = (120 - 2 * 4) / entrySize;
        print(type, name, maxHexChars, valuesOnOneLine, array);
    }

    public static void print(final Class type, final String name, final int maxHexChars, final int valuesOnOneLine,
                              final long... array) {
        final int len = array.length;
        final StringBuilder builder = new StringBuilder(len * 10);
        builder.append("    public static final ").append(type.getName());
        if (len > 1) {
            builder.append("[]");
        }
        builder.append(' ');
        builder.append(name);
        builder.append(" = ");
        if (len > 1) {
            builder.append("new ").append(type.getName()).append("[]{");
        }
        if (len > 4) {
            builder.append("\r\n\t\t");
        }
        final boolean isLong = long.class.equals(type);
        for (int i = 0; i < len; i++) {
            builder.append(toHexString(array[i], maxHexChars, isLong? "L": ""));
            if (i != len - 1) {
                builder.append(",");
                if (i % valuesOnOneLine == valuesOnOneLine - 1) {
                    builder.append("\r\n\t\t");
                } else {
                    builder.append(" ");
                }
            }
        }
        if (len > 1) {
            builder.append('}');
        }
        builder.append(';');
        System.out.println(builder.toString());
    }

    public static String toHexString(final long value, final int maxHexChars, final String suffix) {
        final StringBuilder builder = new StringBuilder(20);
        builder.append("0x");
        final String hex = Long.toHexString(value).toUpperCase();
        builder.append(LONG_LEADING_ZEROS[16 - maxHexChars + hex.length()]);
        builder.append(hex);
        builder.append(suffix);
        return builder.toString();
    }
}