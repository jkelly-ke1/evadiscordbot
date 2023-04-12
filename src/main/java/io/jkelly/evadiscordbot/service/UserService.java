package io.jkelly.evadiscordbot.service;

import io.jkelly.evadiscordbot.models.User;
import io.jkelly.evadiscordbot.repositores.UserRepository;
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
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(long discordId, String updatedUsername) {
        userRepository.getUserByDiscordId(discordId).ifPresent(user -> user.setUsername(updatedUsername));
    }

    public User getUserByDiscordId(long discordId) {
        Optional<User> user = userRepository.getUserByDiscordId(discordId);
        return user.orElseThrow();
    }

}
