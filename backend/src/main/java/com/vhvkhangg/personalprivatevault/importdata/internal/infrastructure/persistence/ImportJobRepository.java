package com.vhvkhangg.personalprivatevault.importdata.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.importdata.internal.domain.ImportJob;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link ImportJob}.
 */
public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM ImportJob j WHERE j.id = :id")
    Optional<ImportJob> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT j FROM ImportJob j ORDER BY j.createdAt DESC, j.id DESC")
    List<ImportJob> findRecent(Pageable pageable);
}
