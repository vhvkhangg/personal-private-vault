package com.vhvkhangg.personalprivatevault.location.hours;

/**
 * Thrown when business hours command inputs fail validation.
 */
public class InvalidBusinessHoursException extends RuntimeException {

    public InvalidBusinessHoursException(String message) {
        super(message);
    }
}
