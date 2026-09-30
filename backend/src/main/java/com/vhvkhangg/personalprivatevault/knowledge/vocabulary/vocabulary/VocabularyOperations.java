package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary;

import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyReviewTransitionResultView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyReviewView;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Public capability-oriented contract for Vocabulary operations.
 */
public interface VocabularyOperations {

    VocabularyItemView create(CreateVocabularyItemCommand command);

    VocabularyItemView update(Long id, UpdateVocabularyItemCommand command);

    Optional<VocabularyItemView> findById(Long id);

    VocabularyReviewTransitionResultView reviewTransition(Long id, VocabularyReviewTransitionCommand command);

    List<VocabularyItemView> findDue(Instant cutoff, int limit);

    List<VocabularyReviewView> findReviewsByVocabularyId(Long vocabularyId, int limit);
}
