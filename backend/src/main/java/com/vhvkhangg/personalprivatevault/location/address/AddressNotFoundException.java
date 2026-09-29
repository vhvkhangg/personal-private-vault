package com.vhvkhangg.personalprivatevault.location.address;

/**
 * Thrown when an address cannot be found by its ID.
 */
public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(Long id) {
        super("Address with ID " + id + " was not found");
    }

    public AddressNotFoundException(String message) {
        super(message);
    }
}
