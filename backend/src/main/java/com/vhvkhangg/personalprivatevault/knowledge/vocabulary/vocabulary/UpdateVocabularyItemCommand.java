package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary;

import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.VocabularyLearningStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Command for updating an existing vocabulary item.
 */
public record UpdateVocabularyItemCommand(
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
        Integer intervalDays,
        BigDecimal easeFactor,
        Integer repetitionCount,
        Integer lapseCount
) {}
