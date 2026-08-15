/* SPDX-License-Identifier: GPL-3.0-only */
package com.niuza.stringartreader2;

import java.util.ArrayList;
import java.util.zip.CRC32;

/** Pure-Java bit packing shared by the Android sender and build-time tests. */
public final class WearableBitPacking {
    private WearableBitPacking() { }

    public static int bitsForNails(int nails) {
        int bits = 1;
        int maximum = Math.max(1, nails - 1);
        while (bits < 16 && (1 << bits) <= maximum) bits++;
        return bits;
    }

    public static byte[] pack(ArrayList<Integer> values, int nails) {
        int bits = bitsForNails(nails);
        byte[] output = new byte[(values.size() * bits + 7) / 8];
        int bitOffset = 0;
        for (int value : values) {
            if (value < 0 || value >= nails) throw new IllegalArgumentException("nail out of range");
            for (int bit = 0; bit < bits; bit++, bitOffset++)
                if (((value >>> bit) & 1) != 0)
                    output[bitOffset >>> 3] |= (byte) (1 << (bitOffset & 7));
        }
        return output;
    }

    public static long crc32(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }
}
