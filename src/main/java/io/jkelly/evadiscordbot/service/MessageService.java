package io.jkelly.evadiscordbot.service;

import io.jkelly.evadiscordbot.models.Message;
import io.jkelly.evadiscordbot.models.User;
import io.jkelly.evadiscordbot.repositores.MessageRepository;
import io.jkelly.evadiscordbot.repositores.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addMessage(Message message, User user) {
        userRepository.save(user);
        messageRepository.save(message);
    }


}
