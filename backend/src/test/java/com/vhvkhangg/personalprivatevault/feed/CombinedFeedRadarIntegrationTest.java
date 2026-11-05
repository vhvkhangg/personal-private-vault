package com.vhvkhangg.personalprivatevault.feed;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedItem;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsClient;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedItemRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssFeedClient;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.command.UpdateFeedSourceCommand;
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

class CombinedFeedRadarIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-11-04T12:00:00Z");

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
    private RssFeedClient rssFeedClient;

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

    private FeedSourceView createSource(String name, FeedSourceType type, String feedUrl, boolean enabled) {
        return feedSourceOperations.createSource(new CreateFeedSourceCommand(
                name,
                type,
                type == FeedSourceType.HACKER_NEWS ? "https://news.ycombinator.com" : null,
                feedUrl,
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
    @DisplayName("Combined Radar: merges items from enabled HN and RSS sources in rolling 24h window")
    void combinedRadar_mergesHnAndRssIn24HourWindow() {
        FeedSourceView hnSource = createSource("HN Source", FeedSourceType.HACKER_NEWS, null, true);
        FeedSourceView rssSource = createSource("RSS Source", FeedSourceType.RSS, "https://example.com/rss.xml", true);

        Instant t1 = FIXED_NOW.minus(Duration.ofHours(3));
        Instant t2 = FIXED_NOW.minus(Duration.ofHours(1));

        Map<String, Object> hnMetadata = Map.of("score", 150, "descendants", 42);
        Map<String, Object> rssMetadata = Map.of(
                "feedFormat", "RSS_2_0",
                "rssCapturedAt", FIXED_NOW.toString(),
                "feedEntryId", "guid-123"
        );

        FeedItem hnItem = insertFeedItem(hnSource.id(), "HN Trending Story", "https://news.ycombinator.com/item?id=123", t1, hnMetadata);
        FeedItem rssItem = insertFeedItem(rssSource.id(), "Tech Blog Post", "https://techblog.com/post", t2, rssMetadata);

        FeedRadarPageView combined = feedItemOperations.getCombinedRadar(50);

        assertThat(combined.items()).hasSize(2);
        // t2 (1h ago) comes before t1 (3h ago)
        assertThat(combined.items().get(0).title()).isEqualTo("Tech Blog Post");
        assertThat(combined.items().get(0).sourceType()).isEqualTo(FeedSourceType.RSS);
        assertThat(combined.items().get(0).signalType()).isEqualTo("RSS_SOURCE_CACHE");
        assertThat(combined.items().get(0).hnStoryId()).isNull();

        assertThat(combined.items().get(1).title()).isEqualTo("HN Trending Story");
        assertThat(combined.items().get(1).sourceType()).isEqualTo(FeedSourceType.HACKER_NEWS);
        assertThat(combined.items().get(1).signalType()).isEqualTo("SOURCE_CACHE");

        verifyNoInteractions(rssFeedClient, hackerNewsClient);
    }

    @Test
    @DisplayName("Combined Radar: excludes items from disabled sources")
    void combinedRadar_excludesDisabledSources() {
        FeedSourceView enabledRss = createSource("Enabled RSS", FeedSourceType.RSS, "https://example.com/rss1.xml", true);
        FeedSourceView disabledRss = createSource("Disabled RSS", FeedSourceType.RSS, "https://example.com/rss2.xml", true);
        FeedSourceView disabledHn = createSource("Disabled HN", FeedSourceType.HACKER_NEWS, null, true);

        Instant published = FIXED_NOW.minus(Duration.ofHours(2));

        insertFeedItem(enabledRss.id(), "Enabled RSS Post", "https://example.com/post-enabled", published, null);
        insertFeedItem(disabledRss.id(), "Disabled RSS Post", "https://example.com/post-disabled", published, null);
        insertFeedItem(disabledHn.id(), "Disabled HN Post", "https://example.com/hn-disabled", published, null);

        // Disable sources
        feedSourceOperations.updateSource(disabledRss.id(), new UpdateFeedSourceCommand(
                disabledRss.name(), FeedSourceType.RSS, null, disabledRss.feedUrl(), false, false, null, null
        ));
        feedSourceOperations.updateSource(disabledHn.id(), new UpdateFeedSourceCommand(
                disabledHn.name(), FeedSourceType.HACKER_NEWS, disabledHn.sourceUrl(), null, false, false, null, null
        ));

        FeedRadarPageView combined = feedItemOperations.getCombinedRadar(50);

        assertThat(combined.items()).hasSize(1);
        assertThat(combined.items().get(0).title()).isEqualTo("Enabled RSS Post");
    }

    @Test
    @DisplayName("Combined Radar: rolling 24h window excludes older, future, and null publishedAt items")
    void combinedRadar_windowExclusions() {
        FeedSourceView rssSource = createSource("RSS Window", FeedSourceType.RSS, "https://example.com/window.xml", true);

        Instant windowStart = FIXED_NOW.minus(Duration.ofHours(24));

        insertFeedItem(rssSource.id(), "At Window Start", "https://example.com/start", windowStart, null);
        insertFeedItem(rssSource.id(), "Before Window Start", "https://example.com/before", windowStart.minusSeconds(1), null);
        insertFeedItem(rssSource.id(), "At Window End", "https://example.com/end", FIXED_NOW, null);
        insertFeedItem(rssSource.id(), "Future Item", "https://example.com/future", FIXED_NOW.plusSeconds(1), null);
        insertFeedItem(rssSource.id(), "Null Published", "https://example.com/null", null, null);

        FeedRadarPageView combined = feedItemOperations.getCombinedRadar(50);

        List<String> titles = combined.items().stream().map(FeedRadarItemView::title).toList();
        assertThat(titles).containsExactly("At Window End", "At Window Start");
        assertThat(titles).doesNotContain("Before Window Start", "Future Item", "Null Published");
    }

    @Test
    @DisplayName("Combined Radar: ties broken deterministically by publishedAt DESC, id DESC")
    void combinedRadar_tiesBrokenByIdDesc() {
        FeedSourceView rssSource = createSource("RSS Ties", FeedSourceType.RSS, "https://example.com/ties.xml", true);
        Instant tieTime = FIXED_NOW.minus(Duration.ofHours(1));

        FeedItem item1 = insertFeedItem(rssSource.id(), "Tie 1", "https://example.com/t1", tieTime, null);
        FeedItem item2 = insertFeedItem(rssSource.id(), "Tie 2", "https://example.com/t2", tieTime, null);

        assertThat(item2.getId()).isGreaterThan(item1.getId());

        FeedRadarPageView combined = feedItemOperations.getCombinedRadar(50);

        List<Long> ids = combined.items().stream().map(FeedRadarItemView::feedItemId).toList();
        assertThat(ids).containsExactly(item2.getId(), item1.getId());
    }

    @Test
    @DisplayName("Combined Radar: limit parameter restricts results and rejects invalid limits")
    void combinedRadar_limitRestrictsResults() {
        FeedSourceView rssSource = createSource("RSS Multi", FeedSourceType.RSS, "https://example.com/multi.xml", true);

        for (int i = 0; i < 5; i++) {
            insertFeedItem(rssSource.id(), "Item " + i, "https://example.com/item/" + i, FIXED_NOW.minus(Duration.ofMinutes(10 * (5 - i))), null);
        }

        FeedRadarPageView limited = feedItemOperations.getCombinedRadar(3);
        assertThat(limited.items()).hasSize(3);

        // Limit validation
        assertThatThrownBy(() -> feedItemOperations.getCombinedRadar(0))
                .isInstanceOf(com.vhvkhangg.personalprivatevault.feed.item.exception.InvalidFeedItemException.class);
        assertThatThrownBy(() -> feedItemOperations.getCombinedRadar(101))
                .isInstanceOf(com.vhvkhangg.personalprivatevault.feed.item.exception.InvalidFeedItemException.class);
    }
}
