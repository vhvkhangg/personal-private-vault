package com.vhvkhangg.personalprivatevault.authentication.internal.application.session;

public class PasswordNotDistinctException extends RuntimeException {

    public PasswordNotDistinctException(String message) {
        super(message);
    }
}
