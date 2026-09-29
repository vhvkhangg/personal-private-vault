package com.vhvkhangg.personalprivatevault.film.internal.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Entity mapping the {@code film_genre_assignments} join table.
 */
@Entity
@Table(name = "film_genre_assignments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilmGenreAssignment {

    @EmbeddedId
    private FilmGenreAssignmentId id;

    public FilmGenreAssignment(FilmGenreAssignmentId id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public FilmGenreAssignment(Long filmId, Long genreId) {
        this(new FilmGenreAssignmentId(filmId, genreId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilmGenreAssignment other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
