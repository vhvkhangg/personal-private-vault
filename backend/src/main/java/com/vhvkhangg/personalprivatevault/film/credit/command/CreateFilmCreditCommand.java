package com.vhvkhangg.personalprivatevault.film.credit.command;

import com.vhvkhangg.personalprivatevault.film.enums.FilmCreditRole;

/**
 * Command to create a new film credit record.
 *
 * @param filmId required parent film ID
 * @param personId required credited person ID
 * @param role required credit role
 * @param characterName optional character name (up to 255 characters)
 * @param note optional note
 */
public record CreateFilmCreditCommand(
        Long filmId,
        Long personId,
        FilmCreditRole role,
        String characterName,
        String note
) {
}
