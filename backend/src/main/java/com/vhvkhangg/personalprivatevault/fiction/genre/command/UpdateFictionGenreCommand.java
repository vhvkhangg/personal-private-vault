package com.vhvkhangg.personalprivatevault.fiction.genre.command;

/**
 * Command to update an existing fiction genre.
 *
 * @param id required genre ID
 * @param name required genre name (up to 150 characters, unique case-insensitively)
 * @param description optional description
 */
public record UpdateFictionGenreCommand(
        Long id,
        String name,
        String description
) {
}
