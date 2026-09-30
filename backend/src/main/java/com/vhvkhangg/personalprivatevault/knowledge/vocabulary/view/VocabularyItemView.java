package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view;

import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.VocabularyLearningStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable view of a vocabulary item.
 */
public record VocabularyItemView(
        Long id,
        String word,
        String languageCode,
        String meaning,
        String example,
        String pronunciation,
        String ipa,
        String partOfSpeech,
        String sourceName,
        String sourceUrl,
        VocabularyLearningStatus learningStatus,
        Instant nextReviewAt,
        int intervalDays,
        BigDecimal easeFactor,
        int repetitionCount,
        int lapseCount
) {}
