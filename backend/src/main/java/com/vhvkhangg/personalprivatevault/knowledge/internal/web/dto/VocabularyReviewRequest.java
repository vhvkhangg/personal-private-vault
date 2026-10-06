package com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto;

import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeSrsReviewResponse;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyLearningStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record VocabularyReviewRequest(
        @NotNull(message = "Review response must not be null")
        KnowledgeSrsReviewResponse response,
        KnowledgeVocabularyLearningStatus learningStatus,
        Instant nextReviewAt,
        Integer newIntervalDays,
        BigDecimal newEaseFactor,
        Integer repetitionCount,
        Integer lapseCount,
        Instant reviewedAt
) {
}
