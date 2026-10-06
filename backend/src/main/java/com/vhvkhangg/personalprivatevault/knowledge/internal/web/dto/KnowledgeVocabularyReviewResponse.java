package com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto;

import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeSrsReviewResponse;

import java.math.BigDecimal;
import java.time.Instant;

public record KnowledgeVocabularyReviewResponse(
        Long id,
        Long vocabularyId,
        KnowledgeSrsReviewResponse response,
        Instant reviewedAt,
        Integer previousIntervalDays,
        Integer newIntervalDays,
        BigDecimal previousEaseFactor,
        BigDecimal newEaseFactor,
        Instant nextReviewAt
) {
}
