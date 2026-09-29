package com.vhvkhangg.personalprivatevault.media.album;

/**
 * Thrown when album command inputs fail validation.
 */
public class InvalidAlbumException extends RuntimeException {

    public InvalidAlbumException(String message) {
        super(message);
    }
}
