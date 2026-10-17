package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import java.time.Instant;
import java.util.UUID;

public record RagJobStatusResponse(
        UUID jobId,
        UUID attemptId,
        Long sourceId,
        String status,
        String indexState,
        Instant leasedUntil,
        String purgeStatus,
        Instant createdAt,
        Instant updatedAt
) {}
