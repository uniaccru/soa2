package org.example.killer.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JacksonXmlRootElement(localName = "error")
@JsonPropertyOrder({"code", "message"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {

    private int code;

    private String message;
}
