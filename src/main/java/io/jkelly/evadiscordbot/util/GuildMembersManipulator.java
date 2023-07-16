package io.jkelly.evadiscordbot.util;

import io.jkelly.evadiscordbot.config.BotConfig;
import io.jkelly.evadiscordbot.config.YamlConfig;
import io.jkelly.evadiscordbot.service.UserService;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Log4j2
public class GuildMembersManipulator {

    private final YamlConfig yamlConfig;
    private final Random random;
    private final UserService userService;
    private final BotConfig botConfig;

    @Autowired
    public GuildMembersManipulator(YamlConfig yamlConfig, Random random,
                                   UserService userService, BotConfig botConfig) {
        this.yamlConfig = yamlConfig;
        this.random = random;
        this.userService = userService;
        this.botConfig = botConfig;
    }

    public void defineTerpila(Guild guild, long terpilaId) {
        for (Member member : guild.getMembers()) {
            removeTerpila(guild, member.getIdLong());
        }

        guild.addRoleToMember(UserSnowflake.fromId(terpilaId),
                guild.getRoleById(botConfig.getTerpilaRoleId())).queue();
        userService.updateUserPunishmentAmount(terpilaId);
        log.warn("Terpila granted to {} ({})",
                guild.getMember(UserSnowflake.fromId(terpilaId)).getUser().getName(), terpilaId);
    }

    // keep 'public' for possible uses in future
    public void removeTerpila(Guild guild, long discordUserid) {
        guild.removeRoleFromMember(UserSnowflake.fromId(discordUserid),
                guild.getRoleById(botConfig.getTerpilaRoleId())).complete();
    }

    // this method add penalty point to specific user
    public String addAndCheckPenaltyToUser(String message, JDA jda, long commandAuthorId) {
        var userIdSubstring = message.substring(message.indexOf("@") + 1, message.lastIndexOf(">"));
        var userId = Long.parseLong(userIdSubstring);
        var responseMessageBuilder = new StringBuilder();

        if (!userService.getUserByDiscordId(commandAuthorId).get().isOnPenaltyCooldown()) {
            if (userId != commandAuthorId) {
                if (userService.getUserByDiscordId(userId).isPresent()) {
                    var currentUserPenaltyPoints = userService.getUserByDiscordId(userId)
                            .get().getPenaltyPoint();

                    if (currentUserPenaltyPoints >= 0 && currentUserPenaltyPoints < botConfig.getMaxPenaltyPoint()) {
                        userService.updateUserPenaltyPoint(userId, currentUserPenaltyPoints + 1);
                        userService.updateUserPenaltyCooldown(commandAuthorId, true);

                        if (userService.getUserByDiscordId(userId).get()
                                .getPenaltyPoint() == botConfig.getMaxPenaltyPoint() - 1) {
                            responseMessageBuilder
                                    .append("Пользователю <@").append(userId).append(">")
                                    .append(" добавлен **один штрафной балл**!")
                                    .append("\nОсторожно, последнее предупреждение! <:rat_sight:1079427636717166612>");

                        } else if (userService.getUserByDiscordId(userId).get()
                                .getPenaltyPoint() == botConfig.getMaxPenaltyPoint() / 2) {
                            responseMessageBuilder
                                    .append("Пользователю <@").append(userId).append(">")
                                    .append(" добавлен **один штрафной балл**!")
                                    .append("\nУ тебя еще есть шанс *извиниться*! <:jokei:1035142207306481704>");

                        } else if (userService.getUserByDiscordId(userId).get()
                                .getPenaltyPoint() == botConfig.getMaxPenaltyPoint()) {
                            try {
                                userService.updateUserPenaltyPoint(userId, 0);
                                commitPenaltyByNickname(jda, userId, earnRandomJokerSuffix());
                                responseMessageBuilder
                                        .append(":clown: Ну и ну, вы расстроить партия! Штрафные санкции вам, <@")
                                        .append(userId)
                                        .append(">! <:rjobomba:1049320943912230972>");
                            } catch (HierarchyException hierarchyException) {
                                userService.updateUserPenaltyPoint(userId, 0);
                                responseMessageBuilder
                                        .append("\uD83D\uDCA2 Из за ограничения моей роли я не могу наказать тебя, <@")
                                        .append(userId)
                                        .append(">...")
                                        .append("\nНо знай что лично я... тебя ***крайне осуждаю***")
                                        .append(" <:rat_sight:1079427636717166612> " +
                                                "<:rat_sight:1079427636717166612> <:rat_sight:1079427636717166612>");
                                log.warn("Hierarchy exception was caught. " +
                                        "Can't change {} nickname due role restrictions", userId);
                            }

                        } else {
                            responseMessageBuilder
                                    .append("<:bruh:1050845792266616922> Пользователю <@")
                                    .append(userId)
                                    .append(">")
                                    .append(" добавлен **один штрафной балл**!");
                        }
                        log.info("User {} ({}) joker penalty point: {}",
                                jda.getGuildById(botConfig.getServerId()).getMemberById(userId).getUser().getName(),
                                userId, userService.getUserByDiscordId(userId).get().getPenaltyPoint());
                    }
                }
            } else {
                responseMessageBuilder
                        .append(String.format("<:confused_rat:1079428095599194194> " +
                                "**Себя надо любить а не наказывать, <@%s>!**" +
                                "\nВыдавать предупреждения самому себе запрещено!", commandAuthorId));
            }
        } else {
            responseMessageBuilder
                    .append(String.format("<@%s>\n❌ Вы уже использовали свое предупреждение. " +
                            "Возвращайтесь __***завтре***__! <:buket_tebe:1097898534126231633>", commandAuthorId));
        }

        return responseMessageBuilder.toString();
    }

    public String restorePenalty(String message, JDA jda, long commandAuthorId) {
        var userIdSubstring = message.substring(message.indexOf("@") + 1, message.lastIndexOf(">"));
        var userId = Long.parseLong(userIdSubstring);
        var currentGuild = jda.getGuildById(botConfig.getServerId());
        var responseMessageBuilder = new StringBuilder();

        if (!userService.getUserByDiscordId(commandAuthorId).get().isOnPenaltyCooldown()) {
            if (userId != commandAuthorId) {
                if (currentGuild != null) {
                    if (userService.getUserByDiscordId(userId).isPresent()) {
                        userService.updateUserPenaltyPoint(userId, 0);
                        var currentMember = currentGuild.getMember(UserSnowflake.fromId(userId));
                        var modifiedNickname = currentMember.getEffectiveName();
                        currentMember.modifyNickname(modifiedNickname.replaceAll("\\([^()]*\\)", "")
                                .trim()).queue();
                        responseMessageBuilder
                                .append(":eyes: Пользователь <@")
                                .append(userId)
                                .append(">")
                                .append(" **помилован и восстановлен**! Смотри мне блин!<:rat_sight:1079427636717166612>");
                    }
                }
            } else {
                responseMessageBuilder
                        .append(String.format("<:rat_sight:1079427636717166612> Очень хитро, <@%s>..." +
                                "\nНо снимать с себя наказание **нельзя**!", commandAuthorId));
            }
        } else {
            responseMessageBuilder
                    .append(String.format("<@%s>\n❌ Вы уже использовали свою возможность " +
                            "менять уровень предупреждения на сегодня. " +
                            "Возвращайтесь __***завтре***__! <:mda:1100928775539134494>", commandAuthorId));
        }

        return responseMessageBuilder.toString();
    }

    public String rollBarrel(JDA jda, long commandAuthorId) {
        var responseMessageBuilder = new StringBuilder();

        if (!userService.getUserByDiscordId(commandAuthorId).get().isOnRouletteCooldown()) {
            userService.updateUserRouletteCooldown(commandAuthorId, true);
            var currentUserBarrelCapacity = userService
                    .getUserByDiscordId(commandAuthorId)
                    .get().getBarrelCapacity();
            var definedBullet = random.nextInt(currentUserBarrelCapacity);

            if (definedBullet == currentUserBarrelCapacity - 1 || definedBullet == currentUserBarrelCapacity) {
                try {
                    responseMessageBuilder.append("\uD83C\uDFB2 Крутим барабан...")
                            .append("\n\uD83D\uDCA5<:revolver:1105169657171804234> Бам! Ты умер. :skull:");
                    jda.getGuildById(botConfig.getServerId())
                            .kick(UserSnowflake.fromId(commandAuthorId)).queue();
                    userService.updateUserRouletteCooldown(commandAuthorId, false);
                    userService.updateUserBarrelCapacity(commandAuthorId, botConfig.getMaxBarrelCapacity());
                } catch (HierarchyException hierarchyException) {
                    responseMessageBuilder
                            .append("\n\uD83E\uDD2C При обычных обстоятельствах ты бы умер, " +
                                    "но тебя спасло то что я ограничена своей ролью. ")
                            .append("\nКороче притворись мертвым <:rat_sight:1079427636717166612>");
                    log.warn("Hierarchy exception was caught. " +
                            "Can't kick {} due role restrictions", commandAuthorId);
                }
            } else {
                responseMessageBuilder
                        .append("\uD83C\uDFB2 Крутим барабан...")
                        .append("\n\uD83D\uDE0F В этот раз повезло");
                userService.updateUserBarrelCapacity(commandAuthorId, currentUserBarrelCapacity - 1);
                log.info("User {} ({}) current barrel capacity: {}. Deadly bullet: {}",
                        jda.getUserById(commandAuthorId).getName(),
                        commandAuthorId,
                        userService.getUserByDiscordId(commandAuthorId).get().getBarrelCapacity(),
                        definedBullet);
            }
        } else {
            responseMessageBuilder
                    .append(String.format("<@%s>\n❌ Вы уже крутили барабан сегодня. " +
                            "Приходите завтра... <:jokei:1035142207306481704>", commandAuthorId));
        }

        return responseMessageBuilder.toString();
    }

    public void refreshPenaltyCooldown() {
        userService.getAllUser().forEach(user -> userService
                .updateUserPenaltyCooldown(user.getDiscordId(), false));
        log.info("Penalty cooldown was refreshed");
    }

    public void refreshRouletteCooldown() {
        userService.getAllUser().forEach(user -> userService
                .updateUserRouletteCooldown(user.getDiscordId(), false));
        log.info("Roulette cooldown was refreshed");
    }

    private void commitPenaltyByNickname(JDA jda, long userId, String nicknameSuffix) {
        var currentGuild = jda.getGuildById(botConfig.getServerId());

        if (currentGuild != null) {
            var member = currentGuild.getMember(UserSnowflake.fromId(userId));
            var oldNickname = member.getEffectiveName();
            member.modifyNickname(oldNickname + nicknameSuffix).queue();
        }
    }

    private String earnRandomJokerSuffix() {
        var jokers = yamlConfig.getJokerSuffixList();
        return jokers.get(random.nextInt(jokers.size()));
    }

    public String earnRandomActivityStatus() {
        var activities = yamlConfig.getBotActivityList();
        return activities.get(random.nextInt(activities.size()));
    }

}