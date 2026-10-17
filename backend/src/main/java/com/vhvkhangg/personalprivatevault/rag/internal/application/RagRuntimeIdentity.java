package com.vhvkhangg.personalprivatevault.rag.internal.application;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Execution-controller boot identity. Standing consent and completed-index
 * lifetime are governed independently by source/reset/policy history.
 */
@Component
public class RagRuntimeIdentity {
    private final UUID instance = UUID.randomUUID();

    public UUID instance() {
        return instance;
    }
}
