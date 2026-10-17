package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDomainSourceRequest(
        @NotNull(message = "sourceType must not be null")
        RagSourceType sourceType,

        @NotBlank(message = "sourceId must not be blank")
        String sourceId,
        @NotNull com.vhvkhangg.personalprivatevault.rag.source.RagClassification classification,
        Long expectedAuthorizationEpoch,
        Integer expectedFieldPolicyVersion
) {}
