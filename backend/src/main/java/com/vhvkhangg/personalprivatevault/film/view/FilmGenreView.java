package com.vhvkhangg.personalprivatevault.film.view;

/**
 * Public immutable view of a film-owned genre.
 *
 * @param id genre ID
 * @param name unique genre name
 * @param description optional genre description
 */
public record FilmGenreView(
        Long id,
        String name,
        String description
) {
}
