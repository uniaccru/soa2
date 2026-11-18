package org.example.dragon.resource.error;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.dragon.resource.dto.ErrorDto;

public final class ErrorResponses {

    private ErrorResponses() {
    }

    public static Response of(int status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_XML)
                .entity(new ErrorDto(status, message))
                .build();
    }
}
