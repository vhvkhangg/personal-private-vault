package com.vhvkhangg.personalprivatevault.drive.internal.exception;

public class DriveCapacityExhaustedException extends DriveException {

    public DriveCapacityExhaustedException(String message) {
        super("DRIVE_CAPACITY_EXHAUSTED", message, 503);
    }
}
