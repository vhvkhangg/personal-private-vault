package com.vhvkhangg.personalprivatevault.feed;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.application.FeedScheduledRefreshService;
import com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshAdmission;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsClient;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssFeedClient;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.scheduling.FeedSchedulerLifecycle;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedSchedulerLifecycleIntegrationTest {

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
            "ppv.feed.scheduler.enabled=false"
    })
    class SchedulerDisabledTest extends AbstractPostgresIntegrationTest {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("When ppv.feed.scheduler.enabled=false, FeedSchedulerLifecycle is absent and no global scheduling infrastructure exists")
        void whenDisabled_lifecycleBeanAbsent() {
            assertThat(applicationContext.getBeansOfType(FeedSchedulerLifecycle.class)).isEmpty();
            assertThat(applicationContext.getBeansOfType(org.springframework.scheduling.TaskScheduler.class)).isEmpty();
            assertThat(applicationContext.getBeansOfType(java.util.concurrent.ScheduledExecutorService.class)).isEmpty();
            assertThat(applicationContext.getBeansOfType(ScheduledAnnotationBeanPostProcessor.class)).hasSize(1);
            assertThat(applicationContext.getBean(RagRecoveryService.class)).isNotNull();
        }
    }

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
            "ppv.feed.scheduler.enabled=true"
    })
    class SchedulerEnabledTest extends AbstractPostgresIntegrationTest {

        @Autowired
        private ApplicationContext applicationContext;

        @Autowired
        private FeedSchedulerLifecycle lifecycle;

        @Autowired
        private FeedScheduledRefreshService refreshService;

        @Autowired
        private FeedSourceOperations feedSourceOperations;

        @Autowired
        private FeedSourceRepository feedSourceRepository;

        @Autowired
        private HackerNewsRefreshAdmission admission;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @MockitoBean
        private HackerNewsClient hackerNewsClient;

        @MockitoBean
        private RssFeedClient rssFeedClient;

        private final ObjectMapper objectMapper = new ObjectMapper();

        private JsonNode createStoryJson(Long id, String title, String url) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", id);
            node.put("type", "story");
            node.put("title", title);
            node.put("url", url);
            node.put("by", "author");
            node.put("time", 1700000000L);
            node.put("score", 10);
            node.put("descendants", 0);
            return node;
        }

        @Test
        @DisplayName("When ppv.feed.scheduler.enabled=true, FeedSchedulerLifecycle is active with zero delta to existing scheduled tasks baseline")
        void whenEnabled_lifecycleActiveWithoutGlobalSchedulingOrRagActivation() {
            assertThat(lifecycle).isNotNull();
            assertThat(lifecycle.isStarted()).isTrue();
            assertThat(lifecycle.isStopped()).isFalse();

            // No global TaskScheduler or ScheduledExecutorService beans
            assertThat(applicationContext.getBeansOfType(org.springframework.scheduling.TaskScheduler.class)).isEmpty();
            assertThat(applicationContext.getBeansOfType(java.util.concurrent.ScheduledExecutorService.class)).isEmpty();

            // Exactly default post processor, no newly introduced scheduling processor
            assertThat(applicationContext.getBeansOfType(ScheduledAnnotationBeanPostProcessor.class)).hasSize(1);
            ScheduledAnnotationBeanPostProcessor bpp = applicationContext.getBean(ScheduledAnnotationBeanPostProcessor.class);

            // Feed task is NOT registered through Spring's ScheduledAnnotationBeanPostProcessor (no new TaskScheduler bean)
            java.util.Set<String> scheduledTaskNames = bpp.getScheduledTasks().stream()
                    .map(task -> task.getTask().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(scheduledTaskNames).noneMatch(name -> name.contains("feed") || name.contains("Feed"));

            // Baseline scheduled tasks remain identical with zero delta in registrar (Moments and RAG reconcile)
            assertThat(scheduledTaskNames).containsExactlyInAnyOrder(
                    "org.springframework.modulith.moments.support.Moments.everyMidnight",
                    "org.springframework.modulith.moments.support.Moments.everyHour",
                    "com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService.reconcile"
            );

            // RAG recovery service exists as singleton bean without new TaskScheduler bean or newly activated execution
            assertThat(applicationContext.getBean(RagRecoveryService.class)).isNotNull();
        }

        @Test
        @DirtiesContext
        @DisplayName("Controlled Spring callback execution: safe fake providers with due and excluded rows, active-provider stop, admission release, no next dispatch, and lifecycle recreation")
        void controlledSpringCallback_withSafeFakeProviders_activeStop_andRecreation() throws Exception {
            // Stop default context lifecycle to isolate controlled testing
            lifecycle.stop();

            // Clean table
            jdbcTemplate.execute("DELETE FROM feed_items");
            jdbcTemplate.execute("DELETE FROM feed_sources");

            // Isolated fresh refresh service for controlled testing
            FeedScheduledRefreshService controlledService = new FeedScheduledRefreshService(
                    feedSourceRepository,
                    applicationContext.getBean(com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshService.class),
                    applicationContext.getBean(com.vhvkhangg.personalprivatevault.feed.internal.application.RssFeedRefreshService.class),
                    Clock.systemUTC()
            );

            // 1. Setup sources:
            // - Excluded source: scheduled_refresh_enabled = false
            FeedSourceView excluded = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Excluded Unscheduled Source",
                    FeedSourceType.HACKER_NEWS,
                    "https://news.ycombinator.com",
                    null,
                    true,
                    false, // scheduled_refresh_enabled = false!
                    60,
                    null
            ));

            // - Due Source 1: eligible
            FeedSourceView due1 = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Due Source 1",
                    FeedSourceType.HACKER_NEWS,
                    "https://news.ycombinator.com",
                    null,
                    true,
                    true, // scheduled_refresh_enabled = true!
                    60,
                    null
            ));

            // - Due Source 2: eligible
            FeedSourceView due2 = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Due Source 2",
                    FeedSourceType.HACKER_NEWS,
                    "https://news.ycombinator.com",
                    null,
                    true,
                    true, // scheduled_refresh_enabled = true!
                    60,
                    null
            ));

            // Reset next_fetch_at to NULL so due1 and due2 are eligible
            jdbcTemplate.update("UPDATE feed_sources SET next_fetch_at = NULL WHERE id IN (?, ?)", due1.id(), due2.id());

            // 2. Setup real recording ThreadPoolTaskScheduler with immediate start seam
            ThreadPoolTaskScheduler controlledScheduler = new ThreadPoolTaskScheduler() {
                @Override
                public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
                    return super.scheduleWithFixedDelay(task, Instant.now(), Duration.ofMillis(10));
                }
            };
            controlledScheduler.setPoolSize(1);
            controlledScheduler.setThreadNamePrefix("feed-spring-rec-");
            controlledScheduler.setWaitForTasksToCompleteOnShutdown(true);
            controlledScheduler.setAwaitTerminationSeconds(2);

            FeedSchedulerLifecycle controlledLifecycle = new FeedSchedulerLifecycle(
                    controlledService,
                    applicationContext,
                    controlledScheduler,
                    2L
            );

            CountDownLatch provider1Started = new CountDownLatch(1);
            CountDownLatch releaseProviderLatch = new CountDownLatch(1);
            AtomicBoolean provider1Interrupted = new AtomicBoolean(false);
            AtomicInteger callCount = new AtomicInteger(0);

            when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenAnswer(inv -> {
                int count = callCount.incrementAndGet();
                if (count == 1) {
                    provider1Started.countDown();
                    try {
                        releaseProviderLatch.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        provider1Interrupted.set(true);
                        throw ex;
                    }
                    return List.of(101L);
                }
                return List.of(102L);
            });
            when(hackerNewsClient.fetchItem(anyLong(), anyLong(), any(AtomicLong.class)))
                    .thenAnswer(inv -> {
                        long id = inv.getArgument(0);
                        return createStoryJson(id, "Safe Story " + id, "https://example.com/safe-" + id);
                    });

            ApplicationReadyEvent readyEvent = mock(ApplicationReadyEvent.class);
            when(readyEvent.getApplicationContext()).thenReturn((ConfigurableApplicationContext) applicationContext);

            try {
                // Start controlled lifecycle
                controlledLifecycle.onApplicationEvent(readyEvent);
                assertThat(controlledLifecycle.isStarted()).isTrue();

                // Wait until provider for Due Source 1 actively starts on the owned scheduler thread
                assertThat(provider1Started.await(5, TimeUnit.SECONDS)).isTrue();

                // Due Source 1 has actively acquired single-flight admission
                boolean isDue1Active = Boolean.TRUE.equals(org.springframework.test.util.ReflectionTestUtils.invokeMethod(admission, "isSourceActive", due1.id()));
                int activeCount = (int) org.springframework.test.util.ReflectionTestUtils.invokeMethod(admission, "getActiveCount");
                assertThat(isDue1Active).isTrue();
                assertThat(activeCount).isEqualTo(1);

                // Stop during active blocked provider
                boolean clean = controlledLifecycle.stop();

                // Stop must terminate cleanly within await timeout
                assertThat(clean).isTrue();
                assertThat(controlledLifecycle.isTerminated()).isTrue();
                assertThat(controlledLifecycle.isStopped()).isTrue();
                assertThat(provider1Interrupted.get()).isTrue();

                // Admission for Due Source 1 must be cleanly released
                boolean isDue1ActiveAfter = Boolean.TRUE.equals(org.springframework.test.util.ReflectionTestUtils.invokeMethod(admission, "isSourceActive", due1.id()));
                int activeCountAfter = (int) org.springframework.test.util.ReflectionTestUtils.invokeMethod(admission, "getActiveCount");
                assertThat(isDue1ActiveAfter).isFalse();
                assertThat(activeCountAfter).isEqualTo(0);

                // Exactly one provider call was dispatched; second candidate (due2) was NOT dispatched (interruption/stop broke the loop)
                assertThat(callCount.get()).isEqualTo(1);

                // 3. Recreate lifecycle and assert fresh cursor/cooldown/delayed registration
                ThreadPoolTaskScheduler freshScheduler = new ThreadPoolTaskScheduler();
                freshScheduler.setPoolSize(1);
                freshScheduler.setThreadNamePrefix("feed-fresh-timer-");
                freshScheduler.setWaitForTasksToCompleteOnShutdown(true);
                freshScheduler.setAwaitTerminationSeconds(2);

                FeedScheduledRefreshService freshService = new FeedScheduledRefreshService(
                        feedSourceRepository,
                        applicationContext.getBean(com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshService.class),
                        applicationContext.getBean(com.vhvkhangg.personalprivatevault.feed.internal.application.RssFeedRefreshService.class),
                        Clock.systemUTC()
                );

                FeedSchedulerLifecycle freshLifecycle = new FeedSchedulerLifecycle(
                        freshService,
                        applicationContext,
                        freshScheduler,
                        2L
                );

                assertThat(freshService.getLastExaminedSourceId()).isNull();
                assertThat(freshService.getCooldownMap()).isEmpty();

                freshLifecycle.onApplicationEvent(readyEvent);
                assertThat(freshLifecycle.isStarted()).isTrue();
                assertThat(freshLifecycle.isInitialized()).isTrue();
                assertThat((Object) freshLifecycle.getScheduledFuture()).isNotNull();
                assertThat(freshLifecycle.getScheduledFuture().isCancelled()).isFalse();

                freshLifecycle.stop();
                assertThat(freshLifecycle.isTerminated()).isTrue();

            } finally {
                releaseProviderLatch.countDown();
                controlledLifecycle.stop();
            }
        }

        @Test
        @DisplayName("Privacy observation: synthetic failure containing sensitive markers is logged safely without leaking markers")
        void failureLoggingPrivacy_doesNotLeakSensitiveMarkers() {
            Logger serviceLogger = (Logger) LoggerFactory.getLogger(FeedScheduledRefreshService.class);
            ListAppender<ILoggingEvent> appender = new ListAppender<>();
            appender.start();
            serviceLogger.addAppender(appender);

            // Synthetic failure containing URL, XML, title, and credential markers
            String sensitiveMarker = "https://user:SUPER_SECRET_PASSWORD@example.com/feed?token=API_KEY_9999 <sensitiveXml>SecretTitle</sensitiveXml>";

            FeedSourceRepository mockRepo = mock(FeedSourceRepository.class);
            when(mockRepo.findScheduledDueSourcesFromStart(any(), any(), any()))
                    .thenThrow(new RuntimeException(sensitiveMarker));

            FeedScheduledRefreshService testService = new FeedScheduledRefreshService(
                    mockRepo,
                    applicationContext.getBean(com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshService.class),
                    applicationContext.getBean(com.vhvkhangg.personalprivatevault.feed.internal.application.RssFeedRefreshService.class),
                    Clock.systemUTC()
            );

            try {
                testService.executeTick();

                // 1. Require that the expected safe warning event WAS logged (not an empty loop!)
                assertThat(appender.list).isNotEmpty();
                assertThat(appender.list)
                        .anyMatch(event -> event.getLevel() == Level.WARN
                                && event.getFormattedMessage().contains("Scheduled due query failed"));

                // 2. Inspect all logged events, arguments, and throwables to verify NO sensitive markers leaked
                for (ILoggingEvent event : appender.list) {
                    assertThat(event.getFormattedMessage()).doesNotContain("SUPER_SECRET_PASSWORD");
                    assertThat(event.getFormattedMessage()).doesNotContain("API_KEY_9999");
                    assertThat(event.getFormattedMessage()).doesNotContain("sensitiveXml");
                    assertThat(event.getFormattedMessage()).doesNotContain("SecretTitle");
                    assertThat(event.getMessage()).doesNotContain("SUPER_SECRET_PASSWORD");
                    if (event.getArgumentArray() != null) {
                        for (Object arg : event.getArgumentArray()) {
                            assertThat(String.valueOf(arg)).doesNotContain("SUPER_SECRET_PASSWORD");
                            assertThat(String.valueOf(arg)).doesNotContain("API_KEY_9999");
                        }
                    }
                    if (event.getThrowableProxy() != null) {
                        assertThat(event.getThrowableProxy().getMessage()).doesNotContain("SUPER_SECRET_PASSWORD");
                    }
                }
            } finally {
                serviceLogger.detachAppender(appender);
            }
        }

        @Test
        @DirtiesContext
        @DisplayName("Stop lifecycle cancels future, shuts down owned ThreadPoolTaskScheduler, and terminates thread cleanly")
        void stopLifecycle_cancelsFutureAndShutsDownScheduler() {
            assertThat(lifecycle.isStarted()).isTrue();
            boolean clean = lifecycle.stop();
            assertThat(clean).isTrue();
            assertThat(lifecycle.isStopped()).isTrue();
            assertThat(lifecycle.getScheduledFuture().isCancelled()).isTrue();
            assertThat(lifecycle.isTerminated()).isTrue();
        }
    }
}
