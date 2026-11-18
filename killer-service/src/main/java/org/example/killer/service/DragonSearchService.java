package org.example.killer.service;

import org.example.killer.dto.DragonDto;

public interface DragonSearchService {

    /**
     * Среди пещер с глубиной не более max выбирает самую глубокую (DEEPEST) или самую мелкую (SHALLOWEST)
     * и возвращает дракона из неё (при равной глубине — с меньшим id).
     *
     * @throws org.example.killer.exception.ResourceNotFoundException если подходящих пещер нет
     */
    DragonDto findByCaveDepth(long max, CaveMode mode);
}
