/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 *
 * Full UI exercise test. Launch with:
 *   SAH_SMOKE=1 npx electron . [--smoke-out <dir>]
 * Drives the real renderer through the complete user journey:
 *   empty → open image FROM A REAL FILE → crop → generate (live preview)
 *   → player (live progress) → save → menu:about → contact/support/tech
 *   → TXT import from a real file → preview animation.
 * Captures PNG screenshots at every stage, then exits.
 */
'use strict';

const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const pcSar = require('../src/core/sar.js');

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function capture(win, outDir, name) {
  const image = await win.webContents.capturePage();
  fs.mkdirSync(outDir, { recursive: true });
  const filePath = path.join(outDir, name + '.png');
  fs.writeFileSync(filePath, image.toPNG());
  console.log('[smoke] captured ' + filePath);
}

async function js(win, expression) {
  return win.webContents.executeJavaScript(expression);
}

async function runSmoke(win) {
  const outDir = process.env.SAH_SMOKE_OUT || path.join(process.env.TEMP || '/tmp', 'sah-smoke');
  const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'sah-smoke-'));
  console.log('[smoke] output dir: ' + outDir);
  await sleep(1500);
  await capture(win, outDir, '01-empty');

  // --- 1. Open a REAL image file (exercises fs:readFileBytes + loadImageFromBytes) ---
  const imageDataUrl = await js(
    win,
    `(async () => {
      const canvas = document.createElement('canvas');
      canvas.width = 900; canvas.height = 700;
      const ctx = canvas.getContext('2d');
      ctx.fillStyle = '#ffffff'; ctx.fillRect(0, 0, 900, 700);
      ctx.fillStyle = '#1f2430';
      ctx.beginPath(); ctx.arc(450, 360, 240, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#f2f0ec';
      ctx.beginPath(); ctx.arc(430, 300, 110, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#2a2f3d';
      ctx.beginPath(); ctx.arc(430, 360, 70, 0, Math.PI * 2); ctx.fill();
      return canvas.toDataURL('image/png');
    })()`
  );
  const imagePath = path.join(tmpDir, 'test-image.png');
  fs.writeFileSync(imagePath, Buffer.from(imageDataUrl.split(',')[1], 'base64'));
  await js(
    win,
    `(async () => {
      const bytes = await window.api.readFileBytes(${JSON.stringify(imagePath)});
      await window.__sah.loadImageFromBytes(bytes);
      return true;
    })()`
  );
  await sleep(900);
  await capture(win, outDir, '02-crop');

  // --- 2. Generate (120 nails for speed) with live per-line preview ---
  await js(
    win,
    `(async () => {
      window.__sah.state.gen.nails = 120;
      window.__sah.state.gen.requestedLines = 1200;
      window.__sah.state.gen.circleMm = 260;
      window.__sah.state.gen.lineMm = 0.2;
      document.getElementById('inp-nails').value = 120;
      document.getElementById('inp-lines').value = 1200;
      window.__sah.startGeneration();
      return true;
    })()`
  );
  await sleep(1500);
  await capture(win, outDir, '02b-generating');

  let mode = 'generating';
  for (let i = 0; i < 180; i += 1) {
    await sleep(500);
    mode = await js(win, 'window.__sah.state.mode');
    if (mode === 'player' || mode === 'empty') break;
  }
  console.log('[smoke] generation finished, mode=' + mode);
  if (mode === 'player') {
    await sleep(300);
    await capture(win, outDir, '03-player-step0');
    await js(win, 'window.__sah.state.project.index = 300; window.__sah.drawPlayerFrame(); true');
    await sleep(300);
    await capture(win, outDir, '04-player-step300');
    await js(win, 'window.__sah.saveProjectToProjects(); true');
    await sleep(700);
    await capture(win, outDir, '05-saved');
  }

  // --- 3. Menu event → About dialog (exercises the fixed onMenu bridge) ---
  win.webContents.send('menu:about');
  await sleep(500);
  await capture(win, outDir, '06-about');
  await js(win, 'window.__sah.showAboutContact(); true');
  await sleep(400);
  await capture(win, outDir, '07-contact');
  await js(win, 'window.__sah.showAboutSupport(); true');
  await sleep(400);
  await capture(win, outDir, '08-support');
  await js(win, 'window.__sah.showAboutTech(); true');
  await sleep(400);
  await capture(win, outDir, '09-tech');
  await js(win, 'window.__sah.closeModal(); true');

  // --- 4. Import a REAL Android-style TXT file (import preview with params) ---
  const txtPath = path.join(tmpDir, 'sample-sequence.txt');
  fs.writeFileSync(
    txtPath,
    '# 绕线助手导出\n# 钉数: 160\n# 线径: 0.15 mm\n# 钉位圆直径: 280 mm\n\n' +
      '0 → 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11 → 12 → 13 → 14 → 15 → 16\n'
  );
  await js(
    win,
    `(async () => {
      const text = await window.api.readTextFile(${JSON.stringify(txtPath)});
      await window.__sah.importTxt(text, 'sample-sequence.txt');
      return true;
    })()`
  );
  await sleep(500);
  const importState = await js(win, `({
    panelVisible: !document.getElementById('import-panel').classList.contains('hidden'),
    info: document.getElementById('import-info').textContent,
    line: document.getElementById('import-line-value').textContent,
    circle: document.getElementById('import-circle-value').textContent
  })`);
  console.log('[smoke] import-preview', JSON.stringify(importState));
  await capture(win, outDir, '10-txt-import');

  // Adjust the thread diameter slider; the preview should update live.
  await js(
    win,
    `(async () => {
      const slider = document.getElementById('import-line-range');
      slider.value = 45;
      slider.dispatchEvent(new Event('input', { bubbles: true }));
      return true;
    })()`
  );
  await sleep(300);
  const importAdjusted = await js(win, `({
    line: document.getElementById('import-line-value').textContent,
    projectLine: window.__sah.state.project.lineMm
  })`);
  console.log('[smoke] import-adjusted', JSON.stringify(importAdjusted));

  // Confirm the import.
  await js(win, `document.getElementById('btn-import-open').click(); true`);
  await sleep(400);
  const importConfirmed = await js(win, `({
    panelHidden: document.getElementById('import-panel').classList.contains('hidden'),
    playerPanelVisible: !document.getElementById('player-panel').classList.contains('hidden'),
    mode: window.__sah.state.mode,
    index: window.__sah.state.project.index,
    lineMm: window.__sah.state.project.lineMm
  })`);
  console.log('[smoke] import-confirmed', JSON.stringify(importConfirmed));
  await capture(win, outDir, '10b-txt-imported');

  // --- 5. Preview animation (fast replay) ---
  await js(
    win,
    `(async () => {
      window.__sah.state.project.index = 0;
      window.__sah.drawPlayerFrame();
      window.__sah.startPreviewAnimation();
      return true;
    })()`
  );
  await sleep(700);
  await capture(win, outDir, '11-animating');
  await js(win, 'window.__sah.stopPreviewAnimation(); true');

  // --- 6. Full-artwork toggle ---
  await js(
    win,
    `(async () => {
      window.__sah.state.player.showAll = true;
      window.__sah.state.project.index = 0;
      window.__sah.drawPlayerFrame();
      return true;
    })()`
  );
  await sleep(300);
  await capture(win, outDir, '12-show-all');

  // --- 7. Jump dialog (HTML modal, not window.prompt) ---
  await js(win, 'window.__sah.jumpToStep(); true');
  await sleep(400);
  const jumpState = await js(win, `({
    modalOpen: !document.getElementById('modal-overlay').classList.contains('hidden'),
    hasInput: !!document.getElementById('jump-input'),
    inputValue: document.getElementById('jump-input') ? document.getElementById('jump-input').value : null
  })`);
  console.log('[smoke] jump-dialog', JSON.stringify(jumpState));
  await capture(win, outDir, '13-jump');
  // Apply the jump.
  await js(
    win,
    `(async () => {
      const input = document.getElementById('jump-input');
      input.value = '7';
      input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
      return true;
    })()`
  );
  await sleep(400);
  const jumpResult = await js(win, `({
    modalClosed: document.getElementById('modal-overlay').classList.contains('hidden'),
    index: window.__sah.state.project.index
  })`);
  console.log('[smoke] jump-applied', JSON.stringify(jumpResult));

  // --- 8. .bin extension save import (legacy Android saves use .bin) ---
  const binPath = path.join(tmpDir, 'legacy-save.bin');
  fs.writeFileSync(
    binPath,
    Buffer.from(pcSar.encodeSar4({
      name: '旧版.bin存档',
      importedFileName: 'legacy.bin',
      index: 3,
      timestamp: 1600000000000,
      params: { nails: 180, circleMm: 300, lineMm: 0.15 },
      thumbnail: new Uint8Array(0),
      sequence: [0, 5, 12, 90, 44, 177, 3, 8]
    }))
  );
  await js(
    win,
    `(async () => {
      await window.__sah.openAnyPath(${JSON.stringify(binPath)});
      return true;
    })()`
  );
  await sleep(500);
  const binResult = await js(win, `({
    mode: window.__sah.state.mode,
    name: window.__sah.state.project ? window.__sah.state.project.name : null,
    nails: window.__sah.state.project ? window.__sah.state.project.nails : null,
    circleMm: window.__sah.state.project ? window.__sah.state.project.circleMm : null,
    seqLen: window.__sah.state.project ? window.__sah.state.project.sequence.length : 0
  })`);
  console.log('[smoke] bin-import', JSON.stringify(binResult));
  await capture(win, outDir, '14-bin-import');

  console.log('[smoke] done');
  return 0;
}

module.exports = { runSmoke };
