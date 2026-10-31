package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Command to execute a grounded question-answering request over permitted Markdown evidence.
 */
public record RagQaCommand(
        UUID requestId,
        String question,
        RagRetrievalMode mode,
        List<RagSourceSelectionEntry> selection,
        String rosterVersion,
        String collectionId,
        Integer topK,
        String responseLanguage,
        List<RagQaHistoryEntry> history
) {
    public RagQaCommand {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(question, "question must not be null");
        if (mode == null) {
            mode = RagRetrievalMode.GENERIC;
        }
        selection = selection == null ? null : List.copyOf(selection);
        if (topK == null) {
            topK = 5;
        }
        history = history == null ? List.of() : List.copyOf(history);
    }

    public RagQaCommand(
            UUID requestId,
            String question,
            RagRetrievalMode mode,
            List<RagSourceSelectionEntry> selection,
            String rosterVersion,
            String collectionId,
            Integer topK,
            List<RagQaHistoryEntry> history,
            String responseLanguage
    ) {
        this(requestId, question, mode, selection, rosterVersion, collectionId, topK, responseLanguage, history);
    }

    public RagRetrievalCommand toRetrievalCommand() {
        return new RagRetrievalCommand(
                requestId,
                question,
                mode,
                selection,
                rosterVersion,
                collectionId,
                topK
        );
    }

    @Override
    public String toString() {
        return "RagQaCommand[requestId=" + requestId
                + ", question=[REDACTED]"
                + ", mode=" + mode
                + ", selection=" + selection
                + ", rosterVersion=" + rosterVersion
                + ", collectionId=" + collectionId
                + ", topK=" + topK
                + ", responseLanguage=" + responseLanguage
                + ", historyCount=" + history.size() + "]";
    }
}
