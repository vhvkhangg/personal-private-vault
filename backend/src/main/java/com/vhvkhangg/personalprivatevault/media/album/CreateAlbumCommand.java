package com.vhvkhangg.personalprivatevault.media.album;

/**
 * Command to create a new Album.
 */
public record CreateAlbumCommand(
        String title,
        String description
) {
}
