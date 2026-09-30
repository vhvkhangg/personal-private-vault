package com.vhvkhangg.personalprivatevault.knowledge.study.study;

/**
 * Exception thrown when a study item fails validation or invariant checks.
 */
public class InvalidStudyItemException extends RuntimeException {

    public InvalidStudyItemException(String message) {
        super(message);
    }
}
