package com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultEntry;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VaultEntryRepository extends JpaRepository<VaultEntry, Long> {
    @Query("SELECT new com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView(e.id,e.entryType,e.createdAt,e.updatedAt,e.deletedAt) FROM VaultEntry e WHERE e.id IN :ids ORDER BY e.id")
    java.util.List<com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView> findSourceViews(@Param("ids") java.util.Collection<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM VaultEntry e WHERE e.id = :id")
    Optional<VaultEntry> findByIdForUpdate(@Param("id") Long id);
}
