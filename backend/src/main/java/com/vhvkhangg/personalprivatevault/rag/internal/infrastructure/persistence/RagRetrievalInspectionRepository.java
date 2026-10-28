package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagRetrievalInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RagRetrievalInspectionRepository extends JpaRepository<RagRetrievalInspection, UUID> {

    Optional<RagRetrievalInspection> findByInspectionId(UUID inspectionId);

    List<RagRetrievalInspection> findByReaderId(UUID readerId);

    List<RagRetrievalInspection> findByAffectedFileSourceId(UUID affectedFileSourceId);

    @Query("SELECT i FROM RagRetrievalInspection i WHERE i.state IN ('UNRESOLVED', 'INCIDENT_PENDING', 'DENIED') OR i.denyNewAdmission = true")
    List<RagRetrievalInspection> findBlockingInspections();

    @Query("SELECT COUNT(i) FROM RagRetrievalInspection i WHERE (i.state IN ('UNRESOLVED', 'INCIDENT_PENDING', 'DENIED') OR i.denyNewAdmission = true) AND i.affectedFileSourceId IN :fileSourceIds")
    long countBlockingByFileSourceIds(@Param("fileSourceIds") Collection<UUID> fileSourceIds);

    @Query("SELECT i FROM RagRetrievalInspection i WHERE (i.state IN ('UNRESOLVED', 'INCIDENT_PENDING', 'DENIED') OR i.denyNewAdmission = true) AND i.affectedFileSourceId IN :fileSourceIds")
    List<RagRetrievalInspection> findBlockingByFileSourceIds(@Param("fileSourceIds") Collection<UUID> fileSourceIds);
}
