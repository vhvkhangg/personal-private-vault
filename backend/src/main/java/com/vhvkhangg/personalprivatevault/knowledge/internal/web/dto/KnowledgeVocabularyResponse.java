package com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto;

import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyLearningStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record KnowledgeVocabularyResponse(
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
) {
}
