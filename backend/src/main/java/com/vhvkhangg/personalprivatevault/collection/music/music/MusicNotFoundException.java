package com.vhvkhangg.personalprivatevault.collection.music.music;

/**
 * Exception thrown when a music track is not found.
 */
public class MusicNotFoundException extends RuntimeException {

    public MusicNotFoundException(Long id) {
        super("Music track not found: " + id);
    }

    public MusicNotFoundException(String message) {
        super(message);
    }
}
