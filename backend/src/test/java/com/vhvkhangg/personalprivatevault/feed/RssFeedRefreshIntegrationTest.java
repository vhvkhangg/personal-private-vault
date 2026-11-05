package com.vhvkhangg.personalprivatevault.feed;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshAdmission;
import com.vhvkhangg.personalprivatevault.feed.internal.application.RssFeedRefreshFinalizer;
import com.vhvkhangg.personalprivatevault.feed.internal.application.RssFeedRefreshService;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedItem;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedItemRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssAtomParser;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssFeedClient;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssFeedDestinationPolicy;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssFeedNormalizer;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.command.UpdateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedSourceNotFoundException;
import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.resource.SavedResourceOperations;
import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateFeedSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.feed.view.RssFeedRefreshResultView;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class RssFeedRefreshIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FeedSourceOperations feedSourceOperations;

    @Autowired
    private FeedItemOperations feedItemOperations;

    @Autowired
    private SavedResourceOperations savedResourceOperations;

    @Autowired
    private RssFeedRefreshFinalizer rssFinalizer;

    @Autowired
    private FeedItemRepository feedItemRepository;

    @Autowired
    private FeedSourceRepository feedSourceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @Autowired
    private HackerNewsRefreshAdmission admission;

    @Autowired
    private RssAtomParser parser;

    @Autowired
    private RssFeedNormalizer normalizer;

    @Autowired
    private RssFeedDestinationPolicy destinationPolicy;

    @MockitoBean
    private RssFeedClient rssFeedClient;

    @MockitoBean
    private Clock clock;

    private static final Instant DEFAULT_NOW = Instant.parse("2026-11-04T12:00:00Z");

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(DEFAULT_NOW);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
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

    private FeedSourceView createRssSource(String name, boolean enabled, String feedUrl) {
        return feedSourceOperations.createSource(new CreateFeedSourceCommand(
                name,
                FeedSourceType.RSS,
                "https://example.com",
                feedUrl != null ? feedUrl : "https://example.com/rss.xml",
                enabled,
                false,
                null,
                null
        ));
    }

    private FeedSourceView createHackerNewsSource(String name) {
        return feedSourceOperations.createSource(new CreateFeedSourceCommand(
                name,
                FeedSourceType.HACKER_NEWS,
                "https://news.ycombinator.com",
                null,
                true,
                false,
                null,
                null
        ));
    }

    @Test
    @DisplayName("Happy path: normalizes RSS 2.0 items, inserts rows with null externalId, updates timestamps")
    void happyPath_rss20ItemsInsertedWithNullExternalId() {
        FeedSourceView source = createRssSource("Tech RSS", true, "https://example.com/rss.xml");

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                  <channel>
                    <title>Tech News</title>
                    <item>
                      <title>PostgreSQL 17 Released</title>
                      <link>https://example.com/pg17</link>
                      <guid>pg-17-guid</guid>
                      <author>postgres-team</author>
                      <pubDate>Wed, 04 Nov 2026 10:00:00 GMT</pubDate>
                    </item>
                    <item>
                      <title>Java 25 GA</title>
                      <link>https://example.com/java25</link>
                      <guid>java-25-guid</guid>
                      <author>oracle-team</author>
                      <pubDate>Wed, 04 Nov 2026 11:00:00 GMT</pubDate>
                    </item>
                  </channel>
                </rss>
                """;

        when(rssFeedClient.fetchFeed(eq("https://example.com/rss.xml"), anyLong()))
                .thenReturn(xml.getBytes(StandardCharsets.UTF_8));

        RssFeedRefreshResultView result = feedSourceOperations.refreshRssSource(source.id());

        assertThat(result.sourceId()).isEqualTo(source.id());
        assertThat(result.fetchedAt()).isEqualTo(DEFAULT_NOW);
        assertThat(result.parsedEntries()).isEqualTo(2);
        assertThat(result.eligibleEntries()).isEqualTo(2);
        assertThat(result.distinctArticleUrls()).isEqualTo(2);
        assertThat(result.processedItems()).isEqualTo(2);

        List<FeedItem> items = feedItemRepository.findAll();
        assertThat(items).hasSize(2);
        assertThat(items).allMatch(i -> i.getExternalId() == null);
        assertThat(items).extracting(FeedItem::getUrl)
                .containsExactlyInAnyOrder("https://example.com/pg17", "https://example.com/java25");

        FeedItem pgItem = items.stream().filter(i -> i.getUrl().equals("https://example.com/pg17")).findFirst().orElseThrow();
        assertThat(pgItem.getTitle()).isEqualTo("PostgreSQL 17 Released");
        assertThat(pgItem.getAuthor()).isEqualTo("postgres-team");
        assertThat(pgItem.getRawMetadata()).isNotNull();
        assertThat(pgItem.getRawMetadata()).containsEntry("feedFormat", "RSS_2_0");
        assertThat(pgItem.getRawMetadata()).containsEntry("feedEntryId", "pg-17-guid");
        assertThat(pgItem.getRawMetadata()).containsEntry("rssCapturedAt", DEFAULT_NOW.toString());

        FeedSource refreshedSource = feedSourceRepository.findById(source.id()).orElseThrow();
        assertThat(refreshedSource.getLastFetchedAt()).isEqualTo(DEFAULT_NOW);
    }

    @Test
    @DisplayName("Same URL updates existing row across refreshes, retaining dual-key integrity")
    void sameUrlUpdatesExistingRow() {
        FeedSourceView source = createRssSource("Update RSS", true, "https://example.com/feed.xml");

        String xmlInitial = """
                <rss version="2.0"><channel>
                  <item>
                    <title>Initial Title</title>
                    <link>https://example.com/item1</link>
                    <guid>id-1</guid>
                    <pubDate>Wed, 04 Nov 2026 09:00:00 GMT</pubDate>
                  </item>
                </channel></rss>
                """;

        when(rssFeedClient.fetchFeed(eq("https://example.com/feed.xml"), anyLong()))
                .thenReturn(xmlInitial.getBytes(StandardCharsets.UTF_8));

        RssFeedRefreshResultView initialResult = feedSourceOperations.refreshRssSource(source.id());
        assertThat(initialResult.processedItems()).isEqualTo(1);
        FeedItem itemBefore = feedItemRepository.findAll().get(0);
        assertThat(itemBefore.getTitle()).isEqualTo("Initial Title");

        String xmlUpdated = """
                <rss version="2.0"><channel>
                  <item>
                    <title>Updated Title</title>
                    <link>https://example.com/item1</link>
                    <guid>id-1</guid>
                    <pubDate>Wed, 04 Nov 2026 10:00:00 GMT</pubDate>
                  </item>
                </channel></rss>
                """;

        when(rssFeedClient.fetchFeed(eq("https://example.com/feed.xml"), anyLong()))
                .thenReturn(xmlUpdated.getBytes(StandardCharsets.UTF_8));

        Instant updatedNow = Instant.parse("2026-11-04T12:30:00Z");
        when(clock.instant()).thenReturn(updatedNow);

        RssFeedRefreshResultView updateResult = feedSourceOperations.refreshRssSource(source.id());
        assertThat(updateResult.processedItems()).isEqualTo(1);

        List<FeedItem> itemsAfter = feedItemRepository.findAll();
        assertThat(itemsAfter).hasSize(1);
        assertThat(itemsAfter.get(0).getId()).isEqualTo(itemBefore.getId());
        assertThat(itemsAfter.get(0).getTitle()).isEqualTo("Updated Title");
        assertThat(itemsAfter.get(0).getFetchedAt()).isEqualTo(updatedNow);
    }

    @Test
    @DisplayName("FR-06: RSS Save-to-Inbox exact link and immutable saved metadata are retained after refresh")
    void saveToInboxRetainsImmutableSnapshotAcrossRssRefresh() {
        FeedSourceView source = createRssSource("Inbox RSS", true, "https://example.com/inbox-feed.xml");

        String initialXml = """
                <rss version="2.0"><channel>
                  <item>
                    <title>Initial Article Title</title>
                    <link>https://example.com/saved-article</link>
                    <guid>saved-guid-1</guid>
                    <author>Initial Author</author>
                    <pubDate>Wed, 04 Nov 2026 09:00:00 GMT</pubDate>
                  </item>
                </channel></rss>
                """;

        when(rssFeedClient.fetchFeed(eq("https://example.com/inbox-feed.xml"), anyLong()))
                .thenReturn(initialXml.getBytes(StandardCharsets.UTF_8));

        feedSourceOperations.refreshRssSource(source.id());

        FeedItem item = feedItemRepository.findAll().stream()
                .filter(i -> i.getUrl().equals("https://example.com/saved-article"))
                .findFirst()
                .orElseThrow();

        SavedResourceView savedView = savedResourceOperations.saveFeedItem(new CreateFeedSavedResourceCommand(
                item.getId(),
                SavedResourceKind.ARTICLE
        ));

        assertThat(savedView.title()).isEqualTo("Initial Article Title");
        assertThat(savedView.resourceUrl()).isEqualTo("https://example.com/saved-article");

        String updatedXml = """
                <rss version="2.0"><channel>
                  <item>
                    <title>Revised Article Title</title>
                    <link>https://example.com/saved-article</link>
                    <guid>saved-guid-1</guid>
                    <author>Revised Author</author>
                    <pubDate>Wed, 04 Nov 2026 11:00:00 GMT</pubDate>
                  </item>
                </channel></rss>
                """;

        when(rssFeedClient.fetchFeed(eq("https://example.com/inbox-feed.xml"), anyLong()))
                .thenReturn(updatedXml.getBytes(StandardCharsets.UTF_8));

        Instant updatedNow = Instant.parse("2026-11-04T12:30:00Z");
        when(clock.instant()).thenReturn(updatedNow);

        feedSourceOperations.refreshRssSource(source.id());

        FeedItem updatedFeedItem = feedItemRepository.findById(item.getId()).orElseThrow();
        assertThat(updatedFeedItem.getTitle()).isEqualTo("Revised Article Title");
        assertThat(updatedFeedItem.getAuthor()).isEqualTo("Revised Author");

        SavedResourceView reloadedSaved = savedResourceOperations.findSavedResourceById(savedView.id()).orElseThrow();
        assertThat(reloadedSaved.title()).isEqualTo("Initial Article Title");
        assertThat(reloadedSaved.resourceUrl()).isEqualTo("https://example.com/saved-article");
    }

    @Test
    @DisplayName("FR-06: Preexisting item with legacy externalId is matched by URL and updated; changed URL creates distinct item")
    void legacyExternalIdPreservedAndChangedArticleUrlCreatesDistinctItem() {
        FeedSourceView source = createRssSource("Legacy RSS", true, "https://example.com/legacy-feed.xml");

        NormalizedFeedItemInput legacyInput = new NormalizedFeedItemInput(
                "legacy-ext-100",
                "Legacy Article Title",
                "https://example.com/article-1",
                "Legacy Author",
                null,
                Instant.parse("2026-11-04T08:00:00Z"),
                null
        );
        feedItemOperations.ingestFetch(source.id(), DEFAULT_NOW, List.of(legacyInput));

        FeedItem seeded = feedItemRepository.findAll().get(0);
        assertThat(seeded.getExternalId()).isEqualTo("legacy-ext-100");
        assertThat(seeded.getUrl()).isEqualTo("https://example.com/article-1");

        String xml = """
                <rss version="2.0"><channel>
                  <item>
                    <title>Updated Article 1</title>
                    <link>https://example.com/article-1</link>
                    <guid>guid-1</guid>
                    <pubDate>Wed, 04 Nov 2026 10:00:00 GMT</pubDate>
                  </item>
                  <item>
                    <title>New Article 2</title>
                    <link>https://example.com/article-2</link>
                    <guid>guid-2</guid>
                    <pubDate>Wed, 04 Nov 2026 10:30:00 GMT</pubDate>
                  </item>
                </channel></rss>
                """;

        when(rssFeedClient.fetchFeed(eq("https://example.com/legacy-feed.xml"), anyLong()))
                .thenReturn(xml.getBytes(StandardCharsets.UTF_8));

        RssFeedRefreshResultView result = feedSourceOperations.refreshRssSource(source.id());
        assertThat(result.processedItems()).isEqualTo(2);

        List<FeedItem> items = feedItemRepository.findAll();
        assertThat(items).hasSize(2);

        FeedItem updatedSeeded = items.stream().filter(i -> i.getUrl().equals("https://example.com/article-1")).findFirst().orElseThrow();
        assertThat(updatedSeeded.getId()).isEqualTo(seeded.getId());
        assertThat(updatedSeeded.getTitle()).isEqualTo("Updated Article 1");
        assertThat(updatedSeeded.getExternalId()).as("Matched legacy externalId must be cleared per ARTICLE_URL contract").isNull();

        FeedItem newItem = items.stream().filter(i -> i.getUrl().equals("https://example.com/article-2")).findFirst().orElseThrow();
        assertThat(newItem.getTitle()).isEqualTo("New Article 2");
        assertThat(newItem.getExternalId()).isNull();
    }

    @Test
    @DisplayName("FR-06: Same provenance ID changing article URL across refreshes creates a new row and retains prior row")
    void sameProvenanceIdChangingUrlAcrossRefreshesCreatesNewRowAndRetainsPriorRow() {
        FeedSourceView source = createRssSource("URL Change RSS", true, "https://example.com/url-change.xml");

        // Refresh 1: provenance ID 'item-guid-1' with URL A
        String xml1 = """
                <rss version="2.0"><channel>
                  <item>
                    <title>Article at URL A</title>
                    <link>https://example.com/url-a</link>
                    <guid>item-guid-1</guid>
                    <pubDate>Wed, 04 Nov 2026 09:00:00 GMT</pubDate>
                  </item>
                </channel></rss>
                """;
        when(rssFeedClient.fetchFeed(eq("https://example.com/url-change.xml"), anyLong()))
                .thenReturn(xml1.getBytes(StandardCharsets.UTF_8));

        feedSourceOperations.refreshRssSource(source.id());

        List<FeedItem> itemsAfter1 = feedItemRepository.findAll();
        assertThat(itemsAfter1).hasSize(1);
        FeedItem itemA = itemsAfter1.get(0);
        assertThat(itemA.getUrl()).isEqualTo("https://example.com/url-a");
        assertThat(itemA.getExternalId()).isNull();
        assertThat(itemA.getRawMetadata()).containsEntry("feedEntryId", "item-guid-1");

        // Refresh 2: SAME provenance ID 'item-guid-1' with CHANGED URL B
        String xml2 = """
                <rss version="2.0"><channel>
                  <item>
                    <title>Article at URL B</title>
                    <link>https://example.com/url-b</link>
                    <guid>item-guid-1</guid>
                    <pubDate>Wed, 04 Nov 2026 10:00:00 GMT</pubDate>
                  </item>
                </channel></rss>
                """;
        when(rssFeedClient.fetchFeed(eq("https://example.com/url-change.xml"), anyLong()))
                .thenReturn(xml2.getBytes(StandardCharsets.UTF_8));

        feedSourceOperations.refreshRssSource(source.id());

        List<FeedItem> itemsAfter2 = feedItemRepository.findAll();
        // Because RSS identity is strictly by (source_id, url) and externalId is null, URL B inserts a new row
        assertThat(itemsAfter2).hasSize(2);
        FeedItem retainedA = itemsAfter2.stream().filter(i -> i.getUrl().equals("https://example.com/url-a")).findFirst().orElseThrow();
        FeedItem newB = itemsAfter2.stream().filter(i -> i.getUrl().equals("https://example.com/url-b")).findFirst().orElseThrow();

        assertThat(retainedA.getId()).isEqualTo(itemA.getId());
        assertThat(retainedA.getExternalId()).isNull();
        assertThat(newB.getId()).isNotEqualTo(itemA.getId());
        assertThat(newB.getUrl()).isEqualTo("https://example.com/url-b");
        assertThat(newB.getExternalId()).isNull();
        assertThat(newB.getRawMetadata()).containsEntry("feedEntryId", "item-guid-1");
    }

    @Test
    @DisplayName("FR-06: Hostile URL credentials and token markers are absent from exception messages and error output")
    void hostileUrlMarkersAbsentFromExceptionsAndLogs() {
        FeedSourceView source = createRssSource("Hostile RSS", true, "https://user:secret-token-xyz123@example.com/feed.xml");

        assertThatThrownBy(() -> feedSourceOperations.refreshRssSource(source.id()))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> {
                    assertThat(ex.getMessage()).doesNotContain("secret-token-xyz123");
                    assertThat(ex.getMessage()).doesNotContain("user:");
                });
    }

    @Test
    @DisplayName("FR-04: Thread interruption is honored and throws UPSTREAM_UNAVAILABLE")
    void refreshHonorsThreadInterruption() {
        FeedSourceView source = createRssSource("Interrupt RSS", true, "https://example.com/interrupt.xml");

        Thread.currentThread().interrupt();
        try {
            assertThatThrownBy(() -> feedSourceOperations.refreshRssSource(source.id()))
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                            .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));
        } finally {
            Thread.interrupted(); // clear interrupted flag
        }
    }

    @Test
    @DisplayName("Valid empty RSS feed commits zero items and advances source fetch timestamps")
    void validEmptyFeedAdvancesTimestamps() {
        FeedSourceView source = createRssSource("Empty RSS", true, "https://example.com/empty.xml");

        String emptyXml = "<rss version=\"2.0\"><channel><title>Empty</title></channel></rss>";
        when(rssFeedClient.fetchFeed(eq("https://example.com/empty.xml"), anyLong()))
                .thenReturn(emptyXml.getBytes(StandardCharsets.UTF_8));

        RssFeedRefreshResultView result = feedSourceOperations.refreshRssSource(source.id());

        assertThat(result.parsedEntries()).isEqualTo(0);
        assertThat(result.eligibleEntries()).isEqualTo(0);
        assertThat(result.distinctArticleUrls()).isEqualTo(0);
        assertThat(result.processedItems()).isEqualTo(0);

        assertThat(feedItemRepository.findAll()).isEmpty();
        FeedSource updatedSource = feedSourceRepository.findById(source.id()).orElseThrow();
        assertThat(updatedSource.getLastFetchedAt()).isEqualTo(DEFAULT_NOW);
    }

    @Test
    @DisplayName("Preflight rejection: disabled source throws SOURCE_DISABLED with zero provider calls")
    void disabledSourceThrowsSourceDisabled() {
        FeedSourceView source = createRssSource("Disabled RSS", false, "https://example.com/rss.xml");

        assertThatThrownBy(() -> feedSourceOperations.refreshRssSource(source.id()))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.SOURCE_DISABLED));

        verifyNoInteractions(rssFeedClient);
    }

    @Test
    @DisplayName("Preflight rejection: non-RSS source throws UNSUPPORTED_SOURCE_TYPE with zero provider calls")
    void wrongTypeThrowsUnsupportedSourceType() {
        FeedSourceView source = createHackerNewsSource("HN Source");

        assertThatThrownBy(() -> feedSourceOperations.refreshRssSource(source.id()))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UNSUPPORTED_SOURCE_TYPE));

        verifyNoInteractions(rssFeedClient);
    }

    @Test
    @DisplayName("Preflight rejection: nonexistent source ID throws FeedSourceNotFoundException")
    void nonexistentSourceThrowsNotFound() {
        assertThatThrownBy(() -> feedSourceOperations.refreshRssSource(999999L))
                .isInstanceOf(FeedSourceNotFoundException.class);

        verifyNoInteractions(rssFeedClient);
    }

    @Test
    @DisplayName("Mid-HTTP source edit rolls back staged changes and throws SOURCE_CHANGED_DURING_REFRESH")
    void midHttpSourceEditRollsBack() {
        FeedSourceView source = createRssSource("MidEdit RSS", true, "https://example.com/original.xml");

        String xml = """
                <rss version="2.0"><channel>
                  <item><title>Item 1</title><link>https://example.com/1</link></item>
                </channel></rss>
                """;

        when(rssFeedClient.fetchFeed(eq("https://example.com/original.xml"), anyLong())).thenAnswer(invocation -> {
            // Mid-HTTP modification of feedUrl
            feedSourceOperations.updateSource(source.id(), new UpdateFeedSourceCommand(
                    source.name(),
                    source.type(),
                    source.sourceUrl(),
                    "https://example.com/changed.xml",
                    source.enabled(),
                    source.scheduledRefreshEnabled(),
                    source.refreshIntervalMinutes(),
                    source.config()
            ));
            return xml.getBytes(StandardCharsets.UTF_8);
        });

        assertThatThrownBy(() -> feedSourceOperations.refreshRssSource(source.id()))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH));

        assertThat(feedItemRepository.findAll()).isEmpty();
        FeedSource untouched = feedSourceRepository.findById(source.id()).orElseThrow();
        assertThat(untouched.getLastFetchedAt()).isNull();
    }

    @Test
    @DisplayName("S2A-07: ingestFetch and source update share identical physical PostgreSQL backend and transaction identity")
    void ingestFetchAndSourceUpdateSharePhysicalTransactionIdentity() {
        RefreshTransactionIdentities identities = observeRefreshTransactionIdentities(false);

        assertThat(identities.ingestCommittedBeforeFinalizerCommit()).isFalse();
        assertSharedPhysicalTransaction(identities);
    }

    @Test
    @DisplayName("S2A-07 negative control: independently committed REQUIRES_NEW ingest fails shared transaction assertion")
    void separatelyCommittedRequiresNewIngestFailsSharedIdentityAssertion() {
        RefreshTransactionIdentities identities = observeRefreshTransactionIdentities(true);

        assertThat(identities.ingestCommittedBeforeFinalizerCommit()).isTrue();
        assertThat(identities.ingest().backendPid()).isNotEqualTo(identities.finalizerBeforeCommit().backendPid());
        assertThat(identities.ingest().transactionId()).isNotEqualTo(identities.finalizerBeforeCommit().transactionId());
        assertThatThrownBy(() -> assertSharedPhysicalTransaction(identities))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("actual ingest and authoritative finalizer beforeCommit");
    }

    @Test
    @DisplayName("FR-06: Synthetic credential tokens and hostile XML markers are absent from exception messages and logs")
    void credentialAndHostileMarkersAbsentFromExceptionsAndLogs(CapturedOutput output) {
        // 1. Synthetic credential token in userInfo: https://user:secret-token-xyz123@example.com/feed.xml
        String credentialUrl = "https://user:secret-token-xyz123@example.com/feed.xml";
        FeedSourceView credSource = createRssSource("Cred Source", true, credentialUrl);

        assertThatThrownBy(() -> feedSourceOperations.refreshRssSource(credSource.id()))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(ex.getMessage()).doesNotContain("secret-token-xyz123"));
        assertThat(output.getAll()).doesNotContain("secret-token-xyz123");

        // 2. Hostile XML marker in payload: EVIL_XXE_MARKER_999
        String hostileXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE test [
                  <!ENTITY xxe SYSTEM "http://127.0.0.1/EVIL_XXE_MARKER_999">
                ]>
                <rss version="2.0"><channel><title>&xxe;</title><item><title>T</title><link>https://example.com</link></item></channel></rss>
                """;
        FeedSourceView hostileSource = createRssSource("Hostile Source", true, "https://example.com/hostile.xml");
        when(rssFeedClient.fetchFeed(eq("https://example.com/hostile.xml"), anyLong()))
                .thenReturn(hostileXml.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> feedSourceOperations.refreshRssSource(hostileSource.id()))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(ex.getMessage()).doesNotContain("EVIL_XXE_MARKER_999"));
        assertThat(output.getAll()).doesNotContain("EVIL_XXE_MARKER_999");
    }

    private RefreshTransactionIdentities observeRefreshTransactionIdentities(boolean requiresNewIngest) {
        FeedSourceView source = createRssSource("RSS Shared Tx", true, "https://example.com/tx.xml");

        AtomicReference<PhysicalTransactionIdentity> ingest = new AtomicReference<>();
        AtomicReference<PhysicalTransactionIdentity> sourceUpdate = new AtomicReference<>();
        AtomicReference<PhysicalTransactionIdentity> finalizerBeforeCommit = new AtomicReference<>();
        AtomicBoolean ingestCommitted = new AtomicBoolean();
        AtomicBoolean ingestCommittedBeforeFinalizerCommit = new AtomicBoolean();

        Object finalizerTarget = AopTestUtils.getUltimateTargetObject(rssFinalizer);
        FeedItemOperations originalOps = (FeedItemOperations) ReflectionTestUtils.getField(finalizerTarget, "feedItemOperations");
        Object ingestTarget = AopTestUtils.getUltimateTargetObject(originalOps);
        FeedSourceRepository originalIngestRepository = (FeedSourceRepository) ReflectionTestUtils.getField(ingestTarget, "feedSourceRepository");
        FeedSourceRepository originalFinalizerRepository = (FeedSourceRepository) ReflectionTestUtils.getField(finalizerTarget, "feedSourceRepository");

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

            String xml = "<rss version=\"2.0\"><channel><item><title>Tx Item</title><link>https://example.com/tx1</link></item></channel></rss>";
            when(rssFeedClient.fetchFeed(eq("https://example.com/tx.xml"), anyLong()))
                    .thenReturn(xml.getBytes(StandardCharsets.UTF_8));

            assertThat(feedSourceOperations.refreshRssSource(source.id()).processedItems()).isEqualTo(1);
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
        return jdbcTemplate.queryForObject(
                "SELECT pg_backend_pid() AS pid, pg_current_xact_id()::text AS xid",
                (rs, rowNum) -> new PhysicalTransactionIdentity(rs.getLong("pid"), rs.getString("xid"))
        );
    }

    private void assertSharedPhysicalTransaction(RefreshTransactionIdentities identities) {
        assertThat(identities.sourceUpdate()).as("generic ingest source update").isEqualTo(identities.ingest());
        assertThat(identities.ingest())
                .as("actual ingest and authoritative finalizer beforeCommit must share PostgreSQL backend PID and transaction identity")
                .isEqualTo(identities.finalizerBeforeCommit());
    }

    private record PhysicalTransactionIdentity(long backendPid, String transactionId) {}

    private record RefreshTransactionIdentities(
            PhysicalTransactionIdentity ingest,
            PhysicalTransactionIdentity sourceUpdate,
            PhysicalTransactionIdentity finalizerBeforeCommit,
            boolean ingestCommittedBeforeFinalizerCommit
    ) {}
}
