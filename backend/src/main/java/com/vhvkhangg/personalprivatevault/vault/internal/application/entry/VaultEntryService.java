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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class VaultEntryService implements VaultEntryOperations {

    private final VaultEntryRepository vaultEntryRepository;
    private final Clock clock;

    @Autowired
    public VaultEntryService(VaultEntryRepository vaultEntryRepository, @Autowired(required = false) Clock clock) {
        this.vaultEntryRepository = Objects.requireNonNull(vaultEntryRepository, "vaultEntryRepository must not be null");
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    public VaultEntryService(VaultEntryRepository vaultEntryRepository) {
        this(vaultEntryRepository, Clock.systemUTC());
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
    @Transactional
    public VaultEntryView moveToTrash(Long vaultEntryId) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        VaultEntry entry = vaultEntryRepository.findById(vaultEntryId)
                .orElseThrow(() -> new NoSuchElementException("Vault entry not found with id: " + vaultEntryId));

        Instant now = clock.instant();
        if (entry.moveToTrash(now)) {
            entry = vaultEntryRepository.save(entry);
        }
        return toView(entry);
    }

    @Override
    @Transactional
    public VaultEntryView restore(Long vaultEntryId) {
        if (vaultEntryId == null) {
            throw new IllegalArgumentException("vaultEntryId must not be null");
        }
        VaultEntry entry = vaultEntryRepository.findById(vaultEntryId)
                .orElseThrow(() -> new NoSuchElementException("Vault entry not found with id: " + vaultEntryId));

        Instant now = clock.instant();
        if (entry.restore(now)) {
            entry = vaultEntryRepository.save(entry);
        }
        return toView(entry);
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
