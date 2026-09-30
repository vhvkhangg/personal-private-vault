package com.vhvkhangg.personalprivatevault.knowledge.note.note;

/**
 * Exception thrown when a requested note cannot be found.
 */
public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(String message) {
        super(message);
    }

    public NoteNotFoundException(Long id) {
        super("Note with id " + id + " does not exist");
    }
}
