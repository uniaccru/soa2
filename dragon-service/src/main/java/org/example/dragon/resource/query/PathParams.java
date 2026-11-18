package org.example.dragon.resource.query;

import org.example.dragon.exception.InvalidDataException;

/** Параметры пути разбираем вручную: при нечисловом значении JAX-RS отдал бы 404 вместо 400. */
public final class PathParams {

    private PathParams() {
    }

    public static long positiveLong(String name, String raw) {
        try {
            long value = Long.parseLong(raw);
            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException ignored) {
            // падаем ниже
        }
        throw new InvalidDataException("Path parameter '" + name + "' must be a positive integer");
    }

    public static int positiveInt(String name, String raw) {
        try {
            int value = Integer.parseInt(raw);
            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException ignored) {
            // падаем ниже
        }
        throw new InvalidDataException("Path parameter '" + name + "' must be a positive 32-bit integer");
    }
}
