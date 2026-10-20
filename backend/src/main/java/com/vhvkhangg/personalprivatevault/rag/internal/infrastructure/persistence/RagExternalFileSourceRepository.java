package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagExternalFileSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RagExternalFileSourceRepository extends JpaRepository<RagExternalFileSource, Long> {

    Optional<RagExternalFileSource> findByFileSourceId(UUID fileSourceId);

    Optional<RagExternalFileSource> findByCanonicalIdentityDigest(String canonicalIdentityDigest);

    @Query("SELECT s FROM RagExternalFileSource s WHERE s.fileSourceId IN :ids ORDER BY s.fileSourceId ASC")
    List<RagExternalFileSource> findAllByFileSourceIdInSorted(@Param("ids") Collection<UUID> ids);

    @Query("SELECT s.selectedRevisionId FROM RagExternalFileSource s WHERE s.selectedRevisionId IS NOT NULL")
    List<UUID> findAllSelectedRevisionIds();

    @Query("SELECT s.pendingRevisionId FROM RagExternalFileSource s WHERE s.pendingRevisionId IS NOT NULL")
    List<UUID> findAllPendingRevisionIds();
}
