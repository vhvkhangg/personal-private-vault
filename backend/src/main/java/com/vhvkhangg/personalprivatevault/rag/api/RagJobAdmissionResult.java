package com.vhvkhangg.personalprivatevault.rag.api;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RagJobAdmissionResult(
        UUID jobId,
        UUID attemptId,
        Long sourceId,
        String leaseToken,
        Instant leasedUntil,
        long authorizationEpoch
) {
    public RagJobAdmissionResult {
        Objects.requireNonNull(jobId, "jobId must not be null");
        Objects.requireNonNull(attemptId, "attemptId must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(leaseToken, "leaseToken must not be null");
        Objects.requireNonNull(leasedUntil, "leasedUntil must not be null");
    }

    @Override
    public String toString() {
        return "RagJobAdmissionResult[jobId=" + jobId + ", attemptId=" + attemptId + ", sourceId=" + sourceId
                + ", leaseToken=[PROTECTED], leasedUntil=" + leasedUntil + ", authorizationEpoch=" + authorizationEpoch + "]";
    }
}
