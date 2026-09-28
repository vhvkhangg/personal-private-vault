package com.vhvkhangg.personalprivatevault.authentication.privatepin;

/**
 * Thrown when current PIN verification fails during PIN change.
 */
public class PinVerificationException extends RuntimeException {

    public PinVerificationException(String message) {
        super(message);
    }
}
