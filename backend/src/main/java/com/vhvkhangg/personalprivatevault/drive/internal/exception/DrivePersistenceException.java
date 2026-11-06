package com.vhvkhangg.personalprivatevault.drive.internal.exception;

public class DrivePersistenceException extends DriveException {

    public DrivePersistenceException(String message) {
        super("DRIVE_PERSISTENCE_FAILURE", message, 500);
    }

    public DrivePersistenceException(String message, Throwable cause) {
        // SQL exceptions can include credential values. Keep them out of application diagnostics.
        super("DRIVE_PERSISTENCE_FAILURE", message, 500);
    }
}
