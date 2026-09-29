package com.vhvkhangg.personalprivatevault.film.credit.exception;

/**
 * Thrown when a requested film credit cannot be found.
 */
public class FilmCreditNotFoundException extends RuntimeException {

    public FilmCreditNotFoundException(Long id) {
        super("Film credit with ID " + id + " not found");
    }

    public FilmCreditNotFoundException(String message) {
        super(message);
    }
}
