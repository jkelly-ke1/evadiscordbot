package io.jkelly.evadiscordbot.service;

import io.jkelly.evadiscordbot.models.User;
import io.jkelly.evadiscordbot.repositores.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public void updateUsername(long discordId, String updatedUsername) {
        userRepository.getUserByDiscordId(discordId).ifPresent(user -> user.setUsername(updatedUsername));
    }

    @Transactional
    public void updateUserPenaltyPoint(long discordId, int penaltyPoint) {
        userRepository.getUserByDiscordId(discordId).ifPresent(user -> user.setPenaltyPoint(penaltyPoint));
    }

    @Transactional
    public void updateUserPenaltyCooldown(long discordId, boolean haveCooldown) {
        userRepository.getUserByDiscordId(discordId).ifPresent(user -> user.setOnPenaltyCooldown(haveCooldown));
    }

    @Transactional
    public void updateUserRouletteCooldown(long discordId, boolean haveRouletteCooldown) {
        userRepository.getUserByDiscordId(discordId).ifPresent(user -> user.setOnRouletteCooldown(haveRouletteCooldown));
    }

    @Transactional
    public void updateUserPunishmentAmount(long discordId) {
        userRepository.getUserByDiscordId(discordId).ifPresent(user -> user
                .setPunishmentAmount(user.getPunishmentAmount() + 1));
    }

    @Transactional
    public void updateUserBarrelCapacity(long discordId, int currentBarrelCapacity) {
        userRepository.getUserByDiscordId(discordId).ifPresent(user -> user
                .setBarrelCapacity(currentBarrelCapacity));
    }

    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByDiscordId(long discordId) {
        return userRepository.getUserByDiscordId(discordId);
    }

}
