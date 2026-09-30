package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view;

/**
 * Result of an atomic vocabulary review transition, containing both the updated item state
 * and the generated review history row.
 */
public record VocabularyReviewTransitionResultView(
        VocabularyItemView item,
        VocabularyReviewView review
) {}
