/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 *
 * worker_thread runner for the core generator, keeping the main process
 * responsive while a sequence is being produced.
 */
'use strict';

const { parentPort } = require('node:worker_threads');
const generator = require('../core/generator.js');

parentPort.on('message', (request) => {
  try {
    let cancelled = false;
    const result = generator.generate({
      pixels: request.pixels,
      width: request.width,
      height: request.height,
      cropX: request.cropX,
      cropY: request.cropY,
      cropZoom: request.cropZoom,
      pinCount: request.pinCount,
      requestedLines: request.requestedLines,
      circleMm: request.circleMm,
      lineMm: request.lineMm,
      autoStop: request.autoStop,
      cancelled: () => cancelled,
      onProgress: (complete, total, snapshot) => {
        parentPort.postMessage({
          type: 'progress',
          complete,
          total,
          sequence: snapshot ? Array.from(snapshot) : null
        });
      }
    });
    parentPort.postMessage(
      result === null ? { type: 'cancelled' } : { type: 'done', result }
    );
  } catch (error) {
    parentPort.postMessage({ type: 'error', message: String(error && error.message) });
  }
});
