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
        long authorizationEpoch,
        UUID operationId,
        String operationDisposition
) {
    public RagJobAdmissionResult(UUID jobId,UUID attemptId,Long sourceId,String token,Instant until,long epoch) {this(jobId,attemptId,sourceId,token,until,epoch,null,null);}

    public RagJobAdmissionResult {
        if(operationId==null) {
        Objects.requireNonNull(jobId, "jobId must not be null");
        Objects.requireNonNull(attemptId, "attemptId must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(leaseToken, "leaseToken must not be null");
        Objects.requireNonNull(leasedUntil, "leasedUntil must not be null");
        } else if(jobId!=null || attemptId!=null || leaseToken!=null || leasedUntil!=null || !"NO_CHANGE".equals(operationDisposition))throw new IllegalArgumentException("Nonpublication cannot carry an indexing lease");
    }

    @Override
    public String toString() {
        return "RagJobAdmissionResult[jobId=" + jobId + ", attemptId=" + attemptId + ", sourceId=" + sourceId
                + ", leaseToken=[PROTECTED], leasedUntil=" + leasedUntil + ", authorizationEpoch=" + authorizationEpoch + "]";
    }
}
