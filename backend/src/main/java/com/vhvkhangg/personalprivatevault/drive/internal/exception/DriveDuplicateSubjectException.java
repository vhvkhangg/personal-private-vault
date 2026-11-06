package com.vhvkhangg.personalprivatevault.drive.internal.exception;

public class DriveDuplicateSubjectException extends DriveException {

    public DriveDuplicateSubjectException(String message) {
        super("DRIVE_DUPLICATE_SUBJECT", message, 409);
    }
}
