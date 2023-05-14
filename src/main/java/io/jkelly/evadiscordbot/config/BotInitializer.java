package io.jkelly.evadiscordbot.config;

import io.jkelly.evadiscordbot.service.DiscordEventHandler;
import io.jkelly.evadiscordbot.util.BotFunctionsHelper;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BotInitializer {

    @Autowired
    BotFunctionsHelper functionsHelper;

    public BotInitializer(BotConfig botConfig, DiscordEventHandler discordEventHandler) {
        var jda = JDABuilder.createDefault(botConfig.getBotToken())
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setActivity(Activity.listening(functionsHelper.earnRandomActivityStatus()))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MEMBERS)
                .build();
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
