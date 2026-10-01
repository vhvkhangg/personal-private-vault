package com.vhvkhangg.personalprivatevault.importdata.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.importdata.internal.domain.ImportJobItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for {@link ImportJobItem}.
 */
public interface ImportJobItemRepository extends JpaRepository<ImportJobItem, Long> {

    List<ImportJobItem> findByImportJobIdOrderByItemIndexAsc(Long importJobId, Pageable pageable);

    List<ImportJobItem> findByImportJobIdOrderByItemIndexAsc(Long importJobId);

    @Modifying
    @Query("DELETE FROM ImportJobItem i WHERE i.importJobId = :importJobId")
    void deleteByImportJobId(@Param("importJobId") Long importJobId);
}
