package org.example.dragon.resource.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.dragon.entity.Cave;
import org.example.dragon.entity.Color;
import org.example.dragon.entity.Coordinates;
import org.example.dragon.entity.Dragon;
import org.example.dragon.entity.Person;
import org.example.dragon.query.DragonPage;
import org.example.dragon.resource.dto.CaveDto;
import org.example.dragon.resource.dto.CoordinatesDto;
import org.example.dragon.resource.dto.DragonDto;
import org.example.dragon.resource.dto.DragonPageDto;
import org.example.dragon.resource.dto.PersonDto;

import java.util.stream.Collectors;

/** Предполагает, что DTO уже прошёл валидацию. */
@ApplicationScoped
public class DragonMapper {

    public Dragon toEntity(DragonDto dto) {
        Dragon dragon = new Dragon();
        dragon.setName(dto.getName());
        dragon.setCoordinates(new Coordinates(dto.getCoordinates().getX(), dto.getCoordinates().getY()));
        dragon.setAge(dto.getAge());
        dragon.setDescription(dto.getDescription());
        dragon.setWeight(dto.getWeight());
        dragon.setColor(dto.getColor() == null ? null : Color.valueOf(dto.getColor()));
        if (dto.getCave() != null) {
            Cave cave = new Cave();
            cave.setDepth(dto.getCave().getDepth());
            dragon.setCave(cave);
        }
        if (dto.getKiller() != null) {
            PersonDto killer = dto.getKiller();
            dragon.setKiller(new Person(killer.getName(), killer.getHeight(),
                    Color.valueOf(killer.getEyeColor()), Color.valueOf(killer.getHairColor())));
        }
        return dragon;
    }

    public DragonDto toDto(Dragon dragon) {
        DragonDto dto = new DragonDto();
        dto.setId(dragon.getId());
        dto.setName(dragon.getName());
        CoordinatesDto coordinates = new CoordinatesDto();
        coordinates.setX(dragon.getCoordinates().getX());
        coordinates.setY(dragon.getCoordinates().getY());
        dto.setCoordinates(coordinates);
        dto.setCreationDate(dragon.getCreationDate().toString());
        dto.setAge(dragon.getAge());
        dto.setDescription(dragon.getDescription());
        dto.setWeight(dragon.getWeight());
        dto.setColor(dragon.getColor() == null ? null : dragon.getColor().name());
        if (dragon.getCave() != null) {
            CaveDto cave = new CaveDto();
            cave.setId(dragon.getCave().getId());
            cave.setDepth(dragon.getCave().getDepth());
            dto.setCave(cave);
        }
        Person killer = dragon.getKiller();
        if (killer != null && killer.getName() != null) {
            PersonDto person = new PersonDto();
            person.setName(killer.getName());
            person.setHeight(killer.getHeight());
            person.setEyeColor(killer.getEyeColor().name());
            person.setHairColor(killer.getHairColor().name());
            dto.setKiller(person);
        }
        return dto;
    }

    public DragonPageDto toPageDto(DragonPage page) {
        DragonPageDto dto = new DragonPageDto();
        dto.setTotalCount(page.totalCount());
        dto.setPage(page.page());
        dto.setSize(page.size());
        dto.setItems(page.items().stream().map(this::toDto).collect(Collectors.toList()));
        return dto;
    }
}
