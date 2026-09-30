package com.vhvkhangg.personalprivatevault.collection.software.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.collection.software.internal.domain.SoftwareItemPlatform;
import com.vhvkhangg.personalprivatevault.collection.software.internal.domain.SoftwareItemPlatformId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link SoftwareItemPlatform}.
 */
@Repository
public interface SoftwareItemPlatformRepository extends JpaRepository<SoftwareItemPlatform, SoftwareItemPlatformId> {

    @Query("SELECT p FROM SoftwareItemPlatform p WHERE p.id.softwareId = :softwareId ORDER BY p.id.platformId ASC")
    List<SoftwareItemPlatform> findPlatformsBySoftwareId(@Param("softwareId") Long softwareId, Pageable pageable);

    @Modifying(flushAutomatically = true)
    @Query(value = "INSERT INTO software_item_platforms (software_id, platform_id) " +
            "VALUES (:softwareId, :platformId) " +
            "ON CONFLICT (software_id, platform_id) DO NOTHING",
            nativeQuery = true)
    void insertIfAbsent(@Param("softwareId") Long softwareId, @Param("platformId") Long platformId);
}
