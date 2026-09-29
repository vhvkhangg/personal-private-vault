package com.vhvkhangg.personalprivatevault.fiction.genre.command;

/**
 * Command to create a new fiction genre.
 *
 * @param name required genre name (up to 150 characters, unique case-insensitively)
 * @param description optional description
 */
public record CreateFictionGenreCommand(
        String name,
        String description
) {
}
