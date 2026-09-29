package com.vhvkhangg.personalprivatevault.film.genre.exception;

/**
 * Thrown when film genre attributes violate validation constraints.
 */
public class InvalidFilmGenreException extends RuntimeException {

    public InvalidFilmGenreException(String message) {
        super(message);
    }
}
