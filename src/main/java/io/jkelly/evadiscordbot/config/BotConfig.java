package io.jkelly.evadiscordbot.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@NoArgsConstructor
@Getter
@Setter
@PropertySource(value = "/application.properties", encoding = "UTF-8")
public class BotConfig {

    @Value("${discord.bot.token}")
    private String botToken;

    @Value("${discord.bot.id}")
    private long botId;

    @Value("${discord.bot.messageLoggingEnabled}")
    private boolean isLoggingEnabled;

    @Value("${discord.bot.myGuildId}")
    private long serverId;

    @Value("${discord.bot.myGuildTerpilaRoleId}")
    private long terpilaRoleId;

    @Value("${discord.bot.mainChatId}")
    private long mainChatId;

    @Value("${discord.bot.maxPenaltyPoint}")
    private int maxPenaltyPoint;

    @Value("${discord.bot.maxBarrelCapacity}")
    private int maxBarrelCapacity;

    @Value("${discord.bot.helpEmbedGifLink}")
    private String helpEmbedGifLink;

    @Value("${discord.bot.lanaTriggerReplyId}")
    private long lanaTriggerReplyId;

}
