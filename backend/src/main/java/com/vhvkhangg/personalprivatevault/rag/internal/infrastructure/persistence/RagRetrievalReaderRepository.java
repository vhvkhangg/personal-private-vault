package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagRetrievalReader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RagRetrievalReaderRepository extends JpaRepository<RagRetrievalReader, UUID> {

    Optional<RagRetrievalReader> findByReaderId(UUID readerId);

    Optional<RagRetrievalReader> findByReservationToken(UUID reservationToken);

    @Query("SELECT r FROM RagRetrievalReader r WHERE r.state <> 'TERMINAL'")
    Optional<RagRetrievalReader> findActiveReader();

    @Query("SELECT COUNT(r) FROM RagRetrievalReader r WHERE r.state <> 'TERMINAL'")
    long countActiveReaders();
}
