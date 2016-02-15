package com.foodlogiq.distributormobile.miscellaneousHelpers;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Formatters used to transform date objects into strings. Also used to parse strings into dates.
 */
public class DateFormatters {
    public static final SimpleDateFormat isoDateFormatter = new SimpleDateFormat
            ("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final SimpleDateFormat gs1Format = new SimpleDateFormat("yyMMdd");
    public static final SimpleDateFormat simpleFormat = new SimpleDateFormat("MM/dd/yyyy");
    public static final SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("MM/dd/yyyy 'at'" +
            " HH:mm:ss z");

    static {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        isoDateFormatter.setTimeZone(tz);
        gs1Format.setTimeZone(tz);
        simpleFormat.setTimeZone(tz);
        simpleTimeFormat.setTimeZone(tz);
    }
}
