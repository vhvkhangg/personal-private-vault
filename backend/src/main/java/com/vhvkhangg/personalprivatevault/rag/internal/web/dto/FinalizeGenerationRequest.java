package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FinalizeGenerationRequest(
        @NotNull(message = "attemptId must not be null")
        UUID attemptId,

        String dependencyDigest
) {}
