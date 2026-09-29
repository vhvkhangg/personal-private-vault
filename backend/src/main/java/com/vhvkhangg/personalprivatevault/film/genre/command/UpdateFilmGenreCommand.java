package com.vhvkhangg.personalprivatevault.film.genre.command;

/**
 * Command to update an existing film genre.
 *
 * @param id required genre ID
 * @param name required unique genre name (case-insensitive, up to 150 characters)
 * @param description optional description
 */
public record UpdateFilmGenreCommand(
        Long id,
        String name,
        String description
) {
}
