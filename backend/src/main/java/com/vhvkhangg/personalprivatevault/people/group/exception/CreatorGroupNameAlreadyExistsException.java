package com.vhvkhangg.personalprivatevault.people.group.exception;

/**
 * Thrown when attempting to create or update a creator group with a name that already exists.
 */
public class CreatorGroupNameAlreadyExistsException extends RuntimeException {

    public CreatorGroupNameAlreadyExistsException(String name) {
        super("Creator group with name '" + name + "' already exists");
    }

    public CreatorGroupNameAlreadyExistsException(String name, Throwable cause) {
        super("Creator group with name '" + name + "' already exists", cause);
    }
}
