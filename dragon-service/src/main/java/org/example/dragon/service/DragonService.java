package org.example.dragon.service;

import org.example.dragon.entity.Dragon;
import org.example.dragon.query.DragonPage;
import org.example.dragon.query.DragonQuery;

import java.util.List;

public interface DragonService {

    Dragon create(Dragon dragon);

    DragonPage find(DragonQuery query);

    Dragon get(long id);

    Dragon update(long id, Dragon data);

    void delete(long id);

    void deleteByWeight(int weight);

    Double averageAge();

    List<String> uniqueDescriptions();
}
