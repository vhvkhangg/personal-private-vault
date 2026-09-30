package com.vhvkhangg.personalprivatevault.account;

import com.vhvkhangg.personalprivatevault.account.account.CreateExternalAccountCommand;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountConflictException;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountNotFoundException;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountOperations;
import com.vhvkhangg.personalprivatevault.account.account.InvalidExternalAccountException;
import com.vhvkhangg.personalprivatevault.account.account.UpdateExternalAccountCommand;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountOwnership;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;
import com.vhvkhangg.personalprivatevault.account.enums.FollowStatus;
import com.vhvkhangg.personalprivatevault.account.enums.FollowerSnapshotSource;
import com.vhvkhangg.personalprivatevault.account.enums.FollowerStatus;
import com.vhvkhangg.personalprivatevault.account.enums.RelationshipSource;
import com.vhvkhangg.personalprivatevault.account.relationship.ExternalAccountRelationshipOperations;
import com.vhvkhangg.personalprivatevault.account.relationship.InvalidExternalAccountRelationshipException;
import com.vhvkhangg.personalprivatevault.account.relationship.SetExternalAccountRelationshipCommand;
import com.vhvkhangg.personalprivatevault.account.snapshot.CreateFollowerSnapshotCommand;
import com.vhvkhangg.personalprivatevault.account.snapshot.CreateFollowerSnapshotEntryCommand;
import com.vhvkhangg.personalprivatevault.account.snapshot.FollowerSnapshotOperations;
import com.vhvkhangg.personalprivatevault.account.snapshot.InvalidFollowerSnapshotException;
import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountRelationshipView;
import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountView;
import com.vhvkhangg.personalprivatevault.account.view.FollowerSnapshotEntryView;
import com.vhvkhangg.personalprivatevault.account.view.FollowerSnapshotView;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class AccountIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private ExternalAccountOperations accountOperations;

    @Autowired
    private ExternalAccountRelationshipOperations relationshipOperations;

    @Autowired
    private FollowerSnapshotOperations snapshotOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Long testPlatformId;

    private Statistics getHibernateStatistics() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        return statistics;
    }

    @BeforeEach
    void setUp() {
        tearDown();
        testPlatformId = jdbcTemplate.query(
                "SELECT id FROM platforms WHERE name = 'Twitter'",
                (rs, rowNum) -> rs.getLong("id")
        ).stream().findFirst().orElseGet(() -> jdbcTemplate.queryForObject(
                "INSERT INTO platforms (name, kind, url) VALUES ('Twitter', 'SOCIAL'::platform_kind, 'https://twitter.com') RETURNING id",
                Long.class
        ));
        assertThat(testPlatformId).isNotNull();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM follower_snapshot_entries");
        jdbcTemplate.execute("DELETE FROM follower_snapshots");
        jdbcTemplate.execute("DELETE FROM external_account_relationships");
        jdbcTemplate.execute("DELETE FROM external_accounts");
        jdbcTemplate.execute("DELETE FROM vault_entries WHERE entry_type = 'EXTERNAL_ACCOUNT'");
        jdbcTemplate.execute("DELETE FROM platforms WHERE name = 'Twitter'");
    }

    @Test
    @DisplayName("Creates External Account backed by Vault Entry and retrieves it by ID")
    void createAndReadExternalAccountBackedByVaultEntry() {
        CreateExternalAccountCommand cmd = new CreateExternalAccountCommand(
                testPlatformId,
                ExternalAccountOwnership.OWNED,
                ExternalAccountType.SOCIAL,
                "alice_in_vault",
                "ext_alice_001",
                "Alice In Vault",
                "https://cdn.example.com/avatar.jpg",
                "https://cdn.example.com/banner.jpg",
                "Personal vault account",
                "Alice",
                "https://social.example.com/alice",
                "Primary test account"
        );

        ExternalAccountView created = accountOperations.create(cmd);

        assertThat(created.id()).isNotNull();
        assertThat(created.platformId()).isEqualTo(testPlatformId);
        assertThat(created.ownership()).isEqualTo(ExternalAccountOwnership.OWNED);
        assertThat(created.accountType()).isEqualTo(ExternalAccountType.SOCIAL);
        assertThat(created.username()).isEqualTo("alice_in_vault");
        assertThat(created.externalId()).isEqualTo("ext_alice_001");
        assertThat(created.displayName()).isEqualTo("Alice In Vault");
        assertThat(created.avatarUrl()).isEqualTo("https://cdn.example.com/avatar.jpg");
        assertThat(created.bannerUrl()).isEqualTo("https://cdn.example.com/banner.jpg");
        assertThat(created.profileDescription()).isEqualTo("Personal vault account");
        assertThat(created.ownerName()).isEqualTo("Alice");
        assertThat(created.url()).isEqualTo("https://social.example.com/alice");
        assertThat(created.notes()).isEqualTo("Primary test account");

        // Verify Vault Entry exists with type EXTERNAL_ACCOUNT and matching ID
        String vaultType = jdbcTemplate.queryForObject(
                "SELECT entry_type FROM vault_entries WHERE id = ?",
                String.class,
                created.id()
        );
        assertThat(vaultType).isEqualTo("EXTERNAL_ACCOUNT");

        // Verify retrieval by ID
        Optional<ExternalAccountView> found = accountOperations.findById(created.id());
        assertThat(found).isPresent();
        assertThat(found.get().username()).isEqualTo("alice_in_vault");

        // Verify bounded retrieval by platform
        List<ExternalAccountView> recent = accountOperations.findRecentByPlatformId(testPlatformId, 10);
        assertThat(recent).isNotEmpty();
        assertThat(recent.getFirst().id()).isEqualTo(created.id());
    }

    @Test
    @DisplayName("Updates External Account metadata cleanly")
    void updateExternalAccountMetadata() {
        ExternalAccountView created = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                "channel_handle", "yt_chan_123", "Old Channel Name", null, null, null, null,
                "https://youtube.com/@channel_handle", "Notes"
        ));

        ExternalAccountView updated = accountOperations.update(created.id(), new UpdateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.OWNED, ExternalAccountType.YOUTUBE_CHANNEL,
                "channel_handle_new", "yt_chan_123", "New Channel Name", "https://cdn.example.com/yt.png",
                null, "Updated description", "Creator", "https://youtube.com/@channel_handle_new", "Updated notes"
        ));

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.ownership()).isEqualTo(ExternalAccountOwnership.OWNED);
        assertThat(updated.username()).isEqualTo("channel_handle_new");
        assertThat(updated.displayName()).isEqualTo("New Channel Name");
        assertThat(updated.avatarUrl()).isEqualTo("https://cdn.example.com/yt.png");
        assertThat(updated.notes()).isEqualTo("Updated notes");

        ExternalAccountView reloaded = accountOperations.findById(created.id()).orElseThrow();
        assertThat(reloaded.displayName()).isEqualTo("New Channel Name");
    }

    @Test
    @DisplayName("Rollback of enclosing transaction rolls back External Account and its Vault Entry")
    void rollbackOfExternalAccountRollsBackVaultEntry() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            accountOperations.create(new CreateExternalAccountCommand(
                    testPlatformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                    "doomed_account", "doomed_ext_01", null, null, null, null, null, null, null
            ));
            throw new RuntimeException("Force rollback");
        })).hasMessageContaining("Force rollback");

        Integer accountCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM external_accounts WHERE username = 'doomed_account'",
                Integer.class
        );
        assertThat(accountCount).isZero();

        Integer vaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE entry_type = 'EXTERNAL_ACCOUNT'",
                Integer.class
        );
        assertThat(vaultCount).isZero();
    }

    @Test
    @DisplayName("Validates platform existence through ReferenceCatalog")
    void platformValidationViaReferenceCatalog() {
        assertThatThrownBy(() -> accountOperations.create(new CreateExternalAccountCommand(
                999999L, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                "user", null, null, null, null, null, null, null, null
        )))
                .isInstanceOf(InvalidExternalAccountException.class)
                .hasMessageContaining("Platform with id 999999 does not exist");
    }

    @Test
    @DisplayName("Sequential duplicate (platform_id, external_id) creation throws domain conflict")
    void duplicatePlatformAndExternalIdThrowsConflict() {
        accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                "user1", "ext-duplicate-1", null, null, null, null, null, null, null
        ));

        assertThatThrownBy(() -> accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.GAME,
                "user2", "ext-duplicate-1", null, null, null, null, null, null, null
        )))
                .isInstanceOf(ExternalAccountConflictException.class)
                .hasMessageContaining("already exists for platform ID " + testPlatformId);
    }

    @Test
    @DisplayName("Permits duplicate usernames and URLs across accounts when external_id is distinct or null")
    void allowedUsernameAndUrlDuplicates() {
        ExternalAccountView acc1 = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                "shared_user", null, "Account 1", null, null, null, null, "https://example.com/shared", null
        ));

        ExternalAccountView acc2 = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                "shared_user", null, "Account 2", null, null, null, null, "https://example.com/shared", null
        ));

        assertThat(acc1.id()).isNotEqualTo(acc2.id());
        assertThat(acc1.username()).isEqualTo(acc2.username());
        assertThat(acc1.url()).isEqualTo(acc2.url());
    }

    @Test
    @DisplayName("Concurrent duplicate external_id creation recovers from unique constraint conflict, rolls back vault entry, and logs no private values")
    void concurrentDuplicateExternalIdThrowsDomainConflictWithPrivacySafeLogging(CapturedOutput output) throws Exception {
        String privateMarker = "PPV_PRIVATE_EXT_ID_MARKER_987654";
        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: In new transaction, inserts account with private marker external_id and holds uncommitted lock
            Future<Long> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                Long vaultId = jdbcTemplate.queryForObject(
                        "INSERT INTO vault_entries (entry_type) VALUES ('EXTERNAL_ACCOUNT') RETURNING id",
                        Long.class
                );
                jdbcTemplate.update(
                        "INSERT INTO external_accounts (id, platform_id, ownership, account_type, username, external_id) " +
                        "VALUES (?, ?, 'OWNED'::external_account_ownership, 'SOCIAL'::external_account_type, 'thread1_user', ?)",
                        vaultId, testPlatformId, privateMarker
                );
                thread1Inserted.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return vaultId;
            }));

            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Attempts to create account with same platform and external_id via service.
            // Bypasses initial application pre-check because Thread 1 is uncommitted, then blocks on PostgreSQL unique index uq_external_accounts_platform_id_external_id.
            Future<ExternalAccountView> thread2Future = executor.submit(() -> accountOperations.create(new CreateExternalAccountCommand(
                    testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                    "thread2_user", privateMarker, null, null, null, null, null, null, null
            )));

            // Observe PostgreSQL lock contention on external_accounts table
            awaitCompetingLock("external_accounts", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            Long id1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(id1).isNotNull();

            // Thread 2 must receive ExternalAccountConflictException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(ExternalAccountConflictException.class)
                    .hasMessageContaining("already exists for platform ID " + testPlatformId);

            // Verify only 1 external account exists in DB with this external_id
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM external_accounts WHERE external_id = ?",
                    Integer.class,
                    privateMarker
            );
            assertThat(count).isEqualTo(1);

            // Verify that losing transaction rolled back its Vault entry: exactly 1 EXTERNAL_ACCOUNT exists
            Integer vaultCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entries WHERE entry_type = 'EXTERNAL_ACCOUNT'",
                    Integer.class
            );
            assertThat(vaultCount).isEqualTo(1);

            // Privacy verification: assert private marker and raw vendor detail are NOT logged
            assertThat(output.getAll())
                    .doesNotContain(privateMarker)
                    .doesNotContain("Detail: Key (platform_id, external_id)=(")
                    .doesNotContain("Detail: Key ");
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Sets relationship idempotently and updates mutable fields on subsequent set calls")
    void relationshipSetIdempotentAndUpdatesFields() throws Exception {
        ExternalAccountView owner = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                "owner_acc", null, null, null, null, null, null, null, null
        ));
        ExternalAccountView target = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                "target_acc", null, null, null, null, null, null, null, null
        ));

        // Initial set
        ExternalAccountRelationshipView rel1 = relationshipOperations.setRelationship(new SetExternalAccountRelationshipCommand(
                owner.id(), target.id(), FollowerStatus.CURRENT_FOLLOWER, RelationshipSource.MANUAL,
                FollowStatus.NOT_FOLLOWED, false, "Initial note"
        ));

        assertThat(rel1.id()).isNotNull();
        assertThat(rel1.ownerAccountId()).isEqualTo(owner.id());
        assertThat(rel1.targetAccountId()).isEqualTo(target.id());
        assertThat(rel1.followerStatus()).isEqualTo(FollowerStatus.CURRENT_FOLLOWER);
        assertThat(rel1.source()).isEqualTo(RelationshipSource.MANUAL);
        assertThat(rel1.followStatus()).isEqualTo(FollowStatus.NOT_FOLLOWED);
        assertThat(rel1.hasLikedPost()).isFalse();
        assertThat(rel1.note()).isEqualTo("Initial note");
        assertThat(rel1.createdAt()).isNotNull();
        assertThat(rel1.updatedAt()).isNotNull();

        // Exact repeat set (identical command) - must be truly idempotent with updatedAt unchanged
        ExternalAccountRelationshipView relRepeat = relationshipOperations.setRelationship(new SetExternalAccountRelationshipCommand(
                owner.id(), target.id(), FollowerStatus.CURRENT_FOLLOWER, RelationshipSource.MANUAL,
                FollowStatus.NOT_FOLLOWED, false, "Initial note"
        ));

        assertThat(relRepeat.id()).isEqualTo(rel1.id());
        assertThat(relRepeat.source()).isEqualTo(RelationshipSource.MANUAL);
        assertThat(relRepeat.followerStatus()).isEqualTo(FollowerStatus.CURRENT_FOLLOWER);
        assertThat(relRepeat.followStatus()).isEqualTo(FollowStatus.NOT_FOLLOWED);
        assertThat(relRepeat.hasLikedPost()).isFalse();
        assertThat(relRepeat.note()).isEqualTo("Initial note");
        assertThat(relRepeat.createdAt()).isEqualTo(rel1.createdAt());
        assertThat(relRepeat.updatedAt()).isEqualTo(rel1.updatedAt());

        // Ensure time moves forward for subsequent mutable-state update
        Thread.sleep(50);

        // Subsequent update: changes mutable fields and provides a different source (SNAPSHOT)
        ExternalAccountRelationshipView rel2 = relationshipOperations.setRelationship(new SetExternalAccountRelationshipCommand(
                owner.id(), target.id(), FollowerStatus.CURRENT_FOLLOWER, RelationshipSource.SNAPSHOT,
                FollowStatus.FOLLOWED, true, "Updated note"
        ));

        assertThat(rel2.id()).isEqualTo(rel1.id());
        // Creation provenance PRESERVED: source remains MANUAL even though subsequent command specified SNAPSHOT
        assertThat(rel2.source()).isEqualTo(RelationshipSource.MANUAL);
        assertThat(rel2.followStatus()).isEqualTo(FollowStatus.FOLLOWED);
        assertThat(rel2.hasLikedPost()).isTrue();
        assertThat(rel2.note()).isEqualTo("Updated note");
        assertThat(rel2.createdAt()).isEqualTo(rel1.createdAt());
        assertThat(rel2.updatedAt()).isAfter(rel1.updatedAt());

        // Verify single row in DB
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM external_account_relationships WHERE owner_account_id = ? AND target_account_id = ?",
                Integer.class,
                owner.id(), target.id()
        );
        assertThat(count).isEqualTo(1);

        // Verify findByPair
        Optional<ExternalAccountRelationshipView> found = relationshipOperations.findByPair(owner.id(), target.id());
        assertThat(found).isPresent();
        assertThat(found.get().note()).isEqualTo("Updated note");
        assertThat(found.get().source()).isEqualTo(RelationshipSource.MANUAL);
    }

    @Test
    @DisplayName("Rejects self-relationship where owner equals target")
    void relationshipRejectsSelfReference() {
        ExternalAccountView acc = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                "self_user", null, null, null, null, null, null, null, null
        ));

        assertThatThrownBy(() -> relationshipOperations.setRelationship(new SetExternalAccountRelationshipCommand(
                acc.id(), acc.id(), FollowerStatus.CURRENT_FOLLOWER, RelationshipSource.MANUAL,
                FollowStatus.FOLLOWED, false, null
        )))
                .isInstanceOf(InvalidExternalAccountRelationshipException.class)
                .hasMessageContaining("Owner account ID and target account ID must not be the same");
    }

    @Test
    @DisplayName("Concurrent relationship writes to same pair preserve one row and coherent command state")
    void concurrentRelationshipSamePairWritesPreserveOneCoherentCommandState() throws Exception {
        ExternalAccountView owner = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                "conc_owner", null, null, null, null, null, null, null, null
        ));
        ExternalAccountView target = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                "conc_target", null, null, null, null, null, null, null, null
        ));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<ExternalAccountRelationshipView> f1 = executor.submit(() -> relationshipOperations.setRelationship(
                    new SetExternalAccountRelationshipCommand(
                            owner.id(), target.id(), FollowerStatus.CURRENT_FOLLOWER, RelationshipSource.MANUAL,
                            FollowStatus.NOT_FOLLOWED, false, "Command 1 State"
                    )
            ));
            Future<ExternalAccountRelationshipView> f2 = executor.submit(() -> relationshipOperations.setRelationship(
                    new SetExternalAccountRelationshipCommand(
                            owner.id(), target.id(), FollowerStatus.NO_LONGER_FOLLOWING, RelationshipSource.API,
                            FollowStatus.FOLLOWED, true, "Command 2 State"
                    )
            ));

            ExternalAccountRelationshipView r1 = f1.get(10, TimeUnit.SECONDS);
            ExternalAccountRelationshipView r2 = f2.get(10, TimeUnit.SECONDS);

            assertThat(r1).isNotNull();
            assertThat(r2).isNotNull();

            // Verify exactly one row in DB
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM external_account_relationships WHERE owner_account_id = ? AND target_account_id = ?",
                    Integer.class,
                    owner.id(), target.id()
            );
            assertThat(count).isEqualTo(1);

            // Fetch final committed state
            ExternalAccountRelationshipView finalView = relationshipOperations.findByPair(owner.id(), target.id()).orElseThrow();

            // Mutable fields must match either Command 1 or Command 2 completely, never a torn mix
            boolean mutableMatchesCmd1 = finalView.followerStatus() == FollowerStatus.CURRENT_FOLLOWER
                    && finalView.followStatus() == FollowStatus.NOT_FOLLOWED
                    && !finalView.hasLikedPost()
                    && "Command 1 State".equals(finalView.note());

            boolean mutableMatchesCmd2 = finalView.followerStatus() == FollowerStatus.NO_LONGER_FOLLOWING
                    && finalView.followStatus() == FollowStatus.FOLLOWED
                    && finalView.hasLikedPost()
                    && "Command 2 State".equals(finalView.note());

            assertThat(mutableMatchesCmd1 || mutableMatchesCmd2)
                    .as("Final mutable state must be coherent from one complete command: %s", finalView)
                    .isTrue();

            // Creation provenance reflects whichever command inserted the row first
            assertThat(finalView.source())
                    .isIn(RelationshipSource.MANUAL, RelationshipSource.API);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Creates follower snapshot atomically with historical entries without mutating relationship state")
    void followerSnapshotCreationCommitsAtomicallyWithEntries() {
        ExternalAccountView owner = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                "snap_owner", null, null, null, null, null, null, null, null
        ));
        ExternalAccountView target1 = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                "target_live_1", "ext_1", "Live Display 1", null, null, null, null, null, null
        ));
        ExternalAccountView target2 = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                "target_live_2", "ext_2", "Live Display 2", null, null, null, null, null, null
        ));

        Instant captureTime = Instant.now().minus(Duration.ofHours(1));
        CreateFollowerSnapshotCommand cmd = new CreateFollowerSnapshotCommand(
                owner.id(),
                captureTime,
                FollowerSnapshotSource.IMPORT,
                1500,
                "export_2026_09.csv",
                List.of(
                        new CreateFollowerSnapshotEntryCommand(target1.id(), "hist_user_1", "Hist Display 1", "hist_ext_1", "https://profile.com/1"),
                        new CreateFollowerSnapshotEntryCommand(target2.id(), "hist_user_2", "Hist Display 2", "hist_ext_2", "https://profile.com/2")
                )
        );

        FollowerSnapshotView snapshot = snapshotOperations.createSnapshot(cmd);

        assertThat(snapshot.id()).isNotNull();
        assertThat(snapshot.ownerAccountId()).isEqualTo(owner.id());
        assertThat(snapshot.source()).isEqualTo(FollowerSnapshotSource.IMPORT);
        assertThat(snapshot.reportedTotalCount()).isEqualTo(1500);
        assertThat(snapshot.importedFileName()).isEqualTo("export_2026_09.csv");
        assertThat(snapshot.entryCount()).isEqualTo(2);

        // Verify entries in DB with historical copies preserved
        List<FollowerSnapshotEntryView> entries = snapshotOperations.findEntriesBySnapshotId(snapshot.id(), 10);
        assertThat(entries).hasSize(2);
        assertThat(entries).anySatisfy(e -> {
            assertThat(e.targetAccountId()).isEqualTo(target1.id());
            assertThat(e.usernameSnapshot()).isEqualTo("hist_user_1");
            assertThat(e.displayNameSnapshot()).isEqualTo("Hist Display 1");
            assertThat(e.externalIdSnapshot()).isEqualTo("hist_ext_1");
            assertThat(e.profileUrlSnapshot()).isEqualTo("https://profile.com/1");
        });

        // Verify relationship state was NOT mutated
        Integer relCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM external_account_relationships",
                Integer.class
        );
        assertThat(relCount).isZero();
    }

    @Test
    @DisplayName("Collapses identical duplicate entries for the same target in one snapshot batch")
    void followerSnapshotCollapsesIdenticalDuplicateEntries() {
        ExternalAccountView owner = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                "owner_dup", null, null, null, null, null, null, null, null
        ));
        ExternalAccountView target = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                "target_dup", null, null, null, null, null, null, null, null
        ));

        CreateFollowerSnapshotCommand cmd = new CreateFollowerSnapshotCommand(
                owner.id(),
                Instant.now(),
                FollowerSnapshotSource.MANUAL,
                1,
                null,
                List.of(
                        new CreateFollowerSnapshotEntryCommand(target.id(), "same_user", "Same Name", "ext_same", "https://url.com"),
                        new CreateFollowerSnapshotEntryCommand(target.id(), " same_user ", "Same Name", "ext_same", "https://url.com")
                )
        );

        FollowerSnapshotView snapshot = snapshotOperations.createSnapshot(cmd);
        assertThat(snapshot.entryCount()).isEqualTo(1);

        List<FollowerSnapshotEntryView> entries = snapshotOperations.findEntriesBySnapshotId(snapshot.id(), 10);
        assertThat(entries).hasSize(1);
        assertThat(entries.getFirst().usernameSnapshot()).isEqualTo("same_user");
    }

    @Test
    @DisplayName("Rejects snapshot batch with conflicting historical copies and commits neither header nor entries")
    void followerSnapshotRejectsConflictingHistoricalCopiesAndCommitsNothing() {
        ExternalAccountView owner = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                "owner_conflict", null, null, null, null, null, null, null, null
        ));
        ExternalAccountView target = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                "target_conflict", null, null, null, null, null, null, null, null
        ));

        CreateFollowerSnapshotCommand cmd = new CreateFollowerSnapshotCommand(
                owner.id(),
                Instant.now(),
                FollowerSnapshotSource.MANUAL,
                2,
                null,
                List.of(
                        new CreateFollowerSnapshotEntryCommand(target.id(), "user_copy_a", "Display A", null, null),
                        new CreateFollowerSnapshotEntryCommand(target.id(), "user_copy_b", "Display A", null, null)
                )
        );

        assertThatThrownBy(() -> snapshotOperations.createSnapshot(cmd))
                .isInstanceOf(InvalidFollowerSnapshotException.class)
                .hasMessageContaining("Conflicting historical snapshot entries submitted for target account ID " + target.id());

        Integer snapshotCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM follower_snapshots",
                Integer.class
        );
        assertThat(snapshotCount).isZero();

        Integer entryCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM follower_snapshot_entries",
                Integer.class
        );
        assertThat(entryCount).isZero();
    }

    @Test
    @DisplayName("Enforces bounded reads and deterministic ordering on recent queries")
    void boundedReadsAndOrdering() {
        ExternalAccountView owner = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                "owner_reads", null, null, null, null, null, null, null, null
        ));

        for (int i = 1; i <= 5; i++) {
            ExternalAccountView target = accountOperations.create(new CreateExternalAccountCommand(
                    testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                    "target_read_" + i, null, null, null, null, null, null, null, null
            ));
            relationshipOperations.setRelationship(new SetExternalAccountRelationshipCommand(
                    owner.id(), target.id(), FollowerStatus.CURRENT_FOLLOWER, RelationshipSource.MANUAL,
                    FollowStatus.NOT_FOLLOWED, false, "Note " + i
            ));
        }

        // Bounded relationships: limit 2
        List<ExternalAccountRelationshipView> rels = relationshipOperations.findRecentByOwner(owner.id(), 2);
        assertThat(rels).hasSize(2);

        // Bounded accounts by platform: limit 3
        List<ExternalAccountView> accounts = accountOperations.findRecentByPlatformId(testPlatformId, 3);
        assertThat(accounts).hasSize(3);

        // Create 4 snapshots
        for (int i = 1; i <= 4; i++) {
            snapshotOperations.createSnapshot(new CreateFollowerSnapshotCommand(
                    owner.id(), Instant.now().minus(Duration.ofMinutes(i)), FollowerSnapshotSource.MANUAL,
                    i, null, List.of()
            ));
        }

        // Bounded snapshots: limit 2
        List<FollowerSnapshotView> snaps = snapshotOperations.findRecentByOwner(owner.id(), 2);
        assertThat(snaps).hasSize(2);
    }

    @Test
    @DisplayName("Follower snapshot creation performs bulk target validation, avoids follower entry selects via known-new state, and recent reads use grouped count projection")
    void followerSnapshotBulkExistenceValidationAndGroupedCountReads() {
        ExternalAccountView owner = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL,
                "snap_owner_bulk", null, null, null, null, null, null, null, null
        ));
        ExternalAccountView target1 = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                "snap_target_bulk_1", null, null, null, null, null, null, null, null
        ));
        ExternalAccountView target2 = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                "snap_target_bulk_2", null, null, null, null, null, null, null, null
        ));
        ExternalAccountView target3 = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                "snap_target_bulk_3", null, null, null, null, null, null, null, null
        ));
        ExternalAccountView target4 = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                "snap_target_bulk_4", null, null, null, null, null, null, null, null
        ));
        ExternalAccountView target5 = accountOperations.create(new CreateExternalAccountCommand(
                testPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL,
                "snap_target_bulk_5", null, null, null, null, null, null, null, null
        ));

        record BatchTestCase(int batchSize, List<CreateFollowerSnapshotEntryCommand> entries) {}

        List<BatchTestCase> batchCases = List.of(
                new BatchTestCase(1, List.of(
                        new CreateFollowerSnapshotEntryCommand(target1.id(), "u1", "D1", "ext1", "https://url1")
                )),
                new BatchTestCase(3, List.of(
                        new CreateFollowerSnapshotEntryCommand(target1.id(), "u1_v2", "D1_v2", "ext1", "https://url1"),
                        new CreateFollowerSnapshotEntryCommand(target2.id(), "u2", "D2", "ext2", "https://url2"),
                        new CreateFollowerSnapshotEntryCommand(target3.id(), "u3", "D3", "ext3", "https://url3")
                )),
                new BatchTestCase(5, List.of(
                        new CreateFollowerSnapshotEntryCommand(target1.id(), "u1_v3", "D1_v3", "ext1", "https://url1"),
                        new CreateFollowerSnapshotEntryCommand(target2.id(), "u2_v3", "D2_v3", "ext2", "https://url2"),
                        new CreateFollowerSnapshotEntryCommand(target3.id(), "u3_v3", "D3_v3", "ext3", "https://url3"),
                        new CreateFollowerSnapshotEntryCommand(target4.id(), "u4", "D4", "ext4", "https://url4"),
                        new CreateFollowerSnapshotEntryCommand(target5.id(), "u5", "D5", "ext5", "https://url5")
                ))
        );

        Logger sqlLogger = (Logger) LoggerFactory.getLogger("org.hibernate.SQL");
        Level previousSqlLevel = sqlLogger.getLevel();

        List<FollowerSnapshotView> createdSnapshots = new ArrayList<>();

        for (int i = 0; i < batchCases.size(); i++) {
            BatchTestCase testCase = batchCases.get(i);
            ListAppender<ILoggingEvent> sqlAppender = new ListAppender<>();
            sqlAppender.start();
            sqlLogger.addAppender(sqlAppender);
            sqlLogger.setLevel(Level.DEBUG);

            FollowerSnapshotView snap;
            try {
                snap = snapshotOperations.createSnapshot(new CreateFollowerSnapshotCommand(
                        owner.id(),
                        Instant.now().minus(Duration.ofMinutes(10L * (batchCases.size() - i))),
                        FollowerSnapshotSource.MANUAL,
                        testCase.batchSize(),
                        "batch_" + testCase.batchSize() + ".json",
                        testCase.entries()
                ));
            } finally {
                sqlLogger.detachAppender(sqlAppender);
                sqlLogger.setLevel(previousSqlLevel);
            }

            assertThat(snap.id()).isNotNull();
            assertThat(snap.entryCount()).isEqualTo(testCase.batchSize());
            createdSnapshots.add(snap);

            List<String> createSnapshotSql = sqlAppender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .toList();

            // 1. SELECT queries on external_accounts: exactly 2 (owner + bulk targets)
            List<String> accountSelectQueries = createSnapshotSql.stream()
                    .filter(sql -> sql.toLowerCase().contains("select") && sql.toLowerCase().contains("external_accounts"))
                    .toList();

            assertThat(accountSelectQueries)
                    .as("Batch size %d must issue exactly 2 external_accounts SELECT queries: 1 for owner and 1 bulk IN query for targets", testCase.batchSize())
                    .hasSize(2);

            assertThat(accountSelectQueries.get(1).toLowerCase())
                    .as("Batch size %d target accounts validation query must use SQL IN clause", testCase.batchSize())
                    .contains(" in ");

            // 2. Zero SELECT queries referencing follower_snapshot_entries (proves known-new Persistable semantics)
            List<String> entrySelectQueries = createSnapshotSql.stream()
                    .filter(sql -> sql.toLowerCase().contains("select") && sql.toLowerCase().contains("follower_snapshot_entries"))
                    .toList();

            assertThat(entrySelectQueries)
                    .as("Batch size %d must issue exactly 0 SELECT queries on follower_snapshot_entries during snapshot creation", testCase.batchSize())
                    .isEmpty();

            // 3. Persisted entries and historical snapshot values round-trip accurately
            List<FollowerSnapshotEntryView> savedEntries = snapshotOperations.findEntriesBySnapshotId(snap.id(), 10);
            assertThat(savedEntries).hasSize(testCase.batchSize());
            for (CreateFollowerSnapshotEntryCommand expectedEntry : testCase.entries()) {
                assertThat(savedEntries).anySatisfy(se -> {
                    assertThat(se.targetAccountId()).isEqualTo(expectedEntry.targetAccountId());
                    assertThat(se.usernameSnapshot()).isEqualTo(expectedEntry.usernameSnapshot());
                    assertThat(se.displayNameSnapshot()).isEqualTo(expectedEntry.displayNameSnapshot());
                    assertThat(se.externalIdSnapshot()).isEqualTo(expectedEntry.externalIdSnapshot());
                    assertThat(se.profileUrlSnapshot()).isEqualTo(expectedEntry.profileUrlSnapshot());
                });
            }
        }

        // Snapshot with 0 entries
        FollowerSnapshotView snap0 = snapshotOperations.createSnapshot(new CreateFollowerSnapshotCommand(
                owner.id(), Instant.now(), FollowerSnapshotSource.MANUAL,
                0, null, List.of()
        ));
        assertThat(snap0.id()).isNotNull();
        assertThat(snap0.entryCount()).isEqualTo(0);

        // Verify findRecentByOwner retrieves all 4 snapshots with accurate grouped entry counts in one roundtrip
        ListAppender<ILoggingEvent> readSqlAppender = new ListAppender<>();
        readSqlAppender.start();
        sqlLogger.addAppender(readSqlAppender);
        sqlLogger.setLevel(Level.DEBUG);

        Statistics stats = getHibernateStatistics();
        stats.clear();

        List<FollowerSnapshotView> recent;
        try {
            recent = snapshotOperations.findRecentByOwner(owner.id(), 10);
        } finally {
            sqlLogger.detachAppender(readSqlAppender);
            sqlLogger.setLevel(previousSqlLevel);
        }

        assertThat(recent).hasSize(4);
        // Ordered by capturedAt DESC (snap0, batch5, batch3, batch1)
        assertThat(recent.get(0).id()).isEqualTo(snap0.id());
        assertThat(recent.get(0).entryCount()).isEqualTo(0);
        assertThat(recent.get(1).id()).isEqualTo(createdSnapshots.get(2).id());
        assertThat(recent.get(1).entryCount()).isEqualTo(5);
        assertThat(recent.get(2).id()).isEqualTo(createdSnapshots.get(1).id());
        assertThat(recent.get(2).entryCount()).isEqualTo(3);
        assertThat(recent.get(3).id()).isEqualTo(createdSnapshots.get(0).id());
        assertThat(recent.get(3).entryCount()).isEqualTo(1);

        // Observable SQL query-shape assertions for recent snapshot reads:
        List<String> readQueries = readSqlAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();

        // Exactly 2 total queries executed:
        // 1 for headers (follower_snapshots) + 1 grouped count for all headers (follower_snapshot_entries)
        assertThat(readQueries)
                .as("Recent snapshots read must execute exactly 2 queries: 1 for headers and 1 grouped count projection")
                .hasSize(2);

        // Verify Hibernate prepared statement count matches exactly 2
        assertThat(stats.getPrepareStatementCount())
                .as("Hibernate prepared statement count must be exactly 2 for recent snapshots read across multiple headers")
                .isEqualTo(2);

        // Filter queries on follower_snapshot_entries
        List<String> entryCountQueries = readQueries.stream()
                .filter(sql -> sql.toLowerCase().contains("follower_snapshot_entries"))
                .toList();

        assertThat(entryCountQueries)
                .as("Exactly 1 entry count query must be issued across all headers")
                .hasSize(1);

        assertThat(entryCountQueries.getFirst().toLowerCase())
                .as("Entry count query must be a GROUP BY projection using SQL IN clause")
                .contains("group by")
                .contains("count")
                .contains(" in ");

        // Verify findById returns exact entry count
        Optional<FollowerSnapshotView> foundSnap = snapshotOperations.findById(createdSnapshots.get(1).id());
        assertThat(foundSnap).isPresent();
        assertThat(foundSnap.get().entryCount()).isEqualTo(3);

        // Bulk validation failure: batch containing existing and non-existent targets fails atomically
        Long nonExistentTargetId = 9_999_999L;
        assertThatThrownBy(() -> snapshotOperations.createSnapshot(new CreateFollowerSnapshotCommand(
                owner.id(), Instant.now(), FollowerSnapshotSource.MANUAL, 10, null,
                List.of(
                        new CreateFollowerSnapshotEntryCommand(target1.id(), "u1", null, null, null),
                        new CreateFollowerSnapshotEntryCommand(nonExistentTargetId, "ghost", null, null, null)
                )
        )))
                .isInstanceOf(InvalidFollowerSnapshotException.class)
                .hasMessageContaining("Target account with id " + nonExistentTargetId + " does not exist");

        // Verify no orphan snapshot was created in DB for this failed attempt (4 total valid snapshots exist)
        Integer totalSnapshots = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM follower_snapshots WHERE owner_account_id = ?",
                Integer.class,
                owner.id()
        );
        assertThat(totalSnapshots).isEqualTo(4);
    }

    private void awaitCompetingLock(String tablePattern, Duration timeout) throws Exception {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM pg_locks l " +
                    "JOIN pg_stat_activity a ON l.pid = a.pid " +
                    "WHERE NOT l.granted " +
                    "  AND a.pid != pg_backend_pid() " +
                    "  AND a.query ILIKE ?",
                    Integer.class,
                    "%" + tablePattern + "%"
            );
            if (count != null && count > 0) {
                return;
            }
            Thread.sleep(10);
        }
        throw new AssertionError("Timed out waiting for competing transaction to reach PostgreSQL lock on " + tablePattern);
    }
}
