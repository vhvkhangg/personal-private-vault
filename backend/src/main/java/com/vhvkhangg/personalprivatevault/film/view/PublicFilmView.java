package com.vhvkhangg.personalprivatevault.film.view;

import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;

/**
 * Public immutable view of an eligible film work (PROFILE_PUBLIC view).
 *
 * @param id film ID (identical to underlying vault entry ID with type FILM)
 * @param title primary title
 * @param originalTitle optional original/native title
 * @param nationalityCode optional 2-character country code
 * @param format format of the work (MOVIE or SERIES)
 * @param productionStyle production style (LIVE_ACTION or ANIMATION)
 * @param isNsfw whether the work is marked NSFW (always false on success)
 */
public record PublicFilmView(
        Long id,
        String title,
        String originalTitle,
        String nationalityCode,
        FilmFormat format,
        FilmProductionStyle productionStyle,
        boolean isNsfw
) {
}
