package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.Objects;

/**
 * Single turn in client-supplied ephemeral conversation history.
 */
public record RagQaHistoryEntry(
        String role,
        String content
) {
    public RagQaHistoryEntry {
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(content, "content must not be null");
    }

    @Override
    public String toString() {
        return "RagQaHistoryEntry[role=" + role + ", content=[REDACTED]]";
    }
}
