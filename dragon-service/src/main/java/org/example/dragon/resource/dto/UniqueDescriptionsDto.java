package org.example.dragon.resource.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "descriptions")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class UniqueDescriptionsDto {

    @XmlElementWrapper(name = "values")
    @XmlElement(name = "value")
    private List<String> values = new ArrayList<>();
}
