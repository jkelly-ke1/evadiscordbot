package io.jkelly.evadiscordbot.service;

import io.jkelly.evadiscordbot.config.BotConfig;
import io.jkelly.evadiscordbot.util.Converter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class DiscordEventHandler extends ListenerAdapter {

    private final Converter converter;
    private final MessageService messageService;
    private final UserService userService;
    private final BotConfig botConfig;

    @Autowired
    public DiscordEventHandler(Converter converter, MessageService messageService,
                               UserService userService, BotConfig botConfig) {
        this.converter = converter;
        this.messageService = messageService;
        this.userService = userService;
        this.botConfig = botConfig;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent slashEvent) {
        if ("vote".equals(slashEvent.getName())) {
            vote(slashEvent,
                    slashEvent.getOption("subject").getAsString(),
                    slashEvent.getOption("1").getAsString(),
                    slashEvent.getOption("2").getAsString());
        }
    }

    // listen all messages
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        var inputMessage = event.getMessage().getContentRaw();

        if (botConfig.isLoggingEnabled())
            messageService.addMessage(event);

        if (inputMessage.equals("!woof"))
            event.getChannel().sendMessage(String.format("Woof-woof, <@%s>!", event.getAuthor().getIdLong())).queue();

        if (inputMessage.startsWith("!кто "))
            event.getChannel().sendMessage(converter.makeAnswer(inputMessage)).queue();

        if (converter.isContainsMageTrigger(inputMessage))
            event.getMessage().addReaction(Emoji.fromUnicode("\uD83E\uDDD9")).queue();

        if (converter.isContainsPigTrigger(inputMessage))
            event.getMessage().addReaction(Emoji.fromUnicode("\uD83D\uDC16")).queue();

        if (converter.isContainsShameTrigger(inputMessage)) {
            event.getMessage().addReaction(Emoji.fromUnicode("\uD83C\uDDF8")).queue();
            event.getMessage().addReaction(Emoji.fromUnicode("\uD83C\uDDED")).queue();
            event.getMessage().addReaction(Emoji.fromUnicode("\uD83C\uDDE6")).queue();
            event.getMessage().addReaction(Emoji.fromUnicode("\uD83C\uDDF2")).queue();
            event.getMessage().addReaction(Emoji.fromUnicode("\uD83C\uDDEA")).queue();
        }

        if (event.getMessage().getContentRaw().contains("\uD83D\uDDF3") &&
                event.getMessage().getAuthor().getName().equals("evabot")) {
            event.getMessage().addReaction(Emoji.fromUnicode("1️⃣")).queue();
            event.getMessage().addReaction(Emoji.fromUnicode("2️⃣")).queue();
        }

    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().getGuildById(botConfig.getServerId()).getDefaultChannel().asTextChannel()
                .sendMessage("✨Утречка!✨\n\uD83D\uDC36Вылезла из будки и берусь за работу!\uD83E\uDD71\uD83D\uDECC")
                .queue();

        var now = ZonedDateTime.now(ZoneId.of("Europe/Kiev"));
        var nextTime = now.withHour(13).withMinute(0).withSecond(0);

        if (now.compareTo(nextTime) > 0)
            nextTime = nextTime.plusDays(1);

        var durationBetweenEvent = Duration.between(now, nextTime);
        var initialDelay = durationBetweenEvent.getSeconds();
        var scheduledService = Executors.newScheduledThreadPool(1);

        scheduledService.scheduleAtFixedRate(() -> {
            var terpilaId = converter.earnRandomServerMember();
            converter.defineTerpila(event.getJDA().getGuildById(botConfig.getServerId()), terpilaId);
            var embed = new EmbedBuilder();

            if (terpilaId == 1089207045581971458L) {
                embed.setDescription(String.format("\uD83C\uDF89 Поздравляю, <@%s>! Ты **ТЕРПИЛА ДНЯ!** \uD83D\uDE40" +
                                "\nОй... \uD83D\uDE35\u200D\uD83D\uDCAB\uD83D\uDE35\u200D\uD83D\uDCAB\uD83D\uDE35\u200D\uD83D\uDCAB",
                        terpilaId));
            } else {
                embed.setDescription(String.format("\uD83C\uDF89 Поздравляю, <@%s>! Ты **ТЕРПИЛА ДНЯ!** \uD83D\uDE40",
                        terpilaId));
            }

            embed.setDescription(String.format("\uD83C\uDF89 Поздравляю, <@%s>! Ты **ТЕРПИЛА ДНЯ!** \uD83D\uDE40", terpilaId));
            embed.setColor(Color.RED);

            event.getJDA().getGuildById(botConfig.getServerId())
                    .getDefaultChannel().asTextChannel()
                    .sendMessageEmbeds(embed.build()).queue();
        }, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS).isDone();

    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        log.info("User {} (id '{}') changed his username to {}!",
                event.getOldName(), event.getUser().getIdLong(), event.getNewName());
        userService.updateUser(event.getUser().getIdLong(), event.getNewName());
    }

    public void vote(SlashCommandInteractionEvent event, String subjectText, String firstOption, String secondOption) {
        var voteMessageText = String.format("\uD83D\uDDF3 %s \n1. %s \n2. %s",
                subjectText, firstOption, secondOption);
        event.reply(voteMessageText).queue();
    }
}