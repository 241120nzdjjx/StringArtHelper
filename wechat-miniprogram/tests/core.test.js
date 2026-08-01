const assert = require('assert')
const sequence = require('../utils/sequence')
const pdf = require('../utils/pdf')
const offlineTts = require('../utils/offline-tts')
const previewMetrics = require('../utils/preview-metrics')

function run() {
  assert.deepStrictEqual(sequence.parseSequence('# count=4\n0 → 12 → 45 → 7'), [0, 12, 45, 7])
  assert.deepStrictEqual(sequence.parseSequence('1: 0 -> 4\n2: 4 -> 8'), [0, 4, 4, 8])
  assert.ok(sequence.toTxt([0, 2, 5], { nails: 10, lineMm: 0.1, circleMm: 260 }).includes('0 → 2 → 5'))

  assert.strictEqual(
    sequence.txtFilename('刻晴_绕线序列_300钉_线径0.20mm_圆径260mm.txt', { nails: 300, lineMm: 0.2, circleMm: 260 }, 'zh'),
    '刻晴_绕线序列_300钉_线径0.20mm_圆径260mm.txt'
  )
  assert.strictEqual(
    sequence.txtFilename('Keqing_string_sequence_300_nails_thread_0.20mm_circle_260mm.txt', { nails: 300, lineMm: 0.2, circleMm: 260 }, 'en'),
    'Keqing_string_sequence_300_nails_thread_0.20mm_circle_260mm.txt'
  )

  const bytes = pdf.generateNailTemplate({ nails: 120, circleMm: 260 })
  const header = String.fromCharCode.apply(null, Array.from(bytes.slice(0, 8)))
  const tail = String.fromCharCode.apply(null, Array.from(bytes.slice(-8)))
  const pdfText = String.fromCharCode.apply(null, Array.from(bytes))
  assert.ok(header.startsWith('%PDF-1.4'))
  assert.ok(tail.includes('%%EOF'))
  assert.ok(bytes.length > 5000)
  assert.ok(pdfText.includes('/MediaBox [0 0 595 842]'))
  assert.ok(pdfText.includes('/Count 2'))
  assert.ok(pdfText.includes('100 mm'))
  assert.ok(pdfText.includes('Print at actual size / 100%'))
  assert.deepStrictEqual(pdf.chooseTiling(276), {
    columns: 2, rows: 1, pages: 2, tileWidthMm: 138, tileHeightMm: 276
  })
  assert.deepStrictEqual(pdf.chooseTiling(1216), {
    columns: 7, rows: 5, pages: 35, tileWidthMm: 1216 / 7, tileHeightMm: 1216 / 5
  })
  assert.strictEqual(pdf.pdfFilename({ nails: 300, circleMm: 260 }, 'zh'), '绕线画钉位模板_300钉_260mm.pdf')
  assert.strictEqual(pdf.pdfFilename({ nails: 300, circleMm: 260 }, 'en'), 'String_Art_Nail_Template_300_nails_260mm.pdf')

  assert.deepStrictEqual(offlineTts.numberTokens(10, 'zh'), ['ten'])
  assert.deepStrictEqual(offlineTts.numberTokens(101, 'zh'), ['one', 'hundred', 'zero', 'one'])
  assert.deepStrictEqual(offlineTts.numberTokens(25, 'en'), ['two', 'five'])
  assert.deepStrictEqual(offlineTts.numberTokens(407, 'en'), ['four', 'zero', 'seven'])
  assert.strictEqual(offlineTts.normalizeRate(0.2), 0.75)
  assert.strictEqual(offlineTts.normalizeRate(1.25), 1.25)
  assert.strictEqual(offlineTts.normalizeRate(3), 1.6)

  const normalThread = previewMetrics.threadMetrics(100, 300, 0.5, 200)
  const zoomedThread = previewMetrics.threadMetrics(200, 300, 0.5, 200)
  assert.strictEqual(zoomedThread.stroke, normalThread.stroke * 2)
  assert.ok(previewMetrics.nailMetrics(200, 200).baseText > previewMetrics.nailMetrics(200, 100).baseText)
  assert.strictEqual(previewMetrics.clampZoom(0.5), 1)
  assert.strictEqual(previewMetrics.clampZoom(8), 5)
  console.log('Original core regression tests passed')
}

run()
