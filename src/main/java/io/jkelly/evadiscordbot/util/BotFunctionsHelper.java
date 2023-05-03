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

    public String addAndCheckPenaltyToUser(String message, JDA jda, long authorId) {
        var userIdSubstring = message.substring(message.indexOf("@") + 1, message.lastIndexOf(">"));
        var substringUserId = Long.parseLong(userIdSubstring);
        var resultMessageBuilder = new StringBuilder();

        if (substringUserId != authorId) {
            if (userService.getUserByDiscordId(substringUserId).isPresent()) {
                var currentUserPenaltyPoints = userService.getUserByDiscordId(substringUserId)
                        .get().getPenaltyPoint();

                if (currentUserPenaltyPoints >= 0 && currentUserPenaltyPoints < botConfig.getMaxPenaltyPoint()) {
                    userService.updateUserPenaltyPoint(substringUserId, currentUserPenaltyPoints + 1);

                    if (userService.getUserByDiscordId(substringUserId).get()
                            .getPenaltyPoint() == botConfig.getMaxPenaltyPoint() - 1) {
                        resultMessageBuilder.append("Пользователю <@").append(substringUserId).append(">")
                                .append(" добавлен **один штрафной балл**!")
                                .append("\nОсторожно, последнее предупреждение! :rat_sight:");

                    } else if (userService.getUserByDiscordId(substringUserId).get()
                            .getPenaltyPoint() == botConfig.getMaxPenaltyPoint() / 2) {
                        resultMessageBuilder.append("Пользователю <@").append(substringUserId).append(">")
                                .append(" добавлен **один штрафной балл**!")
                                .append("\nУ тебя еще есть шанс *извиниться*! <:jokei:1035142207306481704>");

                    } else if (userService.getUserByDiscordId(substringUserId).get()
                            .getPenaltyPoint() == botConfig.getMaxPenaltyPoint()) {
                        try {
                            userService.updateUserPenaltyPoint(substringUserId, 0);
                            commitPenaltyByNickname(jda, substringUserId, earnRandomJokerSuffix());
                            resultMessageBuilder.append(":clown: Ну и ну, вы расстроить партия! Штрафные санкции вам, <@")
                                    .append(substringUserId).append(">! <:rjobomba:1049320943912230972>");
                        } catch (HierarchyException hierarchyException) {
                            userService.updateUserPenaltyPoint(substringUserId, 0);
                            resultMessageBuilder.append("\uD83D\uDCA2 Из за ограничения моей роли я не могу наказать тебя, <@")
                                    .append(substringUserId).append(">...").append("\nНо знай что лично я... " +
                                            "тебя ***крайне осуждаю***")
                                    .append(" <:rat_sight:1079427636717166612> " +
                                            "<:rat_sight:1079427636717166612> <:rat_sight:1079427636717166612>");
                            log.warn("Hierarchy exception was caught. " +
                                    "Can't change {} nickname due role restrictions", substringUserId);
                        }

                    } else {
                        resultMessageBuilder.append("<:bruh:1050845792266616922> Пользователю <@")
                                .append(substringUserId).append(">")
                                .append(" добавлен **один штрафной балл**!");
                    }
                    log.info("User {} ({}) joker penalty point: {}",
                            jda.getGuildById(botConfig.getServerId()).getMemberById(substringUserId).getEffectiveName(),
                            substringUserId, userService.getUserByDiscordId(substringUserId).get().getPenaltyPoint());
                }
            }
        } else {
            resultMessageBuilder
                    .append(String.format("<:confused_rat:1079428095599194194> " +
                            "**Себя надо любить а не наказывать, <@%s>!**" +
                            "\nВыдавать предупреждения самому себе запрещено!", authorId));
        }
        return resultMessageBuilder.toString();
    }

    public String restorePenalty(String message, JDA jda, long authorId) {
        var userIdSubstring = message.substring(message.indexOf("@") + 1, message.lastIndexOf(">"));
        var substringUserId = Long.parseLong(userIdSubstring);
        var currentGuild = jda.getGuildById(botConfig.getServerId());
        var resultMessageBuilder = new StringBuilder();

        if (substringUserId != authorId) {
            if (currentGuild != null) {
                if (userService.getUserByDiscordId(substringUserId).isPresent()) {
                    userService.updateUserPenaltyPoint(substringUserId, 0);
                    var currentMember = currentGuild.getMember(UserSnowflake.fromId(substringUserId));
                    var modifiedNickname = currentMember.getEffectiveName();
                    currentMember.modifyNickname(modifiedNickname.replaceAll("\\([^()]*\\)", "").trim())
                            .queue();
                    resultMessageBuilder.append(":eyes: Пользователь <@").append(substringUserId).append(">")
                            .append(" **помилован и восстановлен**! Смотри мне блин!<:rat_sight:1079427636717166612>");
                }
            }
        } else {
            resultMessageBuilder.append(String.format("<:rat_sight:1079427636717166612> Очень хитро, <@%s>..." +
                    "\nНо снимать с себя наказание **нельзя**!", authorId));
        }

        return resultMessageBuilder.toString();
    }

    public void makeHelpMessage(MessageChannelUnion channel) {
        var embed = new EmbedBuilder();
        var jokePenaltyDescription = String.format("Выдать предупреждение пользователю. " +
                        "Если количество предупреждений превысит некоторое значение (а сейчас это %s), " +
                        "то этот пользователь будет наказан <:sakagami:1049399866826178601>",
                botConfig.getMaxPenaltyPoint());

        embed.setColor(Color.CYAN)
                .setThumbnail(botConfig.getHelpEmbedGifLink())
                .setTitle("Меню помощи! (мне бы кто помог)<:dumb_rat:1079429073446637698>")
                .setDescription("Здесь находится список всех команд которые доступны на данный момент!")
                .addField("**!woof**", "Сделать вуф!", false)
                .addField("**!jokepenalty** %участник_нейм%", jokePenaltyDescription, false)
                .addField("**!restore** %участник_нейм%", "Простить участника и снять нокозание", false)
                .addField("**!кто** %вопрос%", "Задать мне вопрос!", false)
                .addField("**!у кого** %вопрос%", "Тоже задать мне вопрос!!!", false)
                .addField("**!avatar** %участник_нейм%", "Получить аватарку пользователя", false)
                .addField("**!help**", "Увидеть это сообщение", false);

        channel.sendMessageEmbeds(embed.build()).queue();
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

}