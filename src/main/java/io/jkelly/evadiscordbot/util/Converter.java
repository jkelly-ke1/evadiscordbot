package io.jkelly.evadiscordbot.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class Converter {

    public String timestampConverter(long weatherTimestamp, String pattern) {
        var formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
        var instant = Instant.ofEpochSecond(weatherTimestamp);
        return formatter.format(instant);
    }

}
