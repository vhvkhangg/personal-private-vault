package com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.authentication.internal.domain.PublicLoginCredential;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublicLoginCredentialRepository extends JpaRepository<PublicLoginCredential, Short> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT c FROM PublicLoginCredential c WHERE c.ownerId = :ownerId")
    Optional<PublicLoginCredential> findByIdForShare(@Param("ownerId") Short ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM PublicLoginCredential c WHERE c.ownerId = :ownerId")
    Optional<PublicLoginCredential> findByIdForUpdate(@Param("ownerId") Short ownerId);
}
