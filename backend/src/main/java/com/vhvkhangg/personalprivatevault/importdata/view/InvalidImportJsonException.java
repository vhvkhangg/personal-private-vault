package com.vhvkhangg.personalprivatevault.importdata.view;

/**
 * Thrown when imported item JSON payload contains invalid keys, cycles, or unsupported types.
 */
public class InvalidImportJsonException extends RuntimeException {

    public InvalidImportJsonException(String message) {
        super(message);
    }
}
