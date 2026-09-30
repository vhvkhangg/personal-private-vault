package com.vhvkhangg.personalprivatevault.knowledge.api;

/**
 * Exception thrown by the parent Knowledge API when a uniqueness conflict occurs.
 */
public class KnowledgeConflictException extends RuntimeException {

    public KnowledgeConflictException(String message) {
        super(message);
    }
}
