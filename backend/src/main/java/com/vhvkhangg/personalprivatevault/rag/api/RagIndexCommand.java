package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.Map;

/** Manual start intent, including optional exact changed-description confirmations per source. */
public record RagIndexCommand(long expectedSourceVersion, long expectedAuthorizationEpoch,
        int expectedFieldPolicyVersion, Map<Long, String> descriptionConfirmations) {
    public RagIndexCommand {
        if (expectedSourceVersion <= 0 || expectedAuthorizationEpoch <= 0 || expectedFieldPolicyVersion <= 0)
            throw new IllegalArgumentException("Explicit source and policy versions are required");
        descriptionConfirmations = descriptionConfirmations == null ? Map.of() : Map.copyOf(descriptionConfirmations);
    }
}
