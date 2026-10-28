package com.vhvkhangg.personalprivatevault.rag.api;

public class RagReaderBusyException extends RuntimeException {
    public RagReaderBusyException() {
        super("An active retrieval reader or source mutation is in progress");
    }

    public RagReaderBusyException(String message) {
        super(message);
    }
}
