package org.example.dragon.repository;

import org.example.dragon.entity.Dragon;
import org.example.dragon.query.DragonPage;
import org.example.dragon.query.DragonQuery;

import java.util.List;
import java.util.Optional;

public interface DragonRepository {

    Dragon save(Dragon dragon);

    Optional<Dragon> findById(long id);

    List<Dragon> findByWeight(int weight);

    DragonPage find(DragonQuery query);

    void delete(Dragon dragon);

    /** null, если коллекция пуста или ни у кого нет age. */
    Double averageAge();

    List<String> distinctDescriptions();
}
