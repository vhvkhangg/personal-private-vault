package com.vhvkhangg.personalprivatevault.authentication.privatepin;

/**
 * Public synchronous capability contract for private-mode PIN verification and change.
 */
public interface PrivatePinOperations {

    boolean verifyPin(String pin);

    void changePin(ChangePinCommand command);
}
