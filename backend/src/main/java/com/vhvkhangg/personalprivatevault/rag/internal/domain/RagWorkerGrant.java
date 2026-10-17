package com.vhvkhangg.personalprivatevault.rag.internal.domain;

import java.util.UUID;

/**
 * Immutable issued transport scope. Sensitive input/token are excluded from diagnostics.
 */
public record RagWorkerGrant(UUID jobId, UUID attemptId, String token, String stageType, String payload,
                             String inputDigest, String scopeDigest, String outputDigest) {
    /**
     * Issuance is not a current owning-source consumption fence (FR17-5).
     * No configuration switch may enable content transfer before that protocol is proven.
     */
    public static void requireProvenConsumptionFence() {
        throw new IllegalStateException("Worker content execution is disabled: source consumption fence is unproven");
    }

    @Override
    public String toString() {
        return "RagWorkerGrant[jobId=" + jobId + ", attemptId=" + attemptId + "]";
    }
}
