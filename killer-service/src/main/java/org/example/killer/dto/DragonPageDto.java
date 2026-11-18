package org.example.killer.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "dragons")
@JsonPropertyOrder({"totalCount", "page", "size", "items"})
@Getter
@Setter
public class DragonPageDto {

    private long totalCount;

    private int page;

    private int size;

    @JacksonXmlElementWrapper(localName = "items")
    @JacksonXmlProperty(localName = "dragon")
    private List<DragonDto> items = new ArrayList<>();
}
