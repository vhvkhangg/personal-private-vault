package com.vhvkhangg.personalprivatevault.drive.internal.exception;

public class DriveRevocationPendingException extends DriveException {

    public DriveRevocationPendingException(String message) {
        super("DRIVE_REVOCATION_PENDING", message, 409);
    }
}
