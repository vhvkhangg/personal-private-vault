package com.vhvkhangg.personalprivatevault.feed;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.application.FeedScheduledRefreshService;
import com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshService;
import com.vhvkhangg.personalprivatevault.feed.internal.application.RssFeedRefreshService;
import com.vhvkhangg.personalprivatevault.feed.internal.application.ScheduledRefreshOutcome;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedScheduledRefreshTest {

    @Mock
    private FeedSourceRepository feedSourceRepository;

    @Mock
    private HackerNewsRefreshService hackerNewsRefreshService;

    @Mock
    private RssFeedRefreshService rssFeedRefreshService;

    private Instant fixedNow;
    private Clock clock;
    private FeedScheduledRefreshService refreshService;

    private static final Set<FeedSourceType> SUPPORTED_TYPES = Set.of(
            FeedSourceType.HACKER_NEWS,
            FeedSourceType.RSS
    );

    @BeforeEach
    void setUp() {
        fixedNow = Instant.parse("2026-11-05T12:00:00Z");
        clock = Clock.fixed(fixedNow, ZoneOffset.UTC);
        refreshService = new FeedScheduledRefreshService(
                feedSourceRepository,
                hackerNewsRefreshService,
                rssFeedRefreshService,
                clock
        );
    }

    private FeedSource createSource(Long id, FeedSourceType type) {
        FeedSource source = new FeedSource("Source " + id, type, "https://example.com", "https://example.com/rss", true, true, 60, Map.of());
        ReflectionTestUtils.setField(source, "id", id);
        return source;
    }

    @Test
    @DisplayName("Empty page resets cursor to null and dispatches zero provider invocations")
    void executeTick_emptyPage_resetsCursorAndDoesNotInvokeProviders() {
        ReflectionTestUtils.setField(refreshService, "lastExaminedSourceId", 42L);

        when(feedSourceRepository.findScheduledDueSourcesAfterCursor(eq(SUPPORTED_TYPES), eq(fixedNow), eq(42L), any(Pageable.class)))
                .thenReturn(List.of());

        refreshService.executeTick();

        assertThat(refreshService.getLastExaminedSourceId()).isNull();
        verify(hackerNewsRefreshService, never()).refreshScheduled(any());
        verify(rssFeedRefreshService, never()).refreshScheduled(any());
    }

    @Test
    @DisplayName("Candidates below limits invoke providers and update cursor to last examined ID")
    void executeTick_candidatesBelowLimits_invokesAndSetsCursorToLastExamined() {
        FeedSource s1 = createSource(10L, FeedSourceType.HACKER_NEWS);
        FeedSource s2 = createSource(20L, FeedSourceType.RSS);

        when(feedSourceRepository.findScheduledDueSourcesFromStart(eq(SUPPORTED_TYPES), eq(fixedNow), any(Pageable.class)))
                .thenReturn(List.of(s1, s2));
        when(hackerNewsRefreshService.refreshScheduled(10L)).thenReturn(ScheduledRefreshOutcome.REFRESHED);
        when(rssFeedRefreshService.refreshScheduled(20L)).thenReturn(ScheduledRefreshOutcome.REFRESHED);

        refreshService.executeTick();

        assertThat(refreshService.getLastExaminedSourceId()).isEqualTo(20L);
        verify(hackerNewsRefreshService).refreshScheduled(10L);
        verify(rssFeedRefreshService).refreshScheduled(20L);
    }

    @Test
    @DisplayName("More than two candidates cap at two invocations and break immediately with cursor at second examined row")
    void executeTick_moreThanTwoCandidates_capsAtTwoInvocationsAndBreaksImmediately() {
        List<FeedSource> list = new ArrayList<>();
        for (long i = 1; i <= 20; i++) {
            list.add(createSource(i, FeedSourceType.HACKER_NEWS));
        }

        when(feedSourceRepository.findScheduledDueSourcesFromStart(eq(SUPPORTED_TYPES), eq(fixedNow), any(Pageable.class)))
                .thenReturn(list);
        when(hackerNewsRefreshService.refreshScheduled(1L)).thenReturn(ScheduledRefreshOutcome.REFRESHED);
        when(hackerNewsRefreshService.refreshScheduled(2L)).thenReturn(ScheduledRefreshOutcome.REFRESHED);

        refreshService.executeTick();

        assertThat(refreshService.getLastExaminedSourceId()).isEqualTo(2L);
        verify(hackerNewsRefreshService).refreshScheduled(1L);
        verify(hackerNewsRefreshService).refreshScheduled(2L);
        verify(hackerNewsRefreshService, never()).refreshScheduled(3L);
    }

    @Test
    @DisplayName("Cooling rows skip provider invocation but advance cursor")
    void executeTick_coolingRows_skipDispatchButAdvanceCursor() {
        FeedSource s1 = createSource(1L, FeedSourceType.HACKER_NEWS);
        FeedSource s2 = createSource(2L, FeedSourceType.RSS);
        FeedSource s3 = createSource(3L, FeedSourceType.HACKER_NEWS);

        // Put s1 in cooldown until now + 2 minutes
        Map<Long, Instant> map = (Map<Long, Instant>) ReflectionTestUtils.getField(refreshService, "cooldownMap");
        assertThat(map).isNotNull();
        map.put(1L, fixedNow.plus(Duration.ofMinutes(2)));

        when(feedSourceRepository.findScheduledDueSourcesFromStart(eq(SUPPORTED_TYPES), eq(fixedNow), any(Pageable.class)))
                .thenReturn(List.of(s1, s2, s3));
        when(rssFeedRefreshService.refreshScheduled(2L)).thenReturn(ScheduledRefreshOutcome.REFRESHED);
        when(hackerNewsRefreshService.refreshScheduled(3L)).thenReturn(ScheduledRefreshOutcome.REFRESHED);

        refreshService.executeTick();

        // 1 was cooling (skipped), 2 was invoked (1), 3 was invoked (2) -> loop breaks at 3
        assertThat(refreshService.getLastExaminedSourceId()).isEqualTo(3L);
        verify(hackerNewsRefreshService, never()).refreshScheduled(1L);
        verify(rssFeedRefreshService).refreshScheduled(2L);
        verify(hackerNewsRefreshService).refreshScheduled(3L);
    }

    @Test
    @DisplayName("Stopping flag prevents execution before candidate processing")
    void executeTick_stoppingFlag_breaksBeforeExaminingCandidates() {
        refreshService.stop();
        assertThat(refreshService.isStopping()).isTrue();

        refreshService.executeTick();

        verify(feedSourceRepository, never()).findScheduledDueSourcesFromStart(any(), any(), any());
        verify(feedSourceRepository, never()).findScheduledDueSourcesAfterCursor(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Thread interruption before tick exits immediately without query")
    void executeTick_threadInterrupted_breaksImmediately() {
        Thread.currentThread().interrupt();
        try {
            refreshService.executeTick();
            verify(feedSourceRepository, never()).findScheduledDueSourcesFromStart(any(), any(), any());
        } finally {
            Thread.interrupted(); // clear interrupted status
        }
    }

    @Test
    @DisplayName("BUSY and FAILED outcomes record 5-minute cooldown, while REFRESHED removes cooldown")
    void executeTick_outcomesUpdateCooldownMap() {
        FeedSource s1 = createSource(1L, FeedSourceType.HACKER_NEWS);
        FeedSource s2 = createSource(2L, FeedSourceType.RSS);

        when(feedSourceRepository.findScheduledDueSourcesFromStart(eq(SUPPORTED_TYPES), eq(fixedNow), any(Pageable.class)))
                .thenReturn(List.of(s1, s2));
        when(hackerNewsRefreshService.refreshScheduled(1L)).thenReturn(ScheduledRefreshOutcome.BUSY);
        when(rssFeedRefreshService.refreshScheduled(2L)).thenReturn(ScheduledRefreshOutcome.FAILED);

        refreshService.executeTick();

        Map<Long, Instant> cooldowns = refreshService.getCooldownMap();
        assertThat(cooldowns).containsEntry(1L, fixedNow.plus(Duration.ofMinutes(5)));
        assertThat(cooldowns).containsEntry(2L, fixedNow.plus(Duration.ofMinutes(5)));

        // Next tick: ID 1 succeeds -> cooldown removed
        when(feedSourceRepository.findScheduledDueSourcesAfterCursor(eq(SUPPORTED_TYPES), eq(fixedNow), eq(2L), any(Pageable.class)))
                .thenReturn(List.of(s1));
        // Force expiry for test
        mapCooldown(refreshService).remove(1L);
        when(hackerNewsRefreshService.refreshScheduled(1L)).thenReturn(ScheduledRefreshOutcome.REFRESHED);

        refreshService.executeTick();
        assertThat(refreshService.getCooldownMap()).doesNotContainKey(1L);
    }

    @Test
    @DisplayName("NOT_ELIGIBLE outcome counts towards invocation cap but records no cooldown")
    void executeTick_notEligibleOutcome_countsAsInvocationWithoutCooldown() {
        FeedSource s1 = createSource(1L, FeedSourceType.HACKER_NEWS);
        FeedSource s2 = createSource(2L, FeedSourceType.RSS);
        FeedSource s3 = createSource(3L, FeedSourceType.HACKER_NEWS);

        when(feedSourceRepository.findScheduledDueSourcesFromStart(eq(SUPPORTED_TYPES), eq(fixedNow), any(Pageable.class)))
                .thenReturn(List.of(s1, s2, s3));
        when(hackerNewsRefreshService.refreshScheduled(1L)).thenReturn(ScheduledRefreshOutcome.NOT_ELIGIBLE);
        when(rssFeedRefreshService.refreshScheduled(2L)).thenReturn(ScheduledRefreshOutcome.REFRESHED);

        refreshService.executeTick();

        // Invocations count reached 2 (s1 NOT_ELIGIBLE, s2 REFRESHED)
        assertThat(refreshService.getLastExaminedSourceId()).isEqualTo(2L);
        verify(hackerNewsRefreshService).refreshScheduled(1L);
        verify(rssFeedRefreshService).refreshScheduled(2L);
        verify(hackerNewsRefreshService, never()).refreshScheduled(3L);

        assertThat(refreshService.getCooldownMap()).doesNotContainKey(1L);
    }

    @Test
    @DisplayName("Cooldown eviction maintains 512 cap and evicts smallest retryNotBefore, tie smallest source ID")
    void cooldownEviction_at512Cap_evictsSmallestRetryNotBefore_tieSmallestId() {
        Map<Long, Instant> map = mapCooldown(refreshService);
        // Fill 512 entries with times in the future
        for (long i = 1; i <= 512; i++) {
            // Give ID 1 the smallest time
            // Give ID 2 the same smallest time
            Instant time = (i == 1 || i == 2)
                    ? fixedNow.plus(Duration.ofMinutes(1))
                    : fixedNow.plus(Duration.ofMinutes(2 + (int) i));
            map.put(i, time);
        }

        assertThat(map).hasSize(512);

        // Trigger an outcome on ID 999 that adds cooldown
        FeedSource s = createSource(999L, FeedSourceType.HACKER_NEWS);
        when(feedSourceRepository.findScheduledDueSourcesFromStart(eq(SUPPORTED_TYPES), eq(fixedNow), any(Pageable.class)))
                .thenReturn(List.of(s));
        when(hackerNewsRefreshService.refreshScheduled(999L)).thenReturn(ScheduledRefreshOutcome.FAILED);

        refreshService.executeTick();

        assertThat(map).hasSize(512);
        assertThat(map).containsKey(999L);
        // Between ID 1 and ID 2 (both smallest time), ID 1 had smaller ID -> ID 1 must be evicted!
        assertThat(map).doesNotContainKey(1L);
        assertThat(map).containsKey(2L);
    }

    @Test
    @DisplayName("Cooldown is measured from outcome completion instant, not tick start cutoff (FR-02)")
    void cooldown_measuredFromOutcomeCompletionInstant_notTickCutoff() {
        // Controlled advancing clock
        AtomicBoolean providerExecuted = new AtomicBoolean(false);
        Instant tickStart = Instant.parse("2026-11-05T12:00:00Z");
        Instant outcomeInstant = tickStart.plusSeconds(70); // 70s elapsed during provider execution

        Clock advancingClock = new Clock() {
            @Override
            public ZoneOffset getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(java.time.ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return providerExecuted.get() ? outcomeInstant : tickStart;
            }
        };

        FeedScheduledRefreshService serviceWithAdvancingClock = new FeedScheduledRefreshService(
                feedSourceRepository,
                hackerNewsRefreshService,
                rssFeedRefreshService,
                advancingClock
        );

        FeedSource s = createSource(77L, FeedSourceType.HACKER_NEWS);
        when(feedSourceRepository.findScheduledDueSourcesFromStart(eq(SUPPORTED_TYPES), eq(tickStart), any(Pageable.class)))
                .thenReturn(List.of(s));

        when(hackerNewsRefreshService.refreshScheduled(77L)).thenAnswer(inv -> {
            providerExecuted.set(true); // clock advances to tickStart + 70s
            return ScheduledRefreshOutcome.FAILED;
        });

        serviceWithAdvancingClock.executeTick();

        Map<Long, Instant> map = mapCooldown(serviceWithAdvancingClock);
        assertThat(map).containsKey(77L);
        // Cooldown must be outcomeInstant + 5m (12:06:10Z), NOT tickStart + 5m (12:05:00Z)
        Instant expectedCooldown = outcomeInstant.plus(Duration.ofMinutes(5));
        assertThat(map.get(77L)).isEqualTo(expectedCooldown);
    }

    @Test
    @DisplayName("Busy outcome also measures cooldown from outcome completion instant")
    void cooldown_busyOutcome_measuredFromOutcomeCompletionInstant() {
        AtomicBoolean providerExecuted = new AtomicBoolean(false);
        Instant tickStart = Instant.parse("2026-11-05T12:00:00Z");
        Instant outcomeInstant = tickStart.plusSeconds(45);

        Clock advancingClock = new Clock() {
            @Override
            public ZoneOffset getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(java.time.ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return providerExecuted.get() ? outcomeInstant : tickStart;
            }
        };

        FeedScheduledRefreshService serviceWithAdvancingClock = new FeedScheduledRefreshService(
                feedSourceRepository,
                hackerNewsRefreshService,
                rssFeedRefreshService,
                advancingClock
        );

        FeedSource s = createSource(88L, FeedSourceType.RSS);
        when(feedSourceRepository.findScheduledDueSourcesFromStart(eq(SUPPORTED_TYPES), eq(tickStart), any(Pageable.class)))
                .thenReturn(List.of(s));

        when(rssFeedRefreshService.refreshScheduled(88L)).thenAnswer(inv -> {
            providerExecuted.set(true);
            return ScheduledRefreshOutcome.BUSY;
        });

        serviceWithAdvancingClock.executeTick();

        Map<Long, Instant> map = mapCooldown(serviceWithAdvancingClock);
        assertThat(map).containsKey(88L);
        Instant expectedCooldown = outcomeInstant.plus(Duration.ofMinutes(5));
        assertThat(map.get(88L)).isEqualTo(expectedCooldown);
    }

    @Test
    @DisplayName("Expired cooldown entries are pruned before tick processing")
    void cooldownExpiry_prunesExpiredEntriesFirst() {
        Map<Long, Instant> map = mapCooldown(refreshService);
        map.put(100L, fixedNow.minusSeconds(1)); // expired
        map.put(101L, fixedNow); // expired (inclusive)
        map.put(102L, fixedNow.plusSeconds(10)); // active

        when(feedSourceRepository.findScheduledDueSourcesFromStart(eq(SUPPORTED_TYPES), eq(fixedNow), any(Pageable.class)))
                .thenReturn(List.of());

        refreshService.executeTick();

        assertThat(map).doesNotContainKey(100L);
        assertThat(map).doesNotContainKey(101L);
        assertThat(map).containsKey(102L);
    }

    @Test
    @DisplayName("Repository query failure retains cursor and does not fabricate empty wrap")
    void repositoryException_doesNotResetCursorOrWrap() {
        ReflectionTestUtils.setField(refreshService, "lastExaminedSourceId", 50L);

        when(feedSourceRepository.findScheduledDueSourcesAfterCursor(any(), any(), eq(50L), any()))
                .thenThrow(new RuntimeException("DB Connection Timeout"));

        refreshService.executeTick();

        assertThat(refreshService.getLastExaminedSourceId()).isEqualTo(50L);
    }

    @Test
    @DisplayName("Reset methods clear cursor and cooldown map")
    void resetMethods_clearState() {
        ReflectionTestUtils.setField(refreshService, "lastExaminedSourceId", 50L);
        mapCooldown(refreshService).put(10L, fixedNow.plusSeconds(30));

        refreshService.resetCursor();
        refreshService.resetCooldown();

        assertThat(refreshService.getLastExaminedSourceId()).isNull();
        assertThat(refreshService.getCooldownMap()).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Instant> mapCooldown(FeedScheduledRefreshService service) {
        return (Map<Long, Instant>) ReflectionTestUtils.getField(service, "cooldownMap");
    }
}
