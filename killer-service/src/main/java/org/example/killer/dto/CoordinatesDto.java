package org.example.killer.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({"x", "y"})
@Getter
@Setter
public class CoordinatesDto {

    private Integer x;

    private Integer y;
}
