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

    Optional<RagSource> findBySourceTypeAndSourceId(RagSourceType sourceType, String sourceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM RagSource s WHERE s.id = :id")
    Optional<RagSource> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM RagSource s WHERE s.id IN :ids ORDER BY s.id ASC")
    List<RagSource> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);
}
