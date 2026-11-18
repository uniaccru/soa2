package org.example.dragon.resource.error;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Ошибки самого JAX-RS (неизвестный путь, неверный Content-Type, плохой XML) — тоже в формате <error>. */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException e) {
        return ErrorResponses.of(e.getResponse().getStatus(), e.getMessage());
    }
}
