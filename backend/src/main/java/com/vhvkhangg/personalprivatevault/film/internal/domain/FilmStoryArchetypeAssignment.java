package com.vhvkhangg.personalprivatevault.film.internal.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Entity mapping the {@code film_story_archetypes} join table.
 */
@Entity
@Table(name = "film_story_archetypes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilmStoryArchetypeAssignment {

    @EmbeddedId
    private FilmStoryArchetypeId id;

    public FilmStoryArchetypeAssignment(FilmStoryArchetypeId id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public FilmStoryArchetypeAssignment(Long filmId, Long storyArchetypeId) {
        this(new FilmStoryArchetypeId(filmId, storyArchetypeId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilmStoryArchetypeAssignment other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
