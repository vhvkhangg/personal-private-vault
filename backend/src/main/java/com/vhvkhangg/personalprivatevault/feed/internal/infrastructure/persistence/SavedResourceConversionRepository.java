package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.feed.internal.domain.SavedResourceConversion;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.SavedResourceConversionId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for {@link SavedResourceConversion}.
 */
public interface SavedResourceConversionRepository extends JpaRepository<SavedResourceConversion, SavedResourceConversionId> {

    @Query("SELECT c FROM SavedResourceConversion c " +
            "WHERE c.id.savedResourceId = :savedResourceId " +
            "ORDER BY c.createdAt DESC, c.id.targetVaultEntryId DESC")
    List<SavedResourceConversion> findBySavedResourceId(@Param("savedResourceId") Long savedResourceId, Pageable pageable);
}
