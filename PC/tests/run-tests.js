/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 *
 * Node test runner for the PC core modules. Run with: npm test
 * Covers SAR byte-compatibility (fixtures from the mini-program test
 * suite), TXT import/export, Chinese number formatting, the generator
 * and the nail-template PDF writer.
 */
'use strict';

const assert = require('node:assert');
const fs = require('node:fs');
const path = require('node:path');

const sar = require('../src/core/sar.js');
const textCodec = require('../src/core/text-codec.js');
const numberFormat = require('../src/core/number-format.js');
const generator = require('../src/core/generator.js');
const pdfTemplate = require('../src/core/pdf-template.js');
const interop = require('./interop.test.js'); // cross-platform checks vs the Mini Program modules

let passed = 0;
let failed = 0;

function test(name, fn) {
  try {
    fn();
    passed += 1;
    console.log('  ✓ ' + name);
  } catch (error) {
    failed += 1;
    console.error('  ✗ ' + name);
    console.error('    ' + (error && error.stack ? error.stack.split('\n').slice(0, 4).join('\n    ') : error));
  }
}

function hexToBytes(hex) {
  const normalized = String(hex).replace(/\s+/g, '');
  const bytes = new Uint8Array(normalized.length / 2);
  for (let i = 0; i < bytes.length; i += 1) {
    bytes[i] = parseInt(normalized.substr(i * 2, 2), 16);
  }
  return bytes;
}

function bytesToHex(bytes) {
  return Array.from(bytes)
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('');
}

function readFixture(name) {
  return hexToBytes(fs.readFileSync(path.join(__dirname, 'fixtures', name), 'utf8'));
}

/* ------------------------- SAR byte compatibility ------------------------- */

console.log('\n[SAR 编解码]');

let sar2 = null;
test('SAR2 fixture decodes with legacy defaults', () => {
  sar2 = sar.decodeSar(readFixture('sar2.hex'), { miniProgramLimits: false });
  assert.strictEqual(sar2.magic, 'SAR2');
  assert.strictEqual(sar2.name, '固定😀');
  assert.strictEqual(sar2.importedFileName, 'source.txt');
  assert.strictEqual(sar2.index, 1);
  assert.strictEqual(sar2.timestamp, 1700000000000);
  assert.strictEqual(sar2.params.nails, 8); // SAR2: inferred from sequence
  assert.strictEqual(sar2.params.circleMm, 260);
  assert.strictEqual(sar2.params.lineMm, 0.2);
  assert.deepStrictEqual(Array.from(sar2.sequence), [0, 3, 7]);
});

test('SAR3 fixture decodes geometry', () => {
  const sar3 = sar.decodeSar(readFixture('sar3.hex'), { miniProgramLimits: false });
  assert.strictEqual(sar3.magic, 'SAR3');
  assert.strictEqual(sar3.name, '固定😀');
  assert.strictEqual(sar3.params.nails, 8);
  assert.strictEqual(sar3.params.circleMm, 260);
  assert.ok(Math.abs(sar3.params.lineMm - 0.2) < 0.001);
  assert.deepStrictEqual(Array.from(sar3.sequence), [0, 3, 7]);
});

let sar4 = null;
test('SAR4 fixture decodes geometry + thumbnail header', () => {
  sar4 = sar.decodeSar(readFixture('sar4.hex'), { miniProgramLimits: false });
  assert.strictEqual(sar4.magic, 'SAR4');
  assert.strictEqual(sar4.name, '固定😀');
  assert.strictEqual(sar4.params.nails, 8);
  assert.strictEqual(sar4.thumbnail.length, 0);
  assert.deepStrictEqual(Array.from(sar4.sequence), [0, 3, 7]);
});

test('SAR4 encode of decoded fixture is byte-identical', () => {
  const encoded = sar.encodeSar4(sar4);
  assert.strictEqual(bytesToHex(encoded), bytesToHex(readFixture('sar4.hex')));
});

test('SAR4 roundtrip with Chinese name', () => {
  const project = {
    name: '肖像·示例 项目',
    importedFileName: 'photo.png',
    index: 4,
    timestamp: Date.now(),
    params: { nails: 220, circleMm: 260, lineMm: 0.15 },
    thumbnail: new Uint8Array(0),
    sequence: [0, 3, 17, 55, 0, 1, 2, 3]
  };
  const decoded = sar.decodeSar(sar.encodeSar4(project), { miniProgramLimits: false });
  assert.strictEqual(decoded.name, project.name);
  assert.strictEqual(decoded.importedFileName, 'photo.png');
  assert.strictEqual(decoded.index, 4);
  assert.strictEqual(decoded.params.nails, 220);
  assert.strictEqual(decoded.params.circleMm, 260);
  assert.ok(Math.abs(decoded.params.lineMm - 0.15) < 0.001);
  assert.deepStrictEqual(Array.from(decoded.sequence), project.sequence);
});

test('SAR filename follows Android convention', () => {
  const name = sar.sarFilename({ name: '猫咪', index: 11 }, 'zh');
  assert.strictEqual(name, '猫咪_第12步_绕线存档.sar');
  assert.strictEqual(
    sar.sarFilename({ name: 'cat', index: 11 }, 'en'),
    'cat_step_12_string_art_save.sar'
  );
});

test('Modified UTF-8 encodes/decodes Chinese and supplementary chars', () => {
  const encoded = sar.encodeJavaUTF('绕线助手 🧶 0');
  const decoded = sar.decodeJavaUTF(encoded);
  assert.strictEqual(decoded, '绕线助手 \ud83e\uddf6 0');
});

/* ------------------------- TXT 序列 ------------------------- */

console.log('\n[TXT 序列]');

test('parseSequence handles route arrows and comments', () => {
  assert.deepStrictEqual(textCodec.parseSequence('# count=4\n0 → 12 → 45 → 7'), [0, 12, 45, 7]);
  assert.deepStrictEqual(textCodec.parseSequence('1: 0 -> 4\n2: 4 -> 8'), [0, 4, 4, 8]);
});

test('parseSequence handles step prefixes and metadata', () => {
  // Android's count-header heuristic also drops a leading "3" here (first == count-1),
  // so the exact result is the same as the reference implementation.
  assert.deepStrictEqual(textCodec.parseSequence('1: 3 到 5\n2: 5 到 9'), [5, 5, 9]);
  const parsed = textCodec.parseTxt('# 钉数: 300\n0 → 1 → 2', 'abc.txt');
  assert.strictEqual(parsed.nails, 300);
  assert.deepStrictEqual(parsed.sequence, [0, 1, 2]);
});

test('parseTxt round-trips the Android export header', () => {
  const txt = '# 绕线助手导出\n# 钉数: 220\n# 线径: 0.10 mm\n# 钉位圆直径: 260 mm\n\n0 → 87 → 31 → 145\n';
  const parsed = textCodec.parseTxt(txt, 'sample.txt');
  assert.deepStrictEqual(parsed.sequence, [0, 87, 31, 145]);
  assert.strictEqual(parsed.nails, 220);
  assert.strictEqual(parsed.lineMm, 0.1);
  assert.strictEqual(parsed.circleMm, 260);
});

test('parseTxt reads thread and circle metadata from header', () => {
  const text = [
    '# 绕线助手导出',
    '# 钉数: 260',
    '# 线径: 0.12 mm',
    '# 钉位圆直径: 300 mm',
    '',
    '0 → 5 → 10 → 3'
  ].join('\n');
  const parsed = textCodec.parseTxt(text, 'project.txt');
  assert.strictEqual(parsed.nails, 260);
  assert.strictEqual(parsed.lineMm, 0.12);
  assert.strictEqual(parsed.circleMm, 300);
});

test('toTxt produces Android-compatible header', () => {
  const txt = textCodec.toTxt([0, 2, 5], { nails: 10, lineMm: 0.1, circleMm: 260 });
  assert.ok(txt.includes('# 钉数: 10'));
  assert.ok(txt.includes('# 线径: 0.10 mm'));
  assert.ok(txt.includes('# 钉位圆直径: 260 mm'));
  assert.ok(txt.includes('0 → 2 → 5'));
});

test('decodeText handles UTF-8/UTF-16/BOM', () => {
  assert.strictEqual(textCodec.decodeText(Buffer.from('0 → 1', 'utf8')), '0 → 1');
  assert.strictEqual(textCodec.decodeText(Buffer.from([0xef, 0xbb, 0xbf, 0x31])), '1');
  assert.strictEqual(textCodec.decodeText(Buffer.from([0xff, 0xfe, 0x31, 0x00, 0x00, 0x00])), '1\u0000');
  assert.strictEqual(textCodec.decodeText(Buffer.from([0xfe, 0xff, 0x00, 0x31])), '1');
});

/* ------------------------- 中文数字 ------------------------- */

console.log('\n[中文数字]');

test('chineseNumber matches Android wording', () => {
  const cases = [
    [0, '零'],
    [5, '五'],
    [10, '十'],
    [15, '十五'],
    [20, '二十'],
    [100, '一百'],
    [101, '一百零一'],
    [105, '一百零五'],
    [110, '一百一十'],
    [111, '一百一十一'],
    [200, '二百'],
    [1000, '一千'],
    [1001, '1001'],
    [-10, '负十'],
    [-101, '负一百零一']
  ];
  cases.forEach(([input, expected]) => {
    assert.strictEqual(numberFormat.chineseNumber(input), expected, 'input=' + input);
  });
});

/* ------------------------- 生成算法 ------------------------- */

console.log('\n[生成算法]');

function syntheticImage(size, kind) {
  // 256×256 RGBA: a dark disc on white background, or a half-plane.
  const pixels = new Uint8ClampedArray(size * size * 4);
  for (let y = 0; y < size; y += 1) {
    for (let x = 0; x < size; x += 1) {
      const dx = x - size / 2;
      const dy = y - size / 2;
      let dark = 0;
      if (kind === 'disc') dark = Math.hypot(dx, dy) < size * 0.32 ? 1 : 0;
      else dark = x < size * 0.5 ? 1 : 0;
      const v = dark ? 40 : 250;
      const offset = (y * size + x) * 4;
      pixels[offset] = v;
      pixels[offset + 1] = v;
      pixels[offset + 2] = v;
      pixels[offset + 3] = 255;
    }
  }
  return pixels;
}

test('generator is deterministic', () => {
  const pixels = syntheticImage(256, 'disc');
  const options = {
    pixels,
    width: 256,
    height: 256,
    pinCount: 220,
    requestedLines: 600,
    circleMm: 260,
    lineMm: 0.2,
    autoStop: true
  };
  const first = generator.generate(options);
  const second = generator.generate(options);
  assert.deepStrictEqual(Array.from(first.sequence), Array.from(second.sequence));
});

test('generator produces a valid sequence', () => {
  const pixels = syntheticImage(256, 'disc');
  const result = generator.generate({
    pixels,
    width: 256,
    height: 256,
    pinCount: 220,
    requestedLines: 2000,
    circleMm: 260,
    lineMm: 0.2,
    autoStop: true
  });
  assert.ok(result.sequence.length >= 2);
  assert.ok(result.sequence.length <= 2001);
  assert.strictEqual(result.sequence[0], 0);
  result.sequence.forEach((pin) => {
    assert.ok(Number.isInteger(pin) && pin >= 0 && pin < 220, 'pin out of range: ' + pin);
  });
  assert.ok(result.lines === result.sequence.length - 1);
  assert.ok(result.threadMeters > 0);
});

test('generator respects crop parameters', () => {
  const pixels = syntheticImage(256, 'half');
  const full = generator.generate({
    pixels,
    width: 256,
    height: 256,
    pinCount: 160,
    requestedLines: 500,
    circleMm: 260,
    lineMm: 0.2,
    autoStop: true,
    cropX: 0.5,
    cropY: 0.5,
    cropZoom: 1
  });
  const corner = generator.generate({
    pixels,
    width: 256,
    height: 256,
    pinCount: 160,
    requestedLines: 500,
    circleMm: 260,
    lineMm: 0.2,
    autoStop: true,
    cropX: 0.25,
    cropY: 0.5,
    cropZoom: 1.6
  });
  // Different crops should usually give different sequences.
  assert.notDeepStrictEqual(Array.from(full.sequence), Array.from(corner.sequence));
});

test('generator reports progress and cancellation', () => {
  const pixels = syntheticImage(256, 'disc');
  let progressCalls = 0;
  let cancelled = false;
  const result = generator.generate({
    pixels,
    width: 256,
    height: 256,
    pinCount: 220,
    requestedLines: 5000,
    circleMm: 260,
    lineMm: 0.2,
    autoStop: true,
    onProgress: () => {
      progressCalls += 1;
      if (progressCalls > 3) cancelled = true;
    },
    cancelled: () => cancelled
  });
  assert.strictEqual(result, null);
  assert.ok(progressCalls >= 3);
});

test('minimumCropZoom / clampCropZoom math', () => {
  // A square image still needs a small zoom margin so the target circle
  // (251/255 × 0.98 of the square) fits inside: 100·0.9843·0.98/√(2·100²).
  const squareMin = (100 * generator.TARGET_RADIUS_RATIO * generator.FULL_IMAGE_FIT_MARGIN) / Math.hypot(100, 100);
  assert.ok(Math.abs(generator.minimumCropZoom(100, 100) - squareMin) < 1e-6);
  assert.ok(generator.minimumCropZoom(200, 100) < 1);
  assert.strictEqual(generator.clampCropZoom(100, 100, 9), generator.MAX_CROP_ZOOM);
  assert.ok(Math.abs(generator.clampCropZoom(100, 100, 0.1) - squareMin) < 1e-6);
});

/* ------------------------- 模板 PDF ------------------------- */

console.log('\n[模板 PDF]');

test('generateNailTemplate produces a valid PDF header/tail', () => {
  const bytes = pdfTemplate.generateNailTemplate({ nails: 120, circleMm: 260 });
  const header = String.fromCharCode.apply(null, Array.from(bytes.slice(0, 8)));
  const tail = String.fromCharCode.apply(null, Array.from(bytes.slice(-8)));
  assert.ok(header.startsWith('%PDF-1.4'));
  assert.ok(tail.includes('%%EOF'));
  assert.ok(bytes.length > 5000);
});

test('chooseTiling matches mini-program behaviour', () => {
  assert.deepStrictEqual(pdfTemplate.chooseTiling(276), {
    columns: 2,
    rows: 1,
    pages: 2,
    tileWidthMm: 138,
    tileHeightMm: 276
  });
});

test('pdf filename follows project convention', () => {
  assert.strictEqual(
    pdfTemplate.pdfFilename({ nails: 300, circleMm: 260 }, 'zh'),
    '绕线画钉位模板_300钉_260mm.pdf'
  );
  assert.strictEqual(
    pdfTemplate.pdfFilename({ nails: 300, circleMm: 260 }, 'en'),
    'String_Art_Nail_Template_300_nails_260mm.pdf'
  );
});

/* ------------------------- 汇总 ------------------------- */

const totalPassed = passed + (interop && interop.passed ? interop.passed : 0);
const totalFailed = failed + (interop && interop.failed ? interop.failed : 0);
console.log('\n结果: ' + totalPassed + ' 通过, ' + totalFailed + ' 失败');
process.exit(totalFailed ? 1 : 0);
