package com.niuza.stringartreader2;

/** Number wording used for nail-number speech. */
final class NailNumberFormatter {
    private static final String[] CHINESE_DIGITS = {
            "零", "一", "二", "三", "四", "五", "六", "七", "八", "九"
    };

    private NailNumberFormatter() { }

    /**
     * Uses standard Mandarin cardinal-number wording for the supported nail range.
     * Values outside 0..1000 are left numeric so the system TTS can handle them.
     */
    static String chineseNumber(int value) {
        if (value < 0) {
            if (value == Integer.MIN_VALUE) return String.valueOf(value);
            int absolute = -value;
            return absolute <= 1000 ? "负" + chineseNumber(absolute) : String.valueOf(value);
        }
        if (value > 1000) return String.valueOf(value);
        if (value == 1000) return "一千";
        if (value == 0) return CHINESE_DIGITS[0];

        StringBuilder result = new StringBuilder(8);
        int hundreds = value / 100;
        int remainder = value % 100;
        if (hundreds > 0) {
            result.append(CHINESE_DIGITS[hundreds]).append("百");
            if (remainder > 0 && remainder < 10) result.append("零");
        }

        int tens = remainder / 10;
        int ones = remainder % 10;
        if (tens > 0) {
            if (tens > 1 || hundreds > 0) result.append(CHINESE_DIGITS[tens]);
            result.append("十");
        }
        if (ones > 0) result.append(CHINESE_DIGITS[ones]);
        return result.toString();
    }
}
