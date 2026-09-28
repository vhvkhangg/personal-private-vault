package com.vhvkhangg.personalprivatevault.settings.configuration;

/**
 * Thrown when settings values violate schema constraints or business rules.
 */
public class InvalidSettingsException extends RuntimeException {

    public InvalidSettingsException(String message) {
        super(message);
    }

    public InvalidSettingsException(String message, Throwable cause) {
        super(message, cause);
    }
}
