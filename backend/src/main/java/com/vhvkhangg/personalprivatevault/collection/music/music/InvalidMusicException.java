package com.vhvkhangg.personalprivatevault.collection.music.music;

/**
 * Exception thrown when music track validation fails.
 */
public class InvalidMusicException extends RuntimeException {

    public InvalidMusicException(String message) {
        super(message);
    }

    public InvalidMusicException(String message, Throwable cause) {
        super(message, cause);
    }
}
