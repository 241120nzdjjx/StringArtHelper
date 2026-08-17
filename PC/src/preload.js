/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 *
 * Preload bridge: exposes a minimal, promise-based API to the renderer.
 */
'use strict';

const { contextBridge, ipcRenderer, webUtils } = require('electron');
const numberFormat = require('./core/number-format.js');

contextBridge.exposeInMainWorld('api', {
  /* Mandarin cardinal wording for narration (Android NailNumberFormatter port) */
  chineseNumber: (value) => numberFormat.chineseNumber(Number(value)),

  /* drag-and-drop: resolve the absolute path of a dropped File */
  getPathForFile: (file) => {
    try {
      return webUtils.getPathForFile(file);
    } catch (_) {
      return null;
    }
  },

  getInfo: () => ipcRenderer.invoke('app:getInfo'),

  /* dialogs */
  openFile: (options) => ipcRenderer.invoke('dialog:openFile', options),
  saveFile: (options) => ipcRenderer.invoke('dialog:saveFile', options),

  /* filesystem */
  readFileBytes: (filePath) => ipcRenderer.invoke('fs:readFileBytes', filePath),
  readTextFile: (filePath) => ipcRenderer.invoke('fs:readTextFile', filePath),
  writeBytes: (filePath, bytes) => ipcRenderer.invoke('fs:writeBytes', filePath, bytes),
  writeText: (filePath, text) => ipcRenderer.invoke('fs:writeText', filePath, text),

  /* projects (auto-save directory) */
  listProjects: () => ipcRenderer.invoke('projects:list'),
  readProject: (filePath) => ipcRenderer.invoke('projects:read', filePath),
  decodeSave: (bytes) => ipcRenderer.invoke('projects:decode', bytes),
  parseTxt: (text, fileName) => ipcRenderer.invoke('txt:parse', text, fileName),
  saveProject: (project, targetPath) => ipcRenderer.invoke('projects:save', project, targetPath),
  saveProjectAs: (project) => ipcRenderer.invoke('projects:saveAs', project),
  deleteProject: (filePath) => ipcRenderer.invoke('projects:delete', filePath),
  renameProject: (filePath, newName) => ipcRenderer.invoke('projects:rename', filePath, newName),
  openProjectsFolder: () => ipcRenderer.invoke('projects:openFolder'),
  revealPath: (filePath) => ipcRenderer.invoke('projects:reveal', filePath),

  /* recent files */
  listRecent: () => ipcRenderer.invoke('recent:list'),

  /* generation */
  generate: (request) => ipcRenderer.invoke('gen:start', request),
  cancelGenerate: () => ipcRenderer.invoke('gen:cancel'),
  onGenerateProgress: (callback) => {
    const listener = (_event, payload) => callback(payload);
    ipcRenderer.on('gen:progress', listener);
    return () => ipcRenderer.removeListener('gen:progress', listener);
  },

  /* exports */
  savePdfAs: (options) => ipcRenderer.invoke('pdf:saveAs', options),
  generatePdfBytes: (options) => ipcRenderer.invoke('pdf:generate', options),
  saveTxtAs: (project) => ipcRenderer.invoke('txt:saveAs', project),

  /* about / contact / support */
  openExternal: (url) => ipcRenderer.invoke('sys:openExternal', url),
  copyText: (text) => ipcRenderer.invoke('sys:copyText', text),

  /* menu events */
  onMenu: (callback) => {
    const channels = [
      'menu:open-image',
      'menu:open-file',
      'menu:save-project',
      'menu:export-txt',
      'menu:export-pdf',
      'menu:about'
    ];
    // ipcRenderer.on passes the event object as the first argument, so each
    // channel needs its own bound listener to recover the channel name.
    const listeners = channels.map((channel) => {
      const listener = () => callback(channel);
      ipcRenderer.on(channel, listener);
      return { channel, listener };
    });
    return () => listeners.forEach(({ channel, listener }) => ipcRenderer.removeListener(channel, listener));
  }
});
