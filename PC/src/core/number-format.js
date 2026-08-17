/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 *
 * Number wording used for nail-number narration.
 * Ported 1:1 from Android's NailNumberFormatter.java.
 */
'use strict';

const CHINESE_DIGITS = ['零', '一', '二', '三', '四', '五', '六', '七', '八', '九'];

/**
 * Standard Mandarin cardinal wording for the supported nail range.
 * Values outside 0..1000 are left numeric so the system TTS can handle them.
 */
function chineseNumber(value) {
  if (value < 0) {
    if (value === -2147483648) return String(value);
    const absolute = -value;
    return absolute <= 1000 ? '负' + chineseNumber(absolute) : String(value);
  }
  if (value > 1000) return String(value);
  if (value === 1000) return '一千';
  if (value === 0) return CHINESE_DIGITS[0];

  let result = '';
  const hundreds = Math.floor(value / 100);
  const remainder = value % 100;
  if (hundreds > 0) {
    result += CHINESE_DIGITS[hundreds] + '百';
    if (remainder > 0 && remainder < 10) result += '零';
  }

  const tens = Math.floor(remainder / 10);
  const ones = remainder % 10;
  if (tens > 0) {
    if (tens > 1 || hundreds > 0) result += CHINESE_DIGITS[tens];
    result += '十';
  }
  if (ones > 0) result += CHINESE_DIGITS[ones];
  return result;
}

module.exports = { chineseNumber };
