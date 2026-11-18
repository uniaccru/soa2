package org.example.killer.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({"name", "height", "eyeColor", "hairColor"})
@Getter
@Setter
public class PersonDto {

    private String name;

    private Double height;

    private String eyeColor;

    private String hairColor;
}
