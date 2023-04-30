package io.jkelly.evadiscordbot.util;

import io.jkelly.evadiscordbot.config.BotConfig;
import io.jkelly.evadiscordbot.config.YamlConfig;
import io.jkelly.evadiscordbot.service.UserService;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Random;

@Component
@Log4j2
public class BotFunctionsHelper {

    private final YamlConfig yamlConfig;
    private final Random random;
    private final UserService userService;
    private final BotConfig botConfig;

    @Autowired
    public BotFunctionsHelper(YamlConfig yamlConfig, Random random,
                              UserService userService, BotConfig botConfig) {
        this.yamlConfig = yamlConfig;
        this.random = random;
        this.userService = userService;
        this.botConfig = botConfig;
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

    public void defineTerpila(Guild guild, long terpilaId) {
        for (Member member : guild.getMembers()) {
            removeTerpila(guild, member.getIdLong());
        }
        guild.addRoleToMember(UserSnowflake.fromId(terpilaId), guild.getRoleById(botConfig.getTerpilaRoleId())).queue();
        log.warn("Terpila granted to {} ({})",
                guild.getMember(UserSnowflake.fromId(terpilaId)).getUser().getName(), terpilaId);
    }

    public void removeTerpila(Guild guild, long discordUserid) {
        guild.removeRoleFromMember(UserSnowflake.fromId(discordUserid),
                guild.getRoleById(botConfig.getTerpilaRoleId())).complete();
    }

    private long earnRandomServerMember() {
        var users = userService.getAllUser();
        var userId = users.get(random.nextInt(users.size())).getDiscordId();
        log.info("Received random user id {}", userId);
        return userId;
    }

    public void scheduledTerpilaTask(JDA currentJda, TextChannel mainChannel) {
        var terpilaId = earnRandomServerMember();
        defineTerpila(currentJda.getGuildById(botConfig.getServerId()), terpilaId);
        var embed = new EmbedBuilder();

        if (terpilaId == botConfig.getBotId()) {
            embed.setDescription(String.format("\uD83C\uDF89 Поздравляю, <@%s>! Ты **ТЕРПИЛА ДНЯ!** \uD83D\uDE40" +
                            "\nОй... \uD83D\uDE35\u200D\uD83D\uDCAB\uD83D\uDE35\u200D\uD83D\uDCAB\uD83D\uDE35\u200D\uD83D\uDCAB",
                    terpilaId));
        } else {
            embed.setDescription(String.format("\uD83C\uDF89 Поздравляю, <@%s>! Ты **ТЕРПИЛА ДНЯ!** \uD83D\uDE40",
                    terpilaId));
        }

        embed.setDescription(String.format("\uD83C\uDF89 Поздравляю, <@%s>! Ты **ТЕРПИЛА ДНЯ!** \uD83D\uDE40",
                terpilaId));
        embed.setColor(Color.RED);
        mainChannel.sendMessageEmbeds(embed.build()).queue();
    }

}