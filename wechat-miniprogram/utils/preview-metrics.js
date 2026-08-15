function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value))
}

const BASE_RADIUS_RATIO = 0.45
const MIN_ZOOM = 1
const MAX_ZOOM = 5

function clampZoom(value) {
  return clamp(Number(value) || 1, MIN_ZOOM, MAX_ZOOM)
}

function maxPan(side, zoom, edgeAllowance) {
  const baseRadius = Math.max(0, Number(side) || 0) * BASE_RADIUS_RATIO
  const radius = baseRadius * clampZoom(zoom)
  const edge = Math.max(0, Number(edgeAllowance) || 12)
  return Math.max(edge, radius - baseRadius + edge)
}

function threadMetrics(radius, side, lineMm, circleMm) {
  const physicalRatio = Math.max(0.01, Number(lineMm) || 0.1) /
    Math.max(1, Number(circleMm) || 1)
  return {
    physicalRatio,
    stroke: Math.max(0.12, 2 * radius * physicalRatio),
    alpha: clamp(Math.round(26 + side * physicalRatio * 90), 26, 82) / 255
  }
}

function nailMetrics(nails, radius) {
  const count = Math.max(2, Number(nails) || 2)
  const arc = Math.PI * 2 * radius / count
  const baseText = clamp(arc * 0.72, 3, 6)
  const dotRadius = clamp(arc * 0.14, 0.8, 2.2)
  return {
    arc,
    baseText,
    dotRadius,
    labelRadius: radius + dotRadius + Math.max(2, baseText * 0.62)
  }
}

module.exports = {
  BASE_RADIUS_RATIO,
  MIN_ZOOM,
  MAX_ZOOM,
  clampZoom,
  maxPan,
  threadMetrics,
  nailMetrics
}
