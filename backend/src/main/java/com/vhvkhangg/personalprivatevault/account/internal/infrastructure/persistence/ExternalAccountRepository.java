package com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.account.internal.domain.ExternalAccount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExternalAccountRepository extends JpaRepository<ExternalAccount, Long> {

    Optional<ExternalAccount> findByPlatformIdAndExternalId(Long platformId, String externalId);

    @Query("SELECT a FROM ExternalAccount a WHERE a.platformId = :platformId ORDER BY a.id DESC")
    List<ExternalAccount> findRecentByPlatformId(@Param("platformId") Long platformId, Pageable pageable);

    @Query("SELECT a.id FROM ExternalAccount a WHERE a.id IN :ids")
    List<Long> findExistingIds(@Param("ids") Collection<Long> ids);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ExternalAccount a WHERE a.id = :id")
    Optional<ExternalAccount> findByIdForUpdate(@Param("id") Long id);
}
