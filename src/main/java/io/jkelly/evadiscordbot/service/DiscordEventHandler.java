package io.jkelly.evadiscordbot.service;

import io.jkelly.evadiscordbot.models.Message;
import io.jkelly.evadiscordbot.models.User;
import io.jkelly.evadiscordbot.util.Converter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class DiscordEventHandler extends ListenerAdapter {

    private final Converter converter;
    private final MessageService messageService;

    @Autowired
    public DiscordEventHandler(Converter converter, MessageService messageService) {
        this.converter = converter;
        this.messageService = messageService;
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

        messageService.addMessage(messageMapper(event), userMapper(event));

        log.info("message time: " + converter.timestampConverter(event.getMessage().getTimeCreated().toEpochSecond(),
                "HH:mm dd.MM.yyyy")
                + "\nmessage id: " + event.getMessage().getIdLong()
                + "\nauthor nickname: " + event.getAuthor().getName()
                + "\nauthor id: " + event.getAuthor().getIdLong());

        if (inputMessage.equals("!woof"))
            event.getChannel().sendMessage("Woof-woof!").queue();

        if (inputMessage.contains("димас"))
            event.getMessage().addReaction(Emoji.fromUnicode("\uD83E\uDDD9")).queue();

        if (event.getMessage().getContentRaw().contains("\uD83D\uDDF3") &&
                event.getMessage().getAuthor().getName().equals("evabot")) {
            event.getMessage().addReaction(Emoji.fromUnicode("1️⃣")).queue();
            event.getMessage().addReaction(Emoji.fromUnicode("2️⃣")).queue();
        }

    }

    public void vote(SlashCommandInteractionEvent event, String subjectText, String firstOption, String secondOption) {
        var voteMessageText = String.format("\uD83D\uDDF3 %s \n1. %s \n2. %s",
                subjectText, firstOption, secondOption);
        event.reply(voteMessageText).queue();
    }

    private Message messageMapper(MessageReceivedEvent event) {
        var message = new Message();
        message.setDiscordMessageId(event.getMessageIdLong());
        message.setMessageText(event.getMessage().getContentRaw());
        message.setMessageCreatedTime(event.getMessage().getTimeCreated().toLocalDateTime());
        message.setUser(userMapper(event));
        return message;
    }

    private User userMapper(MessageReceivedEvent event) {
        var user = new User();
        user.setDiscordId(event.getMessage().getAuthor().getIdLong());
        user.setUsername(event.getMessage().getAuthor().getName());
        return user;
    }

}