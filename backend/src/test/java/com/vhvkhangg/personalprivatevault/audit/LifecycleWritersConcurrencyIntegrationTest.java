package com.vhvkhangg.personalprivatevault.audit;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.command.UpdateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.journal.diary.DiaryOperations;
import com.vhvkhangg.personalprivatevault.journal.diary.command.CreateDiaryEntryCommand;
import com.vhvkhangg.personalprivatevault.journal.diary.command.UpdateDiaryEntryCommand;
import com.vhvkhangg.personalprivatevault.journal.diary.exception.DiaryEntryNotFoundException;
import com.vhvkhangg.personalprivatevault.journal.view.DiaryEntryView;
import com.vhvkhangg.personalprivatevault.personal.profile.PersonalProfileOperations;
import com.vhvkhangg.personalprivatevault.personal.profile.command.CreatePersonalProfileCommand;
import com.vhvkhangg.personalprivatevault.personal.profile.command.UpdatePersonalProfileCommand;
import com.vhvkhangg.personalprivatevault.personal.profile.exception.PersonalProfileNotFoundException;
import com.vhvkhangg.personalprivatevault.personal.view.PersonalProfileView;
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
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BA15-17 regression tests:
 * Verifies that lifecycle writer pairs (update vs soft-delete/restore in Journal and Personal,
 * and fetch vs configuration update in Feed) are protected against lost updates and stale writes.
 */
class LifecycleWritersConcurrencyIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private DiaryOperations diaryOperations;

    @Autowired
    private PersonalProfileOperations personalProfileOperations;

    @Autowired
    private FeedSourceOperations feedSourceOperations;

    @Autowired
    private FeedItemOperations feedItemOperations;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private ExecutorService executorPool;

    @BeforeEach
    void setUp() {
        executorPool = Executors.newCachedThreadPool();
    }

    @AfterEach
    void tearDown() {
        if (executorPool != null) {
            executorPool.shutdownNow();
        }
    }

    @Test
    @DisplayName("BA15-17: Journal update vs soft-delete serializes cleanly without resurrecting deleted entry")
    void journalUpdateVsSoftDeleteSerializes() throws Exception {
        DiaryEntryView entry = diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(
                LocalDate.now(), "Original Title", "Initial markdown content"
        ));
        Long entryId = entry.id();

        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> deleteFuture = executor.submit(() -> {
            try {
                barrier.await(5, TimeUnit.SECONDS);
                diaryOperations.softDeleteDiaryEntry(entryId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Future<?> updateFuture = executor.submit(() -> {
            try {
                barrier.await(5, TimeUnit.SECONDS);
                try {
                    diaryOperations.updateDiaryEntry(new UpdateDiaryEntryCommand(
                            entryId, LocalDate.now(), "Updated Title", "Updated markdown"
                    ));
                } catch (DiaryEntryNotFoundException expectedIfDeleteWon) {
                    // Acceptable: if delete transaction committed first, update throws DiaryEntryNotFoundException
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        deleteFuture.get(10, TimeUnit.SECONDS);
        updateFuture.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        // The entry MUST remain soft-deleted in all cases (never silently resurrected!)
        assertThat(diaryOperations.findDiaryEntries(null, null, 10))
                .extracting(DiaryEntryView::id)
                .doesNotContain(entryId);
    }

    @Test
    @DisplayName("BA15-17: Journal delete-first concurrent order throws on update; update-first preserves content through delete and restore")
    void journalLifecycleOrderedRacesAndRestore() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // Order 1: Delete holds lock first, Update blocks on row lock; Delete commits first -> Update throws
        DiaryEntryView entry1 = diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(
                LocalDate.now(), "Title 1", "Markdown 1"
        ));
        Long entry1Id = entry1.id();

        CountDownLatch t1DeleteHoldLatch = new CountDownLatch(1);
        CountDownLatch t1DeleteReleaseLatch = new CountDownLatch(1);
        CountDownLatch t2UpdateBlockedLatch = new CountDownLatch(1);
        AtomicReference<Exception> t2Exception = new AtomicReference<>();

        Future<?> f1 = executorPool.submit(() -> {
            txTemplate.execute(status -> {
                diaryOperations.softDeleteDiaryEntry(entry1Id);
                t1DeleteHoldLatch.countDown();
                try {
                    t1DeleteReleaseLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            return null;
        });

        Future<?> f2 = executorPool.submit(() -> {
            try {
                t1DeleteHoldLatch.await(5, TimeUnit.SECONDS);
                t2UpdateBlockedLatch.countDown();
                txTemplate.execute(status -> {
                    diaryOperations.updateDiaryEntry(new UpdateDiaryEntryCommand(
                            entry1Id, LocalDate.now(), "Mutated Title", "Mutated Markdown"
                    ));
                    return null;
                });
                return null;
            } catch (Exception e) {
                t2Exception.set(e);
                return null;
            }
        });

        assertThat(t1DeleteHoldLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(t2UpdateBlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Deterministically verify T2 is blocked on PostgreSQL row lock held by T1
        awaitCompetingLock(java.time.Duration.ofSeconds(5));

        // Release T1 to commit delete
        t1DeleteReleaseLatch.countDown();
        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);

        // T2 update must have failed with DiaryEntryNotFoundException (no resurrection)
        assertThat(t2Exception.get()).isInstanceOf(DiaryEntryNotFoundException.class);

        // Can be restored cleanly with original content
        DiaryEntryView restored1 = diaryOperations.restoreDiaryEntry(entry1Id);
        assertThat(restored1.title()).isEqualTo("Title 1");
        assertThat(restored1.contentMarkdown()).isEqualTo("Markdown 1");

        // Order 2: Update holds lock first, Delete blocks; Update commits first -> Delete soft-deletes updated entry
        DiaryEntryView entry2 = diaryOperations.createDiaryEntry(new CreateDiaryEntryCommand(
                LocalDate.now(), "Title 2", "Markdown 2"
        ));
        Long entry2Id = entry2.id();

        CountDownLatch t3UpdateHoldLatch = new CountDownLatch(1);
        CountDownLatch t3UpdateReleaseLatch = new CountDownLatch(1);
        CountDownLatch t4DeleteBlockedLatch = new CountDownLatch(1);

        Future<?> f3 = executorPool.submit(() -> {
            txTemplate.execute(status -> {
                diaryOperations.updateDiaryEntry(new UpdateDiaryEntryCommand(
                        entry2Id, LocalDate.now(), "Updated Title 2", "Updated Markdown 2"
                ));
                t3UpdateHoldLatch.countDown();
                try {
                    t3UpdateReleaseLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            return null;
        });

        Future<?> f4 = executorPool.submit(() -> {
            try {
                t3UpdateHoldLatch.await(5, TimeUnit.SECONDS);
                t4DeleteBlockedLatch.countDown();
                txTemplate.execute(status -> {
                    diaryOperations.softDeleteDiaryEntry(entry2Id);
                    return null;
                });
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertThat(t3UpdateHoldLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(t4DeleteBlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        awaitCompetingLock(java.time.Duration.ofSeconds(5));

        t3UpdateReleaseLatch.countDown();
        f3.get(10, TimeUnit.SECONDS);
        f4.get(10, TimeUnit.SECONDS);

        // Entry is soft-deleted, active query does not contain it
        assertThat(diaryOperations.findDiaryEntries(null, null, 10))
                .extracting(DiaryEntryView::id)
                .doesNotContain(entry2Id);

        // Restore preserves updated content
        DiaryEntryView restored2 = diaryOperations.restoreDiaryEntry(entry2Id);
        assertThat(restored2.title()).isEqualTo("Updated Title 2");
        assertThat(restored2.contentMarkdown()).isEqualTo("Updated Markdown 2");
    }

    @Test
    @DisplayName("BA15-17: Personal profile update vs soft-delete serializes cleanly")
    void personalProfileUpdateVsSoftDeleteSerializes() throws Exception {
        PersonalProfileView profile = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Test Person", "Friend", false, null, null, null, null, null, null, null, null
        ));
        Long profileId = profile.id();

        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> deleteFuture = executor.submit(() -> {
            try {
                barrier.await(5, TimeUnit.SECONDS);
                personalProfileOperations.softDeleteProfile(profileId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Future<?> updateFuture = executor.submit(() -> {
            try {
                barrier.await(5, TimeUnit.SECONDS);
                try {
                    personalProfileOperations.updateProfile(new UpdatePersonalProfileCommand(
                            profileId, "Updated Person", "Best Friend", false, null, null, null, null, null, null, null, null
                    ));
                } catch (PersonalProfileNotFoundException expectedIfDeleteWon) {
                    // Acceptable if delete won
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        deleteFuture.get(10, TimeUnit.SECONDS);
        updateFuture.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Profile must remain deleted from active query
        assertThat(personalProfileOperations.findProfiles(10))
                .extracting(PersonalProfileView::id)
                .doesNotContain(profileId);
    }

    @Test
    @DisplayName("BA15-17: Personal profile delete-first concurrent order throws on update; update-first preserves content through delete and restore")
    void personalProfileLifecycleOrderedRacesAndRestore() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // Order 1: Delete holds lock, Update blocks on row lock; Delete commits -> Update throws
        PersonalProfileView profile1 = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Person 1", "Colleague", false, null, null, null, null, null, null, null, null
        ));
        Long profile1Id = profile1.id();

        CountDownLatch t1DeleteHoldLatch = new CountDownLatch(1);
        CountDownLatch t1DeleteReleaseLatch = new CountDownLatch(1);
        CountDownLatch t2UpdateBlockedLatch = new CountDownLatch(1);
        AtomicReference<Exception> t2Exception = new AtomicReference<>();

        Future<?> f1 = executorPool.submit(() -> {
            txTemplate.execute(status -> {
                personalProfileOperations.softDeleteProfile(profile1Id);
                t1DeleteHoldLatch.countDown();
                try {
                    t1DeleteReleaseLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            return null;
        });

        Future<?> f2 = executorPool.submit(() -> {
            try {
                t1DeleteHoldLatch.await(5, TimeUnit.SECONDS);
                t2UpdateBlockedLatch.countDown();
                txTemplate.execute(status -> {
                    personalProfileOperations.updateProfile(new UpdatePersonalProfileCommand(
                            profile1Id, "Mutated Person", "Boss", false, null, null, null, null, null, null, null, null
                    ));
                    return null;
                });
                return null;
            } catch (Exception e) {
                t2Exception.set(e);
                return null;
            }
        });

        assertThat(t1DeleteHoldLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(t2UpdateBlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        awaitCompetingLock(java.time.Duration.ofSeconds(5));

        t1DeleteReleaseLatch.countDown();
        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);

        assertThat(t2Exception.get()).isInstanceOf(PersonalProfileNotFoundException.class);

        // Restore preserves original fields
        PersonalProfileView restored1 = personalProfileOperations.restoreProfile(profile1Id);
        assertThat(restored1.name()).isEqualTo("Person 1");
        assertThat(restored1.relationship()).isEqualTo("Colleague");

        // Order 2: Update holds lock, Delete blocks; Update commits -> Delete soft-deletes
        PersonalProfileView profile2 = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Person 2", "Acquaintance", false, null, null, null, null, null, null, null, null
        ));
        Long profile2Id = profile2.id();

        CountDownLatch t3UpdateHoldLatch = new CountDownLatch(1);
        CountDownLatch t3UpdateReleaseLatch = new CountDownLatch(1);
        CountDownLatch t4DeleteBlockedLatch = new CountDownLatch(1);

        Future<?> f3 = executorPool.submit(() -> {
            txTemplate.execute(status -> {
                personalProfileOperations.updateProfile(new UpdatePersonalProfileCommand(
                        profile2Id, "Updated Person 2", "Best Friend", false, null, null, null, null, null, null, null, null
                ));
                t3UpdateHoldLatch.countDown();
                try {
                    t3UpdateReleaseLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            return null;
        });

        Future<?> f4 = executorPool.submit(() -> {
            try {
                t3UpdateHoldLatch.await(5, TimeUnit.SECONDS);
                t4DeleteBlockedLatch.countDown();
                txTemplate.execute(status -> {
                    personalProfileOperations.softDeleteProfile(profile2Id);
                    return null;
                });
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertThat(t3UpdateHoldLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(t4DeleteBlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        awaitCompetingLock(java.time.Duration.ofSeconds(5));

        t3UpdateReleaseLatch.countDown();
        f3.get(10, TimeUnit.SECONDS);
        f4.get(10, TimeUnit.SECONDS);

        assertThat(personalProfileOperations.findProfiles(10))
                .extracting(PersonalProfileView::id)
                .doesNotContain(profile2Id);

        PersonalProfileView restored2 = personalProfileOperations.restoreProfile(profile2Id);
        assertThat(restored2.name()).isEqualTo("Updated Person 2");
        assertThat(restored2.relationship()).isEqualTo("Best Friend");
    }

    @Test
    @DisplayName("BA15-17 / FR15-2: Feed fetch uses authoritative configuration committed concurrently and updates managed state and updatedAt")
    void feedFetchUsesAuthoritativeConcurrentConfigAndUpdatesManagedState() throws Exception {
        FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                "Tech Blog",
                FeedSourceType.RSS,
                "https://techblog.example.com",
                "https://techblog.example.com/feed.xml",
                true,
                true,
                60,
                null
        ));
        Long sourceId = source.id();
        Instant initialUpdatedAt = source.updatedAt();

        CountDownLatch t1ReadLatch = new CountDownLatch(1);
        CountDownLatch t2CommitLatch = new CountDownLatch(1);
        AtomicReference<Instant> fetchedAtRef = new AtomicReference<>();
        AtomicReference<FeedSourceView> sameTxViewRef = new AtomicReference<>();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // Thread 1: Loads FeedSource (sees 60 min), then pauses. After T2 commits interval 120, T1 ingests fetch.
        Future<?> f1 = executorPool.submit(() -> {
            txTemplate.execute(status -> {
                // Prior managed read in T1
                FeedSourceView priorRead = feedSourceOperations.findSourceById(sourceId).orElseThrow();
                assertThat(priorRead.refreshIntervalMinutes()).isEqualTo(60);

                t1ReadLatch.countDown();
                try {
                    t2CommitLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                Instant fetchedAt = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
                fetchedAtRef.set(fetchedAt);
                feedItemOperations.ingestFetch(sourceId, fetchedAt, List.of(
                        new NormalizedFeedItemInput("item-1", "Title 1", "https://techblog.example.com/1", "A", "S", fetchedAt, null)
                ));

                // Same-transaction read after ingestion
                sameTxViewRef.set(feedSourceOperations.findSourceById(sourceId).orElseThrow());
                return null;
            });
            return null;
        });

        assertThat(t1ReadLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // T2: updates configuration to interval 120 min and commits
        txTemplate.execute(status -> {
            feedSourceOperations.updateSource(sourceId, new UpdateFeedSourceCommand(
                    "Tech Blog Renamed",
                    FeedSourceType.RSS,
                    "https://techblog.example.com",
                    "https://techblog.example.com/feed.xml",
                    true,
                    true,
                    120,
                    null
            ));
            return null;
        });
        t2CommitLatch.countDown();

        f1.get(10, TimeUnit.SECONDS);

        // 1. Same-transaction read sees non-null lastFetchedAt (managed state coherent)
        FeedSourceView sameTxView = sameTxViewRef.get();
        assertThat(sameTxView).isNotNull();
        assertThat(sameTxView.lastFetchedAt()).isEqualTo(fetchedAtRef.get());
        // 2. Next fetch is delayed by 120 minutes, NOT the stale 60 minutes!
        assertThat(sameTxView.refreshIntervalMinutes()).isEqualTo(120);
        assertThat(sameTxView.nextFetchAt()).isEqualTo(fetchedAtRef.get().plus(Duration.ofMinutes(120)));

        // 3. Database state after both transactions commit
        FeedSourceView finalView = feedSourceOperations.findSourceById(sourceId).orElseThrow();
        assertThat(finalView.name()).isEqualTo("Tech Blog Renamed");
        assertThat(finalView.refreshIntervalMinutes()).isEqualTo(120);
        assertThat(finalView.lastFetchedAt()).isEqualTo(fetchedAtRef.get());
        assertThat(finalView.nextFetchAt()).isEqualTo(fetchedAtRef.get().plus(Duration.ofMinutes(120)));
        assertThat(finalView.updatedAt()).isNotNull();
        if (initialUpdatedAt != null) {
            assertThat(finalView.updatedAt()).isAfterOrEqualTo(initialUpdatedAt);
        }
    }

    @Test
    @DisplayName("BA15-17: Feed fetch uses authoritative configuration enabling scheduled refresh concurrently")
    void feedFetchUsesAuthoritativeConcurrentConfigEnablingScheduledRefresh() throws Exception {
        FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                "News Feed",
                FeedSourceType.RSS,
                "https://news.example.com",
                "https://news.example.com/feed.xml",
                true,
                false, // initially scheduledRefreshEnabled = false
                90,
                null
        ));
        Long sourceId = source.id();

        CountDownLatch t1ReadLatch = new CountDownLatch(1);
        CountDownLatch t2CommitLatch = new CountDownLatch(1);
        AtomicReference<Instant> fetchedAtRef = new AtomicReference<>();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        Future<?> f1 = executorPool.submit(() -> {
            txTemplate.execute(status -> {
                FeedSourceView priorRead = feedSourceOperations.findSourceById(sourceId).orElseThrow();
                assertThat(priorRead.scheduledRefreshEnabled()).isFalse();

                t1ReadLatch.countDown();
                try {
                    t2CommitLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                Instant fetchedAt = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
                fetchedAtRef.set(fetchedAt);
                feedItemOperations.ingestFetch(sourceId, fetchedAt, List.of(
                        new NormalizedFeedItemInput("news-1", "News 1", "https://news.example.com/1", "A", "S", fetchedAt, null)
                ));
                return null;
            });
            return null;
        });

        assertThat(t1ReadLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // T2: enables scheduledRefreshEnabled
        txTemplate.execute(status -> {
            feedSourceOperations.updateSource(sourceId, new UpdateFeedSourceCommand(
                    "News Feed Active",
                    FeedSourceType.RSS,
                    "https://news.example.com",
                    "https://news.example.com/feed.xml",
                    true,
                    true, // scheduledRefreshEnabled = true
                    90,
                    null
            ));
            return null;
        });
        t2CommitLatch.countDown();

        f1.get(10, TimeUnit.SECONDS);

        FeedSourceView finalView = feedSourceOperations.findSourceById(sourceId).orElseThrow();
        assertThat(finalView.scheduledRefreshEnabled()).isTrue();
        assertThat(finalView.nextFetchAt()).isNotNull();
        assertThat(finalView.nextFetchAt()).isEqualTo(fetchedAtRef.get().plus(Duration.ofMinutes(90)));
    }

    @Test
    @DisplayName("BA15-17: Feed fetch holds lock while configuration writer serializes")
    void feedFetchHoldsLockWhileConfigWriterSerializes() throws Exception {
        FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                "Stream Feed",
                FeedSourceType.RSS,
                "https://stream.example.com",
                "https://stream.example.com/feed.xml",
                true,
                true,
                30,
                null
        ));
        Long sourceId = source.id();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        CountDownLatch t1LockLatch = new CountDownLatch(1);
        CountDownLatch t2BlockedLatch = new CountDownLatch(1);
        CountDownLatch t1ReleaseLatch = new CountDownLatch(1);
        AtomicBoolean t2FinishedBeforeT1Commit = new AtomicBoolean(false);

        Future<?> f1 = executorPool.submit(() -> {
            txTemplate.execute(status -> {
                // Ingest fetch under row lock
                Instant fetchedAt = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
                feedItemOperations.ingestFetch(sourceId, fetchedAt, List.of(
                        new NormalizedFeedItemInput("item-s", "Stream 1", "https://stream.example.com/1", "A", "S", fetchedAt, null)
                ));
                t1LockLatch.countDown();
                try {
                    t1ReleaseLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            return null;
        });

        Future<?> f2 = executorPool.submit(() -> {
            try {
                t1LockLatch.await(5, TimeUnit.SECONDS);
                t2BlockedLatch.countDown();
                txTemplate.execute(status -> {
                    feedSourceOperations.updateSource(sourceId, new UpdateFeedSourceCommand(
                            "Stream Feed Updated",
                            FeedSourceType.RSS,
                            "https://stream.example.com",
                            "https://stream.example.com/feed.xml",
                            false, // disabled
                            false,
                            45,
                            null
                    ));
                    return null;
                });
                if (t1ReleaseLatch.getCount() > 0) {
                    t2FinishedBeforeT1Commit.set(true);
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertThat(t1LockLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(t2BlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        awaitCompetingLock(java.time.Duration.ofSeconds(5));
        assertThat(t2FinishedBeforeT1Commit.get()).isFalse();

        t1ReleaseLatch.countDown();
        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);

        FeedSourceView finalView = feedSourceOperations.findSourceById(sourceId).orElseThrow();
        assertThat(finalView.name()).isEqualTo("Stream Feed Updated");
        assertThat(finalView.enabled()).isFalse();
        assertThat(finalView.refreshIntervalMinutes()).isEqualTo(45);
        assertThat(finalView.lastFetchedAt()).isNotNull();
    }
}
