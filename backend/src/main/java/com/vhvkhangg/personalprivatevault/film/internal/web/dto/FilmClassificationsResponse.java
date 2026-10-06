package com.vhvkhangg.personalprivatevault.film.internal.web.dto;

import java.util.Set;

public record FilmClassificationsResponse(
        Long filmId,
        Set<Long> genreIds,
        Set<Long> storyArchetypeIds,
        Set<Long> worldSettingIds
) {
}
