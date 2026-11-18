package org.example.dragon.resource;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.example.dragon.config.AllowedOrigins;
import org.example.dragon.resource.error.ErrorResponses;

/**
 * CORS для запросов из браузера. Preflight (OPTIONS) отвечается здесь, до выбора ресурса;
 * к остальным ответам, в том числе к ошибкам, добавляется Access-Control-Allow-Origin.
 * Запросы без заголовка Origin (curl, другой сервис) фильтр не затрагивает.
 */
@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private final AllowedOrigins allowedOrigins = AllowedOrigins.fromEnvironment();

    @Override
    public void filter(ContainerRequestContext request) {
        String origin = request.getHeaderString("Origin");
        boolean preflight = "OPTIONS".equals(request.getMethod())
                && origin != null
                && request.getHeaderString("Access-Control-Request-Method") != null;
        if (!preflight) {
            return;
        }
        if (!allowedOrigins.allows(origin)) {
            request.abortWith(ErrorResponses.of(403, "Origin is not allowed"));
            return;
        }
        String requestedHeaders = request.getHeaderString("Access-Control-Request-Headers");
        request.abortWith(Response.noContent()
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", requestedHeaders != null ? requestedHeaders : "Content-Type, Accept")
                .header("Access-Control-Max-Age", "3600")
                .build());
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        String origin = request.getHeaderString("Origin");
        if (!allowedOrigins.allows(origin)) {
            return;
        }
        response.getHeaders().putSingle("Access-Control-Allow-Origin", allowedOrigins.headerValue(origin));
        response.getHeaders().add("Vary", "Origin");
        response.getHeaders().putSingle("Access-Control-Expose-Headers", "Location");
    }
}
