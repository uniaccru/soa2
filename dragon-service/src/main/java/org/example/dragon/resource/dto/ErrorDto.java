package org.example.dragon.resource.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"code", "message"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {

    private int code;

    private String message;
}
