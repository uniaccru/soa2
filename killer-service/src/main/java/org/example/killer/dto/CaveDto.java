package org.example.killer.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({"id", "depth"})
@Getter
@Setter
public class CaveDto {

    private Long id;

    private Long depth;
}
