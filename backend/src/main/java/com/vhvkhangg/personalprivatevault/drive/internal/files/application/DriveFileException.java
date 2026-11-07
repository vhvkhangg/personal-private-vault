package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveException;

/** Safe public category; no provider/SQL/input payload or exception cause is retained. */
public class DriveFileException extends DriveException {
    public DriveFileException(String code, int status) { super(code, message(code), status); }

    private static String message(String code) {
        return switch (code) {
            case "PAYLOAD_TOO_LARGE" -> "File or request exceeds the supported size";
            case "DRIVE_FILE_CAPACITY_EXHAUSTED" -> "File-operation capacity is unavailable; retry later";
            case "UPLOAD_OUTCOME_UNKNOWN", "DRIVE_ROOT_OUTCOME_UNKNOWN" -> "Remote outcome is unknown; retry the same action for verification";
            default -> "Google Drive file operation could not be completed";
        };
    }

    public static DriveFileException input() { return new DriveFileException("DRIVE_INVALID_FILE_INPUT", 400); }
    public static DriveFileException stale() { return new DriveFileException("DRIVE_STALE_AUTHORITY", 409); }
    public static DriveFileException capacity() { return new DriveFileException("DRIVE_FILE_CAPACITY_EXHAUSTED", 503); }
    public static DriveFileException timeout() { return new DriveFileException("DRIVE_PROVIDER_TIMEOUT", 504); }
}
