package com.vhvkhangg.personalprivatevault.media.image;

/**
 * Thrown when image command inputs fail validation.
 */
public class InvalidImageException extends RuntimeException {

    public InvalidImageException(String message) {
        super(message);
    }
}
