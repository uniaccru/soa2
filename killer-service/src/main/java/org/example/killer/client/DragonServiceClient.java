package org.example.killer.client;

import org.example.killer.dto.DragonPageDto;
import org.example.killer.dto.TeamDto;

/**
 * Шлюз к сервису 1. Все сбои превращает в исключения слоя exception
 * (400 / 502 / 503 / 504), чтобы сервисы выше не знали про HTTP.
 */
public interface DragonServiceClient {

    /** Страница драконов, отсортированных по глубине пещеры по возрастанию (без пещеры — в конце). */
    DragonPageDto findPageSortedByCaveDepth(int page, int size);

    TeamDto createTeam(TeamDto team);
}
