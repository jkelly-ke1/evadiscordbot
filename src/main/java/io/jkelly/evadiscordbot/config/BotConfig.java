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
@PropertySource("/application.properties")
public class BotConfig {

    @Value("${discord.bot.token}")
    private String botToken;

    @Value("${discord.bot.messageLoggingEnabled}")
    private boolean isLoggingEnabled;

    @Value("${discord.bot.myGuildId}")
    private long serverId;

    @Value("${discord.bot.myGuildTerpilaRoleName}")
    private String terpilaRoleName;

    @Value("${discord.bot.myGuildTerpilaRoleId}")
    private long terpilaRoleId;

}
