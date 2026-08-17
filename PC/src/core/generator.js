/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 *
 * Greedy string-art generator ported 1:1 from the Android app's
 * StringArtGenerator.java (the project's reference implementation).
 * Pure JavaScript, no platform dependencies, so it runs identically in
 * the Electron main process, a worker thread, or a plain Node test.
 *
 * Working convention (matches Android and the player/template):
 * pin 0 is at the right edge and increasing pin numbers travel clockwise.
 */
'use strict';

const WORK_SIZE = 256;
const TARGET_RADIUS_RATIO = 251 / 255;
/** Emergency cap only: physical line coverage, not ink coverage. */
const MAX_AVERAGE_COVERAGE = 2.6;
const FULL_IMAGE_FIT_MARGIN = 0.98;
/** Do not immediately revisit a recently used nail. */
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
  return Math.min(1, (base * TARGET_RADIUS_RATIO * FULL_IMAGE_FIT_MARGIN) / diagonal);
}

function clampCropZoom(width, height, zoom) {
  return Math.max(minimumCropZoom(width, height), Math.min(MAX_CROP_ZOOM, zoom));
}

function cropSizePx(width, height, zoom) {
  return Math.min(width, height) / clampCropZoom(width, height, zoom);
}

/**
 * Build the 256×256 working residual from a source RGBA buffer.
 * `pixels` is a Uint8ClampedArray/Uint8Array with 4 bytes per pixel,
 * `width`/`height` are the source dimensions, and cropX/cropY are the
 * crop centre in relative (0..1) coordinates, cropZoom as in Android.
 */
function buildResidual(pixels, width, height, cropX, cropY, cropZoom) {
  const size = WORK_SIZE;
  const crop = cropSizePx(width, height, cropZoom);
  const x0 = cropX * width - crop * 0.5;
  const y0 = cropY * height - crop * 0.5;
  const residual = new Float32Array(size * size);
  const center = (size - 1) * 0.5;
  const radius = center - 2;
  const radiusSquared = radius * radius;
  for (let y = 0; y < size; y++) {
    const sy = Math.round(y0 + (y / (size - 1)) * (crop - 1));
    if (sy < 0 || sy >= height) continue;
    const sourceRow = sy * width * 4;
    for (let x = 0; x < size; x++) {
      const dx = x - center;
      const dy = y - center;
      if (dx * dx + dy * dy > radiusSquared) continue;
      const sx = Math.round(x0 + (x / (size - 1)) * (crop - 1));
      // Samples outside the photo are the physical white board. Do not
      // stretch the nearest edge pixel into this otherwise empty area.
      if (sx < 0 || sx >= width) continue;
      const offset = sourceRow + sx * 4;
      const alpha = (pixels[offset + 3] == null ? 255 : pixels[offset + 3]) / 255;
      const luminance = (
        (pixels[offset] || 0) * 0.2126 +
        (pixels[offset + 1] || 0) * 0.7152 +
        (pixels[offset + 2] || 0) * 0.0722
      ) / 255;
      // The physical board is white; transparent pixels arrive as RGB(0,0,0),
      // so composite the alpha over white before computing darkness.
      residual[y * size + x] = 1 - (luminance * alpha + (1 - alpha));
    }
  }
  return residual;
}

/** Android makePath: uniform t-interpolation with rounding (not Bresenham). */
function makePath(size, x0, y0, x1, y1) {
  const dx = x1 - x0;
  const dy = y1 - y0;
  const count = Math.max(Math.abs(dx), Math.abs(dy)) + 1;
  const path = new Int32Array(count);
  for (let i = 0; i < count; i++) {
    const t = count === 1 ? 0 : i / (count - 1);
    const x = Math.round(x0 + dx * t);
    const y = Math.round(y0 + dy * t);
    path[i] = y * size + x;
  }
  return path;
}

/** Simple LRU path cache; symmetric pairs share one rasterisation. */
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

function squaredErrorGain(residual, amount) {
  return 2 * amount * residual - amount * amount;
}

function scoreLine(residual, path, amount) {
  let score = 0;
  for (let i = 0; i < path.length; i++) score += squaredErrorGain(residual[path[i]], amount);
  return score;
}

function subtractResidual(residual, size, x, y, amount) {
  if (x < 0 || y < 0 || x >= size || y >= size) return;
  residual[y * size + x] -= amount;
}

/**
 * A real thread has width: softly clear adjacent samples so the greedy
 * picker does not stack almost identical chords into dark edge bars.
 * Ported exactly from Android's subtractLine.
 */
function subtractLine(residual, size, path, widthPx, opacity, subPixelAmount) {
  for (let i = 0; i < path.length; i++) {
    const p = path[i];
    const x = p % size;
    const y = (p / size) | 0;
    if (widthPx < 1) {
      residual[p] -= subPixelAmount;
      const fringe = subPixelAmount * 0.06;
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
    const length = Math.hypot(tangentX, tangentY);
    const normalX = length > 0 ? -tangentY / length : 0;
    const normalY = length > 0 ? tangentX / length : 1;
    const radius = Math.ceil(widthPx * 0.5 + 0.5);
    for (let offset = -radius; offset <= radius; offset++) {
      const coverage = clamp(widthPx * 0.5 + 0.5 - Math.abs(offset), 0, 1);
      if (coverage <= 0) continue;
      subtractResidual(
        residual,
        size,
        Math.round(x + normalX * offset),
        Math.round(y + normalY * offset),
        opacity * coverage
      );
    }
  }
}

function circularGap(from, to, count) {
  const gap = Math.abs(from - to);
  return Math.min(gap, count - gap);
}

/**
 * Generate a greedy string-art sequence.
 *
 * options:
 *   pixels        Uint8ClampedArray RGBA source (any size; cropped internally)
 *   width,height  source dimensions
 *   cropX,cropY   crop centre in 0..1 (default 0.5)
 *   cropZoom      crop zoom (clamped to [minimumCropZoom, 4])
 *   pinCount      nails on the ring (20..10000, UI range 100..500)
 *   requestedLines max chords to generate (10..100000)
 *   circleMm      physical nail-circle diameter in mm (default 260)
 *   lineMm        physical thread diameter in mm, 0.01..1 (default 0.2)
 *   autoStop      stop when the residual is exhausted (default true)
 *   cancelled     optional () => boolean
 *   onProgress    optional (complete, total) => void
 *
 * Returns { sequence, threadMeters, lines, lineDarkness, pathCacheSize }
 * or null when cancelled.
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
  const safeLineMm = clamp(Number(options.lineMm) || 0.2, 0.01, 1);
  const autoStop = options.autoStop !== false;
  const cancelled = options.cancelled || (() => false);
  const onProgress = options.onProgress || (() => {});

  const residual = buildResidual(pixels, width, height, cropX, cropY, cropZoom);
  const threadWidthPx = Math.max(
    0.12,
    (TARGET_RADIUS_RATIO * (size - 1) * safeLineMm) / Math.max(80, circleMm)
  );
  const threadOpacity = Math.max(26, Math.min(82, 26 + threadWidthPx * 90)) / 255;
  const lineDarkness = threadOpacity * Math.min(1, threadWidthPx);
  const minPinGap = Math.max(8, Math.floor(pinCount / 28));

  const pinX = new Int32Array(pinCount);
  const pinY = new Int32Array(pinCount);
  const center = (size - 1) * 0.5;
  const radius = center - 3;
  for (let i = 0; i < pinCount; i++) {
    const angle = (Math.PI * 2 * i) / pinCount;
    pinX[i] = Math.round(center + Math.cos(angle) * radius);
    pinY[i] = Math.round(center + Math.sin(angle) * radius);
  }

  const pathCache = createPathCache(PATH_CACHE_LIMIT);
  const sequence = new Int32Array(requestedLines + 1);
  const recentPins = new Int32Array(Math.min(RECENT_PIN_WINDOW, pinCount));
  let recentCount = 1;
  let recentCursor = 1 % recentPins.length;
  recentPins[0] = 0;
  let current = 0;
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
      // chords live while generation is still running.
      onProgress(step + 1, requestedLines, sequence.subarray(0, length));
    }
    if (
      autoStop &&
      (threadMm * safeLineMm) / areaMm2 >= MAX_AVERAGE_COVERAGE &&
      bestScore / selected.length < lineDarkness * lineDarkness * 0.15
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
