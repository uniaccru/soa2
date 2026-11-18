package org.example.dragon.query;

import org.example.dragon.entity.Dragon;

import java.util.List;

public record DragonPage(List<Dragon> items, long totalCount, int page, int size) {
}
