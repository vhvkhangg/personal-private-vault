package com.vhvkhangg.personalprivatevault.vault.internal.application.entry;

import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultEntry;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.VaultEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class VaultEntryService implements VaultEntryOperations {

    private final VaultEntryRepository vaultEntryRepository;
    private final VaultEntryMutationGuard vaultEntryMutationGuard;
    private final com.vhvkhangg.personalprivatevault.vault.entry.SourceEditGuard sourceEditGuard;
    private final Clock clock;

    @Autowired
    public VaultEntryService(
            VaultEntryRepository vaultEntryRepository,
            VaultEntryMutationGuard vaultEntryMutationGuard,
            com.vhvkhangg.personalprivatevault.vault.entry.SourceEditGuard sourceEditGuard,
            @Autowired(required = false) Clock clock) {
        this.vaultEntryRepository = Objects.requireNonNull(vaultEntryRepository, "vaultEntryRepository must not be null");
        this.vaultEntryMutationGuard = Objects.requireNonNull(vaultEntryMutationGuard, "vaultEntryMutationGuard must not be null");
        this.sourceEditGuard = Objects.requireNonNull(sourceEditGuard, "sourceEditGuard must not be null");
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    public VaultEntryService(
            VaultEntryRepository vaultEntryRepository,
            VaultEntryMutationGuard vaultEntryMutationGuard,
            com.vhvkhangg.personalprivatevault.vault.entry.SourceEditGuard sourceEditGuard) {
        this(vaultEntryRepository, vaultEntryMutationGuard, sourceEditGuard, Clock.systemUTC());
    }

    public VaultEntryService(
            VaultEntryRepository vaultEntryRepository,
            VaultEntryMutationGuard vaultEntryMutationGuard,
            Clock clock) {
        this(vaultEntryRepository, vaultEntryMutationGuard, ids -> {}, clock);
    }

    public VaultEntryService(
            VaultEntryRepository vaultEntryRepository,
            VaultEntryMutationGuard vaultEntryMutationGuard) {
        this(vaultEntryRepository, vaultEntryMutationGuard, ids -> {}, Clock.systemUTC());
    }

    @Override
    @Transactional
    public VaultEntryView create(VaultEntryType entryType) {
        if (entryType == null) {
            throw new IllegalArgumentException("VaultEntryType must not be null");
        }
        Instant now = clock.instant();
        VaultEntry entry = new VaultEntry(entryType, now);
        VaultEntry saved = vaultEntryRepository.save(entry);
        return toView(saved);
    }

    @Override
    public Optional<VaultEntryView> find(Long vaultEntryId) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        return vaultEntryRepository.findById(vaultEntryId).map(this::toView);
    }

    @Override
    public List<VaultEntryView> findAll(Collection<Long> vaultEntryIds) {
        if (vaultEntryIds == null || vaultEntryIds.isEmpty()) {
            return List.of();
        }
        List<Long> cleanIds = vaultEntryIds.stream().filter(Objects::nonNull).distinct().toList();
        if(cleanIds.size()>100)throw new IllegalArgumentException("Source selection budget exceeded");
        return cleanIds.isEmpty()?List.of():vaultEntryRepository.findSourceViews(cleanIds);
    }

    @Override
    @Transactional
    public VaultEntryView moveToTrash(Long vaultEntryId) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        VaultEntry entry = getAndGuardEntry(vaultEntryId);

        Instant now = clock.instant();
        if (entry.moveToTrash(now)) {
            entry = vaultEntryRepository.saveAndFlush(entry);
        }
        return toView(entry);
    }

    @Override
    @Transactional
    public VaultEntryView restore(Long vaultEntryId) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        VaultEntry entry = getAndGuardEntry(vaultEntryId);

        Instant now = clock.instant();
        if (entry.restore(now)) {
            entry = vaultEntryRepository.saveAndFlush(entry);
        }
        return toView(entry);
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void acquireSourceGuard(Long vaultEntryId) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            throw new IllegalStateException("acquireSourceGuard requires a writable caller transaction, but current transaction is read-only");
        }
        if (!vaultEntryRepository.existsById(vaultEntryId)) {
            throw new NoSuchElementException("Vault entry not found with id: " + vaultEntryId);
        }
        sourceEditGuard.requireEditable(new java.util.TreeSet<>(java.util.Set.of(vaultEntryId)));
        vaultEntryMutationGuard.lockAndRefresh(vaultEntryId);
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void acquireSourceGuards(Collection<Long> vaultEntryIds) {
        if (vaultEntryIds == null) {
            throw new IllegalArgumentException("vaultEntryIds must not be null");
        }
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            throw new IllegalStateException("acquireSourceGuards requires a writable caller transaction, but current transaction is read-only");
        }
        List<Long> sortedUniqueIds = vaultEntryIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        if (sortedUniqueIds.isEmpty()) {
            return;
        }
        List<VaultEntry> existing = vaultEntryRepository.findAllById(sortedUniqueIds);
        if (existing.size() != sortedUniqueIds.size()) {
            java.util.Set<Long> foundIds = existing.stream().map(VaultEntry::getId).collect(java.util.stream.Collectors.toSet());
            for (Long id : sortedUniqueIds) {
                if (!foundIds.contains(id)) {
                    throw new NoSuchElementException("Vault entry not found with id: " + id);
                }
            }
        }
        sourceEditGuard.requireEditable(new java.util.TreeSet<>(sortedUniqueIds));
        vaultEntryMutationGuard.lockAndRefreshAll(sortedUniqueIds);
    }

    private VaultEntry getAndGuardEntry(Long vaultEntryId) {
        acquireSourceGuard(vaultEntryId);
        return vaultEntryRepository.findById(vaultEntryId)
                .orElseThrow(() -> new NoSuchElementException("Vault entry not found with id: " + vaultEntryId));
    }

    private VaultEntryView toView(VaultEntry entry) {
        return new VaultEntryView(
                entry.getId(),
                entry.getEntryType(),
                entry.getCreatedAt(),
                entry.getUpdatedAt(),
                entry.getDeletedAt()
        );
    }
}
