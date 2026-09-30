package com.vhvkhangg.personalprivatevault.knowledge.api;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable public view of a vocabulary item exposed by the parent Knowledge API.
 */
public record KnowledgeVocabularyItemView(
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
        KnowledgeVocabularyLearningStatus learningStatus,
        Instant nextReviewAt,
        int intervalDays,
        BigDecimal easeFactor,
        int repetitionCount,
        int lapseCount
) {}
