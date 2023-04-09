package io.jkelly.evadiscordbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Random;

@SpringBootApplication
public class EvaDiscordBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(EvaDiscordBotApplication.class, args);
	}

	@Bean
	public Random random() {
		return new Random();
	}

}
