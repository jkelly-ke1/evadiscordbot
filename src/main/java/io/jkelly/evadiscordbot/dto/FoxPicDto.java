package io.jkelly.evadiscordbot.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class FoxPicDto {

    @JsonProperty("image")
    private String foxPicUrl;

}
