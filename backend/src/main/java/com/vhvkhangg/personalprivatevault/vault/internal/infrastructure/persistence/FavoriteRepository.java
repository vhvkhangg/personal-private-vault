package com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.vault.internal.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Modifying(flushAutomatically = true)
    @Query(value = "INSERT INTO favorites (vault_entry_id, created_at) VALUES (:vaultEntryId, :createdAt) ON CONFLICT (vault_entry_id) DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@Param("vaultEntryId") Long vaultEntryId, @Param("createdAt") Instant createdAt);
}
