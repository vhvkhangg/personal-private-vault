package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FinalizeDocumentBatchRequest(
        @NotNull UUID attemptId,
        String dependencyDigest
) {
}
