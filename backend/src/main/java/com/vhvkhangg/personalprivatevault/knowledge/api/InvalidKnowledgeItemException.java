package com.vhvkhangg.personalprivatevault.knowledge.api;

/**
 * Exception thrown by the parent Knowledge API when a knowledge item or command fails validation.
 */
public class InvalidKnowledgeItemException extends RuntimeException {

    public InvalidKnowledgeItemException(String message) {
        super(message);
    }
}
