package com.vhvkhangg.personalprivatevault.account.relationship;

/**
 * Thrown when an External Account Relationship command is invalid (e.g. self-relationship, non-existent account).
 */
public class InvalidExternalAccountRelationshipException extends RuntimeException {
    public InvalidExternalAccountRelationshipException(String message) {
        super(message);
    }
}
