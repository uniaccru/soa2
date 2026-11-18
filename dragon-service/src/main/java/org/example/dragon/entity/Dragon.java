package org.example.dragon.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "dragon")
@Getter
@Setter
@NoArgsConstructor
public class Dragon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Embedded
    private Coordinates coordinates;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDate creationDate;

    private Long age;

    private String description;

    private Integer weight;

    @Enumerated(EnumType.STRING)
    private Color color;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cave_id")
    private Cave cave;

    @Embedded
    private Person killer;

    /**
     * Полная замена изменяемых полей (семантика PUT). id и creationDate не трогаем.
     * Существующая пещера сохраняет id, чтобы ссылки на неё не ломались.
     */
    public void applyFrom(Dragon source) {
        this.name = source.name;
        this.coordinates = source.coordinates;
        this.age = source.age;
        this.description = source.description;
        this.weight = source.weight;
        this.color = source.color;
        this.killer = source.killer;
        if (source.cave == null) {
            this.cave = null;
        } else if (this.cave == null) {
            this.cave = source.cave;
        } else {
            this.cave.setDepth(source.cave.getDepth());
        }
    }
}
