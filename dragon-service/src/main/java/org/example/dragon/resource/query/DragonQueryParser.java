package org.example.dragon.resource.query;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;
import org.example.dragon.entity.Color;
import org.example.dragon.exception.InvalidDataException;
import org.example.dragon.query.DragonField;
import org.example.dragon.query.DragonQuery;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Превращает query-параметры GET /dragons в DragonQuery и проверяет их по спецификации. */
@ApplicationScoped
public class DragonQueryParser {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public DragonQuery parse(MultivaluedMap<String, String> params) {
        int page = intParam(params, "page", DEFAULT_PAGE, 0);
        int size = intParam(params, "size", DEFAULT_SIZE, 1);
        DragonField sortBy = sortField(single(params, "sortBy"));
        boolean ascending = ascending(single(params, "sortOrder"));

        Map<DragonField, Object> filters = new EnumMap<>(DragonField.class);
        for (DragonField field : DragonField.values()) {
            String raw = single(params, field.apiName());
            if (raw != null) {
                filters.put(field, parseValue(field, raw));
            }
        }
        return new DragonQuery(filters, sortBy, ascending, page, size);
    }

    private String single(MultivaluedMap<String, String> params, String name) {
        List<String> values = params.get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        if (values.size() > 1) {
            throw new InvalidDataException("Query parameter '" + name + "' must be specified only once");
        }
        return values.get(0);
    }

    private int intParam(MultivaluedMap<String, String> params, String name, int defaultValue, int min) {
        String raw = single(params, name);
        if (raw == null) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(raw);
            if (value >= min) {
                return value;
            }
        } catch (NumberFormatException ignored) {
            // падаем ниже
        }
        throw new InvalidDataException("Query parameter '" + name + "' must be an integer >= " + min);
    }

    private DragonField sortField(String raw) {
        if (raw == null) {
            return null;
        }
        return DragonField.fromApiName(raw)
                .orElseThrow(() -> new InvalidDataException("Unknown sortBy value '" + raw + "'"));
    }

    private boolean ascending(String raw) {
        if (raw == null || raw.equals("asc")) {
            return true;
        }
        if (raw.equals("desc")) {
            return false;
        }
        throw new InvalidDataException("Query parameter 'sortOrder' must be 'asc' or 'desc'");
    }

    private Object parseValue(DragonField field, String raw) {
        try {
            return switch (field) {
                case ID, AGE, CAVE_DEPTH -> Long.parseLong(raw);
                case NAME, DESCRIPTION, KILLER_NAME -> raw;
                case X, Y, WEIGHT -> Integer.parseInt(raw);
                case CREATION_DATE -> LocalDate.parse(raw);
                case COLOR, KILLER_EYE_COLOR, KILLER_HAIR_COLOR -> Color.valueOf(raw);
                case KILLER_HEIGHT -> finite(Double.parseDouble(raw));
            };
        } catch (RuntimeException e) {
            throw new InvalidDataException("Invalid value '" + raw + "' for query parameter '" + field.apiName() + "'");
        }
    }

    private double finite(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("not finite");
        }
        return value;
    }
}
