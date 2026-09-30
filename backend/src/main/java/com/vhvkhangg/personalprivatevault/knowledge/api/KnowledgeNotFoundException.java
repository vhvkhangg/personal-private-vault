package com.vhvkhangg.personalprivatevault.knowledge.api;

/**
 * Exception thrown by the parent Knowledge API when a requested knowledge item cannot be found.
 */
public class KnowledgeNotFoundException extends RuntimeException {

    public KnowledgeNotFoundException(String message) {
        super(message);
    }
}
