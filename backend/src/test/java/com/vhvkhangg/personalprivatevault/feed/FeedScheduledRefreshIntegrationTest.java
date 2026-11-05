package com.vhvkhangg.personalprivatevault.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.application.FeedScheduledRefreshService;
import com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshAdmission;
import com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshService;
import com.vhvkhangg.personalprivatevault.feed.internal.application.RssFeedRefreshService;
import com.vhvkhangg.personalprivatevault.feed.internal.application.ScheduledRefreshOutcome;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsClient;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedItemRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssFeedClient;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class FeedScheduledRefreshIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FeedSourceOperations feedSourceOperations;

    @Autowired
    private FeedSourceRepository feedSourceRepository;

    @Autowired
    private FeedItemRepository feedItemRepository;

    @Autowired
    private FeedScheduledRefreshService scheduledRefreshService;

    @Autowired
    private HackerNewsRefreshAdmission admission;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private HackerNewsRefreshService hackerNewsRefreshService;

    @MockitoSpyBean
    private RssFeedRefreshService rssFeedRefreshService;

    @MockitoBean
    private HackerNewsClient hackerNewsClient;

    @MockitoBean
    private RssFeedClient rssFeedClient;

    @MockitoBean
    private Clock clock;

    private static final Instant BASE_NOW = Instant.parse("2026-11-05T12:00:00Z");
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final byte[] VALID_RSS_BYTES = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <title>Test Feed</title>
                <link>https://example.com</link>
                <item>
                  <title>Article 1</title>
                  <link>https://example.com/article-1</link>
                </item>
              </channel>
            </rss>
            """.getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(BASE_NOW);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        tearDown();
        scheduledRefreshService.resetCursor();
        scheduledRefreshService.resetCooldown();
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
        scheduledRefreshService.resetCursor();
        scheduledRefreshService.resetCooldown();
    }

    private FeedSourceView createSource(String name, FeedSourceType type, boolean enabled, boolean scheduled, Integer interval, String feedUrl) {
        return feedSourceOperations.createSource(new CreateFeedSourceCommand(
                name,
                type,
                type == FeedSourceType.HACKER_NEWS ? "https://news.ycombinator.com" : "https://example.com",
                feedUrl != null ? feedUrl : (type == FeedSourceType.RSS ? "https://example.com/rss.xml" : null),
                enabled,
                scheduled,
                interval,
                null
        ));
    }

    private void updateNextFetchAt(Long sourceId, Instant nextFetchAt) {
        jdbcTemplate.update("UPDATE feed_sources SET next_fetch_at = ? WHERE id = ?",
                nextFetchAt != null ? java.sql.Timestamp.from(nextFetchAt) : null,
                sourceId);
    }

    private ObjectNode createStoryJson(long id, String title, String url) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", id);
        node.put("type", "story");
        node.put("title", title);
        node.put("url", url);
        node.put("by", "author");
        node.put("time", 1700000000L);
        node.put("score", 100);
        node.put("descendants", 5);
        return node;
    }

    @Test
    @DisplayName("Stable ID query: findScheduledDueSourcesFromStart and findScheduledDueSourcesAfterCursor order by id ASC and filter before limiting")
    void stableIdQueries_orderByIdAsc_andFilterBeforeLimiting() {
        // Create 25 sources, alternating between disabled/enabled/unscheduled/unsupported
        List<Long> expectedEligibleIds = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            boolean enabled = (i % 3 != 0); // i=3,6,9,... disabled
            boolean scheduled = (i % 2 != 0); // i=2,4,6,... unscheduled
            FeedSourceType type = (i % 5 == 0) ? FeedSourceType.GITHUB_TRENDING : FeedSourceType.HACKER_NEWS; // i=5,10,15,... unsupported

            FeedSourceView s = createSource("Source " + i, type, enabled, scheduled, 60, null);
            updateNextFetchAt(s.id(), BASE_NOW.minusSeconds(10)); // due

            if (enabled && scheduled && type == FeedSourceType.HACKER_NEWS) {
                expectedEligibleIds.add(s.id());
            }
        }

        Collections.sort(expectedEligibleIds);
        assertThat(expectedEligibleIds.size()).isGreaterThan(0);

        Set<FeedSourceType> supported = Set.of(FeedSourceType.HACKER_NEWS, FeedSourceType.RSS);

        // Page 1 (limit 20) from start
        List<FeedSource> page1 = feedSourceRepository.findScheduledDueSourcesFromStart(supported, BASE_NOW, PageRequest.of(0, 20));
        assertThat(page1).isNotEmpty();

        // Verify ordering is strictly ascending by ID
        for (int i = 0; i < page1.size() - 1; i++) {
            assertThat(page1.get(i).getId()).isLessThan(page1.get(i + 1).getId());
        }

        // Verify all returned sources match predicates
        for (FeedSource s : page1) {
            assertThat(s.isEnabled()).isTrue();
            assertThat(s.isScheduledRefreshEnabled()).isTrue();
            assertThat(supported).contains(s.getType());
            assertThat(s.getNextFetchAt()).isBeforeOrEqualTo(BASE_NOW);
        }

        // After cursor query
        Long cursor = page1.get(0).getId();
        List<FeedSource> afterCursor = feedSourceRepository.findScheduledDueSourcesAfterCursor(supported, BASE_NOW, cursor, PageRequest.of(0, 20));
        for (FeedSource s : afterCursor) {
            assertThat(s.getId()).isGreaterThan(cursor);
        }
    }

    @Test
    @DisplayName("Cutoff boundaries: NULL and exact cutoff are included; one millisecond future is excluded")
    void cutoffBoundaries_nullAndExactCutoffIncluded_futureExcluded() {
        FeedSourceView sNull = createSource("Null Due", FeedSourceType.HACKER_NEWS, true, true, 60, null);
        updateNextFetchAt(sNull.id(), null);

        FeedSourceView sExact = createSource("Exact Due", FeedSourceType.HACKER_NEWS, true, true, 60, null);
        updateNextFetchAt(sExact.id(), BASE_NOW);

        FeedSourceView sPast = createSource("Past Due", FeedSourceType.HACKER_NEWS, true, true, 60, null);
        updateNextFetchAt(sPast.id(), BASE_NOW.minusSeconds(1));

        FeedSourceView sFuture = createSource("Future Due", FeedSourceType.HACKER_NEWS, true, true, 60, null);
        updateNextFetchAt(sFuture.id(), BASE_NOW.plusMillis(1));

        Set<FeedSourceType> supported = Set.of(FeedSourceType.HACKER_NEWS, FeedSourceType.RSS);
        List<FeedSource> due = feedSourceRepository.findScheduledDueSourcesFromStart(supported, BASE_NOW, PageRequest.of(0, 20));

        List<Long> dueIds = due.stream().map(FeedSource::getId).toList();
        assertThat(dueIds).contains(sNull.id(), sExact.id(), sPast.id());
        assertThat(dueIds).doesNotContain(sFuture.id());
    }

    @Test
    @DisplayName("PH-01 Trace 1: 30 alternating HN/RSS sources all invoked by tick 15, empty tick 16 wraps to tick 17")
    void ph01Trace1_thirtySourcesAllInvokedByTick15() {
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(100L));
        when(hackerNewsClient.fetchItem(eq(100L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(100L, "HN Item", "https://example.com/hn-trace1"));
        when(rssFeedClient.fetchFeed(any(), anyLong())).thenReturn(VALID_RSS_BYTES);

        List<Long> createdIds = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            FeedSourceType type = (i % 2 == 1) ? FeedSourceType.HACKER_NEWS : FeedSourceType.RSS;
            String url = (type == FeedSourceType.RSS) ? ("https://example.com/feed-" + i + ".xml") : null;
            FeedSourceView s = createSource("Source " + i, type, true, true, 1, url);
            createdIds.add(s.id());
            if (i == 1) {
                updateNextFetchAt(s.id(), BASE_NOW.minusSeconds(1));
            } else {
                updateNextFetchAt(s.id(), null);
            }
        }

        Collections.sort(createdIds);
        assertThat(createdIds).hasSize(30);

        List<Long> actualInvokedIds = new ArrayList<>();
        doAnswer(inv -> {
            actualInvokedIds.add(inv.getArgument(0));
            return inv.callRealMethod();
        }).when(hackerNewsRefreshService).refreshScheduled(anyLong());

        doAnswer(inv -> {
            actualInvokedIds.add(inv.getArgument(0));
            return inv.callRealMethod();
        }).when(rssFeedRefreshService).refreshScheduled(anyLong());

        // Ticks 1 to 15: clock advances by 60s per tick (t=0, 60, 120, ...)
        for (int tick = 1; tick <= 15; tick++) {
            Instant currentTickTime = BASE_NOW.plus(Duration.ofSeconds(60L * (tick - 1)));
            when(clock.instant()).thenReturn(currentTickTime);
            scheduledRefreshService.executeTick();

            Long expectedLastExamined = createdIds.get(tick * 2 - 1);
            assertThat(scheduledRefreshService.getLastExaminedSourceId()).isEqualTo(expectedLastExamined);

            Long idA = createdIds.get((tick - 1) * 2);
            Long idB = createdIds.get((tick - 1) * 2 + 1);

            // Verify nextFetchAt moved forward in DB for both
            FeedSource sA = feedSourceRepository.findById(idA).orElseThrow();
            FeedSource sB = feedSourceRepository.findById(idB).orElseThrow();
            assertThat(sA.getLastFetchedAt()).isNotNull();
            assertThat(sB.getLastFetchedAt()).isNotNull();
            assertThat(sA.getNextFetchAt()).isEqualTo(currentTickTime.plus(Duration.ofMinutes(1)));
            assertThat(sB.getNextFetchAt()).isEqualTo(currentTickTime.plus(Duration.ofMinutes(1)));
        }

        // All 30 sources observed invoked by tick 15 in exact stable ID order
        assertThat(actualInvokedIds).containsExactlyElementsOf(createdIds);

        // Tick 16: No rows with id > createdIds.get(29) -> empty page, resets cursor to null, 0 invocations
        actualInvokedIds.clear();
        scheduledRefreshService.executeTick();
        assertThat(scheduledRefreshService.getLastExaminedSourceId()).isNull();
        assertThat(actualInvokedIds).isEmpty();

        // Tick 17: Advance clock so sources 1 and 2 are due again (t = 16 * 60s = 960s > 60s due time)
        Instant tick17Time = BASE_NOW.plus(Duration.ofSeconds(60L * 16));
        when(clock.instant()).thenReturn(tick17Time);

        scheduledRefreshService.executeTick();
        assertThat(actualInvokedIds).containsExactly(createdIds.get(0), createdIds.get(1));
        assertThat(scheduledRefreshService.getLastExaminedSourceId()).isEqualTo(createdIds.get(1));
    }

    @Test
    @DisplayName("PH-01 Trace 2: 34 rows with 4 unsupported, 5 permanent failures, 2 busy; all 30 supported invoked by tick 15")
    void ph01Trace2_unsupportedFilteredAndFailuresCooldownWithoutAlteringDbTimes() {
        when(rssFeedClient.fetchFeed(any(), anyLong())).thenReturn(VALID_RSS_BYTES);
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(200L));
        when(hackerNewsClient.fetchItem(eq(200L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(200L, "HN Item", "https://example.com/hn-trace2"));

        List<Long> allCreatedIds = new ArrayList<>();
        List<Long> supportedIds = new ArrayList<>();
        Set<Long> unsupportedIds = new HashSet<>();

        // Create 34 sources with unsupported at indices 4, 12, 19, 27 (1-indexed)
        Set<Integer> unsupportedIndices = Set.of(4, 12, 19, 27);

        for (int i = 1; i <= 34; i++) {
            FeedSourceType type;
            if (unsupportedIndices.contains(i)) {
                type = FeedSourceType.GITHUB_TRENDING;
            } else {
                type = (i % 2 == 1) ? FeedSourceType.HACKER_NEWS : FeedSourceType.RSS;
            }

            String url = (type == FeedSourceType.RSS) ? ("https://example.com/feed-" + i + ".xml") : null;
            FeedSourceView s = createSource("Source " + i, type, true, true, 60, url);
            allCreatedIds.add(s.id());
            jdbcTemplate.update("UPDATE feed_sources SET next_fetch_at = NULL WHERE id = ?", s.id());

            if (unsupportedIndices.contains(i)) {
                unsupportedIds.add(s.id());
            } else {
                supportedIds.add(s.id());
            }
        }

        Collections.sort(allCreatedIds);
        Collections.sort(supportedIds);

        assertThat(supportedIds).hasSize(30);
        assertThat(unsupportedIds).hasSize(4);

        // Failures at first 5 supported IDs
        Set<Long> failingSupportedIds = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            failingSupportedIds.add(supportedIds.get(i));
        }

        // Busy at next 2 supported IDs (supported 5 and 6)
        Long busy1Id = supportedIds.get(5);
        Long busy2Id = supportedIds.get(6);

        record InvocationRecord(Long sourceId, FeedSourceType type, ScheduledRefreshOutcome outcome) {}
        List<InvocationRecord> actualInvocations = new ArrayList<>();

        doAnswer(inv -> {
            Long id = inv.getArgument(0);
            ScheduledRefreshOutcome outcome = (ScheduledRefreshOutcome) inv.callRealMethod();
            actualInvocations.add(new InvocationRecord(id, FeedSourceType.HACKER_NEWS, outcome));
            return outcome;
        }).when(hackerNewsRefreshService).refreshScheduled(anyLong());

        doAnswer(inv -> {
            Long id = inv.getArgument(0);
            ScheduledRefreshOutcome outcome = (ScheduledRefreshOutcome) inv.callRealMethod();
            actualInvocations.add(new InvocationRecord(id, FeedSourceType.RSS, outcome));
            return outcome;
        }).when(rssFeedRefreshService).refreshScheduled(anyLong());

        // Configure client mocks to fail specifically for designated failing IDs
        when(rssFeedClient.fetchFeed(any(), anyLong())).thenAnswer(inv -> {
            String feedUrl = inv.getArgument(0);
            for (Long id : failingSupportedIds) {
                FeedSource s = feedSourceRepository.findById(id).orElse(null);
                if (s != null && feedUrl.equals(s.getFeedUrl())) {
                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Simulated network failure");
                }
            }
            return VALID_RSS_BYTES;
        });

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenAnswer(inv -> {
            Long currentSourceId = scheduledRefreshService.getLastExaminedSourceId();
            if (currentSourceId != null && failingSupportedIds.contains(currentSourceId)) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Simulated HN failure");
            }
            return List.of(200L);
        });

        // Run ticks 1 to 15 with clock advancing 60s per tick
        for (int tick = 1; tick <= 15; tick++) {
            Instant currentTickTime = BASE_NOW.plus(Duration.ofSeconds(60L * (tick - 1)));
            when(clock.instant()).thenReturn(currentTickTime);

            if (tick == 3) {
                admission.acquire(busy1Id);
            } else if (tick == 4) {
                admission.acquire(busy2Id);
            }

            scheduledRefreshService.executeTick();

            if (tick == 4) {
                // At tick 4 (t = 180s), all 5 failing IDs and 2 busy IDs are in cooldown (< 300s cooldown duration)
                Map<Long, Instant> cooldownSnapshot = scheduledRefreshService.getCooldownMap();
                assertThat(cooldownSnapshot).containsKeys(failingSupportedIds.toArray(Long[]::new));
                assertThat(cooldownSnapshot).containsKeys(busy1Id, busy2Id);
            }

            if (tick == 3) {
                admission.release(busy1Id);
            } else if (tick == 4) {
                admission.release(busy2Id);
            }
        }

        // Verify all 30 supported IDs were actually invoked, exactly matching supportedIds order
        List<Long> invokedIds = actualInvocations.stream().map(InvocationRecord::sourceId).toList();
        assertThat(invokedIds).containsExactlyElementsOf(supportedIds);

        // Verify first pairs from contract table:
        // Tick 1: (1, 2)
        assertThat(actualInvocations.get(0).sourceId()).isEqualTo(allCreatedIds.get(0));
        assertThat(actualInvocations.get(1).sourceId()).isEqualTo(allCreatedIds.get(1));
        // Tick 2: (3, 5) -> row 4 was unsupported!
        assertThat(actualInvocations.get(2).sourceId()).isEqualTo(allCreatedIds.get(2));
        assertThat(actualInvocations.get(3).sourceId()).isEqualTo(allCreatedIds.get(4));
        // Tick 3: (6, 7)
        assertThat(actualInvocations.get(4).sourceId()).isEqualTo(allCreatedIds.get(5));
        assertThat(actualInvocations.get(5).sourceId()).isEqualTo(allCreatedIds.get(6));
        // Tick 4: (8, 9)
        assertThat(actualInvocations.get(6).sourceId()).isEqualTo(allCreatedIds.get(7));
        assertThat(actualInvocations.get(7).sourceId()).isEqualTo(allCreatedIds.get(8));
        // Tick 15: (33, 34)
        assertThat(actualInvocations.get(28).sourceId()).isEqualTo(allCreatedIds.get(32));
        assertThat(actualInvocations.get(29).sourceId()).isEqualTo(allCreatedIds.get(33));

        // Verify outcomes: 5 FAILED, 2 BUSY, 23 REFRESHED
        for (int i = 0; i < 5; i++) {
            assertThat(actualInvocations.get(i).outcome()).isEqualTo(ScheduledRefreshOutcome.FAILED);
        }
        assertThat(actualInvocations.get(5).outcome()).isEqualTo(ScheduledRefreshOutcome.BUSY);
        assertThat(actualInvocations.get(6).outcome()).isEqualTo(ScheduledRefreshOutcome.BUSY);
        for (int i = 7; i < 30; i++) {
            assertThat(actualInvocations.get(i).outcome()).isEqualTo(ScheduledRefreshOutcome.REFRESHED);
        }

        // Verify none of the unsupported IDs were ever invoked or updated
        assertThat(invokedIds).doesNotContainAnyElementsOf(unsupportedIds);
        for (Long unsupportedId : unsupportedIds) {
            FeedSource s = feedSourceRepository.findById(unsupportedId).orElseThrow();
            assertThat(s.getLastFetchedAt()).isNull();
        }

        // Verify failing and busy sources never had their fetch timestamps modified
        for (Long failId : failingSupportedIds) {
            FeedSource s = feedSourceRepository.findById(failId).orElseThrow();
            assertThat(s.getLastFetchedAt()).isNull();
            assertThat(s.getNextFetchAt()).isNull();
        }
        FeedSource busy1Source = feedSourceRepository.findById(busy1Id).orElseThrow();
        assertThat(busy1Source.getLastFetchedAt()).isNull();
        FeedSource busy2Source = feedSourceRepository.findById(busy2Id).orElseThrow();
        assertThat(busy2Source.getLastFetchedAt()).isNull();

        // Verify by tick 15 (t = 840s, 14 min), all 5-minute cooldown entries have naturally expired and pruned
        Map<Long, Instant> cooldownMap = scheduledRefreshService.getCooldownMap();
        assertThat(cooldownMap).isEmpty();

            // Verify succeeding sources had timestamps advanced
            for (int i = 7; i < 30; i++) {
                Long successId = actualInvocations.get(i).sourceId();
                FeedSource s = feedSourceRepository.findById(successId).orElseThrow();
                assertThat(s.getLastFetchedAt()).isNotNull();
                assertThat(s.getNextFetchAt()).isNotNull();
        }
    }

    @Test
    @DisplayName("Trace 3: 20 cooling rows advance cursor without invocations; next tick invokes 21 and 22 and breaks at cursor 22 (not page end)")
    void trace3_coolingRowsAdvanceCursorAndNextTickBreaksAtSecondInvocation() {
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(300L));
        when(hackerNewsClient.fetchItem(eq(300L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(300L, "HN Item", "https://example.com/hn-trace3"));

        List<Long> ids = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            FeedSourceView s = createSource("Source " + i, FeedSourceType.HACKER_NEWS, true, true, 60, null);
            ids.add(s.id());
            updateNextFetchAt(s.id(), BASE_NOW.minusSeconds(10));
        }

        Collections.sort(ids);
        assertThat(ids).hasSize(25);

        // Put the first 20 IDs into cooldown until BASE_NOW + 300s
        Map<Long, Instant> map = (Map<Long, Instant>) ReflectionTestUtils.getField(scheduledRefreshService, "cooldownMap");
        assertThat(map).isNotNull();
        for (int i = 0; i < 20; i++) {
            map.put(ids.get(i), BASE_NOW.plus(Duration.ofSeconds(300)));
        }

        // Tick 1 at BASE_NOW:
        // Examines the 20 due rows. All 20 are in cooldown -> 0 invocations.
        // Cursor moves to ID 20!
        scheduledRefreshService.executeTick();
        assertThat(scheduledRefreshService.getLastExaminedSourceId()).isEqualTo(ids.get(19));

        // Tick 2 at BASE_NOW + 60s:
        // Query after cursor 20 fetches remaining rows 21, 22, 23, 24, 25.
        // Candidate 21: not in cooldown -> invocation 1
        // Candidate 22: not in cooldown -> invocation 2 -> reaches MAX_INVOCATIONS_PER_TICK (2)!
        // Loop breaks immediately! Cursor is set to ID 22, NOT page end (ID 25)!
        when(clock.instant()).thenReturn(BASE_NOW.plus(Duration.ofSeconds(60)));
        scheduledRefreshService.executeTick();

        assertThat(scheduledRefreshService.getLastExaminedSourceId()).isEqualTo(ids.get(21));
    }
}
