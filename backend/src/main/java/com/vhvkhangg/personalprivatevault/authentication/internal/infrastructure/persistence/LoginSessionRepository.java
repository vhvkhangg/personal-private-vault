package com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoginSessionRepository extends JpaRepository<LoginSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM LoginSession s WHERE s.id = :id")
    Optional<LoginSession> findByIdForUpdate(@Param("id") UUID id);

    default Optional<LoginSession> findByIdWithLock(UUID id) {
        return findByIdForUpdate(id);
    }
}
