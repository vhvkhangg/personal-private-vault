package com.vhvkhangg.personalprivatevault.media.album;

/**
 * Thrown when an album cannot be found by its ID.
 */
public class AlbumNotFoundException extends RuntimeException {

    public AlbumNotFoundException(Long id) {
        super("Album with ID " + id + " was not found");
    }

    public AlbumNotFoundException(String message) {
        super(message);
    }
}
