function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value))
}

function createSnapController() {
  return {
    activeSnap: null,
    triggerTimes: [],
    pausedUntil: 0
  }
}

function beginPinch(controller) {
  controller.activeSnap = null
  return controller
}

function availableSnapPoints(imageWidth, imageHeight, minimumZoom, maximumZoom) {
  const longSideSnap = Math.min(imageWidth, imageHeight) / Math.max(imageWidth, imageHeight)
  return [longSideSnap, 1].filter((value, index, values) =>
    value >= minimumZoom && value <= maximumZoom && values.indexOf(value) === index
  )
}

function applySnap(controller, options) {
  const now = Number(options.now) || Date.now()
  const minimumZoom = options.minimumZoom
  const maximumZoom = options.maximumZoom
  const gestureZoom = clamp(options.gestureZoom, minimumZoom, maximumZoom)
  if (now < controller.pausedUntil) {
    controller.activeSnap = null
    return gestureZoom
  }
  const enterRatio = Math.min(0.055, 14 / Math.max(1, options.canvasSide))
  const releaseRatio = enterRatio * 1.8
  if (controller.activeSnap != null) {
    if (Math.abs(gestureZoom - controller.activeSnap) <= releaseRatio) return controller.activeSnap
    controller.activeSnap = null
  }

  const points = availableSnapPoints(
    options.imageWidth,
    options.imageHeight,
    minimumZoom,
    maximumZoom
  )
  let nearest = null
  let nearestDistance = Infinity
  points.forEach((point) => {
    const distance = Math.abs(gestureZoom - point)
    if (distance < nearestDistance) {
      nearestDistance = distance
      nearest = point
    }
  })
  if (nearest == null || nearestDistance > enterRatio) return gestureZoom

  controller.triggerTimes = controller.triggerTimes.filter((time) => now - time <= 3000)
  controller.triggerTimes.push(now)
  if (controller.triggerTimes.length >= 4) {
    controller.triggerTimes = []
    controller.pausedUntil = now + 3000
    controller.activeSnap = null
    return gestureZoom
  }
  controller.activeSnap = nearest
  return nearest
}

module.exports = {
  applySnap,
  availableSnapPoints,
  beginPinch,
  createSnapController
}
