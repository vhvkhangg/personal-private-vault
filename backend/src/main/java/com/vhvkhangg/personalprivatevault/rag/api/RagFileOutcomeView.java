package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.List;
import java.util.UUID;

/**
 * Sanitized per-file outcome view for document indexing batch (ADR-0021 Clause 6, 7).
 */
public record RagFileOutcomeView(
        String rootKey,
        String result,
        UUID candidateGenerationId,
        List<String> warningCodes,
        int chunkCount,
        int pageCount,
        String errorCode,
        String errorMessage,
        String currentIndexState
) {
    public RagFileOutcomeView {
        warningCodes = warningCodes != null ? List.copyOf(warningCodes) : List.of();
    }
}
