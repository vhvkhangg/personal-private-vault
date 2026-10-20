package com.vhvkhangg.personalprivatevault.rag.source;

import java.util.UUID;

/** Exact external preview echoed by a manual update or whole-file approval. */
public record RagSnapshotExpectation(long sourceVersion, long consentResetVersion,
        long authorizationEpoch, int fieldPolicyVersion, long fileAuthorizationEpoch,
        int filePolicyVersion, UUID selectedRevisionId, UUID pendingRevisionId,
        String pendingSha256, Long pendingByteLength) {
    public RagSnapshotExpectation {
        if (sourceVersion <= 0 || consentResetVersion <= 0 || authorizationEpoch <= 0
                || fieldPolicyVersion <= 0 || fileAuthorizationEpoch <= 0 || filePolicyVersion <= 0
                || (pendingRevisionId == null) != (pendingSha256 == null)
                || (pendingRevisionId == null) != (pendingByteLength == null)
                || (pendingSha256 != null && !pendingSha256.matches("[a-fA-F0-9]{64}"))
                || (pendingByteLength != null && pendingByteLength < 0)) {
            throw new IllegalArgumentException("Invalid snapshot expectation");
        }
    }

    @Override
    public String toString() {
        return "RagSnapshotExpectation[redacted]";
    }
}
