package com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.vault.internal.domain.Tag;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultEntryTag;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultEntryTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface VaultEntryTagRepository extends JpaRepository<VaultEntryTag, VaultEntryTagId> {

    @Query("SELECT t FROM Tag t, VaultEntryTag vet WHERE vet.id.tagId = t.id AND vet.id.vaultEntryId = :vaultEntryId ORDER BY LOWER(t.name) ASC, t.id ASC")
    List<Tag> findTagsByVaultEntryId(@Param("vaultEntryId") Long vaultEntryId);

    @Modifying(flushAutomatically = true)
    @Query(value = "INSERT INTO vault_entry_tags (vault_entry_id, tag_id, created_at) VALUES (:vaultEntryId, :tagId, :createdAt) ON CONFLICT (vault_entry_id, tag_id) DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@Param("vaultEntryId") Long vaultEntryId, @Param("tagId") Long tagId, @Param("createdAt") Instant createdAt);
}
