package com.niuza.stringartreader2;

public final class NailNumberFormatterTest {
    private static void expect(int value, String expected) {
        String actual = NailNumberFormatter.chineseNumber(value);
        if (!expected.equals(actual)) {
            throw new AssertionError(value + ": expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) {
        expect(0, "零");
        expect(1, "一");
        expect(9, "九");
        expect(10, "十");
        expect(11, "十一");
        expect(19, "十九");
        expect(20, "二十");
        expect(21, "二十一");
        expect(99, "九十九");
        expect(100, "一百");
        expect(101, "一百零一");
        expect(109, "一百零九");
        expect(110, "一百一十");
        expect(119, "一百一十九");
        expect(171, "一百七十一");
        expect(200, "二百");
        expect(210, "二百一十");
        expect(500, "五百");
        expect(999, "九百九十九");
        expect(1000, "一千");
        expect(1001, "1001");
        System.out.println("Chinese nail-number formatting tests passed");
    }
}
