package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record RagRetrievalCommand(
        UUID requestId,
        String question,
        RagRetrievalMode mode,
        List<RagSourceSelectionEntry> selection,
        String rosterVersion,
        String collectionId,
        Integer topK
) {
    public RagRetrievalCommand {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(question, "question must not be null");
        if (mode == null) {
            mode = RagRetrievalMode.GENERIC;
        }
        if (selection != null) {
            selection = List.copyOf(selection);
        }
        if (topK == null) {
            topK = 5;
        }
    }

    @Override
    public String toString() {
        return "RagRetrievalCommand[requestId=" + requestId
                + ", question=[REDACTED]"
                + ", mode=" + mode
                + ", selection=" + selection
                + ", rosterVersion=" + rosterVersion
                + ", collectionId=" + collectionId
                + ", topK=" + topK + "]";
    }
}
