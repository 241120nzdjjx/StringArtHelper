/*
 * SPDX-License-Identifier: GPL-3.0-only
 * 1:1 copy of the Android StringArtGenerator.java with android.graphics.Bitmap
 * replaced by (int[] pixels, int width, int height) so it runs on a plain JDK.
 * Used by GenCompare to byte-compare JS float32 output against real Java float.
 */
public final class GenCore {
    public interface ProgressListener { void onProgress(int complete, int total); }

    private static final int WORK_SIZE = 256;
    private static final int RECENT_PIN_WINDOW = 20;
    static final float MAX_CROP_ZOOM = 4f;
    static final float TARGET_RADIUS_RATIO = 251f / 255f;
    private static final float FULL_IMAGE_FIT_MARGIN = .98f;
    private static final float MAX_AVERAGE_COVERAGE = 2.6f;

    private GenCore() { }

    static float minimumCropZoom(int[] source, int width, int height) {
        if (source == null || width <= 0 || height <= 0) return 1f;
        float w = width;
        float h = height;
        float diagonal = (float) Math.hypot(w, h);
        float base = Math.min(w, h);
        return Math.min(1f, base * TARGET_RADIUS_RATIO * FULL_IMAGE_FIT_MARGIN / diagonal);
    }

    static float clampCropZoom(int[] source, int width, int height, float zoom) {
        return Math.max(minimumCropZoom(source, width, height), Math.min(MAX_CROP_ZOOM, zoom));
    }

    static float cropSizePx(int[] source, int width, int height, float zoom) {
        return Math.min(width, height) / clampCropZoom(source, width, height, zoom);
    }

    static java.util.ArrayList<Integer> generate(int[] pixels, int width, int height,
                                                 int pinCount, int requestedLines,
                                                 int circleMm, float lineMm, boolean autoStop,
                                                 float cropX, float cropY, float cropZoom,
                                                 java.util.concurrent.atomic.AtomicBoolean cancelled,
                                                 ProgressListener listener) {
        int size = WORK_SIZE;
        float[] residual = makeTarget(pixels, width, height, cropX, cropY, cropZoom);
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
        java.util.ArrayList<Integer> result = new java.util.ArrayList<Integer>(requestedLines + 1);
        int current = 0;
        int[] recentPins = new int[Math.min(RECENT_PIN_WINDOW, pinCount)];
        int recentCount = 1;
        int recentCursor = 1 % recentPins.length;
        recentPins[0] = current;
        double threadMm = 0d;
        double areaMm2 = Math.PI * circleMm * circleMm * .25d;
        result.add(current);
        for (int step = 0; step < requestedLines; step++) {
            if (cancelled != null && cancelled.get()) return null;
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
                float score = scoreLine(residual, path, lineDarkness);
                if (score > bestScore) { bestScore = score; best = candidate; }
            }
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
            if (autoStop && threadMm * safeLineMm / areaMm2 >= MAX_AVERAGE_COVERAGE
                    && bestScore / selected.length < lineDarkness * lineDarkness * .15f) break;
        }
        return result;
    }

    private static float[] makeTarget(int[] pixels, int width, int height,
                                      float cropX, float cropY, float cropZoom) {
        int size = WORK_SIZE;
        float crop = cropSizePx(pixels, width, height, cropZoom);
        float x0 = cropX * width - crop * .5f;
        float y0 = cropY * height - crop * .5f;
        float[] target = new float[size * size];
        float center = (size - 1) * 0.5f;
        float radius = center - 2f;
        for (int y = 0; y < size; y++) {
            int sy = Math.round(y0 + y / (size - 1f) * (crop - 1f));
            if (sy < 0 || sy >= height) continue;
            for (int x = 0; x < size; x++) {
                float dx = x - center, dy = y - center;
                if (dx * dx + dy * dy > radius * radius) continue;
                int sx = Math.round(x0 + x / (size - 1f) * (crop - 1f));
                if (sx < 0 || sx >= width) continue;
                int color = pixels[sy * width + sx];
                float alpha = ((color >>> 24) & 255) / 255f;
                float luminance = ((color >> 16 & 255) * 0.2126f + (color >> 8 & 255) * 0.7152f
                        + (color & 255) * 0.0722f) / 255f;
                luminance = luminance * alpha + (1f - alpha);
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

    private static float squaredErrorGain(float residual, float amount) {
        return 2f * amount * residual - amount * amount;
    }

    private static float scoreLine(float[] residual, int[] path, float amount) {
        float score = 0f;
        for (int p : path) score += squaredErrorGain(residual[p], amount);
        return score;
    }

    private static void subtractLine(float[] residual, int[] path, int size,
                                     float widthPx, float opacity, float subPixelAmount) {
        for (int i = 0; i < path.length; i++) {
            int p = path[i];
            int x = p % size, y = p / size;
            if (widthPx < 1f) {
                residual[p] -= subPixelAmount;
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
