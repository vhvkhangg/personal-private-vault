package com.vhvkhangg.personalprivatevault.rag.api;

import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;

import java.util.Objects;
import java.util.Set;

/**
 * Approved document root reference for batch admission (ADR-0021 Clause 1).
 */
public record RagDocumentRootItem(
        String rootKey,
        RagSourceType sourceType,
        String sourceId,
        Long expectedSourceVersion,
        Long expectedAuthorizationEpoch,
        Integer expectedFieldPolicyVersion,
        Set<String> approvedFields
) {
    public RagDocumentRootItem {
        Objects.requireNonNull(rootKey, "rootKey must not be null");
        Objects.requireNonNull(sourceType, "sourceType must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        approvedFields = approvedFields != null ? Set.copyOf(approvedFields) : Set.of();
    }
}
