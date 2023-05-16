package io.jkelly.evadiscordbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class DogPicDto {

    @JsonProperty("message")
    private String dogPicUrl;

}
