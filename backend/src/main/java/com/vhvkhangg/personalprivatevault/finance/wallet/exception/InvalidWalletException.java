package com.vhvkhangg.personalprivatevault.finance.wallet.exception;

/**
 * Thrown when wallet validation fails.
 */
public class InvalidWalletException extends RuntimeException {

    public InvalidWalletException(String message) {
        super(message);
    }
}
