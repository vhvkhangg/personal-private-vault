package com.vhvkhangg.personalprivatevault.rag.api;

public class RagQueryScopeLimitException extends IllegalArgumentException {
    public RagQueryScopeLimitException() {
        super("Complete query scope exceeds the maximum allowable document limit");
    }

    public RagQueryScopeLimitException(String message) {
        super(message);
    }
}
