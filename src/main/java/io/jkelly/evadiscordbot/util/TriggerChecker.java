package io.jkelly.evadiscordbot.util;

import io.jkelly.evadiscordbot.config.YamlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TriggerChecker {

    private final YamlConfig yamlConfig;

    @Autowired
    public TriggerChecker(YamlConfig yamlConfig) {
        this.yamlConfig = yamlConfig;
    }

    public boolean isContainsMageTrigger(String message) {
        var mageList = yamlConfig.getMageNameList();
        return mageList.stream().anyMatch(message::contains);
    }

    public boolean isContainsPigTrigger(String message) {
        var pigList = yamlConfig.getPigNameList();
        return pigList.stream().anyMatch(message::contains);
    }

    public boolean isContainsShameTrigger(String message) {
        var shameList = yamlConfig.getShameList();
        return shameList.stream().anyMatch(message::contains);
    }

}