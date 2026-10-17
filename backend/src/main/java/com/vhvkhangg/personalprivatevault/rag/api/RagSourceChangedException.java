package com.vhvkhangg.personalprivatevault.rag.api;

/** A stale owner command never grants newer consent or silently retries. */
public class RagSourceChangedException extends IllegalStateException {
    public RagSourceChangedException() {
        super("Source or consent changed; review current state");
    }
}
