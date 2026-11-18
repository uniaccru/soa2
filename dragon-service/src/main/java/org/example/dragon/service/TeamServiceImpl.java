package org.example.dragon.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.dragon.entity.Team;
import org.example.dragon.exception.InvalidDataException;
import org.example.dragon.repository.CaveRepository;
import org.example.dragon.repository.TeamRepository;

@ApplicationScoped
@Transactional
public class TeamServiceImpl implements TeamService {

    @Inject
    private TeamRepository teams;

    @Inject
    private CaveRepository caves;

    @Override
    public Team create(Team team) {
        if (teams.existsById(team.getId())) {
            throw new InvalidDataException("Team with id " + team.getId() + " already exists");
        }
        if (!caves.existsById(team.getStartCaveId())) {
            throw new InvalidDataException("Cave with id " + team.getStartCaveId() + " does not exist");
        }
        return teams.save(team);
    }
}
