const MAX_SEQUENCE_LENGTH = 100000
const INTEGER_PATTERN = /\d+/g
const STEP_PREFIX_PATTERN = /^\s*(?:(?:step|line|row|route|index|第|步骤|线路)\s*)?\d+\s*[:：.)、]\s*(.+)$/i
const ONLY_NUMERIC_SYNTAX = /^[\d\s[\]{}(),;|:：>→\-+.]+$/
const NAILS_METADATA_PATTERN = /(?:(?:钉(?:子)?数|nails?|pins?)\s*[:：=_-]?\s*(\d{1,6})|(\d{1,6})\s*(?:钉|nails?|pins?))/i
const THREAD_METADATA_PATTERN = /(?:线径|线直径|thread(?:\s+diameter)?|line\s+diameter)\s*[:：=_-]?\s*(\d+(?:\.\d+)?)\s*(?:mm|毫米)?/i
const CIRCLE_METADATA_PATTERN = /(?:钉位圆(?:直径)?|圆径|nail\s+circle(?:\s+diameter)?|circle\s+diameter)\s*[:：=_-]?\s*(\d+(?:\.\d+)?)\s*(?:mm|毫米)?/i
const ROUTE_SEPARATOR_PATTERN = /→|->|=>|,|;|\sto\s|\s到\s|至/i
const ILLEGAL_FILENAME_PATTERN = /[\\/:*?"<>|]/g

function extractIntegers(value) {
  const matches = String(value || '').match(INTEGER_PATTERN) || []
  const result = []
  for (let index = 0; index < matches.length && result.length < MAX_SEQUENCE_LENGTH; index += 1) {
    const number = Number(matches[index])
    if (Number.isFinite(number) && number >= 0 && number <= 99999) result.push(number)
  }
  return result
}

function containsMetadata(line) {
  return NAILS_METADATA_PATTERN.test(line) ||
    THREAD_METADATA_PATTERN.test(line) ||
    CIRCLE_METADATA_PATTERN.test(line) ||
    /(?:count|total|maximum|max\s+lines?|共\s*\d+\s*个?钉号)/i.test(line)
}

function removeCountHeader(values) {
  if (values.length < 3) return values
  const first = values[0]
  if (first === values.length - 1) return values.slice(1)
  if (first >= 1000) {
    const sampleSize = Math.min(200, values.length - 1)
    let smallCount = 0
    for (let index = 1; index <= sampleSize; index += 1) {
      if (values[index] <= 999) smallCount += 1
    }
    if (sampleSize > 0 && smallCount >= sampleSize * 0.95) return values.slice(1)
  }
  return values
}

function readMetadata(metadataText) {
  const nailsMatch = metadataText.match(NAILS_METADATA_PATTERN)
  const threadMatch = metadataText.match(THREAD_METADATA_PATTERN)
  const circleMatch = metadataText.match(CIRCLE_METADATA_PATTERN)
  return {
    declaredNails: nailsMatch ? Number(nailsMatch[1] || nailsMatch[2]) : NaN,
    lineMm: threadMatch ? Number(threadMatch[1]) : NaN,
    circleMm: circleMatch ? Number(circleMatch[1]) : NaN
  }
}

function parseTxt(text, fileName) {
  const normalized = String(text || '')
    .replace(/\uFEFF/g, ' ')
    .replace(/，/g, ',')
    .replace(/；/g, ';')
  const metadataText = String(fileName || '') + '\n' + normalized.slice(0, 4096)
  const metadata = readMetadata(metadataText)
  const lines = normalized.split(/\r?\n/)
  let sequence = []

  lines.forEach((sourceLine) => {
    let line = sourceLine.trim()
    if (!line || line.indexOf('#') === 0 || line.indexOf('//') === 0) return
    const prefixed = line.match(STEP_PREFIX_PATTERN)
    if (prefixed) line = prefixed[1].trim()
    const numbers = extractIntegers(line)
    if (!numbers.length) return
    if (numbers.length === 1 && /^\s*\d+\s*$/.test(line)) {
      sequence.push(numbers[0])
      return
    }
    if (
      numbers.length >= 2 &&
      (ROUTE_SEPARATOR_PATTERN.test(line) || ONLY_NUMERIC_SYNTAX.test(line) || !containsMetadata(line))
    ) {
      sequence = sequence.concat(numbers)
    }
  })

  if (sequence.length < 2) sequence = extractIntegers(normalized)
  sequence = removeCountHeader(sequence)
    .filter((value) => value >= 0 && value <= 99999)
    .slice(0, MAX_SEQUENCE_LENGTH)

  if (sequence.length < 2) {
    return { sequence: [], nails: 0, lineMm: 0.2, circleMm: 260, metadata }
  }

  const inferredNails = Math.max.apply(null, sequence) + 1
  const plausibleMaximum = Math.max(500, inferredNails * 4)
  const declaredNails = metadata.declaredNails
  const nails = Number.isFinite(declaredNails) &&
    declaredNails >= inferredNails &&
    declaredNails <= Math.min(10000, plausibleMaximum)
    ? declaredNails
    : inferredNails
  const lineMm = Number.isFinite(metadata.lineMm) && metadata.lineMm >= 0.01 && metadata.lineMm <= 1
    ? metadata.lineMm
    : 0.2
  const circleMm = Number.isFinite(metadata.circleMm) && metadata.circleMm >= 80 && metadata.circleMm <= 1200
    ? metadata.circleMm
    : 260

  return { sequence, nails, lineMm, circleMm, metadata }
}

function parseSequence(text) {
  return parseTxt(text, '').sequence
}

function sequencePreview(values, limit) {
  const count = Math.min(limit || 14, values.length)
  const parts = values.slice(0, count).map(String)
  if (values.length > count) parts.push('…')
  return parts.join(' → ')
}

function toTxt(values, metadata) {
  const info = metadata || {}
  const nails = Number(info.nails) || (values.length ? Math.max.apply(null, values) + 1 : 0)
  const lineMm = Number(info.lineMm) || 0.2
  const circleMm = Number(info.circleMm) || 260
  const lines = [
    '# 绕线助手导出',
    '# 钉数: ' + nails,
    '# 线径: ' + lineMm.toFixed(2) + ' mm',
    '# 钉位圆直径: ' + circleMm + ' mm',
    '# 钉号：0 号正右，顺时针递增',
    '# 共 ' + values.length + ' 个钉号',
    ''
  ]
  for (let index = 0; index < values.length; index += 16) {
    lines.push(values.slice(index, index + 16).join(' → '))
  }
  return lines.join('\n') + '\n'
}

function sanitizeFilename(value, fallback) {
  return String(value || fallback || 'string-art')
    .replace(ILLEGAL_FILENAME_PATTERN, '_')
    .replace(/\s+/g, ' ')
    .trim() || String(fallback || 'string-art')
}

function stripTxtParameterSuffix(value) {
  let result = String(value || '').replace(/\.[^.]+$/, '')
  const suffixes = [
    /_绕线序列_\d+钉_线径\d+(?:\.\d+)?mm_圆径\d+(?:\.\d+)?mm$/i,
    /_string_sequence_\d+_nails_thread_\d+(?:\.\d+)?mm_circle_\d+(?:\.\d+)?mm$/i
  ]
  let changed = true
  while (changed) {
    changed = false
    suffixes.forEach((pattern) => {
      const next = result.replace(pattern, '')
      if (next !== result) {
        result = next
        changed = true
      }
    })
  }
  return result
}

function txtFilename(sourceName, metadata, language) {
  const info = metadata || {}
  const nails = Number(info.nails) || 0
  const lineMm = (Number(info.lineMm) || 0.2).toFixed(2)
  const circleMm = Number(info.circleMm) || 260
  const source = sanitizeFilename(stripTxtParameterSuffix(sourceName || 'image'), 'image')
  const name = language === 'en'
    ? source + '_string_sequence_' + nails + '_nails_thread_' + lineMm + 'mm_circle_' + circleMm + 'mm.txt'
    : source + '_绕线序列_' + nails + '钉_线径' + lineMm + 'mm_圆径' + circleMm + 'mm.txt'
  return sanitizeFilename(name, 'string-art.txt')
}

module.exports = {
  CIRCLE_METADATA_PATTERN,
  ILLEGAL_FILENAME_PATTERN,
  INTEGER_PATTERN,
  MAX_SEQUENCE_LENGTH,
  NAILS_METADATA_PATTERN,
  ONLY_NUMERIC_SYNTAX,
  STEP_PREFIX_PATTERN,
  THREAD_METADATA_PATTERN,
  extractIntegers,
  parseSequence,
  parseTxt,
  sanitizeFilename,
  sequencePreview,
  stripTxtParameterSuffix,
  toTxt,
  txtFilename
}
