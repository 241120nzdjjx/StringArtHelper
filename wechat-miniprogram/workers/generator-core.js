const WORK_SIZE = 256
const TARGET_RADIUS_RATIO = 0.9843137255
const RECENT_PIN_WINDOW = 20
const PATH_CACHE_LIMIT = 6000
const MAX_AVERAGE_COVERAGE = 2.6

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value))
}

function buildResidual(pixels, size) {
  const residual = new Float32Array(size * size)
  const center = (size - 1) * 0.5
  const radius = center - 2
  const radiusSquared = radius * radius
  for (let y = 0; y < size; y += 1) {
    for (let x = 0; x < size; x += 1) {
      const dx = x - center
      const dy = y - center
      if (dx * dx + dy * dy > radiusSquared) continue
      const offset = (y * size + x) * 4
      const alpha = (pixels[offset + 3] == null ? 255 : pixels[offset + 3]) / 255
      let luminance = (
        (pixels[offset] || 0) * 0.2126 +
        (pixels[offset + 1] || 0) * 0.7152 +
        (pixels[offset + 2] || 0) * 0.0722
      ) / 255
      luminance = luminance * alpha + (1 - alpha)
      residual[y * size + x] = 1 - luminance
    }
  }
  return residual
}

function makePins(pinCount, size) {
  const x = new Int16Array(pinCount)
  const y = new Int16Array(pinCount)
  const center = (size - 1) * 0.5
  const radius = center - 3
  for (let index = 0; index < pinCount; index += 1) {
    const angle = Math.PI * 2 * index / pinCount
    x[index] = Math.round(center + Math.cos(angle) * radius)
    y[index] = Math.round(center + Math.sin(angle) * radius)
  }
  return { x, y }
}

function makePath(size, x0, y0, x1, y1) {
  const path = []
  const stepX = x0 < x1 ? 1 : -1
  const stepY = y0 < y1 ? 1 : -1
  const deltaX = Math.abs(x1 - x0)
  const deltaY = -Math.abs(y1 - y0)
  let error = deltaX + deltaY
  while (true) {
    path.push(y0 * size + x0)
    if (x0 === x1 && y0 === y1) break
    const doubledError = error * 2
    if (doubledError >= deltaY) {
      error += deltaY
      x0 += stepX
    }
    if (doubledError <= deltaX) {
      error += deltaX
      y0 += stepY
    }
  }
  return path
}

function createPathCache(limit) {
  const cache = new Map()
  const capacity = Math.max(1, Number(limit) || PATH_CACHE_LIMIT)
  return {
    get(size, pins, from, to) {
      const low = Math.min(from, to)
      const high = Math.max(from, to)
      const key = low + ':' + high
      if (cache.has(key)) {
        const value = cache.get(key)
        cache.delete(key)
        cache.set(key, value)
        return value
      }
      const value = makePath(size, pins.x[from], pins.y[from], pins.x[to], pins.y[to])
      cache.set(key, value)
      if (cache.size > capacity) cache.delete(cache.keys().next().value)
      return value
    },
    size() { return cache.size }
  }
}

function squaredErrorGain(residual, amount) {
  return 2 * amount * residual - amount * amount
}

function scorePath(residual, path, lineDarkness) {
  let score = 0
  for (let index = 0; index < path.length; index += 1) {
    score += squaredErrorGain(residual[path[index]], lineDarkness)
  }
  return score
}

function subtractResidual(residual, size, x, y, amount) {
  if (x < 0 || y < 0 || x >= size || y >= size) return
  residual[y * size + x] -= amount
}

function subtractLine(residual, size, pins, from, to, path, widthPx, opacity, lineDarkness) {
  const x0 = pins.x[from]
  const y0 = pins.y[from]
  const x1 = pins.x[to]
  const y1 = pins.y[to]
  const dx = x1 - x0
  const dy = y1 - y0
  const length = Math.sqrt(dx * dx + dy * dy)
  const normalX = length > 0 ? -dy / length : 0
  const normalY = length > 0 ? dx / length : 1
  const radius = Math.ceil(widthPx * 0.5 + 0.5)
  for (let index = 0; index < path.length; index += 1) {
    const position = path[index]
    const x = position % size
    const y = Math.floor(position / size)
    if (widthPx < 1) {
      subtractResidual(residual, size, x, y, lineDarkness)
      subtractResidual(residual, size, x - 1, y, lineDarkness * 0.06)
      subtractResidual(residual, size, x + 1, y, lineDarkness * 0.06)
      subtractResidual(residual, size, x, y - 1, lineDarkness * 0.06)
      subtractResidual(residual, size, x, y + 1, lineDarkness * 0.06)
      continue
    }
    for (let offset = -radius; offset <= radius; offset += 1) {
      const coverage = clamp(widthPx * 0.5 + 0.5 - Math.abs(offset), 0, 1)
      if (coverage <= 0) continue
      subtractResidual(
        residual,
        size,
        Math.round(x + normalX * offset),
        Math.round(y + normalY * offset),
        opacity * coverage
      )
    }
  }
}

function circularGap(from, to, count) {
  const gap = Math.abs(from - to)
  return Math.min(gap, count - gap)
}

function recentHas(recent, state, value) {
  for (let index = 0; index < state.count; index += 1) {
    if (recent[(state.start + index) % recent.length] === value) return true
  }
  return false
}

function recentPush(recent, state, value) {
  if (state.count < recent.length) {
    recent[(state.start + state.count) % recent.length] = value
    state.count += 1
    return
  }
  recent[state.start] = value
  state.start = (state.start + 1) % recent.length
}

function generate(options, callbacks) {
  const callback = callbacks || {}
  const pixels = options.pixels
  const size = WORK_SIZE
  const pinCount = clamp(Math.round(options.pinCount || 200), 20, 500)
  const requestedLines = clamp(Math.round(options.requestedLines || 3000), 10, 20000)
  const circleMm = clamp(Number(options.circleMm) || 260, 80, 1200)
  const safeLineMm = clamp(Number(options.lineMm) || 0.2, 0.01, 1)
  const autoStop = options.autoStop !== false
  if (!pixels || pixels.length < size * size * 4) throw new Error('生成图片像素不足 256×256')

  const residual = buildResidual(pixels, size)
  const pins = makePins(pinCount, size)
  const pathCache = createPathCache(PATH_CACHE_LIMIT)
  const threadWidthPx = Math.max(
    0.12,
    TARGET_RADIUS_RATIO * (WORK_SIZE - 1) * safeLineMm / Math.max(80, circleMm)
  )
  const threadOpacity = Math.max(26, Math.min(82, 26 + threadWidthPx * 90)) / 255
  const lineDarkness = threadOpacity * Math.min(1, threadWidthPx)
  const minPinGap = Math.max(8, Math.floor(pinCount / 28))
  const recent = new Uint16Array(Math.min(RECENT_PIN_WINDOW, pinCount))
  const recentState = { start: 0, count: 0 }
  const sequence = [0]
  let current = 0
  let threadMm = 0
  const areaMm2 = Math.PI * circleMm * circleMm * 0.25
  recentPush(recent, recentState, 0)

  for (let step = 0; step < requestedLines; step += 1) {
    if (callback.cancelled && callback.cancelled()) return null
    let best = -1
    let bestScore = -Infinity
    let selected = null
    for (let candidate = 0; candidate < pinCount; candidate += 1) {
      if (recentHas(recent, recentState, candidate)) continue
      if (circularGap(current, candidate, pinCount) < minPinGap) continue
      const path = pathCache.get(size, pins, current, candidate)
      const score = scorePath(residual, path, lineDarkness)
      if (score > bestScore) {
        bestScore = score
        best = candidate
        selected = path
      }
    }
    if (best < 0 || bestScore <= 0) break

    subtractLine(
      residual,
      size,
      pins,
      current,
      best,
      selected,
      threadWidthPx,
      threadOpacity,
      lineDarkness
    )
    const gap = circularGap(current, best, pinCount)
    threadMm += circleMm * Math.sin(Math.PI * gap / pinCount)
    current = best
    sequence.push(current)
    recentPush(recent, recentState, current)

    if (callback.progress && (step % 20 === 0 || step + 1 === requestedLines)) {
      callback.progress(step + 1, requestedLines)
    }
    if (
      autoStop &&
      threadMm * safeLineMm / areaMm2 >= MAX_AVERAGE_COVERAGE &&
      bestScore / selected.length < lineDarkness * lineDarkness * 0.15
    ) {
      break
    }
  }

  return {
    sequence,
    threadMeters: threadMm / 1000,
    scoreStride: 1,
    lineDarkness,
    pathCacheSize: pathCache.size()
  }
}

module.exports = {
  MAX_AVERAGE_COVERAGE,
  PATH_CACHE_LIMIT,
  RECENT_PIN_WINDOW,
  TARGET_RADIUS_RATIO,
  WORK_SIZE,
  buildResidual,
  createPathCache,
  generate,
  makePath,
  makePins,
  recentHas,
  recentPush,
  scorePath,
  squaredErrorGain,
  subtractResidual
}
