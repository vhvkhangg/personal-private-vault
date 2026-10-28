package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.Objects;
import java.util.UUID;

public record RagRetrievalMatch(
        String citationKey,
        String excerpt,
        Long sourceId,
        UUID fileSourceId,
        UUID selectedRevisionId,
        UUID generationId,
        String displayName,
        RagLocator locator,
        String group,
        String freshnessBasis,
        String snapshotIndexState,
        String originalFreshness
) {
    public RagRetrievalMatch {
        Objects.requireNonNull(citationKey, "citationKey must not be null");
        Objects.requireNonNull(excerpt, "excerpt must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(fileSourceId, "fileSourceId must not be null");
        Objects.requireNonNull(selectedRevisionId, "selectedRevisionId must not be null");
        Objects.requireNonNull(generationId, "generationId must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        Objects.requireNonNull(freshnessBasis, "freshnessBasis must not be null");
        Objects.requireNonNull(snapshotIndexState, "snapshotIndexState must not be null");
        Objects.requireNonNull(originalFreshness, "originalFreshness must not be null");
    }
}
