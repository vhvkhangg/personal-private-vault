package com.vhvkhangg.personalprivatevault.knowledge.study.study;

/**
 * Exception thrown when a study item uniqueness conflict occurs (e.g. YouTube channel account).
 */
public class StudyConflictException extends RuntimeException {

    public StudyConflictException(String message) {
        super(message);
    }
}
