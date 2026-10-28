package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record RagRetrievalResponse(
        UUID requestId,
        RagRetrievalOutcome outcome,
        List<RagRetrievalMatch> matches
) {
    public RagRetrievalResponse {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(outcome, "outcome must not be null");
        if (matches == null) {
            matches = List.of();
        } else {
            matches = List.copyOf(matches);
        }
    }
}
