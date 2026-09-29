package com.vhvkhangg.personalprivatevault.film.film.exception;

/**
 * Thrown when a requested film cannot be found.
 */
public class FilmNotFoundException extends RuntimeException {

    public FilmNotFoundException(Long id) {
        super("Film with ID " + id + " not found");
    }

    public FilmNotFoundException(String message) {
        super(message);
    }
}
