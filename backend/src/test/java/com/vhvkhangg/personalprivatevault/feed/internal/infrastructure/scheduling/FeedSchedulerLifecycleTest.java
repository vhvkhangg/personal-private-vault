package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.scheduling;

import com.vhvkhangg.personalprivatevault.feed.internal.application.FeedScheduledRefreshService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedSchedulerLifecycleTest {

    @Mock
    private FeedScheduledRefreshService refreshService;

    @Mock
    private ConfigurableApplicationContext applicationContext;

    @Mock
    private ThreadPoolTaskScheduler taskScheduler;

    @Test
    @DisplayName("Default constructor leaves scheduler uninitialized until ApplicationReadyEvent")
    void defaultConstructor_uninitializedBeforeReadiness_initializedAfterReadiness() {
        FeedSchedulerLifecycle lifecycle = new FeedSchedulerLifecycle(
                refreshService,
                applicationContext
        );

        assertThat(lifecycle.isInitialized()).isFalse();
        assertThat(lifecycle.isStarted()).isFalse();
        assertThat(lifecycle.isStopped()).isFalse();

        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        when(event.getApplicationContext()).thenReturn(applicationContext);

        lifecycle.onApplicationEvent(event);

        assertThat(lifecycle.isInitialized()).isTrue();
        assertThat(lifecycle.isStarted()).isTrue();

        boolean cleanStop = lifecycle.stop();
        assertThat(cleanStop).isTrue();
        assertThat(lifecycle.isStopped()).isTrue();
        assertThat(lifecycle.isTerminated()).isTrue();
    }

    @Test
    @DisplayName("ApplicationReadyEvent with matching context registers task with 60s initial delay and 60s fixed delay")
    void onApplicationEvent_matchingContext_registersTask() {
        Instant now = Instant.parse("2026-11-05T10:00:00Z");
        when(taskScheduler.getClock()).thenReturn(Clock.fixed(now, ZoneOffset.UTC));
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        when(taskScheduler.scheduleWithFixedDelay(any(Runnable.class), eq(now.plus(Duration.ofSeconds(60))), eq(Duration.ofSeconds(60))))
                .thenAnswer(inv -> future);

        FeedSchedulerLifecycle lifecycle = new FeedSchedulerLifecycle(
                refreshService,
                applicationContext,
                taskScheduler,
                75L
        );

        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        when(event.getApplicationContext()).thenReturn(applicationContext);

        lifecycle.onApplicationEvent(event);

        assertThat(lifecycle.isStarted()).isTrue();
        assertThat((Object) lifecycle.getScheduledFuture()).isSameAs(future);
        verify(taskScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(now.plus(Duration.ofSeconds(60))), eq(Duration.ofSeconds(60)));
    }

    @Test
    @DisplayName("Captured callback invocation explicitly executes refreshService.executeTick()")
    void onApplicationEvent_capturedCallbackInvocation_executesRefreshServiceTick() {
        Instant now = Instant.parse("2026-11-05T10:00:00Z");
        when(taskScheduler.getClock()).thenReturn(Clock.fixed(now, ZoneOffset.UTC));
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(taskScheduler.scheduleWithFixedDelay(runnableCaptor.capture(), any(Instant.class), any(Duration.class)))
                .thenAnswer(inv -> future);

        FeedSchedulerLifecycle lifecycle = new FeedSchedulerLifecycle(
                refreshService,
                applicationContext,
                taskScheduler,
                75L
        );

        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        when(event.getApplicationContext()).thenReturn(applicationContext);

        lifecycle.onApplicationEvent(event);

        Runnable registeredCallback = runnableCaptor.getValue();
        assertThat(registeredCallback).isNotNull();

        verify(refreshService, never()).executeTick();
        registeredCallback.run();
        verify(refreshService, times(1)).executeTick();
    }

    @Test
    @DisplayName("ApplicationReadyEvent with non-matching context is ignored")
    void onApplicationEvent_nonMatchingContext_ignored() {
        ConfigurableApplicationContext foreignContext = mock(ConfigurableApplicationContext.class);

        FeedSchedulerLifecycle lifecycle = new FeedSchedulerLifecycle(
                refreshService,
                applicationContext,
                taskScheduler,
                75L
        );

        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        when(event.getApplicationContext()).thenReturn(foreignContext);

        lifecycle.onApplicationEvent(event);

        assertThat(lifecycle.isStarted()).isFalse();
        verify(taskScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
    }

    @Test
    @DisplayName("Repeated ApplicationReadyEvent is idempotent and registers only once")
    void onApplicationEvent_repeatedEvent_idempotent() {
        Instant now = Instant.parse("2026-11-05T10:00:00Z");
        when(taskScheduler.getClock()).thenReturn(Clock.fixed(now, ZoneOffset.UTC));
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        when(taskScheduler.scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class)))
                .thenAnswer(inv -> future);

        FeedSchedulerLifecycle lifecycle = new FeedSchedulerLifecycle(
                refreshService,
                applicationContext,
                taskScheduler,
                75L
        );

        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        when(event.getApplicationContext()).thenReturn(applicationContext);

        lifecycle.onApplicationEvent(event);
        lifecycle.onApplicationEvent(event);

        assertThat(lifecycle.isStarted()).isTrue();
        verify(taskScheduler, times(1)).scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
    }

    @Test
    @DisplayName("Stopping before readiness prevents subsequent task registration")
    void onApplicationEvent_stoppedBeforeReadiness_doesNotRegister() {
        FeedSchedulerLifecycle lifecycle = new FeedSchedulerLifecycle(
                refreshService,
                applicationContext,
                taskScheduler,
                75L
        );

        boolean cleanStop = lifecycle.stop();
        assertThat(cleanStop).isTrue();
        assertThat(lifecycle.isStopped()).isTrue();

        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        when(event.getApplicationContext()).thenReturn(applicationContext);

        lifecycle.onApplicationEvent(event);

        assertThat(lifecycle.isStarted()).isFalse();
        verify(taskScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
    }

    @Test
    @DisplayName("Registration failure cleans up scheduler and marks lifecycle stopped")
    void onApplicationEvent_registrationFailure_cleansUpAndStops() {
        Instant now = Instant.parse("2026-11-05T10:00:00Z");
        when(taskScheduler.getClock()).thenReturn(Clock.fixed(now, ZoneOffset.UTC));
        when(taskScheduler.scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class)))
                .thenThrow(new IllegalStateException("Scheduler closed"));

        FeedSchedulerLifecycle lifecycle = new FeedSchedulerLifecycle(
                refreshService,
                applicationContext,
                taskScheduler,
                75L
        );

        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        when(event.getApplicationContext()).thenReturn(applicationContext);

        assertThatThrownBy(() -> lifecycle.onApplicationEvent(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Scheduler closed");

        assertThat(lifecycle.isStopped()).isTrue();
        verify(refreshService).stop();
        verify(taskScheduler).destroy();
    }

    @Test
    @DisplayName("Stop with nonterminating task consumes only single await budget and reports failed termination until released")
    void stop_withNonterminatingTask_consumesSingleAwaitBudget_reportsFailedTermination() throws InterruptedException {
        ThreadPoolTaskScheduler realScheduler = new ThreadPoolTaskScheduler();
        realScheduler.setPoolSize(1);
        realScheduler.setThreadNamePrefix("test-single-await-");
        realScheduler.setWaitForTasksToCompleteOnShutdown(true);
        realScheduler.setAwaitTerminationSeconds(1);

        FeedSchedulerLifecycle lifecycle = new FeedSchedulerLifecycle(
                refreshService,
                applicationContext,
                realScheduler,
                1L
        );

        CountDownLatch taskStarted = new CountDownLatch(1);
        CountDownLatch releaseLatch = new CountDownLatch(1);

        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        when(event.getApplicationContext()).thenReturn(applicationContext);

        lifecycle.onApplicationEvent(event);
        assertThat(lifecycle.isStarted()).isTrue();

        // Submit task directly to the owned executor that blocks ignoring interruption until releaseLatch is counted down
        realScheduler.getScheduledThreadPoolExecutor().submit(() -> {
            taskStarted.countDown();
            while (releaseLatch.getCount() > 0) {
                try {
                    releaseLatch.await(50, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignored) {
                    // ignore interruption to simulate nonterminating worker
                }
            }
        });

        assertThat(taskStarted.await(2, TimeUnit.SECONDS)).isTrue();

        long startNanos = System.nanoTime();
        boolean clean = lifecycle.stop();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

        // Single await budget (~1s), strictly less than doubled wait (2s)
        assertThat(durationMs).isGreaterThanOrEqualTo(900L).isLessThan(1700L);
        assertThat(clean).isFalse();
        assertThat(lifecycle.isTerminated()).isFalse();

        // Release the worker and verify it terminates cleanly without leaking threads
        releaseLatch.countDown();
        boolean terminated = realScheduler.getScheduledThreadPoolExecutor().awaitTermination(2, TimeUnit.SECONDS);
        assertThat(terminated).isTrue();
        assertThat(lifecycle.isTerminated()).isTrue();
    }

    @Test
    @DisplayName("Stop with cooperative task terminates cleanly within timeout")
    void stop_withCooperativeTask_terminatesCleanlyWithinTimeout() throws InterruptedException {
        ThreadPoolTaskScheduler realScheduler = new ThreadPoolTaskScheduler() {
            @Override
            public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
                return super.scheduleWithFixedDelay(task, Duration.ofMillis(1));
            }
        };
        realScheduler.setPoolSize(1);
        realScheduler.setThreadNamePrefix("test-cooperative-");
        realScheduler.setWaitForTasksToCompleteOnShutdown(true);
        realScheduler.setAwaitTerminationSeconds(2);

        CountDownLatch taskStarted = new CountDownLatch(1);
        AtomicBoolean taskInterrupted = new AtomicBoolean(false);

        org.mockito.Mockito.doAnswer(inv -> {
            taskStarted.countDown();
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException ex) {
                taskInterrupted.set(true);
            }
            return null;
        }).when(refreshService).executeTick();

        FeedSchedulerLifecycle lifecycle = new FeedSchedulerLifecycle(
                refreshService,
                applicationContext,
                realScheduler,
                2L
        );

        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        when(event.getApplicationContext()).thenReturn(applicationContext);

        lifecycle.onApplicationEvent(event);

        assertThat(taskStarted.await(2, TimeUnit.SECONDS)).isTrue();

        long startNanos = System.nanoTime();
        boolean clean = lifecycle.stop();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

        assertThat(clean).isTrue();
        assertThat(lifecycle.isTerminated()).isTrue();
        assertThat(taskInterrupted.get()).isTrue();
        assertThat(durationMs).isLessThan(1000L);
    }

    @Test
    @DisplayName("Stop is idempotent, stops refresh service, cancels future, and shuts down task scheduler")
    void stop_idempotentAndCleansUp() {
        ThreadPoolTaskScheduler realScheduler = new ThreadPoolTaskScheduler();
        realScheduler.setPoolSize(1);
        realScheduler.setThreadNamePrefix("test-feed-scheduler-");
        realScheduler.setWaitForTasksToCompleteOnShutdown(true);
        realScheduler.setAwaitTerminationSeconds(2);

        FeedSchedulerLifecycle lifecycle = new FeedSchedulerLifecycle(
                refreshService,
                applicationContext,
                realScheduler,
                2L
        );

        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);
        when(event.getApplicationContext()).thenReturn(applicationContext);

        lifecycle.onApplicationEvent(event);
        assertThat(lifecycle.isStarted()).isTrue();
        ScheduledFuture<?> future = lifecycle.getScheduledFuture();
        assertThat((Object) future).isNotNull();

        boolean clean = lifecycle.stop();
        assertThat(clean).isTrue();
        assertThat(lifecycle.isStopped()).isTrue();
        assertThat(future.isCancelled()).isTrue();
        verify(refreshService).stop();

        // Second stop is an idempotent no-op
        boolean secondClean = lifecycle.stop();
        assertThat(secondClean).isTrue();
        verify(refreshService, times(1)).stop();
    }

    @Test
    @DisplayName("Lifecycle constants match production specifications")
    void constants_matchSpecification() {
        assertThat(FeedSchedulerLifecycle.THREAD_POOL_SIZE).isEqualTo(1);
        assertThat(FeedSchedulerLifecycle.THREAD_NAME_PREFIX).isEqualTo("feed-scheduler-");
        assertThat(FeedSchedulerLifecycle.INITIAL_DELAY).isEqualTo(Duration.ofSeconds(60));
        assertThat(FeedSchedulerLifecycle.FIXED_DELAY).isEqualTo(Duration.ofSeconds(60));
        assertThat(FeedSchedulerLifecycle.DEFAULT_SHUTDOWN_AWAIT_SECONDS).isEqualTo(75L);
    }
}
