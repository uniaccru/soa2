package org.example.dragon.resource.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "dragons")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"totalCount", "page", "size", "items"})
@Getter
@Setter
public class DragonPageDto {

    private long totalCount;

    private int page;

    private int size;

    @XmlElementWrapper(name = "items")
    @XmlElement(name = "dragon")
    private List<DragonDto> items = new ArrayList<>();
}
