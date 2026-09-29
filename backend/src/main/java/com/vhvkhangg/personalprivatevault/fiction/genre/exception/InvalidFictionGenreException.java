package com.vhvkhangg.personalprivatevault.fiction.genre.exception;

/**
 * Thrown when fiction genre attributes violate validation rules.
 */
public class InvalidFictionGenreException extends RuntimeException {

    public InvalidFictionGenreException(String message) {
        super(message);
    }

    public InvalidFictionGenreException(String message, Throwable cause) {
        super(message, cause);
    }
}
