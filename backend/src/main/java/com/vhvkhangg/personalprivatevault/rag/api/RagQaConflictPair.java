package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.Objects;

/**
 * Representation of two conflicting claims originating from distinct permitted evidence sources.
 */
public record RagQaConflictPair(
        String claimIdA,
        String claimIdB,
        String description
) {
    public RagQaConflictPair {
        Objects.requireNonNull(claimIdA, "claimIdA must not be null");
        Objects.requireNonNull(claimIdB, "claimIdB must not be null");
        Objects.requireNonNull(description, "description must not be null");
    }

    @Override
    public String toString() {
        return "RagQaConflictPair[claimIdA=" + claimIdA + ", claimIdB=" + claimIdB + ", description=[REDACTED]]";
    }
}
