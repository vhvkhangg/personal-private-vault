package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary;

/**
 * Exception thrown when a vocabulary item or transition fails validation or invariant checks.
 */
public class InvalidVocabularyItemException extends RuntimeException {

    public InvalidVocabularyItemException(String message) {
        super(message);
    }
}
