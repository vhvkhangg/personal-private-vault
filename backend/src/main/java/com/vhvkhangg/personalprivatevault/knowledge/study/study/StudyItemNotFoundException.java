package com.vhvkhangg.personalprivatevault.knowledge.study.study;

/**
 * Exception thrown when a requested study item cannot be found.
 */
public class StudyItemNotFoundException extends RuntimeException {

    public StudyItemNotFoundException(String message) {
        super(message);
    }

    public StudyItemNotFoundException(Long id) {
        super("Study item with id " + id + " does not exist");
    }
}
