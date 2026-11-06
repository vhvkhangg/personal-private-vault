package com.vhvkhangg.personalprivatevault.drive.internal.exception;

public class DriveDifferentSubjectOccupiedException extends DriveException {

    public DriveDifferentSubjectOccupiedException(String message) {
        super("DRIVE_SLOT_OCCUPIED_BY_DIFFERENT_SUBJECT", message, 409);
    }
}
