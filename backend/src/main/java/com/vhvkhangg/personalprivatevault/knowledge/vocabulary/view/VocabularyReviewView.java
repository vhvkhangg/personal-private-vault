package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view;

import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.SrsReviewResponse;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable view of a vocabulary review history entry.
 */
public record VocabularyReviewView(
        Long id,
        Long vocabularyId,
        SrsReviewResponse response,
        Instant reviewedAt,
        Integer previousIntervalDays,
        Integer newIntervalDays,
        BigDecimal previousEaseFactor,
        BigDecimal newEaseFactor,
        Instant nextReviewAt
) {}
