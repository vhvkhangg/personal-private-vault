package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Grounded QA response containing validated claims, citations, and server-rendered answer.
 */
public record RagQaResponse(
        UUID requestId,
        RagQaOutcome outcome,
        String answer,
        List<RagQaClaim> claims,
        List<String> unansweredParts,
        List<RagQaConflictPair> conflictPairs,
        List<RagRetrievalMatch> citations
) {
    public RagQaResponse {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(outcome, "outcome must not be null");
        Objects.requireNonNull(answer, "answer must not be null");
        claims = claims == null ? List.of() : List.copyOf(claims);
        unansweredParts = unansweredParts == null ? List.of() : List.copyOf(unansweredParts);
        conflictPairs = conflictPairs == null ? List.of() : List.copyOf(conflictPairs);
        citations = citations == null ? List.of() : List.copyOf(citations);
    }

    @Override
    public String toString() {
        return "RagQaResponse[requestId=" + requestId
                + ", outcome=" + outcome
                + ", answer=[REDACTED]"
                + ", claimsCount=" + claims.size()
                + ", unansweredPartsCount=" + unansweredParts.size()
                + ", conflictPairsCount=" + conflictPairs.size()
                + ", citationsCount=" + citations.size() + "]";
    }
}
