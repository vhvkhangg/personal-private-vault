package com.vhvkhangg.personalprivatevault.film.view;

import java.util.Set;

/**
 * Public immutable view of genres and narrative classifications assigned to a film.
 *
 * @param filmId ID of the parent film
 * @param genreIds set of assigned film genre IDs owned by the film module
 * @param storyArchetypeIds set of assigned story archetype IDs from the reference module
 * @param worldSettingIds set of assigned world setting IDs from the reference module
 */
public record FilmClassificationsView(
        Long filmId,
        Set<Long> genreIds,
        Set<Long> storyArchetypeIds,
        Set<Long> worldSettingIds
) {
    public FilmClassificationsView {
        genreIds = genreIds != null ? Set.copyOf(genreIds) : Set.of();
        storyArchetypeIds = storyArchetypeIds != null ? Set.copyOf(storyArchetypeIds) : Set.of();
        worldSettingIds = worldSettingIds != null ? Set.copyOf(worldSettingIds) : Set.of();
    }
}
