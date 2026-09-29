package com.vhvkhangg.personalprivatevault.fiction.link.exception;

/**
 * Thrown when a requested fiction link cannot be found or does not belong to the expected parent fiction.
 */
public class FictionLinkNotFoundException extends RuntimeException {

    public FictionLinkNotFoundException(Long linkId) {
        super("Fiction link with ID " + linkId + " not found");
    }

    public FictionLinkNotFoundException(String message) {
        super(message);
    }
}
