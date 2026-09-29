package com.vhvkhangg.personalprivatevault.film.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Film genre entity representing a genre category owned by the film module.
 */
@Entity
@Table(name = "film_genres")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilmGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 150, nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    public FilmGenre(String name, String description) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
    }

    public void update(String name, String description) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilmGenre other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
