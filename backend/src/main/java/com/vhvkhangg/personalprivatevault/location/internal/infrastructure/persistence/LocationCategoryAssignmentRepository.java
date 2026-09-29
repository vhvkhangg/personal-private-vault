package com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.location.internal.domain.LocationCategoryAssignment;
import com.vhvkhangg.personalprivatevault.location.internal.domain.LocationCategoryAssignmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationCategoryAssignmentRepository extends JpaRepository<LocationCategoryAssignment, LocationCategoryAssignmentId> {

    @Query("SELECT a.id.categoryId FROM LocationCategoryAssignment a WHERE a.id.locationId = :locationId")
    List<Long> findCategoryIdsByLocationId(@Param("locationId") Long locationId);

    @Modifying(flushAutomatically = true)
    @Query(value = "INSERT INTO location_category_assignments (location_id, category_id) VALUES (:locationId, :categoryId) ON CONFLICT (location_id, category_id) DO NOTHING", nativeQuery = true)
    void insertIfAbsent(@Param("locationId") Long locationId, @Param("categoryId") Long categoryId);
}
