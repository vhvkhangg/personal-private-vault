package com.vhvkhangg.personalprivatevault.drive.internal.exception;

public class DriveProviderException extends DriveException {

    public DriveProviderException(String errorCode, String message, int httpStatus) {
        super(errorCode, message, httpStatus);
    }

    public DriveProviderException(String errorCode, String message, int httpStatus, Throwable cause) {
        // Provider/parser exceptions can embed raw token bodies. Retain only the safe category.
        super(errorCode, message, httpStatus);
    }
}
