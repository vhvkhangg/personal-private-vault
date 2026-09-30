package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary;

import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.SrsReviewResponse;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.VocabularyLearningStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Command specifying an explicit atomic SRS review transition.
 */
public record VocabularyReviewTransitionCommand(
        SrsReviewResponse response,
        VocabularyLearningStatus learningStatus,
        Instant nextReviewAt,
        Integer newIntervalDays,
        BigDecimal newEaseFactor,
        Integer repetitionCount,
        Integer lapseCount,
        Instant reviewedAt
) {}
