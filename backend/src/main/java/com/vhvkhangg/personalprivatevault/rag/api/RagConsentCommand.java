package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.Objects;
import java.util.Set;

/** Explicit owner intent from the versioned preview, never silently refreshed by the server. */
public record RagConsentCommand(Set<String> approvedFields, long expectedSourceVersion,
        long expectedConsentResetVersion, long expectedAuthorizationEpoch,
        int expectedFieldPolicyVersion, String confirmedDescriptionHash,
        com.vhvkhangg.personalprivatevault.rag.source.RagSnapshotExpectation expectedSnapshot) {
    public RagConsentCommand(Set<String> approvedFields, long expectedSourceVersion,
            long expectedConsentResetVersion, long expectedAuthorizationEpoch,
            int expectedFieldPolicyVersion, String confirmedDescriptionHash) {
        this(approvedFields, expectedSourceVersion, expectedConsentResetVersion, expectedAuthorizationEpoch,
                expectedFieldPolicyVersion, confirmedDescriptionHash, null);
    }
    public RagConsentCommand {
        approvedFields = Set.copyOf(Objects.requireNonNull(approvedFields));
        if (approvedFields.isEmpty() || expectedSourceVersion <= 0 || expectedConsentResetVersion <= 0
                || expectedAuthorizationEpoch <= 0 || expectedFieldPolicyVersion <= 0) {
            throw new IllegalArgumentException("Explicit source and consent versions are required");
        }
    }
}
