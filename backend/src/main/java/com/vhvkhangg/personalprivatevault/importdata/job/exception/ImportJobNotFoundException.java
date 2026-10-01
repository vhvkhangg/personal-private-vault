package com.vhvkhangg.personalprivatevault.importdata.job.exception;

/**
 * Thrown when an import job is not found.
 */
public class ImportJobNotFoundException extends RuntimeException {

    public ImportJobNotFoundException(Long id) {
        super("Import job not found with id: " + id);
    }

    public ImportJobNotFoundException(String message) {
        super(message);
    }
}
