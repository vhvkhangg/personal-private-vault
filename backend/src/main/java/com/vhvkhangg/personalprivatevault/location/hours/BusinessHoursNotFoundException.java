package com.vhvkhangg.personalprivatevault.location.hours;

/**
 * Thrown when business hours for a location cannot be found.
 */
public class BusinessHoursNotFoundException extends RuntimeException {

    public BusinessHoursNotFoundException(Long locationId) {
        super("Business hours for location ID " + locationId + " were not found");
    }

    public BusinessHoursNotFoundException(String message) {
        super(message);
    }
}
