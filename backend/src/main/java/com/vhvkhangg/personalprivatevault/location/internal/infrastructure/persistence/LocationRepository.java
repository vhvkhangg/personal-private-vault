package com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.location.internal.domain.Location;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Location l WHERE l.id = :id")
    Optional<Location> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT l FROM Location l WHERE l.id = :id")
    Optional<Location> findByIdForShare(@Param("id") Long id);
}
