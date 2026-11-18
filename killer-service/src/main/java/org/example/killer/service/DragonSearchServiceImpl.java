package org.example.killer.service;

import org.example.killer.client.DragonServiceClient;
import org.example.killer.dto.DragonDto;
import org.example.killer.dto.DragonPageDto;
import org.example.killer.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DragonSearchServiceImpl implements DragonSearchService {

    private final DragonServiceClient client;
    private final int pageSize;

    public DragonSearchServiceImpl(DragonServiceClient client,
                                   @Value("${service1.page-size:100}") int pageSize) {
        this.client = client;
        this.pageSize = pageSize;
    }

    @Override
    public DragonDto findByCaveDepth(long max, CaveMode mode) {
        DragonDto best = null;
        // Драконы приходят отсортированными по глубине пещеры по возрастанию (id как вторичный ключ),
        // поэтому обход можно остановить, как только глубина превысила max.
        for (int page = 0; ; page++) {
            DragonPageDto result = client.findPageSortedByCaveDepth(page, pageSize);
            List<DragonDto> items = result.getItems() == null ? List.of() : result.getItems();

            for (DragonDto dragon : items) {
                if (dragon.getCave() == null || dragon.getCave().getDepth() == null) {
                    continue;
                }
                long depth = dragon.getCave().getDepth();
                if (depth > max) {
                    return orNotFound(best, max);
                }
                if (mode == CaveMode.SHALLOWEST) {
                    return dragon;
                }
                // DEEPEST: строго глубже, чтобы при равной глубине остался дракон с меньшим id
                if (best == null || depth > best.getCave().getDepth()) {
                    best = dragon;
                }
            }

            boolean lastPage = items.isEmpty() || (long) (page + 1) * pageSize >= result.getTotalCount();
            if (lastPage) {
                return orNotFound(best, max);
            }
        }
    }

    private DragonDto orNotFound(DragonDto dragon, long max) {
        if (dragon == null) {
            throw new ResourceNotFoundException("No dragon lives in a cave with depth <= " + max);
        }
        return dragon;
    }
}
