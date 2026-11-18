package org.example.dragon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Убийца дракона (в API — элемент killer). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    @Column(name = "killer_name")
    private String name;

    @Column(name = "killer_height")
    private Double height;

    @Enumerated(EnumType.STRING)
    @Column(name = "killer_eye_color")
    private Color eyeColor;

    @Enumerated(EnumType.STRING)
    @Column(name = "killer_hair_color")
    private Color hairColor;
}
