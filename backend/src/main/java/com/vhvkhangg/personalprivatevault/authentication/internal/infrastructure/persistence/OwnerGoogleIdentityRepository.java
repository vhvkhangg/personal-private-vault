package com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.authentication.internal.domain.OwnerGoogleIdentity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OwnerGoogleIdentityRepository extends JpaRepository<OwnerGoogleIdentity, Short> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM OwnerGoogleIdentity i WHERE i.ownerId = :ownerId")
    Optional<OwnerGoogleIdentity> findByIdForUpdate(@Param("ownerId") Short ownerId);

    Optional<OwnerGoogleIdentity> findByIssuerAndGoogleSub(String issuer, String googleSub);
}
