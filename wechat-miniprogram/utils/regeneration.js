function changed(left, right, key) {
  return String(left && left[key]) !== String(right && right[key])
}

function shouldRegenerate(previous, next, sequenceLength) {
  const before = previous || {}
  const after = next || {}
  const actualLines = Math.max(0, Number(sequenceLength) - 1)
  if (Number(after.lines) < actualLines) return true
  if (changed(before, after, 'nails')) return true
  if (changed(before, after, 'autoStop')) return true
  if (changed(before, after, 'lineMm')) return true
  if (changed(before, after, 'circleMm')) return true
  if (changed(before, after, 'cropImagePath')) return true
  return false
}

module.exports = { shouldRegenerate }
