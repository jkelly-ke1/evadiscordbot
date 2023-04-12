package io.jkelly.evadiscordbot.service;

import io.jkelly.evadiscordbot.models.Message;
import io.jkelly.evadiscordbot.models.User;
import io.jkelly.evadiscordbot.repositores.MessageRepository;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;

    @Autowired
    public MessageService(MessageRepository messageRepository, UserService userService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    @Transactional
    public void addMessage(MessageReceivedEvent event) {
        messageRepository.save(messageMapper(event));
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
        if (userService.getUserByDiscordId(event.getAuthor().getIdLong()) != null) {
            return userService.getUserByDiscordId(event.getAuthor().getIdLong());
        } else {
            var user = new User();
            user.setDiscordId(event.getMessage().getAuthor().getIdLong());
            user.setUsername(event.getMessage().getAuthor().getName());
            userService.addUser(user);
            return user;
        }
    }

}
