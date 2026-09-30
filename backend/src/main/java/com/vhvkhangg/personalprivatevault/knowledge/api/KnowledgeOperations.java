package com.vhvkhangg.personalprivatevault.knowledge.api;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Public stable facade contract for the Knowledge parent module.
 *
 * <p>Exposes operations across nested Study, Information, Vocabulary, and Note modules
 * using parent-owned types only, keeping callers isolated from nested module internals.</p>
 */
public interface KnowledgeOperations {

    // Study operations
    KnowledgeStudyItemView createStudyItem(CreateKnowledgeStudyItemCommand command);

    KnowledgeStudyItemView updateStudyItem(Long id, UpdateKnowledgeStudyItemCommand command);

    Optional<KnowledgeStudyItemView> findStudyItemById(Long id);

    // Information operations
    KnowledgeInformationItemView createInformationItem(CreateKnowledgeInformationItemCommand command);

    KnowledgeInformationItemView updateInformationItem(Long id, UpdateKnowledgeInformationItemCommand command);

    Optional<KnowledgeInformationItemView> findInformationItemById(Long id);

    // Vocabulary operations
    KnowledgeVocabularyItemView createVocabularyItem(CreateKnowledgeVocabularyItemCommand command);

    KnowledgeVocabularyItemView updateVocabularyItem(Long id, UpdateKnowledgeVocabularyItemCommand command);

    Optional<KnowledgeVocabularyItemView> findVocabularyItemById(Long id);

    KnowledgeVocabularyReviewTransitionResultView reviewVocabularyItem(Long id, KnowledgeVocabularyReviewTransitionCommand command);

    List<KnowledgeVocabularyItemView> findDueVocabularyItems(Instant cutoff, int limit);

    List<KnowledgeVocabularyReviewView> findVocabularyReviews(Long vocabularyId, int limit);

    // Note operations
    KnowledgeNoteView createNote(CreateKnowledgeNoteCommand command);

    KnowledgeNoteView updateNote(Long id, UpdateKnowledgeNoteCommand command);

    Optional<KnowledgeNoteView> findNoteById(Long id);

    Optional<KnowledgeNoteView> findNoteByImportedFileHash(String importedFileHash);
}
