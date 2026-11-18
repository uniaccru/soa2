package org.example.dragon.repository;

import org.example.dragon.entity.Team;

public interface TeamRepository {

    boolean existsById(long id);

    Team save(Team team);
}
