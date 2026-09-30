package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.VocabularyLearningStatus;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.domain.VocabularyItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VocabularyItemRepository extends JpaRepository<VocabularyItem, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VocabularyItem v WHERE v.id = :id")
    Optional<VocabularyItem> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT v FROM VocabularyItem v " +
            "WHERE v.learningStatus <> :excludedStatus " +
            "AND ((v.nextReviewAt IS NOT NULL AND v.nextReviewAt <= :cutoff) " +
            "     OR (v.learningStatus = :newStatus AND v.nextReviewAt IS NULL)) " +
            "ORDER BY " +
            "CASE WHEN v.nextReviewAt IS NOT NULL THEN 0 ELSE 1 END ASC, " +
            "v.nextReviewAt ASC, " +
            "v.id ASC")
    List<VocabularyItem> findDueItems(
            @Param("excludedStatus") VocabularyLearningStatus excludedStatus,
            @Param("newStatus") VocabularyLearningStatus newStatus,
            @Param("cutoff") Instant cutoff,
            Pageable pageable
    );
}
