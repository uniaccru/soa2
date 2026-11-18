package org.example.dragon.resource.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class CoordinatesDto {

    @NotNull
    @Min(-41) // x > -42
    private Integer x;

    @NotNull
    @Max(9)
    private Integer y;
}
