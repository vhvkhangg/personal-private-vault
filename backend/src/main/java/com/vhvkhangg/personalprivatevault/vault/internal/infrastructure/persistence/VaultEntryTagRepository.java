package com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.vault.internal.domain.Tag;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultEntryTag;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultEntryTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VaultEntryTagRepository extends JpaRepository<VaultEntryTag, VaultEntryTagId> {

    @Query("SELECT t FROM Tag t, VaultEntryTag vet WHERE vet.id.tagId = t.id AND vet.id.vaultEntryId = :vaultEntryId ORDER BY LOWER(t.name) ASC, t.id ASC")
    List<Tag> findTagsByVaultEntryId(@Param("vaultEntryId") Long vaultEntryId);
}
