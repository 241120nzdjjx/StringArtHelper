/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 *
 * Greedy string-art generator ported 1:1 from the Android app's
 * StringArtGenerator.java (the project's reference implementation).
 *
 * NUMERIC FIDELITY: Android computes with Java `float` (32-bit IEEE-754)
 * everywhere. JavaScript numbers are `double`, so every intermediate value
 * that Java stores/computes as float is wrapped in Math.fround() here to
 * reproduce the exact float32 rounding — the residual array is a
 * Float32Array (matching Java float[]), sampling coordinates, chord scores,
 * thread-width/opacity and subtract lines all follow float semantics.
 * Double-only paths (threadMeters, auto-stop coverage, sin()) match Java's
 * `double` usage there.
 *
 * Working convention (matches Android and the player/template):
 * pin 0 is at the right edge and increasing pin numbers travel clockwise.
 */
'use strict';

const F = Math.fround;

const WORK_SIZE = 256;
const TARGET_RADIUS_RATIO = F(251 / 255);
const MAX_AVERAGE_COVERAGE = F(2.6);
const FULL_IMAGE_FIT_MARGIN = F(0.98);
const RECENT_PIN_WINDOW = 20;
const MAX_CROP_ZOOM = 4;
/** Cap on cached rasterised chords (symmetric pairs share one entry). */
const PATH_CACHE_LIMIT = 20000;

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value));
}

function minimumCropZoom(width, height) {
  if (!width || !height) return 1;
  const diagonal = Math.hypot(width, height);
  const base = Math.min(width, height);
  // Android: Math.min(1f, base * TARGET_RADIUS_RATIO * FULL_IMAGE_FIT_MARGIN / diagonal)
  // (int*float → float, float*float → float, then float/double → double, result float)
  return F(Math.min(1, F(F(base * TARGET_RADIUS_RATIO) * FULL_IMAGE_FIT_MARGIN) / diagonal));
}

function clampCropZoom(width, height, zoom) {
  return Math.max(minimumCropZoom(width, height), Math.min(MAX_CROP_ZOOM, zoom));
}

function cropSizePx(width, height, zoom) {
  return Math.min(width, height) / clampCropZoom(width, height, zoom);
}

/**
 * Build the 256×256 working residual from a source RGBA buffer, matching
 * Android's makeTarget including float32 semantics and its sample mapping:
 *   sx = round(x0 + x/(size-1) * (crop-1)), x0 = cropX*w - crop/2
 */
function buildResidual(pixels, width, height, cropX, cropY, cropZoom) {
  const size = WORK_SIZE;
  const crop = cropSizePx(width, height, cropZoom);
  // float: cropX * width - crop * 0.5f
  const x0 = F(F(F(cropX) * width) - F(crop * 0.5));
  const y0 = F(F(F(cropY) * height) - F(crop * 0.5));
  const residual = new Float32Array(size * size);
  const center = F((size - 1) * 0.5);
  const radius = F(center - 2);
  const radiusSquared = radius * radius;
  const denom = F(size - 1);
  const cropMinus1 = F(crop - 1);
  for (let y = 0; y < size; y++) {
    // float: round(y0 + y/(size-1) * (crop-1))
    const sy = Math.round(F(F(y0) + F(F(y / denom) * cropMinus1)));
    if (sy < 0 || sy >= height) continue;
    const sourceRow = sy * width * 4;
    for (let x = 0; x < size; x++) {
      const dx = x - center;
      const dy = y - center;
      if (dx * dx + dy * dy > radiusSquared) continue;
      // float: round(x0 + x/(size-1) * (crop-1))
      const sx = Math.round(F(F(x0) + F(F(x / denom) * cropMinus1)));
      if (sx < 0 || sx >= width) continue;
      const offset = sourceRow + sx * 4;
      // float: alpha = ((color >>> 24) & 255) / 255f
      const alpha = F((pixels[offset + 3] == null ? 255 : pixels[offset + 3]) / 255);
      // float luminance (0.2126f / 0.7152f / 0.0722f are float32 constants):
      const luminance = F(
        F(F(F(pixels[offset] * F(0.2126)) + F(pixels[offset + 1] * F(0.7152))) +
          F(pixels[offset + 2] * F(0.0722))) / 255
      );
      // float: luminance = luminance * alpha + (1f - alpha); target = 1f - luminance
      residual[y * size + x] = F(1 - F(F(F(luminance * alpha) + F(1 - alpha))));
    }
  }
  return residual;
}

/** Android makePath with float semantics: t = i/(float)(count-1). */
function makePath(size, x0, y0, x1, y1) {
  const dx = x1 - x0;
  const dy = y1 - y0;
  const count = Math.max(Math.abs(dx), Math.abs(dy)) + 1;
  const path = new Int32Array(count);
  const denom = F(count - 1);
  for (let i = 0; i < count; i++) {
    // float t = count == 1 ? 0f : i / (float)(count - 1);
    const t = count === 1 ? 0 : F(i / denom);
    // int x = Math.round(x0 + dx * t);
    const x = Math.round(F(x0 + F(dx * t)));
    const y = Math.round(F(y0 + F(dy * t)));
    path[i] = y * size + x;
  }
  return path;
}

/** Simple LRU path cache; symmetric pairs share one rasterisation.
 *  makePath is deterministic, so eviction never changes the result. */
function createPathCache(limit) {
  const cache = new Map();
  const capacity = Math.max(1, limit || PATH_CACHE_LIMIT);
  return {
    get(size, pinX, pinY, from, to) {
      const low = from < to ? from : to;
      const high = from < to ? to : from;
      const key = low + ':' + high;
      if (cache.has(key)) {
        const value = cache.get(key);
        cache.delete(key);
        cache.set(key, value);
        return value;
      }
      const value = makePath(size, pinX[from], pinY[from], pinX[to], pinY[to]);
      cache.set(key, value);
      if (cache.size > capacity) cache.delete(cache.keys().next().value);
      return value;
    },
    size() {
      return cache.size;
    }
  };
}

/** Android: 2f * amount * residual - amount * amount (all float). */
function squaredErrorGain(residual, amount) {
  const term1 = F(F(F(2 * amount) * residual));
  const term2 = F(amount * amount);
  return F(term1 - term2);
}

/** Android: float score accumulated per sampled pixel. */
function scoreLine(residual, path, amount) {
  let score = 0;
  for (let i = 0; i < path.length; i++) {
    score = F(score + squaredErrorGain(residual[path[i]], amount));
  }
  return score;
}

function subtractResidual(residual, size, x, y, amount) {
  if (x < 0 || y < 0 || x >= size || y >= size) return;
  residual[y * size + x] = F(residual[y * size + x] - amount);
}

/**
 * Android subtractLine: a real thread has width; softly clear adjacent
 * samples so the greedy picker does not stack almost identical chords.
 */
function subtractLine(residual, size, path, widthPx, opacity, subPixelAmount) {
  for (let i = 0; i < path.length; i++) {
    const p = path[i];
    const x = p % size;
    const y = (p / size) | 0;
    if (widthPx < 1) {
      residual[p] = F(residual[p] - subPixelAmount);
      const fringe = F(subPixelAmount * 0.06);
      subtractResidual(residual, size, x - 1, y, fringe);
      subtractResidual(residual, size, x + 1, y, fringe);
      subtractResidual(residual, size, x, y - 1, fringe);
      subtractResidual(residual, size, x, y + 1, fringe);
      continue;
    }
    const before = path[Math.max(0, i - 1)];
    const after = path[Math.min(path.length - 1, i + 1)];
    const tangentX = after % size - before % size;
    const tangentY = ((after / size) | 0) - ((before / size) | 0);
    const length = F(Math.hypot(tangentX, tangentY));
    // float: -tangentY / length  (int / float → float)
    const normalX = length > 0 ? F(-tangentY / length) : 0;
    const normalY = length > 0 ? F(tangentX / length) : 1;
    // int radius = (int) Math.ceil(widthPx * .5f + .5f);
    const radius = Math.ceil(F(F(widthPx * 0.5) + 0.5));
    for (let offset = -radius; offset <= radius; offset++) {
      // float: widthPx * .5f + .5f - Math.abs(offset)
      const coverage = clamp(F(F(F(widthPx * 0.5) + 0.5) - Math.abs(offset)), 0, 1);
      if (coverage <= 0) continue;
      subtractResidual(
        residual,
        size,
        Math.round(F(x + F(normalX * offset))),
        Math.round(F(y + F(normalY * offset))),
        F(opacity * coverage)
      );
    }
  }
}

function circularGap(from, to, count) {
  const gap = Math.abs(from - to);
  return Math.min(gap, count - gap);
}

/**
 * Generate a greedy string-art sequence. Options match the Android call:
 *   pixels(RGBA), width, height, cropX, cropY, cropZoom,
 *   pinCount, requestedLines, circleMm, lineMm, autoStop,
 *   cancelled, onProgress.
 */
function generate(options) {
  const size = WORK_SIZE;
  const pixels = options.pixels;
  if (!pixels || pixels.length < size * size * 4) {
    throw new Error('生成图片像素不足 ' + size + '×' + size);
  }
  const width = Math.max(1, options.width || 1);
  const height = Math.max(1, options.height || 1);
  const cropX = clamp(Number(options.cropX == null ? 0.5 : options.cropX), 0, 1);
  const cropY = clamp(Number(options.cropY == null ? 0.5 : options.cropY), 0, 1);
  const cropZoom = Number(options.cropZoom == null ? 1 : options.cropZoom);
  const pinCount = clamp(Math.round(options.pinCount || 220), 2, 10000);
  const requestedLines = clamp(Math.round(options.requestedLines || 4000), 10, 20000);
  const circleMm = Math.max(1, Number(options.circleMm) || 260);
  // float safeLineMm = max(.01f, min(1f, lineMm))
  const safeLineMm = F(clamp(Number(options.lineMm) || 0.2, 0.01, 1));
  const autoStop = options.autoStop !== false;
  const cancelled = options.cancelled || (() => false);
  const onProgress = options.onProgress || (() => {});

  const residual = buildResidual(pixels, width, height, cropX, cropY, cropZoom);
  // float: max(.12f, TARGET_RADIUS_RATIO * (size-1f) * safeLineMm / max(80f, circleMm))
  const threadWidthPx = F(Math.max(
    0.12,
    F(F(TARGET_RADIUS_RATIO * F(size - 1)) * safeLineMm) / Math.max(80, circleMm)
  ));
  // float: max(26f, min(82f, 26f + threadWidthPx * 90f)) / 255f
  const threadOpacity = F(Math.max(26, Math.min(82, F(26 + F(threadWidthPx * 90)))) / 255);
  // float: threadOpacity * min(1f, threadWidthPx)
  const lineDarkness = F(threadOpacity * Math.min(1, threadWidthPx));
  // int: max(8, pinCount / 28)
  const minPinGap = Math.max(8, Math.floor(pinCount / 28));

  const pinX = new Int32Array(pinCount);
  const pinY = new Int32Array(pinCount);
  // float center = (size - 1) * 0.5f; float radius = center - 3f;
  const center = F((size - 1) * 0.5);
  const radius = F(center - 3);
  for (let i = 0; i < pinCount; i++) {
    // double angle; Math.round(center + (float) Math.cos(angle) * radius)
    const angle = (Math.PI * 2 * i) / pinCount;
    pinX[i] = Math.round(F(center + F(F(Math.cos(angle)) * radius)));
    pinY[i] = Math.round(F(center + F(F(Math.sin(angle)) * radius)));
  }

  const pathCache = createPathCache(PATH_CACHE_LIMIT);
  const sequence = new Int32Array(requestedLines + 1);
  const recentPins = new Int32Array(Math.min(RECENT_PIN_WINDOW, pinCount));
  let recentCount = 1;
  let recentCursor = 1 % recentPins.length;
  recentPins[0] = 0;
  let current = 0;
  // double threadMm / areaMm2 (Android uses double here)
  let threadMm = 0;
  const areaMm2 = Math.PI * circleMm * circleMm * 0.25;
  sequence[0] = 0;
  let length = 1;

  for (let step = 0; step < requestedLines; step++) {
    if (cancelled()) return null;
    let best = -1;
    let bestScore = 0;
    let selected = null;
    for (let candidate = 0; candidate < pinCount; candidate++) {
      if (isRecentPin(recentPins, recentCount, candidate)) continue;
      const gap = circularGap(current, candidate, pinCount);
      if (gap < minPinGap) continue;
      const path = pathCache.get(size, pinX, pinY, current, candidate);
      const score = scoreLine(residual, path, lineDarkness);
      if (score > bestScore) {
        bestScore = score;
        best = candidate;
        selected = path;
      }
    }
    // Continuing after every possible chord has negative gain can only make
    // the requested image less accurate, even with auto-stop off.
    if (best < 0 || bestScore <= 0) break;

    subtractLine(residual, size, selected, threadWidthPx, threadOpacity, lineDarkness);
    const gap = circularGap(current, best, pinCount);
    // double: threadMm += circleMm * Math.sin(Math.PI * gap / pinCount)
    threadMm += circleMm * Math.sin((Math.PI * gap) / pinCount);
    current = best;
    sequence[length++] = current;
    if (recentCount < recentPins.length) {
      recentPins[recentCount++] = current;
      recentCursor = recentCount % recentPins.length;
    } else {
      recentPins[recentCursor] = current;
      recentCursor = (recentCursor + 1) % recentPins.length;
    }
    if ((step & 15) === 0 || step + 1 === requestedLines) {
      // Pass a snapshot of the sequence built so far so callers can reveal
      // chords live while generation is still running. (Android calls the
      // listener every step; here progress is throttled to 16-step batches
      // purely to bound IPC traffic — it never affects the algorithm.)
      onProgress(step + 1, requestedLines, sequence.subarray(0, length));
    }
    if (
      autoStop &&
      (threadMm * safeLineMm) / areaMm2 >= MAX_AVERAGE_COVERAGE &&
      F(bestScore / selected.length) < F(F(lineDarkness * lineDarkness) * 0.15)
    ) {
      break;
    }
  }

  return {
    sequence: Array.from(sequence.subarray(0, length)),
    threadMeters: threadMm / 1000,
    lines: length - 1,
    lineDarkness,
    pathCacheSize: pathCache.size()
  };
}

function isRecentPin(recentPins, recentCount, candidate) {
  for (let i = 0; i < recentCount; i++) {
    if (recentPins[i] === candidate) return true;
  }
  return false;
}

module.exports = {
  FULL_IMAGE_FIT_MARGIN,
  MAX_AVERAGE_COVERAGE,
  MAX_CROP_ZOOM,
  PATH_CACHE_LIMIT,
  RECENT_PIN_WINDOW,
  TARGET_RADIUS_RATIO,
  WORK_SIZE,
  buildResidual,
  clampCropZoom,
  createPathCache,
  cropSizePx,
  generate,
  makePath,
  minimumCropZoom,
  scoreLine,
  squaredErrorGain,
  subtractLine
};
