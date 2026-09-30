package com.vhvkhangg.personalprivatevault.knowledge.information.information;

/**
 * Exception thrown when a requested information item cannot be found.
 */
public class InformationItemNotFoundException extends RuntimeException {

    public InformationItemNotFoundException(String message) {
        super(message);
    }

    public InformationItemNotFoundException(Long id) {
        super("Information item with id " + id + " does not exist");
    }
}
