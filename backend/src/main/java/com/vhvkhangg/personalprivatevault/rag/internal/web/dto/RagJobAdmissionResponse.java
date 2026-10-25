package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import java.time.Instant;
import java.util.UUID;

public record RagJobAdmissionResponse(
        UUID jobId,
        UUID attemptId,
        Long sourceId,
        String leaseToken,
        Instant leasedUntil,
        long authorizationEpoch,
        UUID operationId,
        String operationDisposition
) {
    public RagJobAdmissionResponse(UUID jobId,UUID attemptId,Long sourceId,String token,Instant until,long epoch) {this(jobId,attemptId,sourceId,token,until,epoch,null,null);}
    @Override
    public String toString() {
        return "RagJobAdmissionResponse[jobId=" + jobId + ", attemptId=" + attemptId + ", sourceId=" + sourceId
                + ", leaseToken=[PROTECTED], leasedUntil=" + leasedUntil + ", authorizationEpoch=" + authorizationEpoch + "]";
    }
}
