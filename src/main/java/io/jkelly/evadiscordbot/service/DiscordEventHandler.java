package io.jkelly.evadiscordbot.service;

import io.jkelly.evadiscordbot.config.BotConfig;
import io.jkelly.evadiscordbot.models.User;
import io.jkelly.evadiscordbot.util.BotFunctionsHelper;
import io.jkelly.evadiscordbot.util.TriggerChecker;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class DiscordEventHandler extends ListenerAdapter {

    private final BotFunctionsHelper botFunctionsHelper;
    private final TriggerChecker triggerChecker;
    private final MessageService messageService;
    private final UserService userService;
    private final BotConfig botConfig;

    @Autowired
    public DiscordEventHandler(BotFunctionsHelper botFunctionsHelper, TriggerChecker triggerChecker,
                               MessageService messageService, UserService userService, BotConfig botConfig) {
        this.botFunctionsHelper = botFunctionsHelper;
        this.triggerChecker = triggerChecker;
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
        var eventMessage = event.getMessage();
        var eventChannel = event.getChannel();
        var messageText = eventMessage.getContentRaw();

        if (botConfig.isLoggingEnabled())
            messageService.addMessage(event);

        if (messageText.startsWith("!jokepenalty "))
            eventChannel.sendMessage(botFunctionsHelper.addAndCheckPenaltyToUser(messageText, event.getJDA(),
                    eventMessage.getAuthor().getIdLong())).queue();

        if (messageText.startsWith("!restore "))
            eventChannel.sendMessage(botFunctionsHelper.restorePenalty(messageText, event.getJDA(),
                    eventMessage.getAuthor().getIdLong())).queue();

        if (messageText.startsWith("!кто "))
            eventMessage.reply(botFunctionsHelper.makeWhoAnswer(messageText)).queue();

        if (messageText.startsWith("!у кого "))
            eventMessage.reply(botFunctionsHelper.makeWhomAnswer(messageText)).queue();

        if (messageText.startsWith("!woof"))
            eventMessage.reply(String.format("Woof-woof, <@%s>!", event.getAuthor().getIdLong())).queue();

        if (messageText.equals("!help"))
            botFunctionsHelper.makeHelpMessage(eventChannel);

        if (messageText.startsWith("!avatar "))
            botFunctionsHelper.makeUserAvatarEmbed(messageText, event.getJDA(), eventChannel);

        if (triggerChecker.isContainsMageTrigger(messageText))
            eventMessage.addReaction(Emoji.fromUnicode("\uD83E\uDDD9")).queue();

        if (triggerChecker.isContainsPigTrigger(messageText))
            eventMessage.addReaction(Emoji.fromUnicode("\uD83D\uDC16")).queue();

        if (triggerChecker.isContainsShameTrigger(messageText)) {
            eventMessage.addReaction(Emoji.fromUnicode("\uD83C\uDDF8")).queue();
            eventMessage.addReaction(Emoji.fromUnicode("\uD83C\uDDED")).queue();
            eventMessage.addReaction(Emoji.fromUnicode("\uD83C\uDDE6")).queue();
            eventMessage.addReaction(Emoji.fromUnicode("\uD83C\uDDF2")).queue();
            eventMessage.addReaction(Emoji.fromUnicode("\uD83C\uDDEA")).queue();
        }

        if (eventMessage.getContentRaw().contains("\uD83D\uDDF3") &&
                eventMessage.getAuthor().getName().equals("evabot")) {
            eventMessage.addReaction(Emoji.fromUnicode("1️⃣")).queue();
            eventMessage.addReaction(Emoji.fromUnicode("2️⃣")).queue();
        }

    }

    // use to send startup message, define new terpila and reset penalty cooldown
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        var membersSb = new StringBuilder();
        var currentJda = event.getJDA();
        var mainChannel = currentJda.getGuildById(botConfig.getServerId())
                .getTextChannelById(botConfig.getMainChatId());

        for (var member : currentJda.getGuildById(botConfig.getServerId()).getMembers()) {
            membersSb.append(member.getEffectiveName()).append(" (").append(member.getIdLong()).append(")").append("; ");
        }
        log.info("Current guild members list:\n{}", membersSb.toString());

        mainChannel.sendMessage("✨Утречка!✨\n\uD83D\uDC36Вылезла из будки и берусь за работу!\uD83E\uDD71\uD83D\uDECC")
                .queue();

        var now = ZonedDateTime.now(ZoneId.of("Europe/Kiev"));
        var nextTime = now.withHour(13).withMinute(15).withSecond(0);

        if (now.compareTo(nextTime) > 0)
            nextTime = nextTime.plusDays(1);

        var durationBetweenEvent = Duration.between(now, nextTime);
        var initialDelay = durationBetweenEvent.getSeconds();
        var scheduledService = Executors.newScheduledThreadPool(1);

        scheduledService.scheduleAtFixedRate(() -> {
                    botFunctionsHelper.scheduledTerpilaTask(currentJda, mainChannel);
                    userService.getAllUser().forEach(user -> userService.updateUserPenaltyCooldown(user.getId(), false));
                },
                initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS).isDone();

    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        var mainChannel = event.getJDA().getGuildById(botConfig.getServerId())
                .getTextChannelById(botConfig.getMainChatId());
        mainChannel.sendMessage("\uD83E\uDD71\uD83D\uDE34 Ушла спать... \uD83D\uDCA4")
                .queue();
        log.info("Shutdown. Time: {}", event.getTimeShutdown().toString());
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        log.info("User {} (id '{}') changed his username to {}!",
                event.getOldName(), event.getUser().getIdLong(), event.getNewName());
        userService.updateUsername(event.getUser().getIdLong(), event.getNewName());
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        log.info("User {} join the server", event.getUser().getIdLong());
        var mainChannel = event.getJDA()
                .getGuildById(botConfig.getServerId()).getTextChannelById(botConfig.getMainChatId());
        mainChannel.sendMessage(String.format("✨Добро пожаловать, <@%s>!✨",
                event.getUser().getIdLong())).queue();
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        var mainChannel = event.getJDA()
                .getGuildById(botConfig.getServerId()).getTextChannelById(botConfig.getMainChatId());
        mainChannel.sendMessage(String.format("Пока, <@%s>. \nТебя будет не хватать... \uD83E\uDD7A",
                event.getJDA().getSelfUser().getIdLong())).queue();

    }

    public void vote(SlashCommandInteractionEvent event, String subjectText, String firstOption, String secondOption) {
        var voteMessageText = String.format("\uD83D\uDDF3 %s \n1. %s \n2. %s",
                subjectText, firstOption, secondOption);
        event.reply(voteMessageText).queue();
    }
}