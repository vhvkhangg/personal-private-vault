package com.vhvkhangg.personalprivatevault.fiction.view;

/**
 * Public immutable view of a fiction genre.
 *
 * @param id unique identifier of the genre
 * @param name unique name of the genre
 * @param description optional descriptive text
 */
public record FictionGenreView(
        Long id,
        String name,
        String description
) {
}
