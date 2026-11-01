package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.SavedResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link SavedResource}.
 */
public interface SavedResourceRepository extends JpaRepository<SavedResource, Long> {

    /**
     * Non-entity projection for candidate keyset scanning without entity persistence-context retention.
     */
    interface CandidateSummary {
        Long getId();
        Instant getSavedAt();
    }

    Optional<SavedResource> findByResourceUrlHash(String resourceUrlHash);

    @Query("SELECT r FROM SavedResource r ORDER BY r.savedAt DESC, r.id DESC")
    List<SavedResource> findRecent(Pageable pageable);

    @Query("SELECT r.id AS id, r.savedAt AS savedAt FROM SavedResource r ORDER BY r.savedAt DESC, r.id DESC")
    List<CandidateSummary> findCandidates(Pageable pageable);

    @Query("SELECT r.id AS id, r.savedAt AS savedAt FROM SavedResource r WHERE r.kind = :kind ORDER BY r.savedAt DESC, r.id DESC")
    List<CandidateSummary> findCandidatesByKind(@Param("kind") SavedResourceKind kind, Pageable pageable);

    @Query("SELECT r.id AS id, r.savedAt AS savedAt FROM SavedResource r " +
            "WHERE (r.savedAt < :savedAt OR (r.savedAt = :savedAt AND r.id < :id)) " +
            "ORDER BY r.savedAt DESC, r.id DESC")
    List<CandidateSummary> findCandidatesAfter(
            @Param("savedAt") Instant savedAt,
            @Param("id") Long id,
            Pageable pageable);

    @Query("SELECT r.id AS id, r.savedAt AS savedAt FROM SavedResource r " +
            "WHERE r.kind = :kind " +
            "AND (r.savedAt < :savedAt OR (r.savedAt = :savedAt AND r.id < :id)) " +
            "ORDER BY r.savedAt DESC, r.id DESC")
    List<CandidateSummary> findCandidatesByKindAfter(
            @Param("kind") SavedResourceKind kind,
            @Param("savedAt") Instant savedAt,
            @Param("id") Long id,
            Pageable pageable);
}
