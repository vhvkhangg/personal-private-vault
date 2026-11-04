package com.vhvkhangg.personalprivatevault.feed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedItem;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsClient;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedItemRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedSourceNotFoundException;
import com.vhvkhangg.personalprivatevault.feed.view.FeedRefreshResultView;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshFinalizer;
import com.vhvkhangg.personalprivatevault.feed.resource.SavedResourceOperations;
import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateFeedSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceInboxPageView;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class HackerNewsRefreshIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FeedSourceOperations feedSourceOperations;

    @Autowired
    private FeedItemOperations feedItemOperations;

    @Autowired
    private SavedResourceOperations savedResourceOperations;

    @Autowired
    private HackerNewsRefreshFinalizer finalizer;

    @Autowired
    private FeedItemRepository feedItemRepository;

    @Autowired
    private FeedSourceRepository feedSourceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @Autowired
    private com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshAdmission admission;

    @Autowired
    private com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsNormalizer normalizer;

    @MockitoBean
    private HackerNewsClient hackerNewsClient;

    @MockitoBean
    private java.time.Clock clock;

    private static final Instant DEFAULT_NOW = Instant.parse("2026-11-02T10:00:00Z");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(DEFAULT_NOW);
        when(clock.getZone()).thenReturn(java.time.ZoneOffset.UTC);
        tearDown();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM favorites");
        jdbcTemplate.execute("DELETE FROM vault_entry_tags");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM saved_resource_conversions");
        jdbcTemplate.execute("DELETE FROM saved_resources");
        jdbcTemplate.execute("DELETE FROM feed_items");
        jdbcTemplate.execute("DELETE FROM feed_sources");
    }

    private FeedSourceView createHackerNewsSource(String name, boolean enabled) {
        return feedSourceOperations.createSource(new CreateFeedSourceCommand(
                name,
                FeedSourceType.HACKER_NEWS,
                "https://news.ycombinator.com",
                null,
                enabled,
                false,
                null,
                null
        ));
    }

    private FeedSourceView createRssSource(String name) {
        return feedSourceOperations.createSource(new CreateFeedSourceCommand(
                name,
                FeedSourceType.RSS,
                "https://example.com",
                "https://example.com/rss",
                true,
                false,
                null,
                null
        ));
    }

    private JsonNode createStoryJson(long id, String title, String url, String author, long time, int score, int descendants) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", id);
        node.put("type", "story");
        node.put("title", title);
        if (url != null) {
            node.put("url", url);
        }
        if (author != null) {
            node.put("by", author);
        }
        node.put("time", time);
        node.put("score", score);
        node.put("descendants", descendants);
        return node;
    }

    @Test
    @DisplayName("Happy path: normalizes stories, inserts items with null externalId, and records provenance")
    void happyPath_normalizesAndInsertsItemsWithNullExternalId() {
        FeedSourceView source = createHackerNewsSource("HN Front", true);

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(101L, 102L, 103L));
        // 101 is an Ask HN story (no URL) -> skipped
        when(hackerNewsClient.fetchItem(eq(101L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(101L, "Ask HN: Favorite tools?", null, "user1", 1700000000L, 15, 3));
        // 102 is a valid article
        when(hackerNewsClient.fetchItem(eq(102L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(102L, "PostgreSQL 17 Released", "https://example.com/pg17", "alice", 1700000100L, 150, 45));
        // 103 is another valid article
        when(hackerNewsClient.fetchItem(eq(103L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(103L, "Java 25 Features", "https://example.com/java25", "bob", 1700000200L, 95, 20));

        FeedRefreshResultView result = feedSourceOperations.refreshSource(source.id(), 30);

        assertThat(result.sourceId()).isEqualTo(source.id());
        assertThat(result.fetchedAt()).isNotNull();
        assertThat(result.selectedTopIds()).isEqualTo(3);
        assertThat(result.eligibleStories()).isEqualTo(2);
        assertThat(result.distinctArticleUrls()).isEqualTo(2);
        assertThat(result.processedItems()).isEqualTo(2);

        List<FeedItem> items = feedItemRepository.findAll();
        assertThat(items).hasSize(2);
        assertThat(items).allMatch(i -> i.getExternalId() == null);
        assertThat(items).extracting(FeedItem::getUrl)
                .containsExactlyInAnyOrder("https://example.com/pg17", "https://example.com/java25");

        FeedItem pgItem = items.stream().filter(i -> i.getUrl().equals("https://example.com/pg17")).findFirst().orElseThrow();
        assertThat(pgItem.getTitle()).isEqualTo("PostgreSQL 17 Released");
        assertThat(pgItem.getAuthor()).isEqualTo("alice");
        assertThat(pgItem.getRawMetadata()).isNotNull();
        assertThat(pgItem.getRawMetadata()).containsEntry("hnStoryId", 102);
        assertThat(pgItem.getRawMetadata()).containsEntry("hnScore", 150);

        FeedSource reloadedSource = feedSourceRepository.findById(source.id()).orElseThrow();
        assertThat(reloadedSource.getLastFetchedAt()).isNotNull();
    }

    @Test
    @DisplayName("URL grouping and tie-breaking: latest submission time then greatest story ID wins")
    void urlGroupingAndTieBreaking_latestSubmissionAndGreatestIdWins() {
        FeedSourceView source = createHackerNewsSource("HN Front", true);

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(201L, 202L, 203L));
        // 201 has time 1000
        when(hackerNewsClient.fetchItem(eq(201L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(201L, "Earlier Title", "https://example.com/duplicate", "userA", 1000L, 10, 1));
        // 202 has time 2000
        when(hackerNewsClient.fetchItem(eq(202L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(202L, "Newer Title 202", "https://example.com/duplicate", "userB", 2000L, 20, 2));
        // 203 has time 2000, ID 203 > 202 -> Winner!
        when(hackerNewsClient.fetchItem(eq(203L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(203L, "Winning Title 203", "https://example.com/duplicate", "userC", 2000L, 30, 3));

        FeedRefreshResultView result = feedSourceOperations.refreshSource(source.id(), 30);

        assertThat(result.selectedTopIds()).isEqualTo(3);
        assertThat(result.eligibleStories()).isEqualTo(3);
        assertThat(result.distinctArticleUrls()).isEqualTo(1);
        assertThat(result.processedItems()).isEqualTo(1);

        List<FeedItem> items = feedItemRepository.findAll();
        assertThat(items).hasSize(1);
        FeedItem item = items.get(0);
        assertThat(item.getTitle()).isEqualTo("Winning Title 203");
        assertThat(item.getAuthor()).isEqualTo("userC");
        assertThat(item.getRawMetadata()).containsEntry("hnStoryId", 203);
    }

    @Test
    @DisplayName("Repeat refresh: idempotent row upsert on matching URL hash")
    void repeatRefresh_idempotenceAndRowUpsert() {
        FeedSourceView source = createHackerNewsSource("HN Front", true);

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(301L));
        when(hackerNewsClient.fetchItem(eq(301L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(301L, "Initial Title", "https://example.com/repeat", "user1", 1700000000L, 50, 10));

        FeedRefreshResultView r1 = feedSourceOperations.refreshSource(source.id(), 30);
        assertThat(r1.processedItems()).isEqualTo(1);

        FeedItem item1 = feedItemRepository.findAll().get(0);
        assertThat(item1.getTitle()).isEqualTo("Initial Title");

        // Second refresh: same URL, updated title and score
        when(hackerNewsClient.fetchItem(eq(301L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(301L, "Updated Title", "https://example.com/repeat", "user1", 1700000000L, 85, 25));

        FeedRefreshResultView r2 = feedSourceOperations.refreshSource(source.id(), 30);
        assertThat(r2.processedItems()).isEqualTo(1);

        List<FeedItem> items = feedItemRepository.findAll();
        assertThat(items).hasSize(1);
        FeedItem item2 = items.get(0);
        assertThat(item2.getId()).isEqualTo(item1.getId());
        assertThat(item2.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("Legacy external ID: preserves existing external ID when updated by matching article URL")
    void legacyExternalIdReplacement_matchesExistingUrlAndPreservesLegacyExternalId() {
        FeedSourceView source = createHackerNewsSource("HN Front", true);

        // Pre-insert legacy item via generic ingestFetch with non-null externalId
        NormalizedFeedItemInput legacyInput = new NormalizedFeedItemInput(
                "legacy-hn-401",
                "Legacy Title",
                "https://example.com/legacy-article",
                "Original Author",
                "Content",
                Instant.ofEpochSecond(1600000000L),
                null
        );
        feedItemOperations.ingestFetch(source.id(), Instant.now(), List.of(legacyInput));

        FeedItem initial = feedItemRepository.findAll().get(0);
        assertThat(initial.getExternalId()).isEqualTo("legacy-hn-401");
        assertThat(initial.getTitle()).isEqualTo("Legacy Title");

        // Now refresh via HN refresh service: normalizer returns candidate with externalId = null
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(501L));
        when(hackerNewsClient.fetchItem(eq(501L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(501L, "Modernized Title", "https://example.com/legacy-article", "New Author", 1700000000L, 100, 20));

        FeedRefreshResultView result = feedSourceOperations.refreshSource(source.id(), 30);
        assertThat(result.processedItems()).isEqualTo(1);

        List<FeedItem> items = feedItemRepository.findAll();
        assertThat(items).hasSize(1);
        FeedItem updated = items.get(0);
        assertThat(updated.getId()).isEqualTo(initial.getId());
        assertThat(updated.getExternalId()).isNull();
        assertThat(updated.getTitle()).isEqualTo("Modernized Title");
    }

    @Test
    @DisplayName("Preflight rejections: disabled source, unsupported type, or not found")
    void preflightRejections_disabledOrUnsupportedTypeOrNotFound() {
        FeedSourceView disabledSource = createHackerNewsSource("Disabled HN", false);
        FeedSourceView rssSource = createRssSource("RSS Feed");

        assertThatThrownBy(() -> feedSourceOperations.refreshSource(disabledSource.id(), 30))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(e -> assertThat(((FeedRefreshException) e).getReason())
                        .isEqualTo(FeedRefreshFailureReason.SOURCE_DISABLED));

        assertThatThrownBy(() -> feedSourceOperations.refreshSource(rssSource.id(), 30))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(e -> assertThat(((FeedRefreshException) e).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UNSUPPORTED_SOURCE_TYPE));

        assertThatThrownBy(() -> feedSourceOperations.refreshSource(999999L, 30))
                .isInstanceOf(FeedSourceNotFoundException.class);

        verifyNoInteractions(hackerNewsClient);
    }

    @Test
    @DisplayName("Empty or all-skipped batch: updates source lastFetchedAt cleanly without errors")
    void emptyOrAllSkippedBatch_finalizesCleanly() {
        FeedSourceView source = createHackerNewsSource("HN Front", true);

        // Empty top list
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of());

        FeedRefreshResultView result = feedSourceOperations.refreshSource(source.id(), 30);
        assertThat(result.selectedTopIds()).isEqualTo(0);
        assertThat(result.eligibleStories()).isEqualTo(0);
        assertThat(result.distinctArticleUrls()).isEqualTo(0);
        assertThat(result.processedItems()).isEqualTo(0);

        FeedSource reloaded = feedSourceRepository.findById(source.id()).orElseThrow();
        assertThat(reloaded.getLastFetchedAt()).isNotNull();
    }

    @Test
    @DisplayName("Post-fetch timestamp capture: fetchedAt reflects post-fetch clock time and stamps hnCapturedAt")
    void postFetchTimestamp_capturedAfterHttpFetchSucceeds() {
        FeedSourceView source = createHackerNewsSource("HN Clock", true);

        Instant t0 = Instant.parse("2026-11-02T10:00:00Z");
        Instant tAfterFetch = Instant.parse("2026-11-02T10:00:45Z");
        when(clock.instant()).thenReturn(t0);

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenAnswer(inv -> {
            when(clock.instant()).thenReturn(tAfterFetch);
            return List.of(701L);
        });
        when(hackerNewsClient.fetchItem(eq(701L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(701L, "Clock Title", "https://example.com/clock", "alice", 1700000000L, 10, 1));

        FeedRefreshResultView result = feedSourceOperations.refreshSource(source.id(), 30);

        assertThat(result.fetchedAt()).isEqualTo(tAfterFetch);

        FeedItem item = feedItemRepository.findAll().stream()
                .filter(i -> i.getUrl().equals("https://example.com/clock"))
                .findFirst()
                .orElseThrow();
        assertThat(item.getFetchedAt()).isEqualTo(tAfterFetch);
        assertThat(item.getRawMetadata()).containsEntry("hnCapturedAt", tAfterFetch.toString());
    }

    @Test
    @DisplayName("Ambient transaction rejection: calling refreshSource inside an active transaction throws IllegalTransactionStateException")
    void ambientTransactionRejection_throwsIllegalTransactionStateException() {
        FeedSourceView source = createHackerNewsSource("HN Tx Rejection", true);
        org.springframework.transaction.support.TransactionTemplate tt =
                new org.springframework.transaction.support.TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tt.execute(status -> feedSourceOperations.refreshSource(source.id(), 30)))
                .isInstanceOf(org.springframework.transaction.IllegalTransactionStateException.class);
    }

    @Test
    @DisplayName("Finalization lock/timeout failure translation: translates lock/transaction exceptions to REFRESH_FINALIZATION_UNAVAILABLE")
    void finalizationFailure_translatedToRefreshFinalizationUnavailable() {
        FeedSourceView source = createHackerNewsSource("HN Lock Fail", true);

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(801L));
        when(hackerNewsClient.fetchItem(eq(801L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(801L, "Lock Item", "https://example.com/lock", "alice", 1700000000L, 10, 1));

        com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshFinalizer mockFinalizer =
                org.mockito.Mockito.mock(com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshFinalizer.class);
        when(mockFinalizer.finalizeRefresh(anyLong(), any(Instant.class), any()))
                .thenThrow(new org.springframework.dao.CannotAcquireLockException("Lock acquire timeout"));

        com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshService customService =
                new com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshService(
                        feedSourceRepository,
                        admission,
                        hackerNewsClient,
                        normalizer,
                        mockFinalizer,
                        clock
                );

        assertThatThrownBy(() -> customService.refreshSource(source.id(), 30))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> {
                    FeedRefreshException fre = (FeedRefreshException) ex;
                    assertThat(fre.getReason()).isEqualTo(FeedRefreshFailureReason.REFRESH_FINALIZATION_UNAVAILABLE);
                });
    }

    @Test
    @DisplayName("FR-05: ingestFetch and source update share identical physical PostgreSQL backend and transaction identity")
    void ingestFetchAndSourceUpdateSharePhysicalTransactionIdentity() {
        RefreshTransactionIdentities identities = observeRefreshTransactionIdentities(false);
        System.out.println("DTR26-FR-05 REQUIRED: " + identities);

        assertThat(identities.ingestCommittedBeforeFinalizerCommit()).isFalse();
        assertSharedPhysicalTransaction(identities);
    }

    @Test
    @DisplayName("FR-05 negative control: independently committed REQUIRES_NEW ingest fails the shared physical transaction assertion")
    void separatelyCommittedRequiresNewIngestIsDetectedAsTransactionMismatch() {
        RefreshTransactionIdentities identities = observeRefreshTransactionIdentities(true);
        System.out.println("DTR26-FR-05 REQUIRES_NEW negative control: " + identities);

        assertThat(identities.ingestCommittedBeforeFinalizerCommit()).isTrue();
        assertThat(identities.ingest().backendPid()).isNotEqualTo(identities.finalizerBeforeCommit().backendPid());
        assertThat(identities.ingest().transactionId()).isNotEqualTo(identities.finalizerBeforeCommit().transactionId());
        assertThatThrownBy(() -> assertSharedPhysicalTransaction(identities))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("actual ingest and authoritative finalizer beforeCommit");
    }

    private RefreshTransactionIdentities observeRefreshTransactionIdentities(boolean requiresNewIngest) {
        FeedSourceView source = createHackerNewsSource("HN Shared Tx", true);

        AtomicReference<PhysicalTransactionIdentity> ingest = new AtomicReference<>();
        AtomicReference<PhysicalTransactionIdentity> sourceUpdate = new AtomicReference<>();
        AtomicReference<PhysicalTransactionIdentity> finalizerBeforeCommit = new AtomicReference<>();
        AtomicBoolean ingestCommitted = new AtomicBoolean();
        AtomicBoolean ingestCommittedBeforeFinalizerCommit = new AtomicBoolean();

        Object finalizerTarget = AopTestUtils.getUltimateTargetObject(finalizer);
        FeedItemOperations originalOps = (FeedItemOperations) ReflectionTestUtils.getField(finalizerTarget, "feedItemOperations");
        Object ingestTarget = AopTestUtils.getUltimateTargetObject(originalOps);
        FeedSourceRepository originalIngestRepository = (FeedSourceRepository) ReflectionTestUtils.getField(ingestTarget, "feedSourceRepository");
        FeedSourceRepository originalFinalizerRepository = (FeedSourceRepository) ReflectionTestUtils.getField(finalizerTarget, "feedSourceRepository");

        // These repository calls originate inside the real ingest body, after its Spring transaction advice.
        ProxyFactory ingestRepository = new ProxyFactory(originalIngestRepository);
        ingestRepository.addAdvice((MethodInterceptor) invocation -> {
            Object result = invocation.proceed();
            if ("findById".equals(invocation.getMethod().getName())) {
                assertThat(ingest.get()).isNull();
                ingest.set(currentPhysicalTransactionIdentity());
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        ingestCommitted.set(true);
                    }
                });
            } else if ("findByIdForUpdate".equals(invocation.getMethod().getName())) {
                sourceUpdate.set(currentPhysicalTransactionIdentity());
            }
            return result;
        });

        // Only the finalizer's own locked authority check registers the authoritative outer commit sample.
        ProxyFactory finalizerRepository = new ProxyFactory(originalFinalizerRepository);
        finalizerRepository.addAdvice((MethodInterceptor) invocation -> {
            Object result = invocation.proceed();
            if ("findByIdForUpdate".equals(invocation.getMethod().getName())) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void beforeCommit(boolean readOnly) {
                        assertThat(finalizerBeforeCommit.get()).isNull();
                        finalizerBeforeCommit.set(currentPhysicalTransactionIdentity());
                        ingestCommittedBeforeFinalizerCommit.set(ingestCommitted.get());
                    }
                });
            }
            return result;
        });

        try {
            ReflectionTestUtils.setField(ingestTarget, "feedSourceRepository", ingestRepository.getProxy());
            ReflectionTestUtils.setField(finalizerTarget, "feedSourceRepository", finalizerRepository.getProxy());
            if (requiresNewIngest) {
                // Test-only fault injection around the unchanged, Spring-proxied generic ingest implementation.
                NameMatchTransactionAttributeSource attributes = new NameMatchTransactionAttributeSource();
                attributes.addTransactionalMethod("ingestFetch",
                        new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
                TransactionInterceptor requiresNewAdvice = new TransactionInterceptor();
                requiresNewAdvice.setTransactionManager(transactionManager);
                requiresNewAdvice.setTransactionAttributeSource(attributes);
                ProxyFactory independentIngest = new ProxyFactory(originalOps);
                independentIngest.addAdvice(requiresNewAdvice);
                ReflectionTestUtils.setField(finalizerTarget, "feedItemOperations", independentIngest.getProxy());
            }

            when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(901L));
            when(hackerNewsClient.fetchItem(eq(901L), anyLong(), any(AtomicLong.class)))
                    .thenReturn(createStoryJson(901L, "Shared Tx Item", "https://example.com/shared-tx", "author", 1700000000L, 10, 1));

            assertThat(feedSourceOperations.refreshSource(source.id(), 30).processedItems()).isEqualTo(1);
            assertThat(feedItemRepository.findAll()).hasSize(1);
            assertThat(feedSourceRepository.findById(source.id()).orElseThrow().getLastFetchedAt()).isEqualTo(DEFAULT_NOW);
            assertThat(ingest.get()).isNotNull();
            assertThat(sourceUpdate.get()).isNotNull();
            assertThat(finalizerBeforeCommit.get()).isNotNull();
            assertThat(ingestCommitted.get()).isTrue();
            return new RefreshTransactionIdentities(ingest.get(), sourceUpdate.get(), finalizerBeforeCommit.get(),
                    ingestCommittedBeforeFinalizerCommit.get());
        } finally {
            ReflectionTestUtils.setField(finalizerTarget, "feedItemOperations", originalOps);
            ReflectionTestUtils.setField(finalizerTarget, "feedSourceRepository", originalFinalizerRepository);
            ReflectionTestUtils.setField(ingestTarget, "feedSourceRepository", originalIngestRepository);
        }
    }

    private PhysicalTransactionIdentity currentPhysicalTransactionIdentity() {
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
        PhysicalTransactionIdentity identity = jdbcTemplate.queryForObject(
                "SELECT pg_backend_pid(), pg_current_xact_id()::text::bigint",
                (row, index) -> new PhysicalTransactionIdentity(row.getLong(1), row.getLong(2)));
        assertThat(identity).isNotNull();
        assertThat(identity.backendPid()).isPositive();
        assertThat(identity.transactionId()).isPositive();
        return identity;
    }

    private void assertSharedPhysicalTransaction(RefreshTransactionIdentities identities) {
        assertThat(identities.sourceUpdate()).as("generic ingest source update").isEqualTo(identities.ingest());
        assertThat(identities.ingest())
                .as("actual ingest and authoritative finalizer beforeCommit must share PostgreSQL backend PID and transaction identity")
                .isEqualTo(identities.finalizerBeforeCommit());
    }

    private record PhysicalTransactionIdentity(long backendPid, long transactionId) {}

    private record RefreshTransactionIdentities(PhysicalTransactionIdentity ingest,
                                               PhysicalTransactionIdentity sourceUpdate,
                                               PhysicalTransactionIdentity finalizerBeforeCommit,
                                               boolean ingestCommittedBeforeFinalizerCommit) {}

    @Test
    @DisplayName("FR-05: Revised URL and representative replacement sequence: Story 101 URL A -> Story 102 URL A updates row -> Story 103 URL B inserts new row")
    void revisedUrlAndRepresentativeReplacementSequence() {
        FeedSourceView source = createHackerNewsSource("HN Replacement Seq", true);

        // Refresh 1: Story 101 with URL A
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(101L));
        when(hackerNewsClient.fetchItem(eq(101L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(101L, "Initial Title 101", "https://example.com/url-a", "authorA", 1700000000L, 10, 1));

        FeedRefreshResultView res1 = feedSourceOperations.refreshSource(source.id(), 30);
        assertThat(res1.processedItems()).isEqualTo(1);

        List<FeedItem> itemsAfter1 = feedItemRepository.findAll();
        assertThat(itemsAfter1).hasSize(1);
        FeedItem item1 = itemsAfter1.get(0);
        assertThat(item1.getExternalId()).isNull();
        assertThat(item1.getUrl()).isEqualTo("https://example.com/url-a");
        assertThat(item1.getTitle()).isEqualTo("Initial Title 101");
        assertThat(((Number) item1.getRawMetadata().get("hnStoryId")).longValue()).isEqualTo(101L);

        // Refresh 2: Story 102 with the SAME URL A (representative replacement / update)
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(102L));
        when(hackerNewsClient.fetchItem(eq(102L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(102L, "Revised Title 102", "https://example.com/url-a", "authorB", 1700000500L, 25, 2));

        FeedRefreshResultView res2 = feedSourceOperations.refreshSource(source.id(), 30);
        assertThat(res2.processedItems()).isEqualTo(1);

        List<FeedItem> itemsAfter2 = feedItemRepository.findAll();
        assertThat(itemsAfter2).hasSize(1);
        FeedItem item2 = itemsAfter2.get(0);
        assertThat(item2.getId()).isEqualTo(item1.getId());
        assertThat(item2.getExternalId()).isNull();
        assertThat(item2.getUrl()).isEqualTo("https://example.com/url-a");
        assertThat(item2.getTitle()).isEqualTo("Revised Title 102");
        assertThat(((Number) item2.getRawMetadata().get("hnStoryId")).longValue()).isEqualTo(102L);

        // Refresh 3: Story 103 with URL B (and Story 102 with URL A) -> inserts new row for URL B
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(102L, 103L));
        when(hackerNewsClient.fetchItem(eq(103L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(103L, "New Story 103", "https://example.com/url-b", "authorC", 1700001000L, 50, 3));

        FeedRefreshResultView res3 = feedSourceOperations.refreshSource(source.id(), 30);
        assertThat(res3.processedItems()).isEqualTo(2);

        List<FeedItem> itemsAfter3 = feedItemRepository.findAll();
        assertThat(itemsAfter3).hasSize(2);
        assertThat(itemsAfter3).extracting(FeedItem::getUrl)
                .containsExactlyInAnyOrder("https://example.com/url-a", "https://example.com/url-b");
    }

    @Test
    @DisplayName("FR-05: SavedResource in Inbox retains original snapshot and URL A across subsequent feed refreshes")
    void savedResourceSnapshotRetainedAcrossSubsequentRefreshes() {
        FeedSourceView source = createHackerNewsSource("HN Snapshot Test", true);

        // Refresh 1: Story 101 with URL A
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(101L));
        when(hackerNewsClient.fetchItem(eq(101L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(101L, "Original Story Title 101", "https://example.com/snap-url-a", "authorA", 1700000000L, 10, 1));

        feedSourceOperations.refreshSource(source.id(), 30);

        FeedItem item = feedItemRepository.findAll().stream()
                .filter(i -> i.getUrl().equals("https://example.com/snap-url-a"))
                .findFirst()
                .orElseThrow();

        // Save feed item to Inbox (SavedResource)
        SavedResourceView savedView = savedResourceOperations.saveFeedItem(new CreateFeedSavedResourceCommand(
                item.getId(),
                SavedResourceKind.ARTICLE
        ));

        assertThat(savedView.title()).isEqualTo("Original Story Title 101");
        assertThat(savedView.resourceUrl()).isEqualTo("https://example.com/snap-url-a");

        // Refresh 2: Story 102 with SAME URL A and updated title
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(102L));
        when(hackerNewsClient.fetchItem(eq(102L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(102L, "Revised Title 102", "https://example.com/snap-url-a", "authorB", 1700000500L, 25, 2));

        feedSourceOperations.refreshSource(source.id(), 30);

        // The FeedItem itself was updated
        FeedItem updatedFeedItem = feedItemRepository.findById(item.getId()).orElseThrow();
        assertThat(updatedFeedItem.getTitle()).isEqualTo("Revised Title 102");

        // But the SavedResource in Inbox retains its original snapshot!
        SavedResourceView reloadedSaved = savedResourceOperations.findSavedResourceById(savedView.id()).orElseThrow();
        assertThat(reloadedSaved.title())
                .as("SavedResource title snapshot must remain unmodified")
                .isEqualTo("Original Story Title 101");
        assertThat(reloadedSaved.resourceUrl())
                .as("SavedResource resourceUrl snapshot must remain unmodified")
                .isEqualTo("https://example.com/snap-url-a");

        // Verify it appears unchanged in the Inbox query
        SavedResourceInboxPageView inboxPage = savedResourceOperations.findInboxSavedResources(null, 50, null);
        assertThat(inboxPage.items()).hasSize(1);
        assertThat(inboxPage.items().get(0).title()).isEqualTo("Original Story Title 101");
        assertThat(inboxPage.items().get(0).resourceUrl()).isEqualTo("https://example.com/snap-url-a");
    }
}
