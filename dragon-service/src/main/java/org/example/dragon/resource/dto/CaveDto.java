package org.example.dragon.resource.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class CaveDto {

    /** Только в ответе; во входных данных игнорируется. */
    private Long id;

    @NotNull
    @PositiveOrZero
    private Long depth;
}
