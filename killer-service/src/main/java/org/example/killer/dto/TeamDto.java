package org.example.killer.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

@JacksonXmlRootElement(localName = "team")
@JsonPropertyOrder({"teamId", "teamName", "teamSize", "startCaveId"})
@Getter
@Setter
public class TeamDto {

    private Long teamId;

    private String teamName;

    private Integer teamSize;

    private Long startCaveId;
}
