package org.example.killer.controller;

import org.example.killer.dto.TeamDto;
import org.example.killer.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/killer/teams", produces = MediaType.APPLICATION_XML_VALUE)
public class TeamController {

    private final TeamService service;

    public TeamController(TeamService service) {
        this.service = service;
    }

    @PostMapping("/create/{team-id}/{team-name}/{team-size}/{start-cave-id}")
    public ResponseEntity<TeamDto> create(@PathVariable("team-id") String teamId,
                                          @PathVariable("team-name") String teamName,
                                          @PathVariable("team-size") String teamSize,
                                          @PathVariable("start-cave-id") String startCaveId) {
        TeamDto team = new TeamDto();
        team.setTeamId(RequestParams.positiveLong("team-id", teamId));
        team.setTeamName(RequestParams.nonEmpty("team-name", teamName));
        team.setTeamSize(RequestParams.positiveInt("team-size", teamSize));
        team.setStartCaveId(RequestParams.longValue("start-cave-id", startCaveId));
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(team));
    }
}
