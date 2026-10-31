package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.Objects;
import java.util.UUID;

/**
 * Result of cancelling a grounded QA request and achieving reader quiescence.
 */
public record RagQaCancelResult(
        UUID requestId,
        String disposition,
        String message
) {
    public RagQaCancelResult {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(disposition, "disposition must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }
}
