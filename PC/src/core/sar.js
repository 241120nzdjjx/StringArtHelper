/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 *
 * Compact, deterministic string-art archive encoding shared by every platform.
 * Byte-compatible with the Android app's DataOutputStream layout:
 *
 *   SAR2: magic, name, importedFileName, index, timestamp, count, values
 *   SAR3: + projectNails, projectCircleMm, projectLineMm
 *   SAR4: + thumbnailLength, thumbnail (PNG, <= 256 KiB)
 *
 * Strings use Java DataOutput "modified UTF-8" (2-byte length prefix).
 * This module is adapted from wechat-miniprogram/utils/sar.js (same project)
 * and cross-validated against the Android implementation.
 */
'use strict';

const MAX_FILE_BYTES = 16 * 1024 * 1024;
const MAX_THUMBNAIL_BYTES = 256 * 1024;
const MAX_SEQUENCE_LENGTH = 100000;
const MAX_NAME_LENGTH = 200;

function asBytes(input) {
  if (input instanceof Uint8Array) return input;
  if (input instanceof ArrayBuffer) return new Uint8Array(input);
  if (ArrayBuffer.isView(input)) {
    return new Uint8Array(input.buffer, input.byteOffset, input.byteLength);
  }
  return new Uint8Array(input || []);
}

/** Java modified UTF-8 encoding (CESU-8 style, no null bytes). */
function encodeJavaUTF(value) {
  const bytes = [];
  const text = String(value == null ? '' : value);
  for (let index = 0; index < text.length; index += 1) {
    const code = text.charCodeAt(index);
    if (code >= 0x0001 && code <= 0x007f) {
      bytes.push(code);
    } else if (code <= 0x07ff) {
      bytes.push(0xc0 | ((code >> 6) & 0x1f), 0x80 | (code & 0x3f));
    } else {
      bytes.push(
        0xe0 | ((code >> 12) & 0x0f),
        0x80 | ((code >> 6) & 0x3f),
        0x80 | (code & 0x3f)
      );
    }
  }
  if (bytes.length > 65535) throw new Error('SAR 字符串编码后超过 65535 字节');
  return new Uint8Array(bytes);
}

function decodeJavaUTF(bytes) {
  let result = '';
  for (let index = 0; index < bytes.length; ) {
    const first = bytes[index++];
    if ((first & 0x80) === 0) {
      if (first === 0) throw new Error('无效的 Modified UTF-8 空字符');
      result += String.fromCharCode(first);
    } else if ((first & 0xe0) === 0xc0) {
      if (index >= bytes.length) throw new Error('Modified UTF-8 意外结束');
      const second = bytes[index++];
      if ((second & 0xc0) !== 0x80) throw new Error('无效的 Modified UTF-8');
      result += String.fromCharCode(((first & 0x1f) << 6) | (second & 0x3f));
    } else if ((first & 0xf0) === 0xe0) {
      if (index + 1 >= bytes.length) throw new Error('Modified UTF-8 意外结束');
      const second = bytes[index++];
      const third = bytes[index++];
      if ((second & 0xc0) !== 0x80 || (third & 0xc0) !== 0x80) {
        throw new Error('无效的 Modified UTF-8');
      }
      result += String.fromCharCode(
        ((first & 0x0f) << 12) | ((second & 0x3f) << 6) | (third & 0x3f)
      );
    } else {
      throw new Error('无效的 Modified UTF-8 起始字节');
    }
  }
  return result;
}

class ByteWriter {
  constructor() {
    this.bytes = [];
  }

  writeByte(value) {
    this.bytes.push(value & 0xff);
  }

  writeUnsignedShort(value) {
    this.writeByte(value >>> 8);
    this.writeByte(value);
  }

  writeInt(value) {
    const number = Number(value) | 0;
    this.writeByte(number >>> 24);
    this.writeByte(number >>> 16);
    this.writeByte(number >>> 8);
    this.writeByte(number);
  }

  writeLong(value) {
    const number = Number(value);
    if (!Number.isSafeInteger(number)) throw new Error('SAR 时间戳超出安全整数范围');
    const high = Math.floor(number / 0x100000000);
    const low = number - high * 0x100000000;
    this.writeInt(high);
    this.writeByte(low / 0x1000000);
    this.writeByte(low / 0x10000);
    this.writeByte(low / 0x100);
    this.writeByte(low);
  }

  writeFloat(value) {
    const buffer = new ArrayBuffer(4);
    new DataView(buffer).setFloat32(0, Number(value), false);
    this.writeBytes(new Uint8Array(buffer));
  }

  writeUTF(value) {
    const encoded = encodeJavaUTF(value);
    this.writeUnsignedShort(encoded.length);
    this.writeBytes(encoded);
  }

  writeBytes(values) {
    const bytes = asBytes(values);
    for (let index = 0; index < bytes.length; index += 1) this.bytes.push(bytes[index]);
  }

  finish() {
    return new Uint8Array(this.bytes);
  }
}

class ByteReader {
  constructor(input) {
    this.bytes = asBytes(input);
    this.offset = 0;
  }

  require(count) {
    if (count < 0 || this.offset + count > this.bytes.length) {
      throw new Error('SAR 文件意外结束');
    }
  }

  readUnsignedByte() {
    this.require(1);
    return this.bytes[this.offset++];
  }

  readUnsignedShort() {
    return (this.readUnsignedByte() << 8) | this.readUnsignedByte();
  }

  readInt() {
    this.require(4);
    const view = new DataView(this.bytes.buffer, this.bytes.byteOffset + this.offset, 4);
    const value = view.getInt32(0, false);
    this.offset += 4;
    return value;
  }

  readLong() {
    const high = this.readInt();
    this.require(4);
    const view = new DataView(this.bytes.buffer, this.bytes.byteOffset + this.offset, 4);
    const low = view.getUint32(0, false);
    this.offset += 4;
    const value = high * 0x100000000 + low;
    if (!Number.isSafeInteger(value)) throw new Error('SAR 时间戳超出安全整数范围');
    return value;
  }

  readFloat() {
    this.require(4);
    const view = new DataView(this.bytes.buffer, this.bytes.byteOffset + this.offset, 4);
    const value = view.getFloat32(0, false);
    this.offset += 4;
    return value;
  }

  readBytes(count) {
    if (!Number.isInteger(count) || count < 0) throw new Error('SAR 长度字段无效');
    this.require(count);
    const value = this.bytes.slice(this.offset, this.offset + count);
    this.offset += count;
    return value;
  }

  readUTF() {
    const length = this.readUnsignedShort();
    return decodeJavaUTF(this.readBytes(length));
  }
}

function isPng(bytes) {
  return (
    bytes.length >= 8 &&
    bytes[0] === 0x89 && bytes[1] === 0x50 && bytes[2] === 0x4e && bytes[3] === 0x47 &&
    bytes[4] === 0x0d && bytes[5] === 0x0a && bytes[6] === 0x1a && bytes[7] === 0x0a
  );
}

function validateProject(project, miniProgramLimits) {
  const name = String(project.name || '');
  if (!name || name.length > MAX_NAME_LENGTH) throw new Error('存档名称为空或超过 200 个字符');
  if (!Array.isArray(project.sequence) || project.sequence.length < 2 || project.sequence.length > MAX_SEQUENCE_LENGTH) {
    throw new Error('SAR 钉号数量异常');
  }
  if (!Number.isInteger(project.index) || project.index < 0 || project.index >= project.sequence.length) {
    throw new Error('SAR 当前进度无效');
  }
  if (!Number.isInteger(project.params.nails) || project.params.nails < 2 || project.params.nails > 10000) {
    throw new Error('SAR 钉数无效');
  }
  if (miniProgramLimits && project.params.nails > 500) throw new Error('该存档钉数超过小程序支持的 500 钉');
  if (!Number.isFinite(project.params.circleMm) || project.params.circleMm <= 0) throw new Error('SAR 圆径无效');
  if (miniProgramLimits && (project.params.circleMm < 80 || project.params.circleMm > 1200)) {
    throw new Error('该存档圆径超出小程序支持的 80～1200 mm');
  }
  if (!Number.isFinite(project.params.lineMm) || project.params.lineMm < 0.01 || project.params.lineMm > 1) {
    throw new Error('SAR 线径无效');
  }
  project.sequence.forEach((value) => {
    if (!Number.isInteger(value) || value < 0 || value >= project.params.nails) {
      throw new Error('SAR 包含非法钉号');
    }
  });
  const thumbnail = asBytes(project.thumbnail || []);
  if (thumbnail.length > MAX_THUMBNAIL_BYTES) throw new Error('SAR 缩略图超过 256 KiB');
  if (thumbnail.length && !isPng(thumbnail)) throw new Error('SAR 缩略图不是有效 PNG');
  return project;
}

function encodeSar4(project) {
  const normalized = validateProject({
    name: String(project.name || ''),
    importedFileName: String(project.importedFileName || project.sourceName || ''),
    index: Number(project.index),
    timestamp: Number(project.timestamp || project.updatedAt || Date.now()),
    params: {
      nails: Number(project.params && project.params.nails),
      circleMm: Math.round(Number(project.params && project.params.circleMm)),
      lineMm: Number(project.params && project.params.lineMm)
    },
    thumbnail: asBytes(project.thumbnail || []),
    sequence: Array.from(project.sequence || [])
  }, false);
  const writer = new ByteWriter();
  writer.writeUTF('SAR4');
  writer.writeUTF(normalized.name);
  writer.writeUTF(normalized.importedFileName);
  writer.writeInt(normalized.index);
  writer.writeLong(normalized.timestamp);
  writer.writeInt(normalized.params.nails);
  writer.writeInt(normalized.params.circleMm);
  writer.writeFloat(normalized.params.lineMm);
  writer.writeInt(normalized.thumbnail.length);
  writer.writeBytes(normalized.thumbnail);
  writer.writeInt(normalized.sequence.length);
  normalized.sequence.forEach((value) => writer.writeInt(value));
  return writer.finish();
}

function decodeSar(input, options) {
  const bytes = asBytes(input);
  if (bytes.length > MAX_FILE_BYTES) throw new Error('SAR 文件超过 16 MiB');
  const reader = new ByteReader(bytes);
  const magic = reader.readUTF();
  if (magic !== 'SAR2' && magic !== 'SAR3' && magic !== 'SAR4') {
    throw new Error('未知 SAR magic: ' + magic);
  }
  const project = {
    magic,
    name: reader.readUTF(),
    importedFileName: reader.readUTF(),
    index: reader.readInt(),
    timestamp: reader.readLong(),
    params: { nails: 0, circleMm: 260, lineMm: 0.2 },
    thumbnail: new Uint8Array(0),
    sequence: []
  };
  if (magic === 'SAR3' || magic === 'SAR4') {
    project.params.nails = reader.readInt();
    project.params.circleMm = reader.readInt();
    project.params.lineMm = reader.readFloat();
    if (!Number.isFinite(project.params.lineMm)) throw new Error('SAR 线径浮点数无效');
    project.params.lineMm = Math.round(project.params.lineMm * 100) / 100;
  }
  if (magic === 'SAR4') {
    const thumbnailLength = reader.readInt();
    if (thumbnailLength < 0 || thumbnailLength > MAX_THUMBNAIL_BYTES) {
      throw new Error('SAR 缩略图长度无效');
    }
    project.thumbnail = reader.readBytes(thumbnailLength);
  }
  const count = reader.readInt();
  if (count < 2 || count > MAX_SEQUENCE_LENGTH) throw new Error('SAR 钉号数量异常');
  for (let index = 0; index < count; index += 1) project.sequence.push(reader.readInt());
  if (reader.offset !== reader.bytes.length) throw new Error('SAR 文件包含异常尾部数据');
  if (magic === 'SAR2') project.params.nails = Math.max.apply(null, project.sequence) + 1;
  return validateProject(project, !options || options.miniProgramLimits !== false);
}

/** Android-compatible save filename, e.g. 名字_第12步_绕线存档.sar */
function sarFilename(project, language) {
  const fallback = language === 'en' ? 'string_art_project' : '绕线项目';
  let name = String((project && project.name) || fallback)
    .replace(/[\\/:*?"<>|\x00-\x1f\x7f]/g, '_')
    .replace(/\s+/g, ' ')
    .trim()
    .replace(/[. ]+$/g, '')
    .slice(0, 60)
    .replace(/[. ]+$/g, '');
  if (!name) name = fallback;
  const step = Math.max(1, Math.floor(Number((project && project.index)) || 0) + 1);
  return language === 'en'
    ? name + '_step_' + step + '_string_art_save.sar'
    : name + '_第' + step + '步_绕线存档.sar';
}

module.exports = {
  ByteReader,
  ByteWriter,
  MAX_FILE_BYTES,
  MAX_NAME_LENGTH,
  MAX_SEQUENCE_LENGTH,
  MAX_THUMBNAIL_BYTES,
  decodeJavaUTF,
  decodeSar,
  encodeJavaUTF,
  encodeSar4,
  isPng,
  sarFilename,
  validateProject
};
