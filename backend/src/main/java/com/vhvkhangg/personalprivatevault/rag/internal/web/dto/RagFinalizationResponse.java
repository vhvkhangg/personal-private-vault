package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import java.time.Instant;
import java.util.UUID;

public record RagFinalizationResponse(
        UUID generationId,
        Long sourceId,
        long authorizationEpoch,
        String status,
        boolean active,
        Instant publishedAt
) {}
