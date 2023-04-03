package io.jkelly.evadiscordbot.config;

import io.jkelly.evadiscordbot.service.DiscordEventHandler;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Component;

@Component
public class BotInitializer {

    public BotInitializer(BotConfig botConfig, DiscordEventHandler discordEventHandler) {
        var jda = JDABuilder.createDefault(botConfig.getBotToken())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS).build();
        jda.addEventListener(discordEventHandler);

        var commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("vote", "Create a poll")
                        .addOption(OptionType.STRING, "subject", "Vote subject", true)
                        .addOption(OptionType.STRING, "1", "Option 1", true)
                        .addOption(OptionType.STRING, "2", "Option 2", true)
        ).queue();

    }

}
