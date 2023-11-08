package io.jkelly.evadiscordbot.util;

import io.jkelly.evadiscordbot.config.BotConfig;
import io.jkelly.evadiscordbot.service.UserService;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Random;

@Log4j2
@Component
public class EmbedSender {

    private final BotConfig botConfig;
    private final GuildMembersManipulator membersManipulator;
    private final UserService userService;
    private final Random random;

    @Autowired
    public EmbedSender(BotConfig botConfig,
                       GuildMembersManipulator membersManipulator,
                       UserService userService, Random random) {
        this.botConfig = botConfig;
        this.membersManipulator = membersManipulator;
        this.userService = userService;
        this.random = random;
    }

    public void makeUserAvatarEmbed(String message, JDA jda, MessageChannelUnion channel) {
        var userIdSubstring = message.substring(message.indexOf("@") + 1, message.lastIndexOf(">"));
        var userId = Long.parseLong(userIdSubstring);
        var embed = new EmbedBuilder();

        try {
            var avatarUrl = jda.getUserById(userId).getAvatarUrl();
            embed.setTitle(String.format("Аватар пользователя %s", jda.getUserById(userId).getAsTag()))
                    .setImage(avatarUrl);
        } catch (NullPointerException nullPo) {
            embed.setTitle("Простите, не могу найти данного пользователя " +
                    "или у него не установлен аватар или еще чо :face_with_spiral_eyes:");
        }

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public void makeHelpEmbed(MessageChannelUnion channel) {
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
                .addField("**!slap** %@участник_нейм%", "Шлепнуть участника по \uD83C\uDF51!", false)
                .addField("**!jokepenalty** %@участник_нейм%", jokePenaltyDescription, false)
                .addField("**!restore** %@участник_нейм%", "Простить участника и снять нокозание", false)
                .addField("**!rr**", "Сыграть в рулетку. Смертельно опасно :skull_crossbones:", false)
                .addField("**!roll**", "Крутануть случайное число, от 0 до 100! \uD83C\uDFB2", false)
                .addField("**!showdoge**", "Показать кросивую собачку! \uD83D\uDC36", false)
                .addField("**!showfoxy**", "Показать *очень* кросивую лисичку! \uD83E\uDD8A", false)
                .addField("**!кто** %вопрос%", "Задать мне вопрос!", false)
                .addField("**!у кого** %вопрос%", "Тоже задать мне вопрос!!!", false)
                .addField("**!вопрос**", "Задать вопрос магическому шару (ему пофик чо вы спросите) " +
                        "\uD83C\uDFB1", false)
                .addField("**!avatar** %@участник_нейм%", "Получить аватарку пользователя", false)
                .addField("**!help**", "Увидеть это сообщение", false);

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public void scheduledTerpilaTask(JDA currentJda, TextChannel mainChannel) {
        var terpilaId = earnRandomServerMember();
        var embed = new EmbedBuilder();

        if (currentJda.getGuildById(botConfig.getServerId()).getMemberById(terpilaId) != null) {
            membersManipulator.defineTerpila(currentJda.getGuildById(botConfig.getServerId()), terpilaId);

            if (terpilaId == botConfig.getBotId()) {
                embed.setDescription(String.format("\uD83C\uDF89 Поздравляю, <@%s>! Ты **ТЕРПИЛА ДНЯ!** \uD83D\uDE40" +
                                "\nОй... \uD83D\uDE35\u200D\uD83D\uDCAB\uD83D\uDE35\u200D\uD83D\uDCAB\uD83D\uDE35\u200D\uD83D\uDCAB",
                        terpilaId));
            } else {
                embed.setDescription(String.format("\uD83C\uDF89 Поздравляю, <@%s>! " +
                                "Ты **ТЕРПИЛА ДНЯ!** \uD83D\uDE40" +
                                "<:terpila:1037028097419116595> <:terpila:1037028097419116595>" +
                                " <:terpila:1037028097419116595>",
                        terpilaId));
            }
            embed.setColor(Color.RED);
            mainChannel.sendMessageEmbeds(embed.build()).queue();

        } else {
            log.warn("Looks like user by {} id don't belong to this server. Finding new one.", terpilaId);
            scheduledTerpilaTask(currentJda, mainChannel);
        }
    }

    private long earnRandomServerMember() {
        var users = userService.getAllUser();
        var userId = users.get(random.nextInt(users.size())).getDiscordId();
        log.info("Received random user id ({})", userId);
        return userId;
    }

}