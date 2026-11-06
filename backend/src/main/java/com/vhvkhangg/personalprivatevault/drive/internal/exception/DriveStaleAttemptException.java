package com.vhvkhangg.personalprivatevault.drive.internal.exception;

public class DriveStaleAttemptException extends DriveException {

    public DriveStaleAttemptException(String message) {
        super("DRIVE_STALE_ATTEMPT", message, 409);
    }
}
