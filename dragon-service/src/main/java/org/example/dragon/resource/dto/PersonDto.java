package org.example.dragon.resource.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class PersonDto {

    @NotNull
    @Size(min = 1)
    private String name;

    @NotNull
    @Positive
    private Double height;

    @NotNull
    @Pattern(regexp = ValidationPatterns.COLOR)
    private String eyeColor;

    @NotNull
    @Pattern(regexp = ValidationPatterns.COLOR)
    private String hairColor;
}
