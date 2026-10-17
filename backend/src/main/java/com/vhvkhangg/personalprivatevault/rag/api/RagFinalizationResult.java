package com.vhvkhangg.personalprivatevault.rag.api;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RagFinalizationResult(
        UUID generationId,
        Long sourceId,
        long authorizationEpoch,
        String status,
        boolean active,
        Instant publishedAt
) {
    public RagFinalizationResult {
        Objects.requireNonNull(generationId, "generationId must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(publishedAt, "publishedAt must not be null");
    }
}
