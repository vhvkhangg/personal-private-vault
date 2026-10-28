package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.Objects;
import java.util.UUID;

public record RagSourceSelectionEntry(
        Long sourceId,
        UUID selectedRevisionId,
        String group,
        String relativePath
) {
    public RagSourceSelectionEntry {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(selectedRevisionId, "selectedRevisionId must not be null");
    }
}
