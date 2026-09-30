package com.vhvkhangg.personalprivatevault.knowledge.note.note;

/**
 * Exception thrown when a note uniqueness conflict occurs (e.g. duplicate imported file hash).
 */
public class NoteConflictException extends RuntimeException {

    public NoteConflictException(String message) {
        super(message);
    }
}
