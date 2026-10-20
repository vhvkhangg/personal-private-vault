package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import java.util.Set;

public record ApproveSourceRequest(
        @jakarta.validation.constraints.NotEmpty Set<String> approvedFields,
        @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive Long expectedSourceVersion,
        @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive Long expectedConsentResetVersion,
        @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive Long expectedAuthorizationEpoch,
        @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive Integer expectedFieldPolicyVersion,
        String confirmedDescriptionHash,
        @jakarta.validation.Valid com.vhvkhangg.personalprivatevault.rag.source.RagSnapshotExpectation expectedSnapshot
) {}
