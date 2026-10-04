package com.vhvkhangg.personalprivatevault.journal.diary.exception;

/**
 * Thrown when diary entry validation fails.
 */
public class InvalidDiaryEntryException extends RuntimeException {

    public InvalidDiaryEntryException(String message) {
        super(message);
    }
}
