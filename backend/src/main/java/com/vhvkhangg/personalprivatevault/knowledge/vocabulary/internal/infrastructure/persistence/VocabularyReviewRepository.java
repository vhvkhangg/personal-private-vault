package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.domain.VocabularyReview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VocabularyReviewRepository extends JpaRepository<VocabularyReview, Long> {

    @Query("SELECT r FROM VocabularyReview r WHERE r.vocabularyId = :vocabularyId ORDER BY r.reviewedAt DESC, r.id DESC")
    List<VocabularyReview> findByVocabularyIdOrderByReviewedAtDesc(@Param("vocabularyId") Long vocabularyId, Pageable pageable);
}
