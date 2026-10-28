package com.vhvkhangg.personalprivatevault.rag.api;

public class RagQueryUnavailableException extends RuntimeException {
    public RagQueryUnavailableException() {
        super("Query service unavailable");
    }

    public RagQueryUnavailableException(String message) {
        super(message);
    }

    public RagQueryUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
