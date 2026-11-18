package org.example.killer.service;

import org.example.killer.client.DragonServiceClient;
import org.example.killer.dto.TeamDto;
import org.springframework.stereotype.Service;

/** Команда хранится в БД сервиса 1; он же проверяет уникальность teamId и существование пещеры. */
@Service
public class TeamServiceImpl implements TeamService {

    private final DragonServiceClient client;

    public TeamServiceImpl(DragonServiceClient client) {
        this.client = client;
    }

    @Override
    public TeamDto create(TeamDto team) {
        return client.createTeam(team);
    }
}
