package tools;

import sf.pnr.base.Utils;

import static sf.pnr.base.Utils.*;

public class AttackArrayGenerator {

    private static final String[] LONG_LEADING_ZEROS = new String[17];

    private static final int[] KNIGHT_DISTANCE = new int[]
       {0, 3, 2, 5, 4, 3, 4, 5,
        3, 4, 1, 2, 3, 4, 5, 4,
        2, 1, 4, 3, 4, 3, 4, 5,
        5, 2, 3, 2, 3, 4, 5, 4,
        4, 3, 4, 3, 4, 3, 4, 5,
        3, 4, 3, 4, 3, 4, 5, 4,
        4, 5, 4, 5, 4, 5, 4, 5,
        5, 4, 5, 4, 5, 4, 5, 6};

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
        print(int.class, "ATTACK_ARRAY", 8, 8, attackArray);
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
        for (int pos = 1; pos < 120; pos++) {
            if ((pos & 0x88) == 0) {
                final int file = getFile(pos);
                final int rank = getRank(pos);
                final int pos64 = Utils.convert0x88To64(pos);
                
                int distance = Math.max(file, rank) << SHIFT_ATTACK_DISTANCE_KING;
                distance |= (KNIGHT_DISTANCE[pos64] << SHIFT_ATTACK_DISTANCE_KNIGHT);

                final int bishopDistance;
                if (file == rank) {
                    bishopDistance = 1;
                } else if ((file - rank) % 2 == 0) {
                    bishopDistance = 2;
                } else {
                    bishopDistance = 7;
                }

                final int rookDistance;
                if (file == 0 || rank == 0) {
                    rookDistance = 1;
                } else {
                    rookDistance = 2;
                }

                distance |= bishopDistance << SHIFT_ATTACK_DISTANCE_BISHOP | rookDistance << SHIFT_ATTACK_DISTANCE_ROOK |
                    Math.min(bishopDistance, rookDistance) << SHIFT_ATTACK_DISTANCE_QUEEN;

                attackArray[pos + 120] |= distance;
                attackArray[pos + 120 - 2 * file] |= distance;
                attackArray[120 - pos] |= distance;
                attackArray[120 - pos + 2 * file] |= distance;
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