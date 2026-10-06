package com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto;

public record VocabularyReviewResultResponse(
        KnowledgeVocabularyResponse item,
        KnowledgeVocabularyReviewResponse review
) {
}
