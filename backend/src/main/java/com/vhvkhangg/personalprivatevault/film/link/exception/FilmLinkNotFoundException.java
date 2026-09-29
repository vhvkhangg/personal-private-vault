package com.vhvkhangg.personalprivatevault.film.link.exception;

/**
 * Thrown when a requested film link cannot be found or does not belong to the target film.
 */
public class FilmLinkNotFoundException extends RuntimeException {

    public FilmLinkNotFoundException(Long id) {
        super("Film link with ID " + id + " not found");
    }

    public FilmLinkNotFoundException(String message) {
        super(message);
    }
}
