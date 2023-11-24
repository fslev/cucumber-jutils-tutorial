package com.cucumber.tutorial.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private DateUtils() {
    }

    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    public static String currentDateTime() {
        return DEFAULT_DATETIME_FORMATTER.format(ZonedDateTime.now());
    }
}
