package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.List;
import java.util.Objects;

/**
 * Individual factual claim grounded in cited evidence excerpts.
 */
public record RagQaClaim(
        String id,
        String text,
        List<RagQaClaimSupport> supports
) {
    public RagQaClaim {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(text, "text must not be null");
        supports = supports == null ? List.of() : List.copyOf(supports);
    }

    @Override
    public String toString() {
        return "RagQaClaim[id=" + id + ", text=[REDACTED], supportsCount=" + supports.size() + "]";
    }
}
