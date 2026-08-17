/* Debug: reproduce "save to projects button does nothing". */
'use strict';

const fs = require('node:fs');
const path = require('node:path');

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function js(win, expression) {
  return win.webContents.executeJavaScript(expression);
}

async function runDiag(win) {
  const savePath = process.env.SAH_SAVE_PATH ||
    'F:/AAA.盘备份/绕线助手/存档文件/项目 · 比耶_第345步_绕线存档.sar';
  await sleep(1500);
  // Load the user's real save through the normal project-open path.
  const loaded = await js(
    win,
    `(async () => {
      const bytes = await window.api.readFileBytes(${JSON.stringify(savePath)});
      const project = await window.api.decodeSave(bytes);
      window.__sah.state.project = {
        name: project.name,
        importedFileName: project.importedFileName,
        index: 4243,
        nails: project.nails,
        circleMm: project.circleMm,
        lineMm: project.lineMm,
        sequence: project.sequence,
        thumbnail: null,
        filePath: null
      };
      window.__sah.state.player.progressSequence = null;
      window.__sah.state.player.useActualRatio = true;
      window.__sah.setMode('player');
      return { nails: project.nails, circleMm: project.circleMm, len: project.sequence.length };
    })()`
  );
  console.log('[diag] loaded', JSON.stringify(loaded));
  await sleep(500);

  // Direct call with full error capture.
  const result = await js(
    win,
    `(async () => {
      try {
        await window.__sah.saveProjectToProjects();
        return { ok: true };
      } catch (error) {
        return { ok: false, error: String(error && error.stack || error) };
      }
    })()`
  );
  console.log('[diag] saveProjectToProjects =>', JSON.stringify(result));

  // Check the projects dir for the new file.
  const projectsDir = process.env.APPDATA + '/StringArtHelper/projects';
  const files = fs.existsSync(projectsDir)
    ? fs.readdirSync(projectsDir).filter((f) => f.endsWith('.sar')).sort()
    : [];
  console.log('[diag] projects dir files:', files.length);
  files.forEach((f) => console.log('  -', f));

  // Real button click (not direct function call).
  const clickResult = await js(
    win,
    `(async () => {
      const button = document.getElementById('btn-save-here');
      const before = window.__sah.state.project.filePath;
      button.click();
      await new Promise((r) => setTimeout(r, 1500));
      const toastEl = document.getElementById('toast');
      return {
        buttonFound: !!button,
        filePathBefore: before,
        filePathAfter: window.__sah.state.project.filePath,
        toastText: toastEl.classList.contains('hidden') ? '(hidden)' : toastEl.textContent,
        projectCount: document.querySelectorAll('#project-list .project-item').length
      };
    })()`
  );
  console.log('[diag] real-click =>', JSON.stringify(clickResult));

  // --- Reproduce the USER's exact scenario: TXT import preview ---
  const importResult = await js(
    win,
    `(async () => {
      // Drag-in style import (no metadata header, defaults used).
      await window.__sah.importTxt('# 绕线助手导出\\n# 钉数: 220\\n0 → 1 → 2 → 3 → 4 → 5 → 6 → 7\\n', 'import.txt');
      await new Promise((r) => setTimeout(r, 400));
      const stateBefore = {
        importing: window.__sah.state.importing,
        mode: window.__sah.state.mode,
        playerPanelVisible: !document.getElementById('player-panel').classList.contains('hidden'),
        importPanelVisible: !document.getElementById('import-panel').classList.contains('hidden'),
        saveHereVisible: !document.getElementById('btn-save-here').closest('#player-panel').classList.contains('hidden'),
        topSaveDisabled: document.getElementById('btn-save-project').disabled
      };
      // Try clicking the top-bar "保存项目" while in import preview.
      document.getElementById('btn-save-project').click();
      await new Promise((r) => setTimeout(r, 1500));
      const toastEl = document.getElementById('toast');
      const afterTopSave = {
        toast: toastEl.classList.contains('hidden') ? '(hidden)' : toastEl.textContent
      };
      // Confirm import, then click "存入项目列表".
      document.getElementById('btn-import-open').click();
      await new Promise((r) => setTimeout(r, 400));
      document.getElementById('btn-save-here').click();
      await new Promise((r) => setTimeout(r, 1500));
      return {
        stateBefore,
        afterTopSave,
        finalToast: document.getElementById('toast').classList.contains('hidden') ? '(hidden)' : document.getElementById('toast').textContent,
        filePath: window.__sah.state.project ? window.__sah.state.project.filePath : null
      };
    })()`
  );
  console.log('[diag] user-scenario =>', JSON.stringify(importResult));

  console.log('[diag] done');
  return 0;
}

module.exports = { runDiag };
