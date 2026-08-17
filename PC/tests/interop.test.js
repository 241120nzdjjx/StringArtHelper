/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 *
 * Cross-platform interoperability tests: the PC core modules are exercised
 * against the WeChat Mini Program's ORIGINAL modules (same repository,
 * byte-compatible with Android). This proves TXT export/parse round-trips,
 * SAR4 encoding/decoding and filename rules match across platforms.
 */
'use strict';

const assert = require('node:assert');

const pcSar = require('../src/core/sar.js');
const pcTxt = require('../src/core/text-codec.js');
const mpSar = require('../../wechat-miniprogram/utils/sar.js');
const mpSeq = require('../../wechat-miniprogram/utils/sequence.js');

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

/* ---------- TXT export/import cross-check ---------- */

/* Sequence pins must be < declared nails (220 → pins 0..219). */
const SAMPLE = [0, 87, 31, 145, 66, 2, 199, 40, 3, 219];

console.log('\n[TXT 跨端互操作]');

test('PC toTxt → Mini Program parseTxt round-trips', () => {
  const text = pcTxt.toTxt(SAMPLE, { nails: 220, lineMm: 0.2, circleMm: 260 });
  const parsed = mpSeq.parseTxt(text, 'round-trip.txt');
  assert.deepStrictEqual(parsed.sequence, SAMPLE);
  assert.strictEqual(parsed.nails, 220);
  assert.strictEqual(parsed.lineMm, 0.2);
  assert.strictEqual(parsed.circleMm, 260);
});

test('Mini Program toTxt → PC parseTxt round-trips', () => {
  const text = mpSeq.toTxt(SAMPLE, { nails: 220, lineMm: 0.2, circleMm: 260 });
  const parsed = pcTxt.parseTxt(text, 'round-trip.txt');
  assert.deepStrictEqual(parsed.sequence, SAMPLE);
  assert.strictEqual(parsed.nails, 220);
  assert.strictEqual(parsed.lineMm, 0.2);
  assert.strictEqual(parsed.circleMm, 260);
});

test('PC toTxt byte-identical to Mini Program toTxt', () => {
  const pc = pcTxt.toTxt(SAMPLE, { nails: 220, lineMm: 0.2, circleMm: 260 });
  const mp = mpSeq.toTxt(SAMPLE, { nails: 220, lineMm: 0.2, circleMm: 260 });
  assert.strictEqual(pc, mp);
});

test('TXT filename rules identical (zh + en)', () => {
  const source = '刻晴';
  const meta = { nails: 300, lineMm: 0.2, circleMm: 260 };
  assert.strictEqual(
    pcTxt.txtFilename(source, meta, 'zh'),
    mpSeq.txtFilename(source, meta, 'zh')
  );
  assert.strictEqual(
    pcTxt.txtFilename('Keqing', meta, 'en'),
    mpSeq.txtFilename('Keqing', meta, 'en')
  );
});

test('TXT metadata regexes identical to Mini Program', () => {
  assert.strictEqual(pcTxt.NAILS_METADATA_PATTERN.source, mpSeq.NAILS_METADATA_PATTERN.source);
  assert.strictEqual(pcTxt.THREAD_METADATA_PATTERN.source, mpSeq.THREAD_METADATA_PATTERN.source);
  assert.strictEqual(pcTxt.CIRCLE_METADATA_PATTERN.source, mpSeq.CIRCLE_METADATA_PATTERN.source);
  assert.strictEqual(pcTxt.STEP_PREFIX_PATTERN.source, mpSeq.STEP_PREFIX_PATTERN.source);
  assert.strictEqual(pcTxt.ONLY_NUMERIC_SYNTAX.source, mpSeq.ONLY_NUMERIC_SYNTAX.source);
});

/* ---------- SAR cross-check ---------- */

const PROJECT = {
  name: '跨端互操作测试',
  importedFileName: 'source.txt',
  index: 4,
  timestamp: 1700000000000,
  params: { nails: 220, circleMm: 260, lineMm: 0.2 },
  thumbnail: new Uint8Array(0),
  sequence: SAMPLE
};

console.log('\n[SAR 跨端互操作]');

test('PC encodeSar4 → Mini Program decodeSar', () => {
  const bytes = pcSar.encodeSar4(PROJECT);
  const decoded = mpSar.decodeSar(bytes, { miniProgramLimits: false });
  assert.strictEqual(decoded.name, PROJECT.name);
  assert.strictEqual(decoded.importedFileName, 'source.txt');
  assert.strictEqual(decoded.index, 4);
  assert.strictEqual(decoded.timestamp, 1700000000000);
  assert.strictEqual(decoded.params.nails, 220);
  assert.strictEqual(decoded.params.circleMm, 260);
  assert.strictEqual(decoded.params.lineMm, 0.2);
  assert.deepStrictEqual(Array.from(decoded.sequence), SAMPLE);
});

test('Mini Program encodeSar4 → PC decodeSar', () => {
  const bytes = mpSar.encodeSar4(PROJECT);
  const decoded = pcSar.decodeSar(bytes, { miniProgramLimits: false });
  assert.strictEqual(decoded.name, PROJECT.name);
  assert.strictEqual(decoded.index, 4);
  assert.strictEqual(decoded.params.nails, 220);
  assert.deepStrictEqual(Array.from(decoded.sequence), SAMPLE);
});

test('PC and Mini Program SAR4 encodings are byte-identical', () => {
  const pc = pcSar.encodeSar4(PROJECT);
  const mp = mpSar.encodeSar4(PROJECT);
  assert.deepStrictEqual(Array.from(pc), Array.from(mp));
});

test('SAR filename rules identical (zh + en)', () => {
  assert.strictEqual(
    pcSar.sarFilename({ name: '刻晴/项目. ', index: 137 }, 'zh'),
    mpSar.sarFilename({ name: '刻晴/项目. ', index: 137 }, 'zh')
  );
  assert.strictEqual(
    pcSar.sarFilename({ name: 'Keqing', index: 137 }, 'en'),
    mpSar.sarFilename({ name: 'Keqing', index: 137 }, 'en')
  );
});

test('Modified UTF-8 codecs interoperate', () => {
  const bytes = pcSar.encodeJavaUTF('跨端·互操作🧶');
  assert.strictEqual(mpSar.decodeJavaUTF(bytes), '跨端·互操作\ud83e\uddf6');
  const mpBytes = mpSar.encodeJavaUTF('中文\0😀');
  assert.strictEqual(pcSar.decodeJavaUTF(mpBytes), '中文\0\ud83d\ude00');
});

/* ---------- summary ---------- */

console.log('\n结果: ' + passed + ' 通过, ' + failed + ' 失败');
if (require.main === module && failed) process.exit(1);
module.exports = { passed, failed };
