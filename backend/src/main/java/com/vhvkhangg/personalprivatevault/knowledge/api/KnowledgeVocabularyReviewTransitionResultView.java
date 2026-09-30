package com.vhvkhangg.personalprivatevault.knowledge.api;

/**
 * Result of an atomic vocabulary review transition exposed by the parent Knowledge API.
 */
public record KnowledgeVocabularyReviewTransitionResultView(
        KnowledgeVocabularyItemView item,
        KnowledgeVocabularyReviewView review
) {}
