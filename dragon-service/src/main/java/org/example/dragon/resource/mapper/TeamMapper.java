package org.example.dragon.resource.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.dragon.entity.Team;
import org.example.dragon.resource.dto.TeamDto;

@ApplicationScoped
public class TeamMapper {

    public Team toEntity(TeamDto dto) {
        return new Team(dto.getTeamId(), dto.getTeamName(), dto.getTeamSize(), dto.getStartCaveId());
    }

    public TeamDto toDto(Team team) {
        TeamDto dto = new TeamDto();
        dto.setTeamId(team.getId());
        dto.setTeamName(team.getName());
        dto.setTeamSize(team.getSize());
        dto.setStartCaveId(team.getStartCaveId());
        return dto;
    }
}
