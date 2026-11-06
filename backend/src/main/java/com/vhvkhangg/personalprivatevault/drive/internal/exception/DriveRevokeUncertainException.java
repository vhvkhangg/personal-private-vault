package com.vhvkhangg.personalprivatevault.drive.internal.exception;

public class DriveRevokeUncertainException extends DriveException {

    public DriveRevokeUncertainException(String message) {
        super("DRIVE_REVOKE_UNCERTAIN", message, 409);
    }
}
