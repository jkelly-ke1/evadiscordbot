package io.jkelly.evadiscordbot.config;

import io.jkelly.evadiscordbot.util.YamlPropertySourceFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "triggerlist")
@PropertySource(value = "/botTriggerList.yaml", factory = YamlPropertySourceFactory.class)
public class YamlConfig {

    private List<String> mageNameList;

    private List<String> pigNameList;

    private List<String> membersList;

    private List<String> altMembersList;

    private List<String> shameList;

    private List<String> jokerSuffixList;

    private List<String> botActivityList;

}
