package io.jkelly.evadiscordbot.service;

import io.jkelly.evadiscordbot.dto.DogPicDto;
import io.jkelly.evadiscordbot.dto.FoxPicDto;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.awt.*;

@Component
@PropertySource("/application.properties")
public class PictureService {

    @Value("${picture.randomDog}")
    private String randomDogLink;

    @Value("${picture.randomFox}")
    private String randomFoxLink;

    private final RestTemplate restTemplate;

    @Autowired
    public PictureService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void makeRandomDogPictureEmbed(MessageChannelUnion channel) {
        var embed = new EmbedBuilder();
        var dogDto = restTemplate.getForObject(randomDogLink, DogPicDto.class);

        if (dogDto != null) {
            embed.setTitle("\uD83D\uDC36 DOGE \uD83D\uDC36")
                    .setColor(Color.WHITE)
                    .setImage(dogDto.getDogPicUrl());
        } else {
            embed.setTitle("\uD83E\uDD14 При получении пикчи собаки произошла какая то ошибка...")
                    .setColor(Color.WHITE);
        }

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public void makeRandomFoxPictureEmbed(MessageChannelUnion channel) {
        var embed = new EmbedBuilder();
        var foxDto = restTemplate.getForObject(randomFoxLink, FoxPicDto.class);

        if (foxDto != null) {
            embed.setTitle("\uD83E\uDD8A \uD83E\uDD8A \uD83E\uDD8A CUTE FOXY \uD83E\uDD8A \uD83E\uDD8A \uD83E\uDD8A")
                    .setColor(0xdf401c)
                    .setImage(foxDto.getFoxPicUrl());
        } else {
            embed.setTitle("\uD83E\uDD14 При получении пикчи лисички произошла какая то ошибка...")
                    .setColor(Color.WHITE);
        }

        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
