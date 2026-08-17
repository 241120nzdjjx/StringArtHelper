/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Cross-checks the PC float32 generator against the REAL Java implementation
 * (tests/java/GenCore.java, a 1:1 copy of Android's StringArtGenerator.java
 * with only Bitmap replaced by an int[] buffer). Requires a JDK (javac/java).
 *   npm run test:java
 * Exits non-zero if the sequences differ at any position.
 */
'use strict';

const { execFileSync } = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');
const os = require('node:os');

const javaDir = path.join(__dirname, 'java');
const outDir = path.join(javaDir, 'out');
const tmp = fs.mkdtempSync(path.join(os.tmpdir(), 'sah-gencmp-'));

try {
  fs.mkdirSync(outDir, { recursive: true });
  console.log('[java-compare] compiling GenCore + GenCompare ...');
  execFileSync('javac', ['-d', outDir, 'GenCore.java', 'GenCompare.java'], { cwd: javaDir, stdio: 'pipe' });

  console.log('[java-compare] running Java reference ...');
  const javaOut = execFileSync('java', ['-cp', outDir, 'GenCompare'], { cwd: javaDir, encoding: 'utf8' });

  console.log('[java-compare] running JS float32 generator ...');
  const jsOut = execFileSync('node', [path.join(__dirname, 'js-compare.js')], { encoding: 'utf8' });

  const extract = (text, label) => {
    const line = String(text).split(/\r?\n/).find((l) => l.startsWith(label));
    return line ? line.slice(label.length) : null;
  };

  let failed = false;
  for (const c of ['CASE1', 'CASE2']) {
    const j = extract(javaOut, c + '_SEQ=');
    const s = extract(jsOut, c + '_SEQ=');
    if (j === null || s === null) {
      console.error('[java-compare] missing output for ' + c);
      failed = true;
      continue;
    }
    const jl = j.split(' ').map(Number);
    const sl = s.split(' ').map(Number);
    let firstDiff = -1;
    const n = Math.min(jl.length, sl.length);
    for (let i = 0; i < n; i++) {
      if (jl[i] !== sl[i]) {
        firstDiff = i;
        break;
      }
    }
    const equal = jl.length === sl.length && firstDiff === -1;
    if (equal) {
      console.log('  ✓ ' + c + ': Java and JS sequences identical (' + jl.length + ' pins)');
    } else {
      failed = true;
      console.error(
        '  ✗ ' + c + ': lengths java=' + jl.length + ' js=' + sl.length +
        ' firstDiff=' + firstDiff + ' java=' + jl[firstDiff] + ' js=' + sl[firstDiff]
      );
    }
  }
  process.exit(failed ? 1 : 0);
} catch (error) {
  console.error('[java-compare] failed:', error.message);
  process.exit(1);
} finally {
  try {
    fs.rmSync(tmp, { recursive: true, force: true });
  } catch (_) { /* ignore */ }
}
