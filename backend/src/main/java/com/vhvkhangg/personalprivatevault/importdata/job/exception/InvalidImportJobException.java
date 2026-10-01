package com.vhvkhangg.personalprivatevault.importdata.job.exception;

/**
 * Thrown when import job creation, configuration, or decision parameters violate invariants.
 */
public class InvalidImportJobException extends RuntimeException {

    public InvalidImportJobException(String message) {
        super(message);
    }
}
