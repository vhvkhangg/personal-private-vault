package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary;

/**
 * Exception thrown when a requested vocabulary item cannot be found.
 */
public class VocabularyItemNotFoundException extends RuntimeException {

    public VocabularyItemNotFoundException(String message) {
        super(message);
    }

    public VocabularyItemNotFoundException(Long id) {
        super("Vocabulary item with id " + id + " does not exist");
    }
}
