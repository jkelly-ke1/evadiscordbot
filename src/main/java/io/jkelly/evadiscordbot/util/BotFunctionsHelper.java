package io.jkelly.evadiscordbot.util;

import io.jkelly.evadiscordbot.config.BotConfig;
import io.jkelly.evadiscordbot.config.YamlConfig;
import io.jkelly.evadiscordbot.service.UserService;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;


@Component
@Log4j2
public class BotFunctionsHelper {

    private final YamlConfig yamlConfig;
    private final Random random;
    private final UserService userService;
    private final BotConfig botConfig;

    @Autowired
    public BotFunctionsHelper(YamlConfig yamlConfig, Random random, UserService userService, BotConfig botConfig) {
        this.yamlConfig = yamlConfig;
        this.random = random;
        this.userService = userService;
        this.botConfig = botConfig;
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

    public void defineTerpila(Guild guild, long terpilaId) {
        for (Member member : guild.getMembers()) {
            removeTerpila(guild, member.getIdLong());
        }
        guild.addRoleToMember(UserSnowflake.fromId(terpilaId), guild.getRoleById(botConfig.getTerpilaRoleId())).queue();
        log.warn("Terpila granted to {} ({})",
                guild.getMember(UserSnowflake.fromId(terpilaId)).getNickname(), terpilaId);
    }

    public void removeTerpila(Guild guild, long discordUserid) {
        guild.removeRoleFromMember(UserSnowflake.fromId(discordUserid),
                guild.getRoleById(botConfig.getTerpilaRoleId())).complete();
    }

    public long earnRandomServerMember() {
        var users = userService.getAllUser();
        return users.get(random.nextInt(users.size())).getDiscordId();
    }

}