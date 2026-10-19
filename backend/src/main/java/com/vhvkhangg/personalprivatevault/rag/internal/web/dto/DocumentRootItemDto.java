package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record DocumentRootItemDto(
        @NotBlank String rootKey,
        @NotNull RagSourceType sourceType,
        @NotBlank String sourceId,
        Long expectedSourceVersion,
        Long expectedAuthorizationEpoch,
        Integer expectedFieldPolicyVersion,
        Set<String> approvedFields
) {
}
