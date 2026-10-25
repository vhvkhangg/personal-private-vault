package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.List;
import java.util.UUID;

/**
 * Sanitized per-file outcome view for document indexing batch (ADR-0021 Clause 6, 7; ADR-0022 Clause 5).
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
        String currentIndexState,
        String freshnessBasis,
        String snapshotIndexState,
        String originalFreshness,
        boolean publishedNewGeneration,
        UUID selectedRevisionId,
        UUID pendingRevisionId
) {
    public RagFileOutcomeView {
        warningCodes = warningCodes != null ? List.copyOf(warningCodes) : List.of();
    }

    public RagFileOutcomeView(
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
        this(rootKey, result, candidateGenerationId, warningCodes, chunkCount, pageCount,
                errorCode, errorMessage, currentIndexState, null, null, null, false, null, null);
    }
}
