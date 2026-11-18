package org.example.killer.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

/** Дракон так, как его отдаёт сервис 1; сервис 2 возвращает его клиенту без изменений. */
@JacksonXmlRootElement(localName = "dragon")
@JsonPropertyOrder({"id", "name", "coordinates", "creationDate", "age", "description",
        "weight", "color", "cave", "killer"})
@Getter
@Setter
public class DragonDto {

    private Long id;

    private String name;

    private CoordinatesDto coordinates;

    private String creationDate;

    private Long age;

    private String description;

    private Integer weight;

    private String color;

    private CaveDto cave;

    private PersonDto killer;
}
