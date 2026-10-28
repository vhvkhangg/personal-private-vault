package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagSource;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RagSourceRepository extends JpaRepository<RagSource, Long> {

    /** Keyset metadata scan; unindexed approved rows never consume the query-scope cap. */
    @Query(value = """
            SELECT s.* FROM rag.sources s
            JOIN rag.external_file_sources e ON e.file_source_id::text=s.source_id
            WHERE s.id>:afterId AND s.source_type='EXTERNAL_FILE' AND s.access_state='APPROVED'
              AND s.classification='NON_PRIVATE' AND s.file_identity IS NOT NULL
              AND e.eligible AND NOT e.snapshot_integrity_failed AND e.selected_revision_id IS NOT NULL
              AND s.approved_owner_reset_version=e.consent_reset_version
              AND EXISTS(SELECT 1 FROM rag.index_generations g JOIN rag.job_leases l ON l.job_id=g.job_id
                  WHERE g.source_id=s.id AND g.is_active AND g.status='PUBLISHED'
                    AND l.status='COMPLETED' AND l.staging_closed)
            ORDER BY s.id
            """, nativeQuery = true)
    List<RagSource> findRetrievalCandidates(@Param("afterId") long afterId,
            org.springframework.data.domain.Pageable pageable);

    Optional<RagSource> findBySourceTypeAndSourceId(RagSourceType sourceType, String sourceId);

    List<RagSource> findBySourceTypeAndAccessState(RagSourceType sourceType, com.vhvkhangg.personalprivatevault.rag.source.RagAccessState accessState);

    List<RagSource> findBySourceTypeAndAccessState(
            RagSourceType sourceType,
            com.vhvkhangg.personalprivatevault.rag.source.RagAccessState accessState,
            org.springframework.data.domain.Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM RagSource s WHERE s.id = :id")
    Optional<RagSource> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM RagSource s WHERE s.id IN :ids ORDER BY s.id ASC")
    List<RagSource> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);
}
