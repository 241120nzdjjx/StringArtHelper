/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 *
 * Nail template PDF (A4, fewest pages, trim/alignment marks, 100 mm ruler).
 * Adapted from wechat-miniprogram/utils/pdf.js (same project), which itself
 * mirrors the Android app's writeTemplatePdf.
 */
'use strict';

const PT_PER_MM = 72 / 25.4;
const PAGE_WIDTH_PT = 595;
const PAGE_HEIGHT_PT = 842;
const PAGE_WIDTH_MM = 210;
const PAGE_HEIGHT_MM = 297;
const USABLE_WIDTH_MM = 190;
const USABLE_HEIGHT_MM = 277;
const LABEL_RING_MM = 8;

function pt(mm) {
  return mm * PT_PER_MM;
}

function clamp(value, minimum, maximum) {
  return Math.max(minimum, Math.min(maximum, value));
}

function asciiBytes(value) {
  const bytes = new Uint8Array(value.length);
  for (let i = 0; i < value.length; i += 1) bytes[i] = value.charCodeAt(i) & 0xff;
  return bytes;
}

function escapePdfText(value) {
  return String(value).replace(/([\\()])/g, '\\$1');
}

function sanitizeFilename(value, fallback) {
  return (
    String(value || fallback || 'String_Art_Nail_Template')
      .replace(/[\\/:*?"<>|\x00-\x1f]/g, '_')
      .replace(/\s+/g, ' ')
      .trim()
      .replace(/[. ]+$/g, '') || String(fallback || 'String_Art_Nail_Template')
  );
}

function pdfFilename(options, language) {
  const nails = Math.round(clamp(Number((options && options.nails)) || 200, 100, 500));
  const circleMm = Math.round(clamp(Number((options && options.circleMm)) || 260, 80, 1200));
  return language === 'en'
    ? 'String_Art_Nail_Template_' + nails + '_nails_' + circleMm + 'mm.pdf'
    : '绕线画钉位模板_' + nails + '钉_' + circleMm + 'mm.pdf';
}

function chooseTiling(templateSizeMm) {
  let best = null;
  for (let columns = 1; columns <= 12; columns += 1) {
    for (let rows = 1; rows <= 12; rows += 1) {
      if (templateSizeMm / columns > USABLE_WIDTH_MM || templateSizeMm / rows > USABLE_HEIGHT_MM) {
        continue;
      }
      const pages = columns * rows;
      if (!best || pages < best.pages || (pages === best.pages && columns > best.columns)) {
        best = { columns, rows, pages };
      }
    }
  }
  if (!best) throw new Error('模板尺寸超出 12 × 12 页的拼接上限');
  best.tileWidthMm = templateSizeMm / best.columns;
  best.tileHeightMm = templateSizeMm / best.rows;
  return best;
}

function circlePath(commands, cx, cy, radius, operation) {
  const k = 0.5522847498;
  commands.push(
    (cx + radius).toFixed(3) + ' ' + cy.toFixed(3) + ' m',
    (cx + radius).toFixed(3) + ' ' + (cy + k * radius).toFixed(3) + ' ' +
      (cx + k * radius).toFixed(3) + ' ' + (cy + radius).toFixed(3) + ' ' +
      cx.toFixed(3) + ' ' + (cy + radius).toFixed(3) + ' c',
    (cx - k * radius).toFixed(3) + ' ' + (cy + radius).toFixed(3) + ' ' +
      (cx - radius).toFixed(3) + ' ' + (cy + k * radius).toFixed(3) + ' ' +
      (cx - radius).toFixed(3) + ' ' + cy.toFixed(3) + ' c',
    (cx - radius).toFixed(3) + ' ' + (cy - k * radius).toFixed(3) + ' ' +
      (cx - k * radius).toFixed(3) + ' ' + (cy - radius).toFixed(3) + ' ' +
      cx.toFixed(3) + ' ' + (cy - radius).toFixed(3) + ' c',
    (cx + k * radius).toFixed(3) + ' ' + (cy - radius).toFixed(3) + ' ' +
      (cx + radius).toFixed(3) + ' ' + (cy - k * radius).toFixed(3) + ' ' +
      (cx + radius).toFixed(3) + ' ' + cy.toFixed(3) + ' c ' + operation
  );
}

function pagePoint(globalX, globalY, tileLeft, tileTop, marginX, marginY) {
  return {
    x: pt(marginX + globalX - tileLeft),
    y: PAGE_HEIGHT_PT - pt(marginY + globalY - tileTop)
  };
}

function addText(commands, text, fontPt, xPt, yPt, align) {
  const value = escapePdfText(text);
  let x = xPt;
  if (align === 'center') x -= String(text).length * fontPt * 0.25;
  if (align === 'right') x -= String(text).length * fontPt * 0.5;
  commands.push('BT /F1 ' + fontPt.toFixed(3) + ' Tf ' + x.toFixed(3) + ' ' + yPt.toFixed(3) + ' Td (' + value + ') Tj ET');
}

function addAlignmentMark(commands, x, y, inwardX, inwardY) {
  const length = pt(4);
  commands.push(
    x.toFixed(3) + ' ' + y.toFixed(3) + ' m ' + (x + inwardX * length).toFixed(3) + ' ' + y.toFixed(3) + ' l S',
    x.toFixed(3) + ' ' + y.toFixed(3) + ' m ' + x.toFixed(3) + ' ' + (y + inwardY * length).toFixed(3) + ' l S'
  );
}

function buildPage(options, tiling, row, column) {
  const nails = options.nails;
  const circleMm = options.circleMm;
  const templateSizeMm = circleMm + LABEL_RING_MM * 2;
  const radiusMm = circleMm / 2;
  const centerMm = LABEL_RING_MM + radiusMm;
  const labelRadiusMm = radiusMm + 3;
  const tileLeft = column * tiling.tileWidthMm;
  const tileTop = row * tiling.tileHeightMm;
  const marginX = (PAGE_WIDTH_MM - tiling.tileWidthMm) / 2;
  const marginY = (PAGE_HEIGHT_MM - tiling.tileHeightMm) / 2;
  const leftPt = pt(marginX);
  const bottomPt = PAGE_HEIGHT_PT - pt(marginY + tiling.tileHeightMm);
  const widthPt = pt(tiling.tileWidthMm);
  const heightPt = pt(tiling.tileHeightMm);
  const commands = ['0 G', '0 g', '0.25 w'];

  addText(commands, 'String Art Nail Template | ' + nails + ' nails | ' + circleMm + ' mm', pt(2.5), pt(10), PAGE_HEIGHT_PT - pt(5.2));
  addText(
    commands,
    'Page ' + (row * tiling.columns + column + 1) + '/' + tiling.pages +
      ' | Column ' + (column + 1) + '/' + tiling.columns + ' | Row ' + (row + 1) + '/' + tiling.rows,
    pt(2.2),
    PAGE_WIDTH_PT - pt(10),
    PAGE_HEIGHT_PT - pt(8.5),
    'right'
  );

  commands.push('[2 2] 0 d', leftPt.toFixed(3) + ' ' + bottomPt.toFixed(3) + ' ' + widthPt.toFixed(3) + ' ' + heightPt.toFixed(3) + ' re S', '[] 0 d');
  addAlignmentMark(commands, leftPt, bottomPt, 1, 1);
  addAlignmentMark(commands, leftPt + widthPt, bottomPt, -1, 1);
  addAlignmentMark(commands, leftPt, bottomPt + heightPt, 1, -1);
  addAlignmentMark(commands, leftPt + widthPt, bottomPt + heightPt, -1, -1);

  commands.push('q', leftPt.toFixed(3) + ' ' + bottomPt.toFixed(3) + ' ' + widthPt.toFixed(3) + ' ' + heightPt.toFixed(3) + ' re W n');
  const templateTopLeft = pagePoint(0, 0, tileLeft, tileTop, marginX, marginY);
  commands.push(
    '0.35 w',
    templateTopLeft.x.toFixed(3) + ' ' + (templateTopLeft.y - pt(templateSizeMm)).toFixed(3) + ' ' +
      pt(templateSizeMm).toFixed(3) + ' ' + pt(templateSizeMm).toFixed(3) + ' re S'
  );

  const center = pagePoint(centerMm, centerMm, tileLeft, tileTop, marginX, marginY);
  commands.push('0.35 w');
  circlePath(commands, center.x, center.y, pt(radiusMm), 'S');
  commands.push(
    (center.x - pt(5)).toFixed(3) + ' ' + center.y.toFixed(3) + ' m ' + (center.x + pt(5)).toFixed(3) + ' ' + center.y.toFixed(3) + ' l S',
    center.x.toFixed(3) + ' ' + (center.y - pt(5)).toFixed(3) + ' m ' + center.x.toFixed(3) + ' ' + (center.y + pt(5)).toFixed(3) + ' l S'
  );

  const arcSpacingMm = (Math.PI * circleMm) / nails;
  const fontSizeMm = clamp(arcSpacingMm * 0.55, 1.35, 2.6);
  const fontPt = pt(fontSizeMm);
  for (let index = 0; index < nails; index += 1) {
    const angle = (Math.PI * 2 * index) / nails;
    const nailX = centerMm + Math.cos(angle) * radiusMm;
    const nailY = centerMm + Math.sin(angle) * radiusMm;
    const dot = pagePoint(nailX, nailY, tileLeft, tileTop, marginX, marginY);
    circlePath(commands, dot.x, dot.y, pt(0.65), 'f');
    const label = pagePoint(
      centerMm + Math.cos(angle) * labelRadiusMm,
      centerMm + Math.sin(angle) * labelRadiusMm,
      tileLeft,
      tileTop,
      marginX,
      marginY
    );
    addText(commands, String(index), fontPt, label.x, label.y - fontPt * 0.32, 'center');
  }
  commands.push('Q');

  const rulerStart = (PAGE_WIDTH_PT - pt(100)) / 2;
  const rulerY = pt(6);
  commands.push(
    '0.5 w',
    rulerStart.toFixed(3) + ' ' + rulerY.toFixed(3) + ' m ' + (rulerStart + pt(100)).toFixed(3) + ' ' + rulerY.toFixed(3) + ' l S'
  );
  for (let tick = 0; tick <= 10; tick += 1) {
    const x = rulerStart + pt(tick * 10);
    commands.push(x.toFixed(3) + ' ' + (rulerY - pt(1.5)).toFixed(3) + ' m ' + x.toFixed(3) + ' ' + (rulerY + pt(1.5)).toFixed(3) + ' l S');
  }
  addText(commands, '100 mm', pt(2.1), PAGE_WIDTH_PT / 2, pt(9.2), 'center');
  addText(commands, 'Print at actual size / 100%', pt(2.4), PAGE_WIDTH_PT / 2, pt(2.2), 'center');
  return commands.join('\n') + '\n';
}

function generateNailTemplate(input) {
  const options = {
    nails: Math.round(clamp(Number((input && input.nails)) || 200, 100, 500)),
    circleMm: Math.round(clamp(Number((input && input.circleMm)) || 260, 80, 1200))
  };
  const tiling = chooseTiling(options.circleMm + LABEL_RING_MM * 2);
  const streams = [];
  for (let row = 0; row < tiling.rows; row += 1) {
    for (let column = 0; column < tiling.columns; column += 1) {
      streams.push(buildPage(options, tiling, row, column));
    }
  }

  const objects = [];
  const pageIds = streams.map((stream, index) => 4 + index * 2);
  objects.push('<< /Type /Catalog /Pages 2 0 R >>');
  objects.push('<< /Type /Pages /Kids [' + pageIds.map((id) => id + ' 0 R').join(' ') + '] /Count ' + streams.length + ' >>');
  objects.push('<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>');
  streams.forEach((stream, index) => {
    const contentId = 5 + index * 2;
    objects.push(
      '<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ' + PAGE_WIDTH_PT + ' ' + PAGE_HEIGHT_PT +
        '] /Resources << /Font << /F1 3 0 R >> >> /Contents ' + contentId + ' 0 R >>'
    );
    objects.push('<< /Length ' + stream.length + ' >>\nstream\n' + stream + 'endstream');
  });

  let pdf = '%PDF-1.4\n%\xE2\xE3\xCF\xD3\n';
  const offsets = [0];
  objects.forEach((object, index) => {
    offsets.push(pdf.length);
    pdf += index + 1 + ' 0 obj\n' + object + '\nendobj\n';
  });
  const xref = pdf.length;
  pdf += 'xref\n0 ' + (objects.length + 1) + '\n0000000000 65535 f \n';
  for (let index = 1; index < offsets.length; index += 1) {
    pdf += String(offsets[index]).padStart(10, '0') + ' 00000 n \n';
  }
  pdf += 'trailer\n<< /Size ' + (objects.length + 1) + ' /Root 1 0 R >>\nstartxref\n' + xref + '\n%%EOF\n';
  return asciiBytes(pdf);
}

module.exports = {
  LABEL_RING_MM,
  PAGE_HEIGHT_PT,
  PAGE_WIDTH_PT,
  PT_PER_MM,
  USABLE_HEIGHT_MM,
  USABLE_WIDTH_MM,
  chooseTiling,
  generateNailTemplate,
  pdfFilename,
  pt
};
