package com.vhvkhangg.personalprivatevault.vault.internal.application.entry;

import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultEntry;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.VaultEntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

    /**
     * Locks all specified vault_entries rows in ascending sorted order,
     * preventing lock-order inversions and deadlocks on physical rows.
     *
     * @param vaultEntryIds collection of vault entry IDs
     * @return list of locked and refreshed VaultEntry instances
     * @throws IllegalArgumentException if vaultEntryIds is null
     * @throws NoSuchElementException if any vault entry does not exist
     */
    public List<VaultEntry> lockAndRefreshAll(Collection<Long> vaultEntryIds) {
        if (vaultEntryIds == null) {
            throw new IllegalArgumentException("vaultEntryIds must not be null");
        }
        List<Long> sortedUniqueIds = vaultEntryIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        if (sortedUniqueIds.isEmpty()) {
            return List.of();
        }
        List<VaultEntry> existing = vaultEntryRepository.findAllById(sortedUniqueIds);
        if (existing.size() != sortedUniqueIds.size()) {
            Set<Long> foundIds = existing.stream().map(VaultEntry::getId).collect(Collectors.toSet());
            for (Long id : sortedUniqueIds) {
                if (!foundIds.contains(id)) {
                    throw new NoSuchElementException("Vault entry not found with id: " + id);
                }
            }
        }
        List<VaultEntry> locked = new ArrayList<>(sortedUniqueIds.size());
        for (Long id : sortedUniqueIds) {
            VaultEntry entry = vaultEntryRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new NoSuchElementException("Vault entry not found with id: " + id));
            entityManager.refresh(entry, LockModeType.PESSIMISTIC_WRITE);
            locked.add(entry);
        }
        return locked;
    }
}
