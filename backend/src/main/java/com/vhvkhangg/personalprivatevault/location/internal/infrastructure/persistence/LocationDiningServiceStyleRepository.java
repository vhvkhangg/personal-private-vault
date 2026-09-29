package com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.location.enums.DiningServiceStyle;
import com.vhvkhangg.personalprivatevault.location.internal.domain.LocationDiningServiceStyleAssignment;
import com.vhvkhangg.personalprivatevault.location.internal.domain.LocationDiningServiceStyleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface LocationDiningServiceStyleRepository extends JpaRepository<LocationDiningServiceStyleAssignment, LocationDiningServiceStyleId> {

    @Query("SELECT a.id.serviceStyle FROM LocationDiningServiceStyleAssignment a WHERE a.id.locationId = :locationId")
    Set<DiningServiceStyle> findServiceStylesByLocationId(@Param("locationId") Long locationId);

    @Modifying(flushAutomatically = true)
    @Query(value = "INSERT INTO location_dining_service_styles (location_id, service_style) " +
            "VALUES (:locationId, CAST(:serviceStyle AS dining_service_style)) " +
            "ON CONFLICT (location_id, service_style) DO NOTHING",
            nativeQuery = true)
    void insertIfAbsent(@Param("locationId") Long locationId, @Param("serviceStyle") String serviceStyle);
}
