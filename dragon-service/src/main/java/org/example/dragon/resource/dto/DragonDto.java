package org.example.dragon.resource.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

/**
 * Один класс и для запроса (DragonInput), и для ответа (Dragon).
 * id и creationDate заполняются только в ответе, во входных данных игнорируются.
 * Перечисления передаются строками и проверяются через @Pattern: так неизвестное значение
 * даёт понятную ошибку 400, а не тихий null при разборе JAXB.
 */
@XmlRootElement(name = "dragon")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"id", "name", "coordinates", "creationDate", "age", "description",
        "weight", "color", "cave", "killer"})
@Getter
@Setter
public class DragonDto {

    private Long id;

    @NotNull
    @Size(min = 1)
    private String name;

    @NotNull
    @Valid
    private CoordinatesDto coordinates;

    private String creationDate;

    @Positive
    private Long age;

    private String description;

    @Positive
    private Integer weight;

    @Pattern(regexp = ValidationPatterns.COLOR)
    private String color;

    @Valid
    private CaveDto cave;

    @Valid
    private PersonDto killer;
}
