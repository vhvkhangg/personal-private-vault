package com.vhvkhangg.personalprivatevault.rag.api;

public class RagJobBusyException extends IllegalStateException {
    public RagJobBusyException() { super("Another indexing attempt must finish safe recovery first"); }
}
