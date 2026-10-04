package com.vhvkhangg.personalprivatevault.finance.wallet.exception;

/**
 * Thrown when a wallet operation violates domain integrity constraints, such as mutating
 * currency while retained transaction or recurring entries reference the wallet.
 */
public class WalletConflictException extends RuntimeException {

    public WalletConflictException(String message) {
        super(message);
    }
}
