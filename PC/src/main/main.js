/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 *
 * Electron main process: window, native menu, project storage and the
 * image-to-sequence generation worker bridge.
 */
'use strict';

const { app, BrowserWindow, Menu, clipboard, dialog, ipcMain, shell } = require('electron');
const path = require('node:path');
const fs = require('node:fs');
const fsp = require('node:fs/promises');
const { Worker } = require('node:worker_threads');

const sar = require('../core/sar.js');
const textCodec = require('../core/text-codec.js');
const pdfTemplate = require('../core/pdf-template.js');

const MAX_IMAGE_BYTES = 64 * 1024 * 1024;
const MAX_TEXT_BYTES = 8 * 1024 * 1024;

let mainWindow = null;
let generatorWorker = null;

function projectsDir() {
  const dir = path.join(app.getPath('userData'), 'projects');
  fs.mkdirSync(dir, { recursive: true });
  return dir;
}

function recentFile() {
  return path.join(app.getPath('userData'), 'recent.json');
}

function readRecent() {
  try {
    const parsed = JSON.parse(fs.readFileSync(recentFile(), 'utf8'));
    return Array.isArray(parsed) ? parsed : [];
  } catch (_) {
    return [];
  }
}

function writeRecent(list) {
  try {
    fs.writeFileSync(recentFile(), JSON.stringify(list.slice(0, 50), null, 1), 'utf8');
  } catch (_) {
    /* non-fatal */
  }
}

function pushRecent(filePath, kind) {
  const list = readRecent().filter((entry) => entry.path !== filePath);
  list.unshift({ path: filePath, kind: kind || 'project', openedAt: Date.now() });
  writeRecent(list);
}

function decodeProjectBytes(bytes) {
  // .sar and legacy .bin both use the SAR container.
  return sar.decodeSar(bytes, { miniProgramLimits: false });
}

function createWindow() {
  const iconPath = path.join(__dirname, '..', 'renderer', 'assets', 'icon.png');
  mainWindow = new BrowserWindow({
    width: 1360,
    height: 860,
    minWidth: 1024,
    minHeight: 680,
    backgroundColor: '#101016',
    icon: fs.existsSync(iconPath) ? iconPath : undefined,
    title: '绕线助手 · String Art Helper (PC)',
    webPreferences: {
      preload: path.join(__dirname, '..', 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: false
    }
  });
  mainWindow.loadFile(path.join(__dirname, '..', 'renderer', 'index.html'));
  mainWindow.on('closed', () => {
    mainWindow = null;
  });
  if (process.env.SAH_SMOKE === '1' && !app.isPackaged) {
    // Dev-only UI smoke test; never run inside a packaged app, where
    // tests/smoke.js is not bundled.
    mainWindow.webContents.once('did-finish-load', () => {
      const runner = process.env.SAH_DIAG === '1'
        ? require('../../tests/diag-linewidth.js').runDiag
        : require('../../tests/smoke.js').runSmoke;
      runner(mainWindow).then((code) => app.exit(code || 0)).catch((error) => {
        console.error('[smoke] failed', error);
        app.exit(1);
      });
    });
  }
}

function buildMenu() {
  const isMac = process.platform === 'darwin';
  const template = [
    ...(isMac ? [{ role: 'appMenu' }] : []),
    {
      label: '文件',
      submenu: [
        {
          label: '打开图片并生成…',
          accelerator: 'CmdOrCtrl+O',
          click: () => mainWindow && mainWindow.webContents.send('menu:open-image')
        },
        {
          label: '打开序列/存档…',
          accelerator: 'CmdOrCtrl+Shift+O',
          click: () => mainWindow && mainWindow.webContents.send('menu:open-file')
        },
        { type: 'separator' },
        {
          label: '保存当前项目 (.sar)…',
          accelerator: 'CmdOrCtrl+S',
          click: () => mainWindow && mainWindow.webContents.send('menu:save-project')
        },
        {
          label: '导出序列 (.txt)…',
          accelerator: 'CmdOrCtrl+E',
          click: () => mainWindow && mainWindow.webContents.send('menu:export-txt')
        },
        {
          label: '导出钉位模板 (.pdf)…',
          accelerator: 'CmdOrCtrl+P',
          click: () => mainWindow && mainWindow.webContents.send('menu:export-pdf')
        },
        { type: 'separator' },
        { role: 'quit', label: '退出' }
      ]
    },
    {
      label: '视图',
      submenu: [
        { role: 'reload', label: '重新加载' },
        { role: 'toggleDevTools', label: '开发者工具' },
        { type: 'separator' },
        { role: 'resetZoom', label: '实际大小' },
        { role: 'zoomIn', label: '放大' },
        { role: 'zoomOut', label: '缩小' },
        { type: 'separator' },
        { role: 'togglefullscreen', label: '全屏' }
      ]
    },
    {
      label: '帮助',
      submenu: [
        {
          label: '关于',
          accelerator: 'F1',
          click: () => mainWindow && mainWindow.webContents.send('menu:about')
        },
        {
          label: '项目主页 (GitHub)',
          click: () => shell.openExternal('https://github.com/241120nzdjjx/StringArtHelper')
        },
        {
          label: '打开存档目录',
          click: () => shell.openPath(projectsDir())
        }
      ]
    }
  ];
  Menu.setApplicationMenu(Menu.buildFromTemplate(template));
}

/* ------------------------- IPC handlers ------------------------- */

function registerIpc() {
  ipcMain.handle('app:getInfo', () => ({
    version: app.getVersion(),
    electron: process.versions.electron,
    chrome: process.versions.chrome,
    node: process.versions.node,
    platform: process.platform,
    isPackaged: app.isPackaged,
    projectsDir: projectsDir()
  }));

  /* About / contact / support helpers (mirror the Android app's about page). */
  ipcMain.handle('sys:openExternal', (_event, url) => {
    const value = String(url || '');
    if (/^(https?|mailto):/i.test(value)) {
      shell.openExternal(value);
      return true;
    }
    return false;
  });

  ipcMain.handle('sys:copyText', (_event, text) => {
    clipboard.writeText(String(text == null ? '' : text));
    return true;
  });

  ipcMain.handle('dialog:openFile', async (_event, options) => {
    const result = await dialog.showOpenDialog(mainWindow, {
      properties: ['openFile'],
      filters: options && options.filters ? options.filters : undefined
    });
    if (result.canceled || !result.filePaths.length) return null;
    return result.filePaths[0];
  });

  ipcMain.handle('dialog:saveFile', async (_event, options) => {
    const result = await dialog.showSaveDialog(mainWindow, {
      defaultPath: options && options.defaultPath,
      filters: options && options.filters
    });
    if (result.canceled || !result.filePath) return null;
    return result.filePath;
  });

  ipcMain.handle('fs:readFileBytes', async (_event, filePath) => {
    const stat = await fsp.stat(filePath);
    if (stat.size > MAX_IMAGE_BYTES) throw new Error('文件超过 64 MB');
    return new Uint8Array(await fsp.readFile(filePath));
  });

  ipcMain.handle('fs:readTextFile', async (_event, filePath) => {
    const stat = await fsp.stat(filePath);
    if (stat.size > MAX_TEXT_BYTES) throw new Error('文件超过 8 MB');
    const bytes = await fsp.readFile(filePath);
    return textCodec.decodeText(new Uint8Array(bytes));
  });

  ipcMain.handle('fs:writeBytes', async (_event, filePath, bytes) => {
    await fsp.writeFile(filePath, Buffer.from(bytes));
    return true;
  });

  ipcMain.handle('fs:writeText', async (_event, filePath, text) => {
    await fsp.writeFile(filePath, text, 'utf8');
    return true;
  });

  ipcMain.handle('projects:list', async () => {
    const dir = projectsDir();
    const entries = await fsp.readdir(dir, { withFileTypes: true });
    const list = [];
    for (const entry of entries) {
      if (!entry.isFile() || !/\.(sar|bin)$/i.test(entry.name)) continue;
      const filePath = path.join(dir, entry.name);
      try {
        const stat = await fsp.stat(filePath);
        const bytes = await fsp.readFile(filePath);
        const project = decodeProjectBytes(bytes);
        list.push({
          name: project.name,
          path: filePath,
          index: project.index,
          timestamp: project.timestamp,
          nails: project.params.nails,
          circleMm: project.params.circleMm,
          lineMm: project.params.lineMm,
          size: stat.size,
          hasThumbnail: project.thumbnail.length > 0,
          thumbnail: project.thumbnail.length ? arrayToDataUrl(project.thumbnail) : null
        });
      } catch (err) {
        list.push({ name: entry.name, path: filePath, error: String(err && err.message) });
      }
    }
    list.sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0));
    return list;
  });

  ipcMain.handle('projects:read', async (_event, filePath) => {
    const bytes = await fsp.readFile(filePath);
    const project = decodeProjectBytes(bytes);
    pushRecent(filePath, 'project');
    return projectToRenderer(project);
  });

  ipcMain.handle('projects:decode', (_event, bytes) => {
    const project = decodeProjectBytes(new Uint8Array(bytes));
    return projectToRenderer(project);
  });

  ipcMain.handle('txt:parse', (_event, text, fileName) => {
    const parsed = textCodec.parseTxt(text, fileName || '');
    return {
      sequence: parsed.sequence,
      nails: parsed.nails,
      lineMm: parsed.lineMm,
      circleMm: parsed.circleMm
    };
  });

  ipcMain.handle('projects:save', async (_event, project, targetPath) => {
    let filePath = targetPath;
    if (!filePath) {
      filePath = path.join(projectsDir(), sar.sarFilename(project, 'zh'));
    }
    const bytes = sar.encodeSar4(project);
    await fsp.writeFile(filePath, Buffer.from(bytes));
    pushRecent(filePath, 'project');
    return filePath;
  });

  ipcMain.handle('projects:saveAs', async (_event, project) => {
    const suggested = sar.sarFilename(project, 'zh');
    const result = await dialog.showSaveDialog(mainWindow, {
      defaultPath: path.join(app.getPath('documents'), suggested),
      filters: [{ name: '绕线存档', extensions: ['sar', 'bin'] }]
    });
    if (result.canceled || !result.filePath) return null;
    const bytes = sar.encodeSar4(project);
    await fsp.writeFile(result.filePath, Buffer.from(bytes));
    pushRecent(result.filePath, 'project');
    return result.filePath;
  });

  ipcMain.handle('projects:delete', async (_event, filePath) => {
    await shell.trashItem(filePath);
    return true;
  });

  ipcMain.handle('projects:rename', async (_event, filePath, newName) => {
    const dir = path.dirname(filePath);
    const safe = String(newName).replace(/[\\/:*?"<>|]/g, '_').trim();
    if (!safe) throw new Error('存档名不能为空');
    const ext = path.extname(filePath);
    const target = path.join(dir, safe + ext);
    if (target !== filePath) await fsp.rename(filePath, target);
    return target;
  });

  ipcMain.handle('projects:openFolder', () => shell.openPath(projectsDir()));

  ipcMain.handle('projects:reveal', (_event, filePath) => shell.showItemInFolder(filePath));

  ipcMain.handle('recent:list', () => {
    const list = readRecent();
    return list
      .filter((entry) => {
        try {
          return fs.existsSync(entry.path);
        } catch (_) {
          return false;
        }
      })
      .map((entry) => ({ path: entry.path, kind: entry.kind, openedAt: entry.openedAt }));
  });

  /* image-to-sequence generation */
  ipcMain.handle('gen:start', async (_event, request) => {
    if (generatorWorker) {
      generatorWorker.terminate();
      generatorWorker = null;
    }
    return new Promise((resolve, reject) => {
      const worker = new Worker(path.join(__dirname, 'generate-worker.js'));
      generatorWorker = worker;
      let settled = false;
      const send = (channel, payload) => {
        if (mainWindow && !mainWindow.isDestroyed()) {
          mainWindow.webContents.send(channel, payload);
        }
      };
      worker.on('message', (message) => {
        if (message.type === 'progress') {
          send('gen:progress', message);
        } else if (message.type === 'done') {
          if (settled) return;
          settled = true;
          generatorWorker = null;
          resolve(message.result);
        } else if (message.type === 'cancelled') {
          if (settled) return;
          settled = true;
          generatorWorker = null;
          resolve(null);
        } else if (message.type === 'error') {
          if (settled) return;
          settled = true;
          generatorWorker = null;
          reject(new Error(message.message));
        }
      });
      worker.on('error', (error) => {
        if (settled) return;
        settled = true;
        generatorWorker = null;
        reject(error);
      });
      worker.on('exit', (code) => {
        if (settled) return;
        settled = true;
        generatorWorker = null;
        if (code !== 0) reject(new Error('生成线程异常退出 (' + code + ')'));
        else resolve(null);
      });
      worker.postMessage(request);
    });
  });

  ipcMain.handle('gen:cancel', () => {
    if (generatorWorker) {
      generatorWorker.terminate();
      generatorWorker = null;
    }
    return true;
  });

  ipcMain.handle('pdf:generate', (_event, options) => {
    return pdfTemplate.generateNailTemplate({
      nails: options && options.nails,
      circleMm: options && options.circleMm
    });
  });

  ipcMain.handle('pdf:saveAs', async (_event, options) => {
    const bytes = pdfTemplate.generateNailTemplate({
      nails: options && options.nails,
      circleMm: options && options.circleMm
    });
    const suggested = pdfTemplate.pdfFilename(options, 'zh');
    const result = await dialog.showSaveDialog(mainWindow, {
      defaultPath: path.join(app.getPath('documents'), suggested),
      filters: [{ name: 'PDF', extensions: ['pdf'] }]
    });
    if (result.canceled || !result.filePath) return null;
    await fsp.writeFile(result.filePath, Buffer.from(bytes));
    return result.filePath;
  });

  ipcMain.handle('txt:saveAs', async (_event, project) => {
    const values = project && project.sequence;
    if (!values || values.length < 2) throw new Error('没有可导出的序列');
    const text = textCodec.toTxt(values, {
      nails: project.nails,
      lineMm: project.lineMm,
      circleMm: project.circleMm
    });
    const suggested = textCodec.txtFilename(
      project.importedFileName || 'image',
      { nails: project.nails, lineMm: project.lineMm, circleMm: project.circleMm },
      'zh'
    );
    const result = await dialog.showSaveDialog(mainWindow, {
      defaultPath: path.join(app.getPath('documents'), suggested),
      filters: [{ name: '文本', extensions: ['txt'] }]
    });
    if (result.canceled || !result.filePath) return null;
    await fsp.writeFile(result.filePath, text, 'utf8');
    return result.filePath;
  });
}

async function loadProjectFromPath(filePath) {
  const bytes = await fsp.readFile(filePath);
  const project = decodeProjectBytes(bytes);
  pushRecent(filePath, 'project');
  return projectToRenderer(project);
}

function projectToRenderer(project) {
  return {
    name: project.name,
    importedFileName: project.importedFileName,
    index: project.index,
    timestamp: project.timestamp,
    nails: project.params.nails,
    circleMm: project.params.circleMm,
    lineMm: project.params.lineMm,
    sequence: project.sequence,
    thumbnail: project.thumbnail.length ? arrayToDataUrl(project.thumbnail) : null,
    magic: project.magic
  };
}

function arrayToDataUrl(bytes) {
  return 'data:image/png;base64,' + Buffer.from(bytes).toString('base64');
}

app.whenReady().then(() => {
  createWindow();
  buildMenu();
  registerIpc();
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (generatorWorker) {
    generatorWorker.terminate();
    generatorWorker = null;
  }
  if (process.platform !== 'darwin') app.quit();
});
