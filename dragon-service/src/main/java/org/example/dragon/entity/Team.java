package org.example.dragon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Команда убийц драконов. id задаёт клиент (team-id в API). */
@Entity
@Table(name = "dragon_team")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    private Long id;

    @Column(name = "team_name", nullable = false)
    private String name;

    @Column(name = "team_size", nullable = false)
    private int size;

    @Column(name = "start_cave_id", nullable = false)
    private long startCaveId;
}
