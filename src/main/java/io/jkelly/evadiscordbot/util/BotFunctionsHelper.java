package io.jkelly.evadiscordbot.util;

import io.jkelly.evadiscordbot.config.BotConfig;
import io.jkelly.evadiscordbot.config.YamlConfig;
import io.jkelly.evadiscordbot.service.UserService;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Random;

@Component
@Log4j2
public class BotFunctionsHelper {

    private final YamlConfig yamlConfig;
    private final Random random;
    private final UserService userService;
    private final BotConfig botConfig;

    @Autowired
    public BotFunctionsHelper(YamlConfig yamlConfig, Random random,
                              UserService userService, BotConfig botConfig) {
        this.yamlConfig = yamlConfig;
        this.random = random;
        this.userService = userService;
        this.botConfig = botConfig;
    }

    public String makeWhoAnswer(String question) {
        var answer = new StringBuilder();
        answer.append(question)
                .replace(0, 4, yamlConfig.getMembersList()
                        .get(random.nextInt(yamlConfig.getMembersList().size())))
                .append("!");
        return String.format("**%s**", answer);
    }

    public String makeWhomAnswer(String question) {
        var answer = new StringBuilder();
        answer.append("У")
                .append(question.substring(7))
                .insert(2, yamlConfig.getAltMembersList()
                        .get(random.nextInt(yamlConfig.getAltMembersList().size())) + " ")
                .append("!");
        return String.format("**%s**", answer);
    }

    public void defineTerpila(Guild guild, long terpilaId) {
        for (Member member : guild.getMembers()) {
            removeTerpila(guild, member.getIdLong());
        }
        guild.addRoleToMember(UserSnowflake.fromId(terpilaId), guild.getRoleById(botConfig.getTerpilaRoleId())).queue();
        log.warn("Terpila granted to {} ({})",
                guild.getMember(UserSnowflake.fromId(terpilaId)).getUser().getName(), terpilaId);
    }

    // keep 'public' for possible uses in future
    public void removeTerpila(Guild guild, long discordUserid) {
        guild.removeRoleFromMember(UserSnowflake.fromId(discordUserid),
                guild.getRoleById(botConfig.getTerpilaRoleId())).complete();
    }

    public void scheduledTerpilaTask(JDA currentJda, TextChannel mainChannel) {
        var terpilaId = earnRandomServerMember();
        defineTerpila(currentJda.getGuildById(botConfig.getServerId()), terpilaId);
        var embed = new EmbedBuilder();

        if (terpilaId == botConfig.getBotId()) {
            embed.setDescription(String.format("\uD83C\uDF89 Поздравляю, <@%s>! Ты **ТЕРПИЛА ДНЯ!** \uD83D\uDE40" +
                            "\nОй... \uD83D\uDE35\u200D\uD83D\uDCAB\uD83D\uDE35\u200D\uD83D\uDCAB\uD83D\uDE35\u200D\uD83D\uDCAB",
                    terpilaId));
        } else {
            embed.setDescription(String.format("\uD83C\uDF89 Поздравляю, <@%s>! Ты **ТЕРПИЛА ДНЯ!** \uD83D\uDE40" +
                            "<:terpila:1037028097419116595> <:terpila:1037028097419116595> <:terpila:1037028097419116595>",
                    terpilaId));
        }

        embed.setColor(Color.RED);
        mainChannel.sendMessageEmbeds(embed.build()).queue();
    }

    public void makeUserAvatarEmbed(String message, JDA jda, MessageChannelUnion channel) {
        var userIdSubstring = message.substring(message.indexOf("@") + 1, message.lastIndexOf(">"));
        var userId = Long.parseLong(userIdSubstring);
        var embed = new EmbedBuilder();

        try {
            var avatarUrl = jda.getUserById(userId).getAvatarUrl();
            embed.setTitle(String.format("Аватар пользователя %s", jda.getUserById(userId).getAsTag()));
            embed.setImage(avatarUrl);
        } catch (NullPointerException nullPo) {
            embed.setTitle("Простите, не могу найти данного пользователя " +
                    "или у него не установлен аватар или еще чо :face_with_spiral_eyes:");
        }

        channel.sendMessageEmbeds(embed.build()).queue();
    }

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
                            responseMessageBuilder.append("Пользователю <@").append(userId).append(">")
                                    .append(" добавлен **один штрафной балл**!")
                                    .append("\nОсторожно, последнее предупреждение! <:rat_sight:1079427636717166612>");

                        } else if (userService.getUserByDiscordId(userId).get()
                                .getPenaltyPoint() == botConfig.getMaxPenaltyPoint() / 2) {
                            responseMessageBuilder.append("Пользователю <@").append(userId).append(">")
                                    .append(" добавлен **один штрафной балл**!")
                                    .append("\nУ тебя еще есть шанс *извиниться*! <:jokei:1035142207306481704>");

                        } else if (userService.getUserByDiscordId(userId).get()
                                .getPenaltyPoint() == botConfig.getMaxPenaltyPoint()) {
                            try {
                                userService.updateUserPenaltyPoint(userId, 0);
                                commitPenaltyByNickname(jda, userId, earnRandomJokerSuffix());
                                responseMessageBuilder.append(":clown: Ну и ну, вы расстроить партия! Штрафные санкции вам, <@")
                                        .append(userId).append(">! <:rjobomba:1049320943912230972>");
                            } catch (HierarchyException hierarchyException) {
                                userService.updateUserPenaltyPoint(userId, 0);
                                responseMessageBuilder.append("\uD83D\uDCA2 Из за ограничения моей роли я не могу наказать тебя, <@")
                                        .append(userId).append(">...").append("\nНо знай что лично я... " +
                                                "тебя ***крайне осуждаю***")
                                        .append(" <:rat_sight:1079427636717166612> " +
                                                "<:rat_sight:1079427636717166612> <:rat_sight:1079427636717166612>");
                                log.warn("Hierarchy exception was caught. " +
                                        "Can't change {} nickname due role restrictions", userId);
                            }

                        } else {
                            responseMessageBuilder.append("<:bruh:1050845792266616922> Пользователю <@")
                                    .append(userId).append(">")
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
            responseMessageBuilder.append(String.format("<@%s>\n❌ Вы уже использовали свое предупреждение. " +
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
                        currentMember.modifyNickname(modifiedNickname.replaceAll("\\([^()]*\\)", "").trim())
                                .queue();
                        responseMessageBuilder.append(":eyes: Пользователь <@").append(userId).append(">")
                                .append(" **помилован и восстановлен**! Смотри мне блин!<:rat_sight:1079427636717166612>");
                    }
                }
            } else {
                responseMessageBuilder.append(String.format("<:rat_sight:1079427636717166612> Очень хитро, <@%s>..." +
                        "\nНо снимать с себя наказание **нельзя**!", commandAuthorId));
            }
        } else {
            responseMessageBuilder.append(String.format("<@%s>\n❌ Вы уже использовали свою возможность " +
                    "менять уровень предупреждения на сегодня. " +
                    "Возвращайтесь __***завтре***__! <:mda:1100928775539134494>", commandAuthorId));
        }

        return responseMessageBuilder.toString();
    }

    public String rollBarrel(JDA jda, long commandAuthorId) {
        var responseMessageBuilder = new StringBuilder();

        if (!userService.getUserByDiscordId(commandAuthorId).get().isOnRouletteCooldown()) {
            userService.updateUserRouletteCooldown(commandAuthorId, true);

            if (random.nextInt(7) == 6) {
                try {
                    responseMessageBuilder.append("\uD83C\uDFB2 Крутим барабан...")
                            .append("\n\uD83D\uDCA5<:revolver:1105169657171804234> Бам! Ты умер. :skull:");
                    jda.getGuildById(botConfig.getServerId())
                            .kick(UserSnowflake.fromId(commandAuthorId)).queue();
                    userService.updateUserRouletteCooldown(commandAuthorId, false);
                } catch (HierarchyException hierarchyException) {
                    responseMessageBuilder
                            .append("\n\uD83E\uDD2C При обычных обстоятельствах ты бы умер, " +
                                    "но тебя спасло то что я ограниченна своей ролью. ")
                            .append("\nКороче притворись мертвым <:rat_sight:1079427636717166612>");
                    log.warn("Hierarchy exception was caught. " +
                            "Can't kick {} due role restrictions", commandAuthorId);
                }
            } else {
                responseMessageBuilder.append("\uD83C\uDFB2 Крутим барабан...")
                        .append("\n\uD83D\uDE0F В этот раз повезло");
            }
        } else {
            responseMessageBuilder.append(String.format("<@%s>\n❌ Вы уже крутили барабан сегодня. " +
                    "Приходите завтра... <:jokei:1035142207306481704>", commandAuthorId));
        }

        return responseMessageBuilder.toString();
    }

    public void makeHelpMessage(MessageChannelUnion channel) {
        var embed = new EmbedBuilder();
        var jokePenaltyDescription = String.format("Выдать предупреждение пользователю. " +
                        "Если количество предупреждений превысит некоторое значение (а сейчас это %s), " +
                        "то этот пользователь будет наказан <:sakagami:1049399866826178601>" +
                        "\nДоступно один раз в день.",
                botConfig.getMaxPenaltyPoint());

        embed.setColor(Color.CYAN)
                .setThumbnail(botConfig.getHelpEmbedGifLink())
                .setTitle("Меню помощи! (мне бы кто помог)<:dumb_rat:1079429073446637698>")
                .setDescription("Здесь находится список всех команд которые доступны на данный момент!")
                .addField("**!woof**", "Сделать вуф!", false)
                .addField("**!jokepenalty** %@участник_нейм%", jokePenaltyDescription, false)
                .addField("**!restore** %@участник_нейм%", "Простить участника и снять нокозание", false)
                .addField("**!rr**", "Сыграть в рулетку. Смертельно опасно :skull_crossbones:", false)
                .addField("**!кто** %вопрос%", "Задать мне вопрос!", false)
                .addField("**!у кого** %вопрос%", "Тоже задать мне вопрос!!!", false)
                .addField("**!avatar** %@участник_нейм%", "Получить аватарку пользователя", false)
                .addField("**!help**", "Увидеть это сообщение", false);

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public void refreshPenaltyCooldown() {
        userService.getAllUser().forEach(user -> userService.updateUserPenaltyCooldown(user.getDiscordId(), false));
        log.info("Penalty cooldown was refreshed");
    }

    public void refreshRouletteCooldown() {
        userService.getAllUser().forEach(user -> userService.updateUserRouletteCooldown(user.getDiscordId(), false));
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

    private long earnRandomServerMember() {
        var users = userService.getAllUser();
        var userId = users.get(random.nextInt(users.size())).getDiscordId();
        log.info("Received random user id ({})", userId);
        return userId;
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