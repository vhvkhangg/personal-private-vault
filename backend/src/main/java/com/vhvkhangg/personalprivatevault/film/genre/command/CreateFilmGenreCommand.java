package com.vhvkhangg.personalprivatevault.film.genre.command;

/**
 * Command to create a new film genre.
 *
 * @param name required unique genre name (case-insensitive, up to 150 characters)
 * @param description optional description
 */
public record CreateFilmGenreCommand(
        String name,
        String description
) {
}
