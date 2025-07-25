package com.example.be_voluongquang.utils;

import java.math.BigDecimal;

public class CsvParserUtils {

    public static BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty())
            return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty())
            return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty())
            return null;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty())
            return null;
        return Boolean.parseBoolean(value.trim());
    }

    public static String parseString(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }
}