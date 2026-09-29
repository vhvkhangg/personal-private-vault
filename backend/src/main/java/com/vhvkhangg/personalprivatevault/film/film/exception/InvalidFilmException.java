package com.vhvkhangg.personalprivatevault.film.film.exception;

/**
 * Thrown when film attributes or reference IDs violate domain validation rules.
 */
public class InvalidFilmException extends RuntimeException {

    public InvalidFilmException(String message) {
        super(message);
    }
}
