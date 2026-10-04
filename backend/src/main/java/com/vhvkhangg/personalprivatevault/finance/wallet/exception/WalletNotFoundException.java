package com.vhvkhangg.personalprivatevault.finance.wallet.exception;

/**
 * Thrown when a wallet cannot be found by its identifier.
 */
public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(Long id) {
        super("Wallet not found with id: " + id);
    }
}
