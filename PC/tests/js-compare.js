/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Runs the PC float32 generator with the SAME synthetic images as
 * tests/java/GenCompare.java and prints the sequence + hash for comparison.
 */
'use strict';

const fs = require('node:fs');
const path = require('node:path');
const generator = require('../src/core/generator.js');

function synthetic(size, kind) {
  const pixels = new Uint8ClampedArray(size * size * 4);
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      const dx = x - size / 2;
      const dy = y - size / 2;
      let dark = 0;
      if (kind === 'disc') dark = Math.hypot(dx, dy) < size * 0.32 ? 1 : 0;
      else dark = x < size * 0.5 ? 1 : 0;
      const v = dark ? 40 : 250;
      const o = (y * size + x) * 4;
      pixels[o] = v;
      pixels[o + 1] = v;
      pixels[o + 2] = v;
      pixels[o + 3] = 255;
    }
  }
  return pixels;
}

function runCase(pixels, opts) {
  const result = generator.generate(Object.assign({ pixels }, opts));
  return {
    len: result.sequence.length,
    seq: result.sequence.join(' '),
    hash: result.sequence.join(' ').hashCodeHex()
  };
}

String.prototype.hashCodeHex = function () {
  let hash = 0;
  const str = this;
  for (let i = 0; i < str.length; i++) {
    hash = (hash * 31 + str.charCodeAt(i)) | 0;
  }
  return (hash >>> 0).toString(16);
};

const size = 256;

// Case 1: disc, 220 nails, 800 lines, circle 260, line 0.2, autoStop, crop centre 1x.
const c1 = runCase(synthetic(size, 'disc'), {
  width: size, height: size,
  pinCount: 220, requestedLines: 800, circleMm: 260, lineMm: 0.2, autoStop: true,
  cropX: 0.5, cropY: 0.5, cropZoom: 1
});
console.log('CASE1_LEN=' + c1.len);
console.log('CASE1_SEQ=' + c1.seq);
console.log('CASE1_HASH=' + c1.hash);

// Case 2: half-plane, 160 nails, 500 lines, crop 0.25/0.5 zoom 1.6.
const c2 = runCase(synthetic(size, 'half'), {
  width: size, height: size,
  pinCount: 160, requestedLines: 500, circleMm: 260, lineMm: 0.2, autoStop: true,
  cropX: 0.25, cropY: 0.5, cropZoom: 1.6
});
console.log('CASE2_LEN=' + c2.len);
console.log('CASE2_SEQ=' + c2.seq);
console.log('CASE2_HASH=' + c2.hash);

fs.writeFileSync(path.join(__dirname, 'java', 'js-result.txt'),
  'CASE1_LEN=' + c1.len + '\nCASE1_HASH=' + c1.hash + '\nCASE1_SEQ=' + c1.seq +
  '\nCASE2_LEN=' + c2.len + '\nCASE2_HASH=' + c2.hash + '\nCASE2_SEQ=' + c2.seq, 'ascii');
console.log('saved');
