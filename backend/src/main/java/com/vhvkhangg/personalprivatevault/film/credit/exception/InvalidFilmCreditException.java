package com.vhvkhangg.personalprivatevault.film.credit.exception;

/**
 * Thrown when film credit attributes or person/film references violate domain validation rules.
 */
public class InvalidFilmCreditException extends RuntimeException {

    public InvalidFilmCreditException(String message) {
        super(message);
    }
}
