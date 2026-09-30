package com.vhvkhangg.personalprivatevault.knowledge.api;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable public view of a vocabulary review history entry exposed by the parent Knowledge API.
 */
public record KnowledgeVocabularyReviewView(
        Long id,
        Long vocabularyId,
        KnowledgeSrsReviewResponse response,
        Instant reviewedAt,
        Integer previousIntervalDays,
        Integer newIntervalDays,
        BigDecimal previousEaseFactor,
        BigDecimal newEaseFactor,
        Instant nextReviewAt
) {}
