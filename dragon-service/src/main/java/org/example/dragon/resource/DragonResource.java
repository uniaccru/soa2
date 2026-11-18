package org.example.dragon.resource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.example.dragon.entity.Dragon;
import org.example.dragon.resource.dto.AverageAgeDto;
import org.example.dragon.resource.dto.DragonDto;
import org.example.dragon.resource.dto.DragonPageDto;
import org.example.dragon.resource.dto.UniqueDescriptionsDto;
import org.example.dragon.resource.mapper.DragonMapper;
import org.example.dragon.resource.query.DragonQueryParser;
import org.example.dragon.resource.query.PathParams;
import org.example.dragon.service.DragonService;

import java.net.URI;

/** Тонкий слой: разобрать вход, вызвать сервис, собрать ответ. Бизнес-логики здесь нет. */
@Path("/dragons")
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)
@RequestScoped
public class DragonResource {

    @Inject
    private DragonService service;

    @Inject
    private DragonMapper mapper;

    @Inject
    private DragonQueryParser queryParser;

    @Inject
    private RequestValidator validator;

    @POST
    public Response create(DragonDto body, @Context UriInfo uriInfo) {
        validator.validate(body);
        Dragon created = service.create(mapper.toEntity(body));
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build();
        return Response.created(location).entity(mapper.toDto(created)).build();
    }

    @GET
    public DragonPageDto list(@Context UriInfo uriInfo) {
        return mapper.toPageDto(service.find(queryParser.parse(uriInfo.getQueryParameters())));
    }

    @GET
    @Path("/{id}")
    public DragonDto get(@PathParam("id") String id) {
        return mapper.toDto(service.get(PathParams.positiveLong("id", id)));
    }

    @PUT
    @Path("/{id}")
    public DragonDto update(@PathParam("id") String id, DragonDto body) {
        long dragonId = PathParams.positiveLong("id", id);
        validator.validate(body);
        return mapper.toDto(service.update(dragonId, mapper.toEntity(body)));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        service.delete(PathParams.positiveLong("id", id));
        return Response.noContent().build();
    }

    @DELETE
    @Path("/by-weight/{weight}")
    public Response deleteByWeight(@PathParam("weight") String weight) {
        service.deleteByWeight(PathParams.positiveInt("weight", weight));
        return Response.noContent().build();
    }

    @GET
    @Path("/average-age")
    public AverageAgeDto averageAge() {
        AverageAgeDto dto = new AverageAgeDto();
        dto.setAverageAge(service.averageAge());
        return dto;
    }

    @GET
    @Path("/unique-descriptions")
    public UniqueDescriptionsDto uniqueDescriptions() {
        UniqueDescriptionsDto dto = new UniqueDescriptionsDto();
        dto.setValues(service.uniqueDescriptions());
        return dto;
    }
}
