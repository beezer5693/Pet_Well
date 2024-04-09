package org.brandon.petwellbackend.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    
    private DateTimeUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String parseAndFormatDateTime(LocalDateTime localDateTime) {
        return LocalDateTime
                .parse(localDateTime.toString())
                .format(DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm:ss:SS a"));
    }
}
