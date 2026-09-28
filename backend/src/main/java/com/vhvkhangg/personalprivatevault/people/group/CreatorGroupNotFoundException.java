package com.vhvkhangg.personalprivatevault.people.group;

/**
 * Thrown when a creator group is not found by ID.
 */
public class CreatorGroupNotFoundException extends RuntimeException {

    public CreatorGroupNotFoundException(Long groupId) {
        super("Creator group not found with id: " + groupId);
    }

    public CreatorGroupNotFoundException(String message) {
        super(message);
    }

    public CreatorGroupNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
