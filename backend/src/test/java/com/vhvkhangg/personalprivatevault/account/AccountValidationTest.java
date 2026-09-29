package com.vhvkhangg.personalprivatevault.account;

import com.vhvkhangg.personalprivatevault.account.account.CreateExternalAccountCommand;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountConflictException;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountNotFoundException;
import com.vhvkhangg.personalprivatevault.account.account.InvalidExternalAccountException;
import com.vhvkhangg.personalprivatevault.account.account.UpdateExternalAccountCommand;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountOwnership;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;
import com.vhvkhangg.personalprivatevault.account.enums.FollowStatus;
import com.vhvkhangg.personalprivatevault.account.enums.FollowerSnapshotSource;
import com.vhvkhangg.personalprivatevault.account.enums.FollowerStatus;
import com.vhvkhangg.personalprivatevault.account.enums.RelationshipSource;
import com.vhvkhangg.personalprivatevault.account.internal.application.ExternalAccountRelationshipService;
import com.vhvkhangg.personalprivatevault.account.internal.application.ExternalAccountService;
import com.vhvkhangg.personalprivatevault.account.internal.application.FollowerSnapshotService;
import com.vhvkhangg.personalprivatevault.account.internal.domain.ExternalAccount;
import com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence.ExternalAccountRelationshipRepository;
import com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence.ExternalAccountRepository;
import com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence.FollowerSnapshotEntryRepository;
import com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence.FollowerSnapshotRepository;
import com.vhvkhangg.personalprivatevault.account.relationship.InvalidExternalAccountRelationshipException;
import com.vhvkhangg.personalprivatevault.account.relationship.SetExternalAccountRelationshipCommand;
import com.vhvkhangg.personalprivatevault.account.snapshot.CreateFollowerSnapshotCommand;
import com.vhvkhangg.personalprivatevault.account.snapshot.CreateFollowerSnapshotEntryCommand;
import com.vhvkhangg.personalprivatevault.account.snapshot.InvalidFollowerSnapshotException;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.reference.enums.PlatformKind;
import com.vhvkhangg.personalprivatevault.reference.view.PlatformView;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountValidationTest {

    private ReferenceCatalog referenceCatalog;
    private VaultEntryOperations vaultEntryOperations;
    private ExternalAccountRepository accountRepository;
    private ExternalAccountRelationshipRepository relationshipRepository;
    private FollowerSnapshotRepository snapshotRepository;
    private FollowerSnapshotEntryRepository snapshotEntryRepository;

    private ExternalAccountService accountService;
    private ExternalAccountRelationshipService relationshipService;
    private FollowerSnapshotService snapshotService;

    @BeforeEach
    void setUp() {
        referenceCatalog = mock(ReferenceCatalog.class);
        vaultEntryOperations = mock(VaultEntryOperations.class);
        accountRepository = mock(ExternalAccountRepository.class);
        relationshipRepository = mock(ExternalAccountRelationshipRepository.class);
        snapshotRepository = mock(FollowerSnapshotRepository.class);
        snapshotEntryRepository = mock(FollowerSnapshotEntryRepository.class);

        accountService = new ExternalAccountService(referenceCatalog, vaultEntryOperations, accountRepository);
        relationshipService = new ExternalAccountRelationshipService(accountRepository, relationshipRepository);
        snapshotService = new FollowerSnapshotService(accountRepository, snapshotRepository, snapshotEntryRepository);
    }

    @Nested
    @DisplayName("External Account Validation")
    class ExternalAccountValidationTests {

        @Test
        @DisplayName("Rejects null create command")
        void createRejectsNullCommand() {
            assertThatThrownBy(() -> accountService.create(null))
                    .isInstanceOf(InvalidExternalAccountException.class)
                    .hasMessageContaining("must not be null");
        }

        @Test
        @DisplayName("Rejects null platform ID")
        void createRejectsNullPlatformId() {
            CreateExternalAccountCommand cmd = new CreateExternalAccountCommand(
                    null, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                    "alice", null, null, null, null, null, null, null, null
            );
            assertThatThrownBy(() -> accountService.create(cmd))
                    .isInstanceOf(InvalidExternalAccountException.class)
                    .hasMessageContaining("Platform ID must not be null");
        }

        @Test
        @DisplayName("Rejects non-existent platform ID")
        void createRejectsNonExistentPlatform() {
            when(referenceCatalog.platform(999L)).thenReturn(Optional.empty());
            CreateExternalAccountCommand cmd = new CreateExternalAccountCommand(
                    999L, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                    "alice", null, null, null, null, null, null, null, null
            );
            assertThatThrownBy(() -> accountService.create(cmd))
                    .isInstanceOf(InvalidExternalAccountException.class)
                    .hasMessageContaining("Platform with id 999 does not exist");
        }

        @Test
        @DisplayName("Rejects missing ownership")
        void createRejectsNullOwnership() {
            when(referenceCatalog.platform(1L)).thenReturn(Optional.of(new PlatformView(1L, "Twitter", PlatformKind.SOCIAL, null, Instant.now())));
            CreateExternalAccountCommand cmd = new CreateExternalAccountCommand(
                    1L, null, ExternalAccountType.SOCIAL,
                    "alice", null, null, null, null, null, null, null, null
            );
            assertThatThrownBy(() -> accountService.create(cmd))
                    .isInstanceOf(InvalidExternalAccountException.class)
                    .hasMessageContaining("Ownership must not be null");
        }

        @Test
        @DisplayName("Rejects missing account type")
        void createRejectsNullAccountType() {
            when(referenceCatalog.platform(1L)).thenReturn(Optional.of(new PlatformView(1L, "Twitter", PlatformKind.SOCIAL, null, Instant.now())));
            CreateExternalAccountCommand cmd = new CreateExternalAccountCommand(
                    1L, ExternalAccountOwnership.OWNED, null,
                    "alice", null, null, null, null, null, null, null, null
            );
            assertThatThrownBy(() -> accountService.create(cmd))
                    .isInstanceOf(InvalidExternalAccountException.class)
                    .hasMessageContaining("AccountType must not be null");
        }

        @Test
        @DisplayName("Rejects when all identifiers (username, external_id, url) are null or empty")
        void createRejectsWhenNoIdentifierProvided() {
            when(referenceCatalog.platform(1L)).thenReturn(Optional.of(new PlatformView(1L, "Twitter", PlatformKind.SOCIAL, null, Instant.now())));
            CreateExternalAccountCommand cmd = new CreateExternalAccountCommand(
                    1L, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                    "  ", "", "Alice", null, null, null, null, null, null
            );
            assertThatThrownBy(() -> accountService.create(cmd))
                    .isInstanceOf(InvalidExternalAccountException.class)
                    .hasMessageContaining("At least one identifier");
        }

        @Test
        @DisplayName("Rejects identifier and text fields that exceed max length")
        void createRejectsOverlongFields() {
            when(referenceCatalog.platform(1L)).thenReturn(Optional.of(new PlatformView(1L, "Twitter", PlatformKind.SOCIAL, null, Instant.now())));
            String long256 = "a".repeat(256);
            String long501 = "a".repeat(501);
            String long2049 = "a".repeat(2049);

            assertThatThrownBy(() -> accountService.create(new CreateExternalAccountCommand(
                    1L, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                    long256, null, null, null, null, null, null, null, null
            ))).isInstanceOf(InvalidExternalAccountException.class).hasMessageContaining("Username must not exceed 255");

            assertThatThrownBy(() -> accountService.create(new CreateExternalAccountCommand(
                    1L, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                    null, long256, null, null, null, null, null, null, null
            ))).isInstanceOf(InvalidExternalAccountException.class).hasMessageContaining("External ID must not exceed 255");

            assertThatThrownBy(() -> accountService.create(new CreateExternalAccountCommand(
                    1L, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                    "alice", null, long501, null, null, null, null, null, null
            ))).isInstanceOf(InvalidExternalAccountException.class).hasMessageContaining("Display name must not exceed 500");

            assertThatThrownBy(() -> accountService.create(new CreateExternalAccountCommand(
                    1L, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                    "alice", null, null, long2049, null, null, null, null, null
            ))).isInstanceOf(InvalidExternalAccountException.class).hasMessageContaining("Avatar URL must not exceed 2048");

            assertThatThrownBy(() -> accountService.create(new CreateExternalAccountCommand(
                    1L, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                    "alice", null, null, null, null, null, null, long2049, null
            ))).isInstanceOf(InvalidExternalAccountException.class).hasMessageContaining("URL must not exceed 2048");
        }

        @Test
        @DisplayName("Sequential duplicate (platform_id, external_id) pre-check throws ExternalAccountConflictException without leaking external ID")
        void createRejectsDuplicatePlatformAndExternalId() {
            when(referenceCatalog.platform(1L)).thenReturn(Optional.of(new PlatformView(1L, "Twitter", PlatformKind.SOCIAL, null, Instant.now())));
            ExternalAccount existing = mock(ExternalAccount.class);
            when(accountRepository.findByPlatformIdAndExternalId(1L, "ext-12345")).thenReturn(Optional.of(existing));

            CreateExternalAccountCommand cmd = new CreateExternalAccountCommand(
                    1L, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                    "alice", "ext-12345", null, null, null, null, null, null, null
            );

            assertThatThrownBy(() -> accountService.create(cmd))
                    .isInstanceOf(ExternalAccountConflictException.class)
                    .hasMessageContaining("already exists for platform ID 1")
                    .hasMessageNotContaining("ext-12345");
        }

        @Test
        @DisplayName("Update rejects null ID or null command")
        void updateRejectsNullArguments() {
            assertThatThrownBy(() -> accountService.update(null, mock(UpdateExternalAccountCommand.class)))
                    .isInstanceOf(InvalidExternalAccountException.class)
                    .hasMessageContaining("External account ID must not be null");

            assertThatThrownBy(() -> accountService.update(1L, null))
                    .isInstanceOf(InvalidExternalAccountException.class)
                    .hasMessageContaining("UpdateExternalAccountCommand must not be null");
        }

        @Test
        @DisplayName("Update throws ExternalAccountNotFoundException when account does not exist")
        void updateThrowsNotFound() {
            when(accountRepository.findById(999L)).thenReturn(Optional.empty());
            UpdateExternalAccountCommand cmd = new UpdateExternalAccountCommand(
                    1L, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                    "alice", null, null, null, null, null, null, null, null
            );

            assertThatThrownBy(() -> accountService.update(999L, cmd))
                    .isInstanceOf(ExternalAccountNotFoundException.class)
                    .hasMessageContaining("External account with id 999 does not exist");
        }
    }

    @Nested
    @DisplayName("External Account Relationship Validation")
    class RelationshipValidationTests {

        @Test
        @DisplayName("Rejects null relationship command")
        void setRejectsNullCommand() {
            assertThatThrownBy(() -> relationshipService.setRelationship(null))
                    .isInstanceOf(InvalidExternalAccountRelationshipException.class)
                    .hasMessageContaining("must not be null");
        }

        @Test
        @DisplayName("Rejects null owner or target account IDs")
        void setRejectsNullAccountIds() {
            assertThatThrownBy(() -> relationshipService.setRelationship(new SetExternalAccountRelationshipCommand(
                    null, 2L, FollowerStatus.CURRENT_FOLLOWER, RelationshipSource.MANUAL, FollowStatus.FOLLOWED, false, null
            ))).isInstanceOf(InvalidExternalAccountRelationshipException.class).hasMessageContaining("Owner account ID must not be null");

            assertThatThrownBy(() -> relationshipService.setRelationship(new SetExternalAccountRelationshipCommand(
                    1L, null, FollowerStatus.CURRENT_FOLLOWER, RelationshipSource.MANUAL, FollowStatus.FOLLOWED, false, null
            ))).isInstanceOf(InvalidExternalAccountRelationshipException.class).hasMessageContaining("Target account ID must not be null");
        }

        @Test
        @DisplayName("Rejects self-relationship where owner equals target")
        void setRejectsSelfRelationship() {
            SetExternalAccountRelationshipCommand cmd = new SetExternalAccountRelationshipCommand(
                    10L, 10L, FollowerStatus.CURRENT_FOLLOWER, RelationshipSource.MANUAL, FollowStatus.FOLLOWED, false, null
            );
            assertThatThrownBy(() -> relationshipService.setRelationship(cmd))
                    .isInstanceOf(InvalidExternalAccountRelationshipException.class)
                    .hasMessageContaining("Owner account ID and target account ID must not be the same");
        }

        @Test
        @DisplayName("Rejects non-existent owner or target accounts")
        void setRejectsNonExistentAccounts() {
            when(accountRepository.findById(10L)).thenReturn(Optional.empty());
            when(accountRepository.findById(20L)).thenReturn(Optional.of(mock(ExternalAccount.class)));

            SetExternalAccountRelationshipCommand cmd1 = new SetExternalAccountRelationshipCommand(
                    10L, 20L, FollowerStatus.CURRENT_FOLLOWER, RelationshipSource.MANUAL, FollowStatus.FOLLOWED, false, null
            );
            assertThatThrownBy(() -> relationshipService.setRelationship(cmd1))
                    .isInstanceOf(InvalidExternalAccountRelationshipException.class)
                    .hasMessageContaining("Owner account with id 10 does not exist");

            when(accountRepository.findById(10L)).thenReturn(Optional.of(mock(ExternalAccount.class)));
            when(accountRepository.findById(20L)).thenReturn(Optional.empty());

            SetExternalAccountRelationshipCommand cmd2 = new SetExternalAccountRelationshipCommand(
                    10L, 20L, FollowerStatus.CURRENT_FOLLOWER, RelationshipSource.MANUAL, FollowStatus.FOLLOWED, false, null
            );
            assertThatThrownBy(() -> relationshipService.setRelationship(cmd2))
                    .isInstanceOf(InvalidExternalAccountRelationshipException.class)
                    .hasMessageContaining("Target account with id 20 does not exist");
        }
    }

    @Nested
    @DisplayName("Follower Snapshot Validation")
    class FollowerSnapshotValidationTests {

        @Test
        @DisplayName("Rejects null snapshot command")
        void createSnapshotRejectsNullCommand() {
            assertThatThrownBy(() -> snapshotService.createSnapshot(null))
                    .isInstanceOf(InvalidFollowerSnapshotException.class)
                    .hasMessageContaining("must not be null");
        }

        @Test
        @DisplayName("Rejects missing owner account or non-existent owner")
        void createSnapshotRejectsInvalidOwner() {
            assertThatThrownBy(() -> snapshotService.createSnapshot(new CreateFollowerSnapshotCommand(
                    null, Instant.now(), FollowerSnapshotSource.MANUAL, 10, null, List.of()
            ))).isInstanceOf(InvalidFollowerSnapshotException.class).hasMessageContaining("Owner account ID must not be null");

            when(accountRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> snapshotService.createSnapshot(new CreateFollowerSnapshotCommand(
                    99L, Instant.now(), FollowerSnapshotSource.MANUAL, 10, null, List.of()
            ))).isInstanceOf(InvalidFollowerSnapshotException.class).hasMessageContaining("Owner account with id 99 does not exist");
        }

        @Test
        @DisplayName("Rejects missing capture time or source")
        void createSnapshotRejectsMissingMetadata() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(mock(ExternalAccount.class)));

            assertThatThrownBy(() -> snapshotService.createSnapshot(new CreateFollowerSnapshotCommand(
                    1L, null, FollowerSnapshotSource.MANUAL, 10, null, List.of()
            ))).isInstanceOf(InvalidFollowerSnapshotException.class).hasMessageContaining("Captured at must not be null");

            assertThatThrownBy(() -> snapshotService.createSnapshot(new CreateFollowerSnapshotCommand(
                    1L, Instant.now(), null, 10, null, List.of()
            ))).isInstanceOf(InvalidFollowerSnapshotException.class).hasMessageContaining("Source must not be null");
        }

        @Test
        @DisplayName("Rejects negative reportedTotalCount")
        void createSnapshotRejectsNegativeReportedCount() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(mock(ExternalAccount.class)));

            assertThatThrownBy(() -> snapshotService.createSnapshot(new CreateFollowerSnapshotCommand(
                    1L, Instant.now(), FollowerSnapshotSource.MANUAL, -1, null, List.of()
            ))).isInstanceOf(InvalidFollowerSnapshotException.class).hasMessageContaining("Reported total count must be non-negative");
        }

        @Test
        @DisplayName("Rejects entry where target equals owner")
        void createSnapshotRejectsTargetEqualsOwner() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(mock(ExternalAccount.class)));

            CreateFollowerSnapshotCommand cmd = new CreateFollowerSnapshotCommand(
                    1L, Instant.now(), FollowerSnapshotSource.MANUAL, 1, null,
                    List.of(new CreateFollowerSnapshotEntryCommand(1L, "self", null, null, null))
            );

            assertThatThrownBy(() -> snapshotService.createSnapshot(cmd))
                    .isInstanceOf(InvalidFollowerSnapshotException.class)
                    .hasMessageContaining("Target account ID must not equal owner account ID");
        }

        @Test
        @DisplayName("Rejects entry referencing non-existent target account")
        void createSnapshotRejectsNonExistentTarget() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(mock(ExternalAccount.class)));
            when(accountRepository.findExistingIds(any())).thenReturn(List.of());

            CreateFollowerSnapshotCommand cmd = new CreateFollowerSnapshotCommand(
                    1L, Instant.now(), FollowerSnapshotSource.MANUAL, 1, null,
                    List.of(new CreateFollowerSnapshotEntryCommand(2L, "target2", null, null, null))
            );

            assertThatThrownBy(() -> snapshotService.createSnapshot(cmd))
                    .isInstanceOf(InvalidFollowerSnapshotException.class)
                    .hasMessageContaining("Target account with id 2 does not exist");
        }

        @Test
        @DisplayName("Rejects batch containing conflicting historical copies for the same target")
        void createSnapshotRejectsConflictingEntriesForSameTarget() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(mock(ExternalAccount.class)));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(mock(ExternalAccount.class)));

            CreateFollowerSnapshotCommand cmd = new CreateFollowerSnapshotCommand(
                    1L, Instant.now(), FollowerSnapshotSource.MANUAL, 2, null,
                    List.of(
                            new CreateFollowerSnapshotEntryCommand(2L, "user_old", "Display Name A", "ext-1", "https://profile.com/a"),
                            new CreateFollowerSnapshotEntryCommand(2L, "user_new", "Display Name A", "ext-1", "https://profile.com/a")
                    )
            );

            assertThatThrownBy(() -> snapshotService.createSnapshot(cmd))
                    .isInstanceOf(InvalidFollowerSnapshotException.class)
                    .hasMessageContaining("Conflicting historical snapshot entries submitted for target account ID 2");
        }
    }
}
