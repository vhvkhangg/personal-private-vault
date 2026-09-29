package com.vhvkhangg.personalprivatevault.media.image;

/**
 * Thrown when an image cannot be created due to a unique constraint conflict (e.g. duplicate object_key or checksum_sha256).
 */
public class ImageConflictException extends RuntimeException {

    public ImageConflictException(String message) {
        super(message);
    }

    public ImageConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
