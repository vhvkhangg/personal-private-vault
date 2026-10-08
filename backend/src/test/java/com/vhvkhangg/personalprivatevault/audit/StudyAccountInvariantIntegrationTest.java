package com.vhvkhangg.personalprivatevault.audit;

import com.vhvkhangg.personalprivatevault.account.account.CreateExternalAccountCommand;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountConflictException;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountOperations;
import com.vhvkhangg.personalprivatevault.account.account.UpdateExternalAccountCommand;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountOwnership;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;
import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountView;
import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyType;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.CreateStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyItemOperations;
import com.vhvkhangg.personalprivatevault.knowledge.study.view.StudyItemView;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BA15-13 regression test:
 * Verifies that the YouTube Study / ExternalAccount invariant is preserved after assignment,
 * preventing mutation of referenced accounts away from YOUTUBE_CHANNEL type or YouTube platform,
 * and refreshing authoritative state under pessimistic lock to prevent stale-read race defects.
 */
class StudyAccountInvariantIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private ExternalAccountOperations externalAccountOperations;

    @Autowired
    private StudyItemOperations studyItemOperations;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long youtubePlatformId;
    private Long twitterPlatformId;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newCachedThreadPool();

        youtubePlatformId = jdbcTemplate.query(
                "SELECT id FROM platforms WHERE name = 'YouTube'",
                (rs, rowNum) -> rs.getLong("id")
        ).stream().findFirst().orElseGet(() -> jdbcTemplate.queryForObject(
                "INSERT INTO platforms (name, kind, url) VALUES ('YouTube', 'MEDIA'::platform_kind, 'https://youtube.com') RETURNING id",
                Long.class
        ));

        twitterPlatformId = jdbcTemplate.query(
                "SELECT id FROM platforms WHERE name = 'Twitter'",
                (rs, rowNum) -> rs.getLong("id")
        ).stream().findFirst().orElseGet(() -> jdbcTemplate.queryForObject(
                "INSERT INTO platforms (name, kind, url) VALUES ('Twitter', 'SOCIAL'::platform_kind, 'https://twitter.com') RETURNING id",
                Long.class
        ));
    }

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
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

    @Test
    @DisplayName("BA15-13: Mutating external account away from YOUTUBE_CHANNEL is rejected when referenced by Study item")
    void mutatingTypeAwayFromYouTubeChannelRejectedWhenReferenced() {
        ExternalAccountView account = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId,
                ExternalAccountOwnership.TRACKED,
                ExternalAccountType.YOUTUBE_CHANNEL,
                "fireship",
                "UC_fireship_channel",
                "Fireship",
                null,
                null,
                null,
                null,
                "https://youtube.com/@fireship",
                "Dev channel"
        ));

        StudyItemView studyItem = studyItemOperations.create(new CreateStudyItemCommand(
                "Fireship Channel",
                null,
                StudyType.YOUTUBE_CHANNEL,
                null,
                account.id(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));
        assertThat(studyItem.youtubeChannelAccountId()).isEqualTo(account.id());

        // Attempt to change account type away from YOUTUBE_CHANNEL to OTHER
        assertThatThrownBy(() -> externalAccountOperations.update(account.id(), new UpdateExternalAccountCommand(
                youtubePlatformId,
                ExternalAccountOwnership.TRACKED,
                ExternalAccountType.OTHER,
                "fireship",
                "UC_fireship_channel",
                "Fireship",
                null,
                null,
                null,
                null,
                "https://youtube.com/@fireship",
                "Dev channel"
        ))).isInstanceOf(ExternalAccountConflictException.class)
                .hasMessageContaining("Cannot change account type away from YOUTUBE_CHANNEL");
    }

    @Test
    @DisplayName("BA15-13: Mutating platform away from YouTube is rejected when referenced by Study item")
    void mutatingPlatformAwayFromYouTubeRejectedWhenReferenced() {
        ExternalAccountView account = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId,
                ExternalAccountOwnership.TRACKED,
                ExternalAccountType.YOUTUBE_CHANNEL,
                "veritasium",
                "UC_veritasium_channel",
                "Veritasium",
                null,
                null,
                null,
                null,
                "https://youtube.com/@veritasium",
                "Science channel"
        ));

        studyItemOperations.create(new CreateStudyItemCommand(
                "Veritasium Channel",
                null,
                StudyType.YOUTUBE_CHANNEL,
                null,
                account.id(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        // Attempt to change platform from YouTube to Twitter
        assertThatThrownBy(() -> externalAccountOperations.update(account.id(), new UpdateExternalAccountCommand(
                twitterPlatformId,
                ExternalAccountOwnership.TRACKED,
                ExternalAccountType.YOUTUBE_CHANNEL,
                "veritasium",
                "UC_veritasium_channel",
                "Veritasium",
                null,
                null,
                null,
                null,
                "https://youtube.com/@veritasium",
                "Science channel"
        ))).isInstanceOf(ExternalAccountConflictException.class)
                .hasMessageContaining("Cannot change platform away from YouTube");
    }

    @Test
    @DisplayName("BA15-13: Normal metadata update on referenced account succeeds when type and platform remain unchanged")
    void normalUpdateOnReferencedAccountSucceeds() {
        ExternalAccountView account = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId,
                ExternalAccountOwnership.TRACKED,
                ExternalAccountType.YOUTUBE_CHANNEL,
                "kurzgesagt",
                "UC_kurzgesagt",
                "Kurzgesagt",
                null,
                null,
                null,
                null,
                "https://youtube.com/@kurzgesagt",
                "In a nutshell"
        ));

        studyItemOperations.create(new CreateStudyItemCommand(
                "Kurzgesagt Channel",
                null,
                StudyType.YOUTUBE_CHANNEL,
                null,
                account.id(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        ExternalAccountView updated = externalAccountOperations.update(account.id(), new UpdateExternalAccountCommand(
                youtubePlatformId,
                ExternalAccountOwnership.TRACKED,
                ExternalAccountType.YOUTUBE_CHANNEL,
                "kurzgesagt",
                "UC_kurzgesagt",
                "Kurzgesagt - Updated Display Name",
                null,
                null,
                null,
                null,
                "https://youtube.com/@kurzgesagt",
                "Updated notes"
        ));

        assertThat(updated.displayName()).isEqualTo("Kurzgesagt - Updated Display Name");
    }

    @Test
    @DisplayName("BA15-13: Unreferenced account can mutate type and platform freely")
    void unreferencedAccountMutatesFreely() {
        ExternalAccountView unreferenced = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId,
                ExternalAccountOwnership.TRACKED,
                ExternalAccountType.YOUTUBE_CHANNEL,
                "unreferenced",
                "UC_unref",
                "Unreferenced",
                null,
                null,
                null,
                null,
                "https://youtube.com/@unref",
                null
        ));

        ExternalAccountView updated = externalAccountOperations.update(unreferenced.id(), new UpdateExternalAccountCommand(
                twitterPlatformId,
                ExternalAccountOwnership.TRACKED,
                ExternalAccountType.SOCIAL,
                "unreferenced_tw",
                "TW_unref",
                "Twitter Account",
                null,
                null,
                null,
                null,
                "https://twitter.com/unreferenced_tw",
                null
        ));

        assertThat(updated.accountType()).isEqualTo(ExternalAccountType.SOCIAL);
        assertThat(updated.platformId()).isEqualTo(twitterPlatformId);
    }

    @Test
    @DisplayName("BA15-13 / FR15-1: Counterexample 1 - Prior managed read of OTHER account refreshed under lock detects concurrent referencing Study and rejects invalidating update")
    void counterexample1PriorManagedReadRefreshedUnderLockDetectsReferencingStudy() throws Exception {
        ExternalAccountView account = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId,
                ExternalAccountOwnership.TRACKED,
                ExternalAccountType.OTHER,
                "initial_other",
                "EXT_1",
                "Initial Other",
                null, null, null, null,
                "https://example.com/other",
                null
        ));
        Long accountId = account.id();

        CountDownLatch t1ReadLatch = new CountDownLatch(1);
        CountDownLatch t2CommitLatch = new CountDownLatch(1);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        Future<?> f1 = executor.submit(() -> {
            txTemplate.execute(status -> {
                ExternalAccountView priorRead = externalAccountOperations.findById(accountId).orElseThrow();
                assertThat(priorRead.accountType()).isEqualTo(ExternalAccountType.OTHER);

                t1ReadLatch.countDown();
                try {
                    t2CommitLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                externalAccountOperations.update(accountId, new UpdateExternalAccountCommand(
                        youtubePlatformId,
                        ExternalAccountOwnership.TRACKED,
                        ExternalAccountType.OTHER,
                        "updated_username_other",
                        "EXT_1",
                        "Updated Display Name",
                        null, null, null, null,
                        "https://example.com/other",
                        null
                ));
                return null;
            });
            return null;
        });

        assertThat(t1ReadLatch.await(5, TimeUnit.SECONDS)).isTrue();

        txTemplate.execute(status -> {
            externalAccountOperations.update(accountId, new UpdateExternalAccountCommand(
                    youtubePlatformId,
                    ExternalAccountOwnership.TRACKED,
                    ExternalAccountType.YOUTUBE_CHANNEL,
                    "now_yt",
                    "EXT_1",
                    "Now YouTube",
                    null, null, null, null,
                    "https://youtube.com/@now_yt",
                    null
            ));
            studyItemOperations.create(new CreateStudyItemCommand(
                    "Referencing Study",
                    null,
                    StudyType.YOUTUBE_CHANNEL,
                    null,
                    accountId,
                    null, null, null, null, null, null, null, null, null, null, null
            ));
            return null;
        });
        t2CommitLatch.countDown();

        assertThatThrownBy(() -> f1.get(10, TimeUnit.SECONDS))
                .hasCauseInstanceOf(ExternalAccountConflictException.class);

        ExternalAccountView finalAccount = externalAccountOperations.findById(accountId).orElseThrow();
        assertThat(finalAccount.accountType()).isEqualTo(ExternalAccountType.YOUTUBE_CHANNEL);
    }

    @Test
    @DisplayName("BA15-13 / FR15-1: Counterexample 2 - Prior managed read of valid YouTube account refreshed under findAndLock sees concurrent mutation to OTHER and rejects Study creation")
    void counterexample2PriorManagedReadRefreshedUnderFindAndLockRejectsStudyCreation() throws Exception {
        ExternalAccountView account = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId,
                ExternalAccountOwnership.TRACKED,
                ExternalAccountType.YOUTUBE_CHANNEL,
                "yt_valid",
                "EXT_2",
                "Valid YouTube",
                null, null, null, null,
                "https://youtube.com/@valid",
                null
        ));
        Long accountId = account.id();

        CountDownLatch t1ReadLatch = new CountDownLatch(1);
        CountDownLatch t2CommitLatch = new CountDownLatch(1);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        Future<?> f1 = executor.submit(() -> {
            txTemplate.execute(status -> {
                ExternalAccountView priorRead = externalAccountOperations.findById(accountId).orElseThrow();
                assertThat(priorRead.accountType()).isEqualTo(ExternalAccountType.YOUTUBE_CHANNEL);

                t1ReadLatch.countDown();
                try {
                    t2CommitLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                studyItemOperations.create(new CreateStudyItemCommand(
                        "New Study Item",
                        null,
                        StudyType.YOUTUBE_CHANNEL,
                        null,
                        accountId,
                        null, null, null, null, null, null, null, null, null, null, null
                ));
                return null;
            });
            return null;
        });

        assertThat(t1ReadLatch.await(5, TimeUnit.SECONDS)).isTrue();

        txTemplate.execute(status -> {
            externalAccountOperations.update(accountId, new UpdateExternalAccountCommand(
                    youtubePlatformId,
                    ExternalAccountOwnership.TRACKED,
                    ExternalAccountType.OTHER,
                    "mutated_to_other",
                    "EXT_2",
                    "Mutated to Other",
                    null, null, null, null,
                    "https://example.com/other",
                    null
            ));
            return null;
        });
        t2CommitLatch.countDown();

        assertThatThrownBy(() -> f1.get(10, TimeUnit.SECONDS))
                .hasCauseInstanceOf(com.vhvkhangg.personalprivatevault.knowledge.study.study.InvalidStudyItemException.class);
    }

    @Test
    @DisplayName("BA15-13 / FR15-1: Forced race - Study assignment update versus concurrent account invalidating mutation detects mutation and rolls back")
    void studyAssignmentUpdateVersusConcurrentAccountInvalidatingMutationRace() throws Exception {
        ExternalAccountView ytOrig = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                "yt_orig_race", "EXT_RACE_1", "Orig Race", null, null, null, null, "https://youtube.com/@orig_race", null
        ));
        ExternalAccountView ytCandidate = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                "yt_cand_race", "EXT_RACE_2", "Candidate Race", null, null, null, null, "https://youtube.com/@cand_race", null
        ));

        StudyItemView study = studyItemOperations.create(new CreateStudyItemCommand(
                "Study Item Race", null, StudyType.YOUTUBE_CHANNEL, null, ytOrig.id(), null, null, null, null, null, null, null, null, null, null, null
        ));
        Long studyId = study.id();
        Long candidateId = ytCandidate.id();

        CountDownLatch t1ReadLatch = new CountDownLatch(1);
        CountDownLatch t2CommitLatch = new CountDownLatch(1);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        Future<?> f1 = executor.submit(() -> {
            txTemplate.execute(status -> {
                ExternalAccountView priorRead = externalAccountOperations.findById(candidateId).orElseThrow();
                assertThat(priorRead.accountType()).isEqualTo(ExternalAccountType.YOUTUBE_CHANNEL);

                t1ReadLatch.countDown();
                try {
                    t2CommitLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Study update assigning the candidate account - findAndLock will see refreshed state
                studyItemOperations.update(studyId, new com.vhvkhangg.personalprivatevault.knowledge.study.study.UpdateStudyItemCommand(
                        "Study Item Updated Concurrently", null, StudyType.YOUTUBE_CHANNEL, null, candidateId,
                        null, null, null, null, null, null, null, null, null, null, null
                ));
                return null;
            });
            return null;
        });

        assertThat(t1ReadLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // T2 mutates candidate account away from YOUTUBE_CHANNEL to OTHER and commits
        txTemplate.execute(status -> {
            externalAccountOperations.update(candidateId, new UpdateExternalAccountCommand(
                    youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.OTHER,
                    "mutated_to_other", "EXT_RACE_2", "Mutated Other", null, null, null, null, "https://example.com/other", null
            ));
            return null;
        });
        t2CommitLatch.countDown();

        assertThatThrownBy(() -> f1.get(10, TimeUnit.SECONDS))
                .hasCauseInstanceOf(com.vhvkhangg.personalprivatevault.knowledge.study.study.InvalidStudyItemException.class)
                .hasMessageContaining("must be of type YOUTUBE_CHANNEL");

        // Verify Study item rollback: retains original reference to ytOrig, not mutated candidate
        StudyItemView finalStudy = studyItemOperations.findById(studyId).orElseThrow();
        assertThat(finalStudy.youtubeChannelAccountId()).isEqualTo(ytOrig.id());
        assertThat(finalStudy.title()).isEqualTo("Study Item Race");
    }

    @Test
    @DisplayName("BA15-13 / FR15-1: Forced race - Account invalidating mutation versus concurrent Study assignment update detects reference and rolls back")
    void accountInvalidatingMutationVersusConcurrentStudyAssignmentUpdateRace() throws Exception {
        ExternalAccountView ytCandidate = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                "yt_cand_race3", "EXT_RACE_3", "Candidate Race 3", null, null, null, null, "https://youtube.com/@cand_race3", null
        ));
        StudyItemView study = studyItemOperations.create(new CreateStudyItemCommand(
                "Study Item Race 3", null, StudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, null, null
        ));
        Long candidateId = ytCandidate.id();
        Long studyId = study.id();

        CountDownLatch t1ReadLatch = new CountDownLatch(1);
        CountDownLatch t2CommitLatch = new CountDownLatch(1);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        Future<?> f1 = executor.submit(() -> {
            txTemplate.execute(status -> {
                ExternalAccountView priorRead = externalAccountOperations.findById(candidateId).orElseThrow();
                assertThat(priorRead.accountType()).isEqualTo(ExternalAccountType.YOUTUBE_CHANNEL);

                t1ReadLatch.countDown();
                try {
                    t2CommitLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // T1 attempts to mutate account to OTHER - will check isReferencedByStudy under lock
                externalAccountOperations.update(candidateId, new UpdateExternalAccountCommand(
                        youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.OTHER,
                        "mutated_to_other3", "EXT_RACE_3", "Mutated Other 3", null, null, null, null, "https://example.com/other3", null
                ));
                return null;
            });
            return null;
        });

        assertThat(t1ReadLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Concurrent T2 assigns candidate account to study item and commits
        txTemplate.execute(status -> {
            studyItemOperations.update(studyId, new com.vhvkhangg.personalprivatevault.knowledge.study.study.UpdateStudyItemCommand(
                    "Study Item Assigned Candidate", null, StudyType.YOUTUBE_CHANNEL, null, candidateId,
                    null, null, null, null, null, null, null, null, null, null, null
            ));
            return null;
        });
        t2CommitLatch.countDown();

        assertThatThrownBy(() -> f1.get(10, TimeUnit.SECONDS))
                .hasCauseInstanceOf(ExternalAccountConflictException.class)
                .hasMessageContaining("Cannot change account type away from YOUTUBE_CHANNEL");

        // Verify Account mutation rollback: retains YOUTUBE_CHANNEL
        ExternalAccountView finalAccount = externalAccountOperations.findById(candidateId).orElseThrow();
        assertThat(finalAccount.accountType()).isEqualTo(ExternalAccountType.YOUTUBE_CHANNEL);
        assertThat(finalAccount.platformId()).isEqualTo(youtubePlatformId);

        // Verify Study item retains assignment
        StudyItemView finalStudy = studyItemOperations.findById(studyId).orElseThrow();
        assertThat(finalStudy.youtubeChannelAccountId()).isEqualTo(candidateId);
    }

    @Test
    @DisplayName("BA15-13 / FR15-7: Competing writers - Account mutation to OTHER holds lock; concurrent Study create blocks on guard, unblocks and rolls back")
    void competingWritersAccountTypeMutationHoldsLockVsConcurrentStudyCreateBlocksAndRollsBack() throws Exception {
        ExternalAccountView candidate = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                "yt_compete_1", "EXT_COMP_1", "Compete 1", null, null, null, null, "https://youtube.com/@comp1", null
        ));
        Long candidateId = candidate.id();

        CountDownLatch t1HoldingLockLatch = new CountDownLatch(1);
        CountDownLatch t1ReleaseLatch = new CountDownLatch(1);
        AtomicBoolean t2FinishedBeforeT1Commit = new AtomicBoolean(false);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // T1 starts transaction, updates candidate account to OTHER, holds lock without committing
        Future<?> t1Future = executor.submit(() -> {
            txTemplate.execute(status -> {
                externalAccountOperations.update(candidateId, new UpdateExternalAccountCommand(
                        youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.OTHER,
                        "mutated_other_comp1", "EXT_COMP_1", "Mutated Other", null, null, null, null, "https://example.com/other1", null
                ));
                t1HoldingLockLatch.countDown();
                try {
                    t1ReleaseLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            return null;
        });

        assertThat(t1HoldingLockLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // T2 attempts Study create assigning candidate account, which calls findAndLock(candidateId)
        Future<StudyItemView> t2Future = executor.submit(() -> {
            try {
                return txTemplate.execute(status -> {
                    return studyItemOperations.create(new CreateStudyItemCommand(
                            "Study Compete 1", null, StudyType.YOUTUBE_CHANNEL, null, candidateId,
                            null, null, null, null, null, null, null, null, null, null, null
                    ));
                });
            } finally {
                if (t1ReleaseLatch.getCount() > 0) {
                    t2FinishedBeforeT1Commit.set(true);
                }
            }
        });

        // Deterministically verify T2 is blocked on PostgreSQL row lock on external_accounts held by T1
        awaitCompetingLock("external_accounts", Duration.ofSeconds(5));
        assertThat(t2FinishedBeforeT1Commit.get()).isFalse();

        // Release T1 to commit
        t1ReleaseLatch.countDown();
        t1Future.get(10, TimeUnit.SECONDS);

        // T2 unblocks, observes refreshed account type OTHER under lock, throws InvalidStudyItemException and rolls back
        assertThatThrownBy(() -> t2Future.get(10, TimeUnit.SECONDS))
                .hasCauseInstanceOf(com.vhvkhangg.personalprivatevault.knowledge.study.study.InvalidStudyItemException.class)
                .hasMessageContaining("must be of type YOUTUBE_CHANNEL");

        // Verify account is OTHER and no Study item was created
        ExternalAccountView finalAccount = externalAccountOperations.findById(candidateId).orElseThrow();
        assertThat(finalAccount.accountType()).isEqualTo(ExternalAccountType.OTHER);
        Integer studyCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM study_items WHERE youtube_channel_account_id = ?",
                Integer.class,
                candidateId
        );
        assertThat(studyCount).isEqualTo(0);
    }

    @Test
    @DisplayName("BA15-13 / FR15-7: Competing writers - Account mutation platform away from YouTube holds lock; concurrent Study update blocks on guard, unblocks and rolls back")
    void competingWritersAccountPlatformMutationHoldsLockVsConcurrentStudyUpdateBlocksAndRollsBack() throws Exception {
        ExternalAccountView origAccount = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                "yt_orig_comp2", "EXT_ORIG_2", "Orig 2", null, null, null, null, "https://youtube.com/@orig2", null
        ));
        ExternalAccountView candidate = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                "yt_compete_2", "EXT_COMP_2", "Compete 2", null, null, null, null, "https://youtube.com/@comp2", null
        ));
        Long candidateId = candidate.id();

        StudyItemView existingStudy = studyItemOperations.create(new CreateStudyItemCommand(
                "Study Existing 2", null, StudyType.YOUTUBE_CHANNEL, null, origAccount.id(),
                null, null, null, null, null, null, null, null, null, null, null
        ));
        Long studyId = existingStudy.id();

        CountDownLatch t1HoldingLockLatch = new CountDownLatch(1);
        CountDownLatch t1ReleaseLatch = new CountDownLatch(1);
        AtomicBoolean t2FinishedBeforeT1Commit = new AtomicBoolean(false);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // T1 starts transaction, updates candidate platform away from YouTube to Twitter, holds lock
        Future<?> t1Future = executor.submit(() -> {
            txTemplate.execute(status -> {
                externalAccountOperations.update(candidateId, new UpdateExternalAccountCommand(
                        twitterPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                        "yt_compete_2", "EXT_COMP_2", "Compete 2 Twitter", null, null, null, null, "https://twitter.com/comp2", null
                ));
                t1HoldingLockLatch.countDown();
                try {
                    t1ReleaseLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            return null;
        });

        assertThat(t1HoldingLockLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // T2 attempts Study update assigning candidate account, which calls findAndLock(candidateId)
        Future<?> t2Future = executor.submit(() -> {
            try {
                return txTemplate.execute(status -> {
                    return studyItemOperations.update(studyId, new com.vhvkhangg.personalprivatevault.knowledge.study.study.UpdateStudyItemCommand(
                            "Study Updated 2", null, StudyType.YOUTUBE_CHANNEL, null, candidateId,
                            null, null, null, null, null, null, null, null, null, null, null
                    ));
                });
            } finally {
                if (t1ReleaseLatch.getCount() > 0) {
                    t2FinishedBeforeT1Commit.set(true);
                }
            }
        });

        // Deterministically verify T2 is blocked on PostgreSQL row lock held by T1
        awaitCompetingLock("external_accounts", Duration.ofSeconds(5));
        assertThat(t2FinishedBeforeT1Commit.get()).isFalse();

        // Release T1 to commit
        t1ReleaseLatch.countDown();
        t1Future.get(10, TimeUnit.SECONDS);

        // T2 unblocks, observes refreshed platform Twitter under lock, throws InvalidStudyItemException and rolls back
        assertThatThrownBy(() -> t2Future.get(10, TimeUnit.SECONDS))
                .hasCauseInstanceOf(com.vhvkhangg.personalprivatevault.knowledge.study.study.InvalidStudyItemException.class)
                .hasMessageContaining("must belong to the YouTube platform");

        // Verify candidate platform is Twitter and Study item still points to origAccount
        ExternalAccountView finalAccount = externalAccountOperations.findById(candidateId).orElseThrow();
        assertThat(finalAccount.platformId()).isEqualTo(twitterPlatformId);
        StudyItemView finalStudy = studyItemOperations.findById(studyId).orElseThrow();
        assertThat(finalStudy.youtubeChannelAccountId()).isEqualTo(origAccount.id());
    }

    @Test
    @DisplayName("BA15-13 / FR15-7: Competing writers - Study create holds account lock via findAndLock; concurrent Account mutation to OTHER blocks, unblocks and rolls back")
    void competingWritersStudyCreateHoldsAccountLockVsConcurrentAccountMutationToOtherBlocksAndRollsBack() throws Exception {
        ExternalAccountView candidate = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                "yt_compete_3", "EXT_COMP_3", "Compete 3", null, null, null, null, "https://youtube.com/@comp3", null
        ));
        Long candidateId = candidate.id();

        CountDownLatch t1HoldingLockLatch = new CountDownLatch(1);
        CountDownLatch t1ReleaseLatch = new CountDownLatch(1);
        AtomicBoolean t2FinishedBeforeT1Commit = new AtomicBoolean(false);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // T1 creates Study item assigning candidate (calling findAndLock which locks candidate in external_accounts)
        Future<StudyItemView> t1Future = executor.submit(() -> {
            return txTemplate.execute(status -> {
                StudyItemView created = studyItemOperations.create(new CreateStudyItemCommand(
                        "Study Compete 3", null, StudyType.YOUTUBE_CHANNEL, null, candidateId,
                        null, null, null, null, null, null, null, null, null, null, null
                ));
                t1HoldingLockLatch.countDown();
                try {
                    t1ReleaseLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return created;
            });
        });

        assertThat(t1HoldingLockLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // T2 attempts Account mutation to OTHER, calling findByIdForUpdate(candidateId)
        Future<?> t2Future = executor.submit(() -> {
            try {
                return txTemplate.execute(status -> {
                    return externalAccountOperations.update(candidateId, new UpdateExternalAccountCommand(
                            youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.OTHER,
                            "mutated_other_comp3", "EXT_COMP_3", "Mutated Other 3", null, null, null, null, "https://example.com/other3", null
                    ));
                });
            } finally {
                if (t1ReleaseLatch.getCount() > 0) {
                    t2FinishedBeforeT1Commit.set(true);
                }
            }
        });

        // Deterministically verify T2 is blocked on PostgreSQL row lock held by T1
        awaitCompetingLock("external_accounts", Duration.ofSeconds(5));
        assertThat(t2FinishedBeforeT1Commit.get()).isFalse();

        // Release T1 to commit Study creation
        t1ReleaseLatch.countDown();
        StudyItemView createdStudy = t1Future.get(10, TimeUnit.SECONDS);
        assertThat(createdStudy.youtubeChannelAccountId()).isEqualTo(candidateId);

        // T2 unblocks, calls mutation guard which detects referencing Study item, throws ExternalAccountConflictException and rolls back
        assertThatThrownBy(() -> t2Future.get(10, TimeUnit.SECONDS))
                .hasCauseInstanceOf(ExternalAccountConflictException.class)
                .hasMessageContaining("Cannot change account type away from YOUTUBE_CHANNEL");

        // Verify account remains YOUTUBE_CHANNEL and Study item retains reference
        ExternalAccountView finalAccount = externalAccountOperations.findById(candidateId).orElseThrow();
        assertThat(finalAccount.accountType()).isEqualTo(ExternalAccountType.YOUTUBE_CHANNEL);
        StudyItemView finalStudy = studyItemOperations.findById(createdStudy.id()).orElseThrow();
        assertThat(finalStudy.youtubeChannelAccountId()).isEqualTo(candidateId);
    }

    @Test
    @DisplayName("BA15-13 / FR15-7: Competing writers - Study update holds account lock via findAndLock; concurrent Account platform mutation blocks, unblocks and rolls back")
    void competingWritersStudyUpdateHoldsAccountLockVsConcurrentAccountMutationPlatformBlocksAndRollsBack() throws Exception {
        ExternalAccountView origAccount = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                "yt_orig_comp4", "EXT_ORIG_4", "Orig 4", null, null, null, null, "https://youtube.com/@orig4", null
        ));
        ExternalAccountView candidate = externalAccountOperations.create(new CreateExternalAccountCommand(
                youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                "yt_compete_4", "EXT_COMP_4", "Compete 4", null, null, null, null, "https://youtube.com/@comp4", null
        ));
        Long candidateId = candidate.id();

        StudyItemView existingStudy = studyItemOperations.create(new CreateStudyItemCommand(
                "Study Existing 4", null, StudyType.YOUTUBE_CHANNEL, null, origAccount.id(),
                null, null, null, null, null, null, null, null, null, null, null
        ));
        Long studyId = existingStudy.id();

        CountDownLatch t1HoldingLockLatch = new CountDownLatch(1);
        CountDownLatch t1ReleaseLatch = new CountDownLatch(1);
        AtomicBoolean t2FinishedBeforeT1Commit = new AtomicBoolean(false);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // T1 updates Study to assign candidate (findAndLock locks candidate in external_accounts)
        Future<StudyItemView> t1Future = executor.submit(() -> {
            return txTemplate.execute(status -> {
                StudyItemView updated = studyItemOperations.update(studyId, new com.vhvkhangg.personalprivatevault.knowledge.study.study.UpdateStudyItemCommand(
                        "Study Updated 4", null, StudyType.YOUTUBE_CHANNEL, null, candidateId,
                        null, null, null, null, null, null, null, null, null, null, null
                ));
                t1HoldingLockLatch.countDown();
                try {
                    t1ReleaseLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return updated;
            });
        });

        assertThat(t1HoldingLockLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // T2 attempts Account mutation changing platform to Twitter
        Future<?> t2Future = executor.submit(() -> {
            try {
                return txTemplate.execute(status -> {
                    return externalAccountOperations.update(candidateId, new UpdateExternalAccountCommand(
                            twitterPlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                            "yt_compete_4", "EXT_COMP_4", "Compete 4 Twitter", null, null, null, null, "https://twitter.com/comp4", null
                    ));
                });
            } finally {
                if (t1ReleaseLatch.getCount() > 0) {
                    t2FinishedBeforeT1Commit.set(true);
                }
            }
        });

        // Deterministically verify T2 is blocked on PostgreSQL row lock held by T1
        awaitCompetingLock("external_accounts", Duration.ofSeconds(5));
        assertThat(t2FinishedBeforeT1Commit.get()).isFalse();

        // Release T1 to commit Study update
        t1ReleaseLatch.countDown();
        StudyItemView updatedStudy = t1Future.get(10, TimeUnit.SECONDS);
        assertThat(updatedStudy.youtubeChannelAccountId()).isEqualTo(candidateId);

        // T2 unblocks, calls mutation guard which detects referencing Study item, throws ExternalAccountConflictException and rolls back
        assertThatThrownBy(() -> t2Future.get(10, TimeUnit.SECONDS))
                .hasCauseInstanceOf(ExternalAccountConflictException.class)
                .hasMessageContaining("Cannot change platform away from YouTube");

        // Verify account platform remains YouTube and Study item retains candidate assignment
        ExternalAccountView finalAccount = externalAccountOperations.findById(candidateId).orElseThrow();
        assertThat(finalAccount.platformId()).isEqualTo(youtubePlatformId);
        StudyItemView finalStudy = studyItemOperations.findById(studyId).orElseThrow();
        assertThat(finalStudy.youtubeChannelAccountId()).isEqualTo(candidateId);
    }
}
