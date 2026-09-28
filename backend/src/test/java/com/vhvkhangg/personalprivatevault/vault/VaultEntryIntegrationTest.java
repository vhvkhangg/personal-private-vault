package com.vhvkhangg.personalprivatevault.vault;

import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;

import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.internal.application.entry.VaultEntryService;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.VaultEntryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class VaultEntryIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private VaultEntryOperations vaultEntryOperations;

    @Autowired
    private VaultEntryRepository vaultEntryRepository;

    @Test
    @DisplayName("Create vault entry sets active identity and matching timestamps")
    void createSetsActiveIdentityAndTimestamps() {
        VaultEntryView view = vaultEntryOperations.create(VaultEntryType.FICTION);

        assertThat(view.id()).isNotNull();
        assertThat(view.entryType()).isEqualTo(VaultEntryType.FICTION);
        assertThat(view.createdAt()).isNotNull();
        assertThat(view.updatedAt()).isEqualTo(view.createdAt());
        assertThat(view.deletedAt()).isNull();

        Optional<VaultEntryView> found = vaultEntryOperations.find(view.id());
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(view);
    }

    @Test
    @DisplayName("Create with null entry type throws IllegalArgumentException")
    void createWithNullTypeThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> vaultEntryOperations.create(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Find with null ID throws IllegalArgumentException; non-existent ID returns Optional.empty")
    void findBehavior() {
        assertThatThrownBy(() -> vaultEntryOperations.find(null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(vaultEntryOperations.find(999999L)).isEmpty();
    }

    @Test
    @DisplayName("Trash and restore lifecycle: timestamps, idempotency, and state transitions")
    void trashAndRestoreLifecycleWithDeterministicClock() {
        Instant t0 = Instant.parse("2026-09-27T10:00:00Z");
        Instant t1 = Instant.parse("2026-09-27T10:15:00Z");
        Instant t2 = Instant.parse("2026-09-27T10:30:00Z");
        Instant t3 = Instant.parse("2026-09-27T10:45:00Z");
        Instant t4 = Instant.parse("2026-09-27T11:00:00Z");

        // 1. Create with fixed clock at t0
        Clock clock0 = Clock.fixed(t0, ZoneOffset.UTC);
        VaultEntryService service0 = new VaultEntryService(vaultEntryRepository, clock0);
        VaultEntryView created = service0.create(VaultEntryType.FILM);

        assertThat(created.createdAt()).isEqualTo(t0);
        assertThat(created.updatedAt()).isEqualTo(t0);
        assertThat(created.deletedAt()).isNull();

        // 2. Move to trash at t1
        Clock clock1 = Clock.fixed(t1, ZoneOffset.UTC);
        VaultEntryService service1 = new VaultEntryService(vaultEntryRepository, clock1);
        VaultEntryView trashed = service1.moveToTrash(created.id());

        assertThat(trashed.deletedAt()).isEqualTo(t1);
        assertThat(trashed.updatedAt()).isEqualTo(t1);
        assertThat(trashed.createdAt()).isEqualTo(t0);

        // 3. Repeated trash at t2 is idempotent: preserves original deletedAt and does not change updatedAt
        Clock clock2 = Clock.fixed(t2, ZoneOffset.UTC);
        VaultEntryService service2 = new VaultEntryService(vaultEntryRepository, clock2);
        VaultEntryView repeatTrashed = service2.moveToTrash(created.id());

        assertThat(repeatTrashed.deletedAt()).isEqualTo(t1);
        assertThat(repeatTrashed.updatedAt()).isEqualTo(t1);

        // 4. Restore at t3
        Clock clock3 = Clock.fixed(t3, ZoneOffset.UTC);
        VaultEntryService service3 = new VaultEntryService(vaultEntryRepository, clock3);
        VaultEntryView restored = service3.restore(created.id());

        assertThat(restored.deletedAt()).isNull();
        assertThat(restored.updatedAt()).isEqualTo(t3);
        assertThat(restored.createdAt()).isEqualTo(t0);

        // 5. Repeated restore at t4 is idempotent: does not alter updatedAt
        Clock clock4 = Clock.fixed(t4, ZoneOffset.UTC);
        VaultEntryService service4 = new VaultEntryService(vaultEntryRepository, clock4);
        VaultEntryView repeatRestored = service4.restore(created.id());

        assertThat(repeatRestored.deletedAt()).isNull();
        assertThat(repeatRestored.updatedAt()).isEqualTo(t3);
    }

    @Test
    @DisplayName("Mutating non-existent entry throws NoSuchElementException; null ID throws IllegalArgumentException")
    void missingTargetAndNullIdThrowExpectedExceptions() {
        assertThatThrownBy(() -> vaultEntryOperations.moveToTrash(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultEntryOperations.restore(null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> vaultEntryOperations.moveToTrash(999999L))
                .isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> vaultEntryOperations.restore(999999L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
