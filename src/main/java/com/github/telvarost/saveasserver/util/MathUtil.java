package com.github.telvarost.saveasserver.util;

public class MathUtil {
    public static int tryParseInt(String toParse, int defaultValue) {
        try {
            return Integer.parseInt(toParse.trim());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }
}
