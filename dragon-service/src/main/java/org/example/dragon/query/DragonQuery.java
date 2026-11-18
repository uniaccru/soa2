package org.example.dragon.query;

import java.util.Map;

/**
 * Запрос коллекции: фильтры (равенство, объединяются по И), сортировка по одному полю, страница.
 * sortBy == null означает сортировку по id.
 */
public record DragonQuery(Map<DragonField, Object> filters,
                          DragonField sortBy,
                          boolean ascending,
                          int page,
                          int size) {
}
