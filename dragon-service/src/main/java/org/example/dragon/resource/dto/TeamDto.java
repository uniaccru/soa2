package org.example.dragon.resource.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "team")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"teamId", "teamName", "teamSize", "startCaveId"})
@Getter
@Setter
public class TeamDto {

    @NotNull
    @Positive
    private Long teamId;

    @NotNull
    @Size(min = 1)
    private String teamName;

    @NotNull
    @Positive
    private Integer teamSize;

    @NotNull
    private Long startCaveId;
}
