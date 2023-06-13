package io.jkelly.evadiscordbot.util;


import io.jkelly.evadiscordbot.config.YamlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PlainMessageMaker {

    private final YamlConfig yamlConfig;
    private final Random random;

    @Autowired
    public PlainMessageMaker(YamlConfig yamlConfig, Random random) {
        this.yamlConfig = yamlConfig;
        this.random = random;
    }

    public String makeWhoAnswer(String question) {
        var answer = new StringBuilder();
        answer.append(question)
                .replace(0, 4, yamlConfig.getMembersList()
                        .get(random.nextInt(yamlConfig.getMembersList().size())))
                .append("!");
        return String.format("**%s**", answer);
    }

    public String makeWhomAnswer(String question) {
        var answer = new StringBuilder();
        answer.append("У")
                .append(question.substring(7))
                .insert(2, yamlConfig.getAltMembersList()
                        .get(random.nextInt(yamlConfig.getAltMembersList().size())) + " ")
                .append("!");
        return String.format("**%s**", answer);
    }

    public String makeSlapAnswer(String message, long authorId) {
        var userIdSubstring = message.substring(message.indexOf("@") + 1, message.lastIndexOf(">"));
        return String.format("\uD83C\uDF51\uD83D\uDD90 ***Хлясь!***\n" +
                "<@%s> дал <@%s> **по жопе**!", authorId, userIdSubstring);
    }

    public String makeMagicBallAnswer(long authorId) {
        return String.format("\uD83C\uDFB1 | %s, <@%s>.", earnRandomMagicBallAnswer(), authorId);
    }

    private String earnRandomMagicBallAnswer() {
        var ballAnswers = yamlConfig.getMagicBallAnswersList();
        return ballAnswers.get(random.nextInt(ballAnswers.size()));
    }
}
