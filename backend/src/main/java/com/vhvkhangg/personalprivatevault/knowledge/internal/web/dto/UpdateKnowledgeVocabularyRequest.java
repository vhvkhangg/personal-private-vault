package com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto;

import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyLearningStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateKnowledgeVocabularyRequest(
        @NotBlank(message = "Word must not be blank")
        @Size(max = 255, message = "Word must not exceed 255 characters")
        String word,
        @NotBlank(message = "Language code must not be blank")
        @Size(max = 10, message = "Language code must not exceed 10 characters")
        String languageCode,
        @NotBlank(message = "Meaning must not be blank")
        String meaning,
        String example,
        @Size(max = 255, message = "Pronunciation must not exceed 255 characters")
        String pronunciation,
        @Size(max = 255, message = "IPA must not exceed 255 characters")
        String ipa,
        @Size(max = 50, message = "Part of speech must not exceed 50 characters")
        String partOfSpeech,
        @Size(max = 255, message = "Source name must not exceed 255 characters")
        String sourceName,
        @Size(max = 2048, message = "Source URL must not exceed 2048 characters")
        String sourceUrl,
        KnowledgeVocabularyLearningStatus learningStatus,
        Instant nextReviewAt,
        Integer intervalDays,
        BigDecimal easeFactor,
        Integer repetitionCount,
        Integer lapseCount
) {
}
