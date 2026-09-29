package com.vhvkhangg.personalprivatevault.film.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite primary key for {@link FilmStoryArchetypeAssignment}.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class FilmStoryArchetypeId implements Serializable {

    @Column(name = "film_id", nullable = false)
    private Long filmId;

    @Column(name = "story_archetype_id", nullable = false)
    private Long storyArchetypeId;
}
