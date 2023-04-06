package io.jkelly.evadiscordbot.config;

import io.jkelly.evadiscordbot.util.YamlPropertySourceFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "triggerlist")
@PropertySource(value = "/botTriggerList.yaml", factory = YamlPropertySourceFactory.class)
public class YamlConfig {

    private List<String> mageNameList;

    private List<String> pigNameList;

}

