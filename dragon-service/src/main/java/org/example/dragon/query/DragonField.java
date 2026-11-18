package org.example.dragon.query;

import java.util.Optional;

/** Поля, по которым можно сортировать и фильтровать (имена как в API). */
public enum DragonField {
    ID("id"),
    NAME("name"),
    X("x"),
    Y("y"),
    CREATION_DATE("creationDate"),
    AGE("age"),
    DESCRIPTION("description"),
    WEIGHT("weight"),
    COLOR("color"),
    KILLER_NAME("killerName"),
    KILLER_HEIGHT("killerHeight"),
    KILLER_EYE_COLOR("killerEyeColor"),
    KILLER_HAIR_COLOR("killerHairColor"),
    CAVE_DEPTH("caveDepth");

    private final String apiName;

    DragonField(String apiName) {
        this.apiName = apiName;
    }

    public String apiName() {
        return apiName;
    }

    public static Optional<DragonField> fromApiName(String name) {
        for (DragonField field : values()) {
            if (field.apiName.equals(name)) {
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }
}
