package com.vhvkhangg.personalprivatevault.knowledge.api;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Command for an explicit atomic review transition via the parent Knowledge API.
 */
public record KnowledgeVocabularyReviewTransitionCommand(
        KnowledgeSrsReviewResponse response,
        KnowledgeVocabularyLearningStatus learningStatus,
        Instant nextReviewAt,
        Integer newIntervalDays,
        BigDecimal newEaseFactor,
        Integer repetitionCount,
        Integer lapseCount,
        Instant reviewedAt
) {}
