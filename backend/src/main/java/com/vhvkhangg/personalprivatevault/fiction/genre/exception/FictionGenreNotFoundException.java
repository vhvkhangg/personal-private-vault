package com.vhvkhangg.personalprivatevault.fiction.genre.exception;

/**
 * Thrown when a requested fiction genre cannot be found.
 */
public class FictionGenreNotFoundException extends RuntimeException {

    public FictionGenreNotFoundException(Long genreId) {
        super("Fiction genre with ID " + genreId + " not found");
    }

    public FictionGenreNotFoundException(String message) {
        super(message);
    }
}
