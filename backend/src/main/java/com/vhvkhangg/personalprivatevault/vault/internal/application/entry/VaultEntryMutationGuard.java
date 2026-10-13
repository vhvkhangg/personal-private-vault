package com.vhvkhangg.personalprivatevault.vault.internal.application.entry;

import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultEntry;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.VaultEntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Coordinates pessimistic write locks and fresh lifecycle state reloading on existing vault_entries rows.
 */
@Component
public class VaultEntryMutationGuard {

    private final VaultEntryRepository vaultEntryRepository;
    private final EntityManager entityManager;

    public VaultEntryMutationGuard(VaultEntryRepository vaultEntryRepository, EntityManager entityManager) {
        this.vaultEntryRepository = Objects.requireNonNull(vaultEntryRepository, "vaultEntryRepository must not be null");
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager must not be null");
    }

    /**
     * Locks the physical vault_entries row with PESSIMISTIC_WRITE and authoritatively refreshes
     * its lifecycle state in the caller's persistence context.
     *
     * @param vaultEntryId the vault entry ID
     * @return the locked and refreshed VaultEntry instance
     * @throws IllegalArgumentException if vaultEntryId is null
     * @throws NoSuchElementException if no vault entry exists with the given ID
     */
    public VaultEntry lockAndRefresh(Long vaultEntryId) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        VaultEntry entry = vaultEntryRepository.findByIdForUpdate(vaultEntryId)
                .orElseThrow(() -> new NoSuchElementException("Vault entry not found with id: " + vaultEntryId));
        entityManager.refresh(entry, LockModeType.PESSIMISTIC_WRITE);
        return entry;
    }
}
