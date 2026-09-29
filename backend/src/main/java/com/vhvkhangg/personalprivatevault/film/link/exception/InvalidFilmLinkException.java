package com.vhvkhangg.personalprivatevault.film.link.exception;

/**
 * Thrown when film link attributes or reference codes violate domain validation rules.
 */
public class InvalidFilmLinkException extends RuntimeException {

    public InvalidFilmLinkException(String message) {
        super(message);
    }
}
