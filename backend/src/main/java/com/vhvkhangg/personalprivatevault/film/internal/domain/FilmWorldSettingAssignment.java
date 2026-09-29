package com.vhvkhangg.personalprivatevault.film.internal.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Entity mapping the {@code film_world_settings} join table.
 */
@Entity
@Table(name = "film_world_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilmWorldSettingAssignment {

    @EmbeddedId
    private FilmWorldSettingId id;

    public FilmWorldSettingAssignment(FilmWorldSettingId id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public FilmWorldSettingAssignment(Long filmId, Long worldSettingId) {
        this(new FilmWorldSettingId(filmId, worldSettingId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilmWorldSettingAssignment other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
