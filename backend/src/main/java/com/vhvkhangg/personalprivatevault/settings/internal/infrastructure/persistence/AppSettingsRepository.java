package com.vhvkhangg.personalprivatevault.settings.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.settings.internal.domain.AppSettings;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppSettingsRepository extends JpaRepository<AppSettings, Short> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AppSettings s WHERE s.id = :id")
    Optional<AppSettings> findByIdForUpdate(@Param("id") Short id);
}
