package com.vhvkhangg.personalprivatevault.rag.api;

public class RagRetrievalCancelledException extends RuntimeException {
    public RagRetrievalCancelledException() {
        super("Retrieval cancelled before response emission");
    }

    public RagRetrievalCancelledException(String message) {
        super(message);
    }
}
