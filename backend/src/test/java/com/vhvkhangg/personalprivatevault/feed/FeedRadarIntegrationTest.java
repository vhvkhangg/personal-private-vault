package com.vhvkhangg.personalprivatevault.feed;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedItem;
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
import com.vhvkhangg.personalprivatevault.feed.view.FeedRadarItemView;
import com.vhvkhangg.personalprivatevault.feed.view.FeedRadarPageView;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FeedRadarIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-11-02T12:00:00Z");

    @Autowired
    private FeedSourceOperations feedSourceOperations;

    @Autowired
    private FeedItemOperations feedItemOperations;

    @Autowired
    private FeedItemRepository feedItemRepository;

    @Autowired
    private FeedSourceRepository feedSourceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private HackerNewsClient hackerNewsClient;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(FIXED_NOW);
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

    private FeedSourceView createSource(String name, FeedSourceType type, boolean enabled) {
        return feedSourceOperations.createSource(new CreateFeedSourceCommand(
                name,
                type,
                "https://example.com",
                type == FeedSourceType.RSS ? "https://example.com/rss" : null,
                enabled,
                false,
                null,
                null
        ));
    }

    private FeedItem insertFeedItem(Long sourceId, String title, String url, Instant publishedAt, Map<String, Object> rawMetadata) {
        NormalizedFeedItemInput input = new NormalizedFeedItemInput(
                null,
                title,
                url,
                "Test Author",
                null,
                publishedAt,
                rawMetadata
        );
        feedItemOperations.ingestFetch(sourceId, Instant.now(), List.of(input));
        return feedItemRepository.findAll().stream()
                .filter(i -> i.getUrl().equals(url))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Rolling 24-hour window: boundary equality inclusive, outside exclusive, null/future excluded")
    void rolling24HourWindow_boundariesAndExclusions() {
        FeedSourceView hnSource = createSource("HN Main", FeedSourceType.HACKER_NEWS, true);

        Instant now = FIXED_NOW;
        Instant windowStart = now.minus(Duration.ofHours(24));
        Instant windowEnd = now;

        // Exactly at windowStart (inclusive)
        insertFeedItem(hnSource.id(), "At Window Start", "https://example.com/start", windowStart, null);
        // 1 second before windowStart (excluded)
        insertFeedItem(hnSource.id(), "Before Window Start", "https://example.com/before", windowStart.minusSeconds(1), null);
        // Inside window
        insertFeedItem(hnSource.id(), "Inside Window", "https://example.com/middle", now.minus(Duration.ofHours(6)), null);
        // Exactly at windowEnd (inclusive)
        insertFeedItem(hnSource.id(), "At Window End", "https://example.com/end", windowEnd, null);
        // 1 second in future (excluded)
        insertFeedItem(hnSource.id(), "In Future", "https://example.com/future", now.plusSeconds(1), null);
        // Null publishedAt (excluded)
        insertFeedItem(hnSource.id(), "Null Published", "https://example.com/null-pub", null, null);

        FeedRadarPageView page = feedItemOperations.getRadar(null, 50);

        List<String> titles = page.items().stream().map(FeedRadarItemView::title).toList();
        assertThat(titles).containsExactly(
                "At Window End",
                "Inside Window",
                "At Window Start"
        );
        assertThat(titles).doesNotContain("Before Window Start", "In Future", "Null Published");

        verifyNoInteractions(hackerNewsClient);
    }

    @Test
    @DisplayName("Deterministic ordering: publishedAt DESC then id DESC for identical submission times")
    void deterministicOrder_publishedAtDescThenIdDesc() {
        FeedSourceView hnSource = createSource("HN Ties", FeedSourceType.HACKER_NEWS, true);

        Instant tieTime = FIXED_NOW.minus(Duration.ofHours(2));

        FeedItem item1 = insertFeedItem(hnSource.id(), "Tie Item 1", "https://example.com/tie1", tieTime, null);
        FeedItem item2 = insertFeedItem(hnSource.id(), "Tie Item 2", "https://example.com/tie2", tieTime, null);
        FeedItem item3 = insertFeedItem(hnSource.id(), "Tie Item 3", "https://example.com/tie3", tieTime, null);

        // IDs are sequential: item3 > item2 > item1
        assertThat(item3.getId()).isGreaterThan(item2.getId());
        assertThat(item2.getId()).isGreaterThan(item1.getId());

        FeedRadarPageView page = feedItemOperations.getRadar(null, 50);

        List<Long> itemIds = page.items().stream().map(FeedRadarItemView::feedItemId).toList();
        assertThat(itemIds).containsExactly(item3.getId(), item2.getId(), item1.getId());
    }

    @Test
    @DisplayName("Source filtering and eligibility: includes only enabled HN sources, rejects invalid filters")
    void sourceFilteringAndExeligibility() {
        FeedSourceView enabledHn = createSource("Enabled HN", FeedSourceType.HACKER_NEWS, true);
        FeedSourceView disabledHn = createSource("Disabled HN", FeedSourceType.HACKER_NEWS, true);
        FeedSourceView enabledRss = createSource("Enabled RSS", FeedSourceType.RSS, true);

        Instant published = FIXED_NOW.minus(Duration.ofHours(1));

        insertFeedItem(enabledHn.id(), "HN Item", "https://example.com/hn", published, null);
        insertFeedItem(disabledHn.id(), "Disabled HN Item", "https://example.com/disabled-hn", published, null);
        insertFeedItem(enabledRss.id(), "RSS Item", "https://example.com/rss-item", published, null);

        // Now disable disabledHn
        feedSourceOperations.updateSource(disabledHn.id(), new com.vhvkhangg.personalprivatevault.feed.source.command.UpdateFeedSourceCommand(
                disabledHn.name(),
                FeedSourceType.HACKER_NEWS,
                disabledHn.sourceUrl(),
                disabledHn.feedUrl(),
                false,
                false,
                null,
                null
        ));

        // Global query: only enabled HN items returned
        FeedRadarPageView globalPage = feedItemOperations.getRadar(null, 50);
        assertThat(globalPage.items()).hasSize(1);
        assertThat(globalPage.items().get(0).title()).isEqualTo("HN Item");

        // Filtered by enabled HN source: succeeds
        FeedRadarPageView filteredPage = feedItemOperations.getRadar(enabledHn.id(), 50);
        assertThat(filteredPage.items()).hasSize(1);
        assertThat(filteredPage.items().get(0).title()).isEqualTo("HN Item");

        // Filtered by disabled source: throws FeedRefreshException(SOURCE_DISABLED)
        assertThatThrownBy(() -> feedItemOperations.getRadar(disabledHn.id(), 50))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.SOURCE_DISABLED));

        // Filtered by RSS source: throws FeedRefreshException(UNSUPPORTED_SOURCE_TYPE)
        assertThatThrownBy(() -> feedItemOperations.getRadar(enabledRss.id(), 50))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UNSUPPORTED_SOURCE_TYPE));

        // Filtered by non-existent source: throws FeedSourceNotFoundException
        assertThatThrownBy(() -> feedItemOperations.getRadar(888888L, 50))
                .isInstanceOf(FeedSourceNotFoundException.class);
    }

    @Test
    @DisplayName("Signal and provenance mapping: maps HN capture details when present, falls back to SOURCE_CACHE")
    void provenanceMapping_validAndLegacyFallback() {
        FeedSourceView hnSource = createSource("HN Provenance", FeedSourceType.HACKER_NEWS, true);
        Instant published = FIXED_NOW.minus(Duration.ofHours(3));

        // 1. Valid HN provenance metadata
        Map<String, Object> validHnMeta = new LinkedHashMap<>();
        validHnMeta.put("hnStoryId", 77777L);
        validHnMeta.put("hnDiscussionUrl", "https://news.ycombinator.com/item?id=77777");
        validHnMeta.put("hnRank", 5);
        validHnMeta.put("hnCapturedAt", "2026-11-02T08:00:00Z");

        insertFeedItem(hnSource.id(), "With HN Provenance", "https://example.com/valid-hn", published, validHnMeta);

        // 2. Legacy / missing provenance metadata
        insertFeedItem(hnSource.id(), "Legacy Without Provenance", "https://example.com/legacy-hn", published.minusSeconds(10), null);

        FeedRadarPageView page = feedItemOperations.getRadar(null, 50);
        assertThat(page.items()).hasSize(2);

        FeedRadarItemView validItem = page.items().get(0);
        assertThat(validItem.title()).isEqualTo("With HN Provenance");
        assertThat(validItem.signalType()).isEqualTo("HN_TOP_STORIES_AT_CAPTURE");
        assertThat(validItem.sourceRank()).isEqualTo(5);
        assertThat(validItem.hnStoryId()).isEqualTo(77777L);
        assertThat(validItem.hnDiscussionUrl()).isEqualTo("https://news.ycombinator.com/item?id=77777");
        assertThat(validItem.signalCapturedAt()).isEqualTo(Instant.parse("2026-11-02T08:00:00Z"));

        FeedRadarItemView legacyItem = page.items().get(1);
        assertThat(legacyItem.title()).isEqualTo("Legacy Without Provenance");
        assertThat(legacyItem.signalType()).isEqualTo("SOURCE_CACHE");
        assertThat(legacyItem.sourceRank()).isNull();
        assertThat(legacyItem.hnStoryId()).isNull();
        assertThat(legacyItem.hnDiscussionUrl()).isNull();
        assertThat(legacyItem.signalCapturedAt()).isNull();
    }

    @Test
    @DisplayName("Database-side paging: limit strictly bounds returned results")
    void limitBounds_appliesDatabaseSidePaging() {
        FeedSourceView hnSource = createSource("HN Paging", FeedSourceType.HACKER_NEWS, true);
        Instant baseTime = FIXED_NOW.minus(Duration.ofHours(5));

        for (int i = 1; i <= 10; i++) {
            insertFeedItem(hnSource.id(), "Article " + i, "https://example.com/page/" + i, baseTime.plusSeconds(i * 10), null);
        }

        FeedRadarPageView page = feedItemOperations.getRadar(null, 4);
        assertThat(page.items()).hasSize(4);
        assertThat(page.items().get(0).title()).isEqualTo("Article 10");
        assertThat(page.items().get(3).title()).isEqualTo("Article 7");
    }

    @Test
    @DisplayName("Invalid legacy article URLs are excluded from Radar view")
    void invalidLegacyArticleUrls_excludedFromRadar() {
        FeedSourceView hnSource = createSource("HN Invalid URL", FeedSourceType.HACKER_NEWS, true);
        Instant published = FIXED_NOW.minus(Duration.ofHours(2));

        // Insert item directly into repository bypassing ingestion URL validation if possible, or using feedItemOperations
        // Note: feedItemOperations allows any valid string that was saved in legacy DB;
        // In this test, we can use direct JdbcTemplate to simulate legacy rows with invalid URLs:
        jdbcTemplate.update(
                "INSERT INTO feed_items (feed_source_id, external_id, title, url, url_hash, author, published_at, fetched_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                hnSource.id(), null, "Invalid Scheme", "ftp://example.com/file.txt", "hash-ftp", "Author",
                java.sql.Timestamp.from(published), java.sql.Timestamp.from(FIXED_NOW)
        );
        jdbcTemplate.update(
                "INSERT INTO feed_items (feed_source_id, external_id, title, url, url_hash, author, published_at, fetched_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                hnSource.id(), null, "Javascript Scheme", "javascript:alert(1)", "hash-js", "Author",
                java.sql.Timestamp.from(published), java.sql.Timestamp.from(FIXED_NOW)
        );
        insertFeedItem(hnSource.id(), "Valid HTTP Item", "https://example.com/valid", published, null);

        FeedRadarPageView page = feedItemOperations.getRadar(null, 50);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).title()).isEqualTo("Valid HTTP Item");
    }

    @Test
    @DisplayName("Malformed provenance metadata safely falls back to SOURCE_CACHE with null provenance fields")
    void malformedProvenance_fallsBackToSourceCache() {
        FeedSourceView hnSource = createSource("HN Bad Meta", FeedSourceType.HACKER_NEWS, true);
        Instant published = FIXED_NOW.minus(Duration.ofHours(2));

        // Negative story ID
        Map<String, Object> badIdMeta = Map.of(
                "hnStoryId", -10L,
                "hnDiscussionUrl", "https://news.ycombinator.com/item?id=-10",
                "hnRank", 1,
                "hnCapturedAt", "2026-11-02T10:00:00Z"
        );
        insertFeedItem(hnSource.id(), "Negative Story ID", "https://example.com/neg-id", published, badIdMeta);

        // Negative / out of bound rank
        Map<String, Object> badRankMeta = Map.of(
                "hnStoryId", 100L,
                "hnDiscussionUrl", "https://news.ycombinator.com/item?id=100",
                "hnRank", -4,
                "hnCapturedAt", "2026-11-02T10:00:00Z"
        );
        insertFeedItem(hnSource.id(), "Negative Rank", "https://example.com/neg-rank", published, badRankMeta);

        // Mismatched discussion URL
        Map<String, Object> mismatchedUrlMeta = Map.of(
                "hnStoryId", 200L,
                "hnDiscussionUrl", "https://news.ycombinator.com/item?id=999",
                "hnRank", 2,
                "hnCapturedAt", "2026-11-02T10:00:00Z"
        );
        insertFeedItem(hnSource.id(), "Mismatched Discussion URL", "https://example.com/mismatch-url", published, mismatchedUrlMeta);

        // Unparseable timestamp
        Map<String, Object> badTimestampMeta = Map.of(
                "hnStoryId", 300L,
                "hnDiscussionUrl", "https://news.ycombinator.com/item?id=300",
                "hnRank", 3,
                "hnCapturedAt", "not-a-valid-instant"
        );
        insertFeedItem(hnSource.id(), "Bad Timestamp", "https://example.com/bad-time", published, badTimestampMeta);

        FeedRadarPageView page = feedItemOperations.getRadar(null, 50);
        assertThat(page.items()).hasSize(4);

        for (FeedRadarItemView item : page.items()) {
            assertThat(item.signalType()).isEqualTo("SOURCE_CACHE");
            assertThat(item.sourceRank()).isNull();
            assertThat(item.hnStoryId()).isNull();
            assertThat(item.hnDiscussionUrl()).isNull();
            assertThat(item.signalCapturedAt()).isNull();
        }
    }

    @Test
    @DisplayName("FR-04: Near-integer decimal, floating point, and overflow provenance safely fall back to SOURCE_CACHE")
    void nearIntegerAndFloatingPointProvenance_fallsBackToSourceCache() {
        FeedSourceView hnSource = createSource("HN Numeric Precision", FeedSourceType.HACKER_NEWS, true);
        Instant published = FIXED_NOW.minus(Duration.ofHours(2));

        // 1. Near-integer decimal story ID: BigDecimal("1.0000000000000000001")
        Map<String, Object> nearIntegerIdMeta = Map.of(
                "hnStoryId", new java.math.BigDecimal("1.0000000000000000001"),
                "hnDiscussionUrl", "https://news.ycombinator.com/item?id=1",
                "hnRank", 1,
                "hnCapturedAt", "2026-11-02T10:00:00Z"
        );
        insertFeedItem(hnSource.id(), "Near-Integer Story ID", "https://example.com/near-int-id", published, nearIntegerIdMeta);

        // 2. Near-integer decimal rank: BigDecimal("1.0000000000000000001")
        Map<String, Object> nearIntegerRankMeta = Map.of(
                "hnStoryId", 100L,
                "hnDiscussionUrl", "https://news.ycombinator.com/item?id=100",
                "hnRank", new java.math.BigDecimal("1.0000000000000000001"),
                "hnCapturedAt", "2026-11-02T10:00:00Z"
        );
        insertFeedItem(hnSource.id(), "Near-Integer Rank", "https://example.com/near-int-rank", published, nearIntegerRankMeta);

        // 3. Double floating point value: Double(1.0)
        Map<String, Object> doubleIdMeta = Map.of(
                "hnStoryId", 1.0d,
                "hnDiscussionUrl", "https://news.ycombinator.com/item?id=1",
                "hnRank", 1,
                "hnCapturedAt", "2026-11-02T10:00:00Z"
        );
        insertFeedItem(hnSource.id(), "Double Story ID", "https://example.com/double-id", published, doubleIdMeta);

        // 4. Overflow rank: 51
        Map<String, Object> rankOverflowMeta = Map.of(
                "hnStoryId", 100L,
                "hnDiscussionUrl", "https://news.ycombinator.com/item?id=100",
                "hnRank", 51,
                "hnCapturedAt", "2026-11-02T10:00:00Z"
        );
        insertFeedItem(hnSource.id(), "Rank Overflow 51", "https://example.com/rank-51", published, rankOverflowMeta);

        // 5. Zero rank: 0
        Map<String, Object> rankZeroMeta = Map.of(
                "hnStoryId", 100L,
                "hnDiscussionUrl", "https://news.ycombinator.com/item?id=100",
                "hnRank", 0,
                "hnCapturedAt", "2026-11-02T10:00:00Z"
        );
        insertFeedItem(hnSource.id(), "Rank Zero", "https://example.com/rank-0", published, rankZeroMeta);

        FeedRadarPageView page = feedItemOperations.getRadar(null, 50);
        assertThat(page.items()).hasSize(5);

        for (FeedRadarItemView item : page.items()) {
            assertThat(item.signalType()).isEqualTo("SOURCE_CACHE");
            assertThat(item.sourceRank()).isNull();
            assertThat(item.hnStoryId()).isNull();
            assertThat(item.hnDiscussionUrl()).isNull();
            assertThat(item.signalCapturedAt()).isNull();
        }
    }
}
