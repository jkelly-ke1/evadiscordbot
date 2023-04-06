package io.jkelly.evadiscordbot.util;

import io.jkelly.evadiscordbot.config.YamlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class Converter {

    YamlConfig yamlConfig;

    @Autowired
    public Converter(YamlConfig yamlConfig) {
        this.yamlConfig = yamlConfig;
    }

    public String timestampConverter(long weatherTimestamp, String pattern) {
        var formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
        var instant = Instant.ofEpochSecond(weatherTimestamp);
        return formatter.format(instant);
    }

    public boolean isContainsTrigger(String message) {
        List<String> dimasList = yamlConfig.getMageNameList();
        List<String> pigList = yamlConfig.getPigNameList();

        if (dimasList.stream().anyMatch(message::contains))
            return true;

        if (pigList.stream().anyMatch(message::contains))
            return true;

        return false;
    }

}
