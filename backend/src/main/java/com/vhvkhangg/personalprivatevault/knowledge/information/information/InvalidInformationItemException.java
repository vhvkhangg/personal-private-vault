package com.vhvkhangg.personalprivatevault.knowledge.information.information;

/**
 * Exception thrown when an information item fails validation or invariant checks.
 */
public class InvalidInformationItemException extends RuntimeException {

    public InvalidInformationItemException(String message) {
        super(message);
    }
}
