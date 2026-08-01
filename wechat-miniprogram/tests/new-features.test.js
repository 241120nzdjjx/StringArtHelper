const assert = require('assert')
const fs = require('fs')
const path = require('path')
const generator = require('../workers/generator-core')
const sequence = require('../utils/sequence')
const snap = require('../utils/crop-snap')
const sar = require('../utils/sar')
const { shouldRegenerate } = require('../utils/regeneration')
const { decodeText } = require('../utils/text-codec')

function fixture(name) {
  return Buffer.from(fs.readFileSync(path.join(__dirname, 'fixtures', name), 'utf8').trim(), 'hex')
}

function whiteImage() {
  const pixels = new Uint8ClampedArray(256 * 256 * 4)
  pixels.fill(255)
  return pixels
}

function run() {
  const residual = new Float32Array([0.02])
  generator.subtractResidual(residual, 1, 0, 0, 0.1)
  assert.ok(residual[0] < 0, 'residual must be allowed below zero')
  assert.ok(
    generator.squaredErrorGain(-0.2, 0.1) < generator.squaredErrorGain(0.2, 0.1),
    'over-dark pixels must lower candidate gain'
  )
  assert.strictEqual(generator.makePath(256, 0, 0, 8, 3).length, 9, 'Bresenham path must sample every grid step')

  const recent = new Uint16Array(20)
  const recentState = { start: 0, count: 0 }
  for (let pin = 0; pin < 20; pin += 1) generator.recentPush(recent, recentState, pin)
  for (let pin = 0; pin < 20; pin += 1) assert.ok(generator.recentHas(recent, recentState, pin))
  generator.recentPush(recent, recentState, 20)
  assert.ok(!generator.recentHas(recent, recentState, 0))
  assert.ok(generator.recentHas(recent, recentState, 20))

  const stopped = generator.generate({ pixels: whiteImage(), pinCount: 100, requestedLines: 100, circleMm: 260, lineMm: 0.1, autoStop: false })
  assert.deepStrictEqual(stopped.sequence, [0], 'non-positive best gain must stop even when autoStop is disabled')

  const androidTxt = '# 绕线助手导出\n# 钉数: 220\n# 线径: 0.10 mm\n# 钉位圆直径: 260 mm\n\n0 → 87 → 31 → 145\n'
  const parsed = sequence.parseTxt(androidTxt, 'sample.txt')
  assert.deepStrictEqual(parsed.sequence, [0, 87, 31, 145])
  assert.strictEqual(parsed.nails, 220)
  assert.strictEqual(parsed.lineMm, 0.1)
  assert.strictEqual(parsed.circleMm, 260)
  const roundTrip = sequence.parseTxt(sequence.toTxt(parsed.sequence, parsed), 'round-trip.txt')
  assert.deepStrictEqual(roundTrip.sequence, parsed.sequence)
  assert.strictEqual(roundTrip.nails, 220)
  assert.strictEqual(sequence.parseTxt('Step 1: 0 -> 4\nStep 2: 4 -> 8', '').sequence[0], 0)
  assert.strictEqual(decodeText(Uint8Array.from([0xff, 0xfe, 0x30, 0x00, 0x20, 0x00, 0x32, 0x00])), '0 2')
  assert.strictEqual(decodeText(Uint8Array.from([0xfe, 0xff, 0x00, 0x30, 0x00, 0x20, 0x00, 0x32])), '0 2')

  ;['sar2.hex', 'sar3.hex', 'sar4.hex'].forEach((name, index) => {
    const decoded = sar.decodeSar(fixture(name))
    assert.strictEqual(decoded.magic, 'SAR' + (index + 2))
    assert.deepStrictEqual(decoded.sequence, [0, 3, 7])
  })
  const special = '中文\0😀'
  assert.strictEqual(sar.decodeJavaUTF(sar.encodeJavaUTF(special)), special)
  const project = {
    name: special, sourceName: 'source.txt', index: 1, updatedAt: 1700000000000,
    params: { nails: 8, circleMm: 260, lineMm: 0.2 }, sequence: [0, 3, 7]
  }
  const encoded = sar.encodeSar4(project)
  const decoded = sar.decodeSar(encoded)
  assert.strictEqual(decoded.name, special)
  assert.deepStrictEqual(decoded.sequence, project.sequence)
  assert.strictEqual(decoded.params.lineMm, 0.2, 'SAR Float32 line diameter must be rounded to hundredths')
  const roundedLine = sar.decodeSar(sar.encodeSar4(Object.assign({}, project, {
    params: Object.assign({}, project.params, { lineMm: 0.126 })
  })))
  assert.strictEqual(roundedLine.params.lineMm, 0.13)
  assert.strictEqual(sar.sarFilename({ name: '刻晴/项目. ', index: 137 }, 'zh'), '刻晴_项目_第138步_绕线存档.sar')
  assert.strictEqual(sar.sarFilename({ name: 'Keqing', index: 137 }, 'en'), 'Keqing_step_138_string_art_save.sar')
  assert.throws(() => sar.decodeSar(encoded.slice(0, encoded.length - 1)), /意外结束/)
  const unknown = encoded.slice(); unknown[5] = 0x58
  assert.throws(() => sar.decodeSar(unknown), /未知 SAR magic/)
  const forged = encoded.slice()
  const countOffset = forged.length - 16
  new DataView(forged.buffer, forged.byteOffset).setInt32(countOffset, 100001, false)
  assert.throws(() => sar.decodeSar(forged), /数量异常/)
  assert.throws(() => sar.encodeSar4(Object.assign({}, project, { sequence: [0, 9] })), /非法钉号/)
  assert.throws(() => sar.encodeSar4(Object.assign({}, project, { thumbnail: new Uint8Array(256 * 1024 + 1) })), /256 KiB/)

  const controller = snap.createSnapController()
  snap.beginPinch(controller)
  const options = { imageWidth: 200, imageHeight: 100, minimumZoom: 0.4, maximumZoom: 5, canvasSide: 300 }
  assert.strictEqual(snap.applySnap(controller, Object.assign({ gestureZoom: 0.52, now: 1000 }, options)), 0.5)
  assert.strictEqual(snap.applySnap(controller, Object.assign({ gestureZoom: 0.57, now: 1100 }, options)), 0.5)
  assert.strictEqual(snap.applySnap(controller, Object.assign({ gestureZoom: 0.61, now: 1200 }, options)), 0.61)
  for (let index = 0; index < 3; index += 1) {
    snap.beginPinch(controller)
    snap.applySnap(controller, Object.assign({ gestureZoom: 1.01, now: 1300 + index * 100 }, options))
  }
  snap.beginPinch(controller)
  assert.strictEqual(snap.applySnap(controller, Object.assign({ gestureZoom: 0.99, now: 1700 }, options)), 0.99)
  assert.ok(controller.pausedUntil >= 4500)

  const before = { lines: 1000, nails: 220, autoStop: true, lineMm: 0.1, circleMm: 260, cropImagePath: 'a' }
  assert.strictEqual(shouldRegenerate(before, Object.assign({}, before, { lines: 5000 }), 4001), false)
  assert.strictEqual(shouldRegenerate(before, Object.assign({}, before, { lines: 3999 }), 4001), true)
  assert.strictEqual(shouldRegenerate(before, Object.assign({}, before, { lineMm: 0.2 }), 4001), true)
  console.log('New algorithm, TXT, SAR and crop-snap tests passed')
}

run()
