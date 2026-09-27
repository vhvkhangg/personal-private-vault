package com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaultEntryRepository extends JpaRepository<VaultEntry, Long> {
}
