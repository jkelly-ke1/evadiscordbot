package io.jkelly.evadiscordbot.util;

import io.jkelly.evadiscordbot.config.YamlConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;


@Component
@Log4j2
public class Converter {

    private final YamlConfig yamlConfig;
    private final Random random;

    @Autowired
    public Converter(YamlConfig yamlConfig, Random random) {
        this.yamlConfig = yamlConfig;
        this.random = random;
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

    public String makeAnswer(String question) {
        var answerBuilder = new StringBuilder();
        answerBuilder.append(question)
                .replace(0, 4, yamlConfig.getMembersList()
                        .get(random.nextInt(yamlConfig.getMembersList().size())))
                .append("!");
        return String.format("**%s**", answerBuilder.toString());
    }

}