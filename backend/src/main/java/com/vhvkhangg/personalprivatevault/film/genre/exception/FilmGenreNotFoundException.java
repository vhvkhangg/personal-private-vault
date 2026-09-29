package com.vhvkhangg.personalprivatevault.film.genre.exception;

/**
 * Thrown when a requested film genre cannot be found.
 */
public class FilmGenreNotFoundException extends RuntimeException {

    public FilmGenreNotFoundException(Long id) {
        super("Film genre with ID " + id + " not found");
    }

    public FilmGenreNotFoundException(String message) {
        super(message);
    }
}
