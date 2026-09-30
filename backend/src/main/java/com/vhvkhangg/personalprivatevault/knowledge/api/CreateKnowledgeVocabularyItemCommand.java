package com.vhvkhangg.personalprivatevault.knowledge.api;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Command for creating a vocabulary item via the parent Knowledge API.
 */
public record CreateKnowledgeVocabularyItemCommand(
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
        Integer intervalDays,
        BigDecimal easeFactor,
        Integer repetitionCount,
        Integer lapseCount
) {}
