/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 */
package com.niuza.stringartreader2;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Offline, deliberately dependency-free greedy string-art generator.
 * Coordinates use the same convention as the player/template: pin 0 is at the
 * right edge and increasing pin numbers travel clockwise on an Android canvas.
 */
final class StringArtGenerator {
    interface ProgressListener { void onProgress(int complete, int total); }

    private static final int WORK_SIZE = 256;
    /**
     * Do not immediately revisit a recently used nail.  Without this short
     * tabu window the greedy picker tends to oscillate between a few strong
     * facial edges and spends a disproportionate number of strings there.
     */
    private static final int RECENT_PIN_WINDOW = 20;
    static final float MAX_CROP_ZOOM = 4f;
    /** The target stops two working pixels inside the 256 px square. */
    static final float TARGET_RADIUS_RATIO = 251f / 255f;
    /** Keep the fitted image just inside the target circle instead of touching it. */
    private static final float FULL_IMAGE_FIT_MARGIN = .98f;
    /* This is an emergency cap only.  Physical line coverage is not ink coverage:
       intersections overlap, therefore the old 0.62 cap stopped fine work after
       only a few hundred strings on a small board. */
    private static final float MAX_AVERAGE_COVERAGE = 2.6f;

    private StringArtGenerator() { }

    static float minimumCropZoom(Bitmap source) {
        if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0) return 1f;
        float width = source.getWidth();
        float height = source.getHeight();
        float diagonal = (float) Math.hypot(width, height);
        float base = Math.min(width, height);
        return Math.min(1f, base * TARGET_RADIUS_RATIO * FULL_IMAGE_FIT_MARGIN / diagonal);
    }

    static float clampCropZoom(Bitmap source, float zoom) {
        return Math.max(minimumCropZoom(source), Math.min(MAX_CROP_ZOOM, zoom));
    }

    static float cropSizePx(Bitmap source, float zoom) {
        return Math.min(source.getWidth(), source.getHeight()) / clampCropZoom(source, zoom);
    }

    static ArrayList<Integer> generate(Bitmap source, int pinCount, int requestedLines,
                                       int circleMm, float lineMm, boolean autoStop, float cropX, float cropY, float cropZoom,
                                       AtomicBoolean cancelled, ProgressListener listener) {
        int size = WORK_SIZE;
        float[] residual = makeTarget(source, size, cropX, cropY, cropZoom);
        float safeLineMm = Math.max(.01f, Math.min(1f, lineMm));
        final float threadWidthPx = Math.max(.12f,
                TARGET_RADIUS_RATIO * (WORK_SIZE - 1f) * safeLineMm
                        / Math.max(80f, circleMm));
        final float threadOpacity = Math.max(26f, Math.min(82f,
                26f + threadWidthPx * 90f)) / 255f;
        final float lineDarkness = threadOpacity * Math.min(1f, threadWidthPx);
        final int minPinGap = Math.max(8, pinCount / 28);
        int[] pinX = new int[pinCount];
        int[] pinY = new int[pinCount];
        float center = (size - 1) * 0.5f;
        float radius = center - 3f;
        for (int i = 0; i < pinCount; i++) {
            double angle = Math.PI * 2d * i / pinCount;
            pinX[i] = Math.round(center + (float) Math.cos(angle) * radius);
            pinY[i] = Math.round(center + (float) Math.sin(angle) * radius);
        }
        int[][] paths = new int[pinCount * pinCount][];
        ArrayList<Integer> result = new ArrayList<Integer>(requestedLines + 1);
        int current = 0;
        int[] recentPins = new int[Math.min(RECENT_PIN_WINDOW, pinCount)];
        int recentCount = 1;
        int recentCursor = 1 % recentPins.length;
        recentPins[0] = current;
        double threadMm = 0d;
        double areaMm2 = Math.PI * circleMm * circleMm * .25d;
        result.add(current);
        for (int step = 0; step < requestedLines; step++) {
            if (cancelled.get()) return null;
            int best = -1;
            float bestScore = 0f;
            for (int candidate = 0; candidate < pinCount; candidate++) {
                if (isRecentPin(candidate, recentPins, recentCount)) continue;
                int gap = Math.abs(candidate - current);
                gap = Math.min(gap, pinCount - gap);
                if (gap < minPinGap) continue;
                int key = current * pinCount + candidate;
                int[] path = paths[key];
                if (path == null) {
                    path = makePath(pinX[current], pinY[current], pinX[candidate], pinY[candidate], size);
                    paths[key] = path;
                    paths[candidate * pinCount + current] = path;
                }
                /*
                 * Minimise squared image error instead of merely looking for the
                 * darkest remaining average.  residual is target darkness minus
                 * darkness already drawn, and is deliberately allowed below zero:
                 * drawing through an over-dark pixel must hurt the candidate.
                 *
                 * For one affected sample:
                 *   oldError - newError = r² - (r-a)² = 2ar-a²
                 * Summing (rather than averaging) measures the total improvement
                 * made by the complete physical chord.
                 */
                float score = scoreLine(residual, path, lineDarkness);
                if (score > bestScore) { bestScore = score; best = candidate; }
            }
            // Continuing after every possible chord has negative gain can only
            // make the requested image less accurate, even with auto-stop off.
            if (best < 0 || bestScore <= 0f) break;
            int[] selected = paths[current * pinCount + best];
            subtractLine(residual, selected, size, threadWidthPx, threadOpacity, lineDarkness);
            int gap = Math.abs(current - best);
            gap = Math.min(gap, pinCount - gap);
            threadMm += circleMm * Math.sin(Math.PI * gap / pinCount);
            current = best;
            result.add(current);
            if (recentCount < recentPins.length) {
                recentPins[recentCount++] = current;
                recentCursor = recentCount % recentPins.length;
            } else {
                recentPins[recentCursor] = current;
                recentCursor = (recentCursor + 1) % recentPins.length;
            }
            if (listener != null) listener.onProgress(step + 1, requestedLines);
            // Residual exhaustion is the normal automatic stop.  The coverage cap is
            // deliberately much looser and only catches truly pathological inputs.
            if (autoStop && threadMm * safeLineMm / areaMm2 >= MAX_AVERAGE_COVERAGE
                    && bestScore / selected.length < lineDarkness * lineDarkness * .15f) break;
        }
        return result;
    }

    private static float[] makeTarget(Bitmap source, int size, float cropX, float cropY, float cropZoom) {
        float crop = cropSizePx(source, cropZoom);
        float x0 = cropX * source.getWidth() - crop * .5f;
        float y0 = cropY * source.getHeight() - crop * .5f;
        float[] target = new float[size * size];
        float center = (size - 1) * 0.5f;
        float radius = center - 2f;
        for (int y = 0; y < size; y++) {
            int sy = Math.round(y0 + y / (size - 1f) * (crop - 1f));
            if (sy < 0 || sy >= source.getHeight()) continue;
            for (int x = 0; x < size; x++) {
                float dx = x - center, dy = y - center;
                if (dx * dx + dy * dy > radius * radius) continue;
                int sx = Math.round(x0 + x / (size - 1f) * (crop - 1f));
                // Samples outside the photo are the physical white board. Do not
                // stretch the nearest edge pixel into this otherwise empty area.
                if (sx < 0 || sx >= source.getWidth()) continue;
                int color = source.getPixel(sx, sy);
                // The physical board is white. Transparent PNG pixels otherwise arrive as
                // RGB(0,0,0), which falsely turns the whole transparent background black.
                float alpha = ((color >>> 24) & 255) / 255f;
                float luminance = ((color >> 16 & 255) * 0.2126f + (color >> 8 & 255) * 0.7152f
                        + (color & 255) * 0.0722f) / 255f;
                luminance = luminance * alpha + (1f - alpha);
                // Preserve the full tonal range.  The old 0.90 white cut-off plus
                // 1.35× contrast boost discarded light facial shading and pushed
                // too much of the string budget into eyes and hard outlines.
                target[y * size + x] = 1f - luminance;
            }
        }
        return target;
    }

    private static boolean isRecentPin(int candidate, int[] recentPins, int recentCount) {
        for (int i = 0; i < recentCount; i++) {
            if (recentPins[i] == candidate) return true;
        }
        return false;
    }

    static float squaredErrorGain(float residual, float amount) {
        return 2f * amount * residual - amount * amount;
    }

    private static float scoreLine(float[] residual, int[] path, float amount) {
        float score = 0f;
        for (int p : path) score += squaredErrorGain(residual[p], amount);
        return score;
    }

    /** A real thread has width.  Softly clearing adjacent samples stops the greedy
        picker from repeatedly stacking almost identical chords into dark edge bars. */
    private static void subtractLine(float[] residual, int[] path, int size,
                                     float widthPx, float opacity, float subPixelAmount) {
        for (int i = 0; i < path.length; i++) {
            int p = path[i];
            int x = p % size, y = p / size;
            if (widthPx < 1f) {
                residual[p] -= subPixelAmount;
                // A small antialias fringe discourages repeatedly selecting nearly
                // identical chords without pretending that a thin thread is wider.
                subtractResidual(residual, size, x - 1, y, subPixelAmount * .06f);
                subtractResidual(residual, size, x + 1, y, subPixelAmount * .06f);
                subtractResidual(residual, size, x, y - 1, subPixelAmount * .06f);
                subtractResidual(residual, size, x, y + 1, subPixelAmount * .06f);
                continue;
            }
            int before = path[Math.max(0, i - 1)];
            int after = path[Math.min(path.length - 1, i + 1)];
            float tangentX = after % size - before % size;
            float tangentY = after / size - before / size;
            float length = (float) Math.hypot(tangentX, tangentY);
            float normalX = length > 0f ? -tangentY / length : 0f;
            float normalY = length > 0f ? tangentX / length : 1f;
            int radius = (int) Math.ceil(widthPx * .5f + .5f);
            for (int offset = -radius; offset <= radius; offset++) {
                float coverage = Math.max(0f,
                        Math.min(1f, widthPx * .5f + .5f - Math.abs(offset)));
                if (coverage <= 0f) continue;
                int sampleX = Math.round(x + normalX * offset);
                int sampleY = Math.round(y + normalY * offset);
                subtractResidual(residual, size, sampleX, sampleY, opacity * coverage);
            }
        }
    }

    private static void subtractResidual(float[] residual, int size, int x, int y, float amount) {
        if (x < 0 || y < 0 || x >= size || y >= size) return;
        int p = y * size + x;
        residual[p] -= amount;
    }

    private static int[] makePath(int x0, int y0, int x1, int y1, int size) {
        int dx = x1 - x0, dy = y1 - y0;
        // Score every raster pixel, not every other one.  The previous sparse sample
        // made thin strokes and diagonal outlines disproportionately unstable.
        int count = Math.max(Math.abs(dx), Math.abs(dy)) + 1;
        int[] path = new int[count];
        for (int i = 0; i < count; i++) {
            float t = count == 1 ? 0f : i / (float) (count - 1);
            int x = Math.round(x0 + dx * t);
            int y = Math.round(y0 + dy * t);
            path[i] = y * size + x;
        }
        return path;
    }
}
