package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import jakarta.validation.constraints.NotNull;

public record AdmitJobRequest(
        @NotNull(message = "sourceId must not be null")
        Long sourceId,
        @NotNull @jakarta.validation.constraints.Positive Long expectedSourceVersion,
        @NotNull @jakarta.validation.constraints.Positive Long expectedAuthorizationEpoch,
        @NotNull @jakarta.validation.constraints.Positive Integer expectedFieldPolicyVersion,
        java.util.Map<Long, String> descriptionConfirmations
) {}
