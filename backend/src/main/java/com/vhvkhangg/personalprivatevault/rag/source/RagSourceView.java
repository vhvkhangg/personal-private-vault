package com.vhvkhangg.personalprivatevault.rag.source;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * Public view of a RAG registered source and its current policy epoch.
 */
public record RagSourceView(
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
        String currentDescriptionRevisionHash,
        RagSnapshotExpectation snapshot
) {
    public RagSourceView(Long id, RagSourceType sourceType, String sourceId, RagClassification classification,
            long authorizationEpoch, int fieldPolicyVersion, Set<String> approvedFields, RagAccessState accessState,
            String sourceRevisionHash, String descriptionRevisionHash, Instant createdAt, Instant updatedAt,
            Instant revokedAt, long sourceVersion, long consentResetVersion, String currentDescriptionRevisionHash) {
        this(id, sourceType, sourceId, classification, authorizationEpoch, fieldPolicyVersion, approvedFields,
                accessState, sourceRevisionHash, descriptionRevisionHash, createdAt, updatedAt, revokedAt,
                sourceVersion, consentResetVersion, currentDescriptionRevisionHash, null);
    }
    public RagSourceView {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(sourceType, "sourceType must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(classification, "classification must not be null");
        Objects.requireNonNull(approvedFields, "approvedFields must not be null");
        Objects.requireNonNull(accessState, "accessState must not be null");
        Objects.requireNonNull(sourceRevisionHash, "sourceRevisionHash must not be null");
        Objects.requireNonNull(descriptionRevisionHash, "descriptionRevisionHash must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public boolean isEligible() {
        return classification == RagClassification.NON_PRIVATE && accessState == RagAccessState.APPROVED;
    }
}
