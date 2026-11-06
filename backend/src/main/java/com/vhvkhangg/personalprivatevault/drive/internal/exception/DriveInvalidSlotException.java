package com.vhvkhangg.personalprivatevault.drive.internal.exception;

public class DriveInvalidSlotException extends DriveException {

    public DriveInvalidSlotException(int slot) {
        super("DRIVE_INVALID_SLOT", "Slot must be either 1 or 2, received: " + slot, 400);
    }
}
