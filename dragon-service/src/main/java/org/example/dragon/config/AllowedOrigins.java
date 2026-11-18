package org.example.dragon.config;

import java.util.Arrays;
import java.util.List;

/**
 * Список origin, которым разрешён доступ из браузера (CORS).
 * Источник: системное свойство cors.allowed.origins или переменная окружения CORS_ALLOWED_ORIGINS,
 * значения через запятую, например "https://se.ifmo.ru,http://localhost:5173". "*" разрешает любой origin.
 * Пусто = кросс-доменные запросы из браузера не разрешены.
 */
public final class AllowedOrigins {

    private final List<String> origins;

    private AllowedOrigins(List<String> origins) {
        this.origins = origins;
    }

    public static AllowedOrigins fromEnvironment() {
        String raw = System.getProperty("cors.allowed.origins");
        if (raw == null) {
            raw = System.getenv("CORS_ALLOWED_ORIGINS");
        }
        return parse(raw);
    }

    static AllowedOrigins parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new AllowedOrigins(List.of());
        }
        return new AllowedOrigins(Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(AllowedOrigins::withoutTrailingSlash)
                .toList());
    }

    public boolean allowsAny() {
        return origins.contains("*");
    }

    public boolean allows(String origin) {
        return origin != null && !origin.isBlank() && (allowsAny() || origins.contains(origin));
    }

    /** Значение заголовка Access-Control-Allow-Origin для разрешённого origin. */
    public String headerValue(String origin) {
        return allowsAny() ? "*" : origin;
    }

    public List<String> asList() {
        return origins;
    }

    private static String withoutTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
