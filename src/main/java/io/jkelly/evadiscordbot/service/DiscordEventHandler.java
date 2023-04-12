package io.jkelly.evadiscordbot.service;

import io.jkelly.evadiscordbot.util.Converter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class DiscordEventHandler extends ListenerAdapter {

    private final Converter converter;
    private final MessageService messageService;
    private final UserService userService;

    @Autowired
    public DiscordEventHandler(Converter converter, MessageService messageService, UserService userService) {
        this.converter = converter;
        this.messageService = messageService;
        this.userService = userService;
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
        messageService.addMessage(event);

        if (inputMessage.equals("!woof"))
            event.getChannel().sendMessage("Woof-woof!").queue();

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