package com.cucumber.tutorial.util;

public class StringUtils {

    public static String toOnelineReducedString(Object s, int limit) {
        return s != null ? crop(s.toString().replace(System.lineSeparator(), ""), limit) : null;
    }

    public static String crop(String msg, int limit) {
        if (msg != null && msg.length() > limit) {
            return msg.substring(0, limit) + "...";
        }
        return msg;
    }
}
