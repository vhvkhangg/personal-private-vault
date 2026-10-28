package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.Objects;
import java.util.UUID;

public record RagRetrievalCancelResult(
        UUID requestId,
        String status,
        String message
) {
    public RagRetrievalCancelResult {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }
}
