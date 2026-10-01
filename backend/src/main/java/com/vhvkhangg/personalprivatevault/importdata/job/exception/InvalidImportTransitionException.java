package com.vhvkhangg.personalprivatevault.importdata.job.exception;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportJobStatus;
import lombok.Getter;

/**
 * Thrown when an import job mutation transition is incompatible with the job's authoritative current status.
 */
@Getter
public class InvalidImportTransitionException extends RuntimeException {

    private final Long jobId;
    private final ImportJobStatus currentStatus;
    private final String requestedTransition;

    public InvalidImportTransitionException(Long jobId, ImportJobStatus currentStatus, String requestedTransition) {
        super("Cannot " + requestedTransition + " import job " + jobId + " from status " + currentStatus);
        this.jobId = jobId;
        this.currentStatus = currentStatus;
        this.requestedTransition = requestedTransition;
    }
}
