package com.vhvkhangg.personalprivatevault.film.genre.exception;

/**
 * Thrown when attempting to create or update a film genre with a name that already exists (case-insensitive).
 */
public class FilmGenreNameAlreadyExistsException extends RuntimeException {

    public FilmGenreNameAlreadyExistsException(String name) {
        super("Film genre with name '" + name + "' already exists");
    }

    public FilmGenreNameAlreadyExistsException(String name, Throwable cause) {
        super("Film genre with name '" + name + "' already exists", cause);
    }
}
