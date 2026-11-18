package org.example.dragon.resource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.dragon.entity.Team;
import org.example.dragon.resource.dto.TeamDto;
import org.example.dragon.resource.mapper.TeamMapper;
import org.example.dragon.service.TeamService;

/** Хранение команд убийц драконов; его вызывает сервис 2 (/killer/teams/create/...). */
@Path("/teams")
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)
@RequestScoped
public class TeamResource {

    @Inject
    private TeamService service;

    @Inject
    private TeamMapper mapper;

    @Inject
    private RequestValidator validator;

    @POST
    public Response create(TeamDto body) {
        validator.validate(body);
        Team created = service.create(mapper.toEntity(body));
        return Response.status(Response.Status.CREATED).entity(mapper.toDto(created)).build();
    }
}
