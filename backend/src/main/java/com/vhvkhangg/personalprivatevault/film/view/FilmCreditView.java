package com.vhvkhangg.personalprivatevault.film.view;

import com.vhvkhangg.personalprivatevault.film.enums.FilmCreditRole;

/**
 * Public immutable view of a film credit record.
 *
 * @param id credit ID (identical to underlying vault entry ID with type FILM_CREDIT)
 * @param filmId parent film ID
 * @param personId person ID of the credited individual
 * @param role credit role (MAIN, SUPPORTING, CAMEO, GUEST, VOICE)
 * @param characterName optional character name
 * @param note optional notes regarding the credit
 */
public record FilmCreditView(
        Long id,
        Long filmId,
        Long personId,
        FilmCreditRole role,
        String characterName,
        String note
) {
}
