package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.source.RagAccessState;
import com.vhvkhangg.personalprivatevault.rag.source.RagClassification;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;

import java.time.Instant;
import java.util.Set;

public record RagSourceResponse(
        Long id,
        RagSourceType sourceType,
        String sourceId,
        RagClassification classification,
        long authorizationEpoch,
        int fieldPolicyVersion,
        Set<String> approvedFields,
        RagAccessState accessState,
        String sourceRevisionHash,
        String descriptionRevisionHash,
        Instant createdAt,
        Instant updatedAt,
        Instant revokedAt,
        long sourceVersion,
        long consentResetVersion,
        String currentDescriptionRevisionHash
) {}
