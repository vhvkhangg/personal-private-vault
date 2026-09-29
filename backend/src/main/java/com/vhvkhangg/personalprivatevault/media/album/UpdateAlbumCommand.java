package com.vhvkhangg.personalprivatevault.media.album;

/**
 * Command to update an existing Album.
 */
public record UpdateAlbumCommand(
        Long id,
        String title,
        String description
) {
}
