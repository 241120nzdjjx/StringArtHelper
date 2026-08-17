/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Drives GenCore with the same synthetic image as the JS float32 test and
 * prints the resulting sequence (and a hash) for byte-comparison.
 */
import java.io.*;
import java.util.*;

public class GenCompare {
    public static void main(String[] args) throws Exception {
        int size = 256;
        // Same synthetic disc as tests/run-tests.js syntheticImage(256,'disc').
        int[] pixels = new int[size * size];
        Random r = new Random(42); // deterministic, though image is fixed
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - size / 2.0;
                double dy = y - size / 2.0;
                int v = Math.hypot(dx, dy) < size * 0.32 ? 40 : 250;
                pixels[y * size + x] = (255 << 24) | (v << 16) | (v << 8) | v;
            }
        }

        // Case 1: disc, 220 nails, 800 lines, circle 260, line 0.2, autoStop.
        ArrayList<Integer> seq1 = GenCore.generate(pixels, size, size,
                220, 800, 260, 0.2f, true, 0.5f, 0.5f, 1f, null, null);
        System.out.println("CASE1_LEN=" + seq1.size());
        StringBuilder sb = new StringBuilder();
        for (int v : seq1) { if (sb.length() > 0) sb.append(' '); sb.append(v); }
        System.out.println("CASE1_SEQ=" + sb.toString());
        System.out.println("CASE1_HASH=" + Integer.toHexString(sb.toString().hashCode()));

        // Case 2: half-plane (left dark), 160 nails, 500 lines, no autoStop? autoStop true.
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int v = x < size * 0.5 ? 40 : 250;
                pixels[y * size + x] = (255 << 24) | (v << 16) | (v << 8) | v;
            }
        }
        ArrayList<Integer> seq2 = GenCore.generate(pixels, size, size,
                160, 500, 260, 0.2f, true, 0.25f, 0.5f, 1.6f, null, null);
        System.out.println("CASE2_LEN=" + seq2.size());
        StringBuilder sb2 = new StringBuilder();
        for (int v : seq2) { if (sb2.length() > 0) sb2.append(' '); sb2.append(v); }
        System.out.println("CASE2_SEQ=" + sb2.toString());
        System.out.println("CASE2_HASH=" + Integer.toHexString(sb2.toString().hashCode()));
    }
}
