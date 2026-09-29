package com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.location.internal.domain.LocationBusinessHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationBusinessHourRepository extends JpaRepository<LocationBusinessHour, Long> {

    List<LocationBusinessHour> findByLocationIdOrderByDayOfWeekAscSequenceAsc(Long locationId);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM LocationBusinessHour h WHERE h.locationId = :locationId")
    void deleteByLocationId(@Param("locationId") Long locationId);
}
