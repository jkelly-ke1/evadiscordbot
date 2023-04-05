package io.jkelly.evadiscordbot.service;


import io.jkelly.evadiscordbot.models.User;
import io.jkelly.evadiscordbot.repositores.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void addUser(User user) {
        if (userRepository.getUserByDiscordId(user.getDiscordId()).isEmpty())
            userRepository.save(user);
    }

    public User getUserByDiscordId(long discordId) {
        Optional<User> user = userRepository.getUserByDiscordId(discordId);
        return user.orElseThrow();
    }

}
