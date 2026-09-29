package com.vhvkhangg.personalprivatevault.fiction.genre.exception;

/**
 * Thrown when attempting to create or update a fiction genre with a name that already exists (case-insensitively).
 */
public class FictionGenreNameAlreadyExistsException extends RuntimeException {

    public FictionGenreNameAlreadyExistsException(String name) {
        super("Fiction genre with name '" + name + "' already exists");
    }

    public FictionGenreNameAlreadyExistsException(String name, Throwable cause) {
        super("Fiction genre with name '" + name + "' already exists", cause);
    }
}
