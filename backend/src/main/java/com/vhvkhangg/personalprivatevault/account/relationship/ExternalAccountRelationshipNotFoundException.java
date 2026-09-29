package com.vhvkhangg.personalprivatevault.account.relationship;

/**
 * Thrown when an External Account Relationship is not found.
 */
public class ExternalAccountRelationshipNotFoundException extends RuntimeException {
    public ExternalAccountRelationshipNotFoundException(String message) {
        super(message);
    }
}
