package com.vhvkhangg.personalprivatevault.journal.diary.exception;

/**
 * Thrown when a diary entry cannot be found by its identifier.
 */
public class DiaryEntryNotFoundException extends RuntimeException {

    public DiaryEntryNotFoundException(Long id) {
        super("Diary entry not found with id: " + id);
    }
}
