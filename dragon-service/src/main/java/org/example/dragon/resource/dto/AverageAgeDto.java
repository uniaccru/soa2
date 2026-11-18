package org.example.dragon.resource.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/** Как в спецификации: корневой элемент averageAge содержит элемент averageAge (может отсутствовать). */
@XmlRootElement(name = "averageAge")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class AverageAgeDto {

    private Double averageAge;
}
