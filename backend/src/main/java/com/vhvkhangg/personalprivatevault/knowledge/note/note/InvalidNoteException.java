package com.vhvkhangg.personalprivatevault.knowledge.note.note;

/**
 * Exception thrown when a note fails validation or invariant checks.
 */
public class InvalidNoteException extends RuntimeException {

    public InvalidNoteException(String message) {
        super(message);
    }
}
