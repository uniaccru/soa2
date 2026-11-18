package org.example.killer.controller;

import org.example.killer.exception.InvalidDataException;
import org.example.killer.service.CaveMode;

/** Разбор и проверка параметров запроса по спецификации; ошибки -> 400. */
final class RequestParams {

    private RequestParams() {
    }

    static long longValue(String name, String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Parameter '" + name + "' must be a 64-bit integer");
        }
    }

    static long positiveLong(String name, String raw) {
        long value = longValue(name, raw);
        if (value <= 0) {
            throw new InvalidDataException("Parameter '" + name + "' must be greater than 0");
        }
        return value;
    }

    static int positiveInt(String name, String raw) {
        try {
            int value = Integer.parseInt(raw);
            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException ignored) {
            // падаем ниже
        }
        throw new InvalidDataException("Parameter '" + name + "' must be a 32-bit integer greater than 0");
    }

    static String nonEmpty(String name, String raw) {
        if (raw == null || raw.isEmpty()) {
            throw new InvalidDataException("Parameter '" + name + "' must not be empty");
        }
        return raw;
    }

    static CaveMode caveMode(String raw) {
        if (raw == null || raw.equals("deepest")) {
            return CaveMode.DEEPEST;
        }
        if (raw.equals("shallowest")) {
            return CaveMode.SHALLOWEST;
        }
        throw new InvalidDataException("Query parameter 'mode' must be 'deepest' or 'shallowest'");
    }
}
