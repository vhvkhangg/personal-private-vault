package com.vhvkhangg.personalprivatevault.media.image;

/**
 * Thrown when an image cannot be found by its ID.
 */
public class ImageNotFoundException extends RuntimeException {

    public ImageNotFoundException(Long id) {
        super("Image with ID " + id + " was not found");
    }

    public ImageNotFoundException(String message) {
        super(message);
    }
}
