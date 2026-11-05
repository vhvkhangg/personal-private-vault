package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.scheduling;

import com.vhvkhangg.personalprivatevault.feed.internal.application.FeedScheduledRefreshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the lifecycle of the private Feed scheduled refresh timer.
 *
 * <p>Conditionally active only when {@code ppv.feed.scheduler.enabled=true}.
 * Uses a private, single-thread {@link ThreadPoolTaskScheduler} without exposing a global
 * scheduling bean or enabling annotation-driven scheduling, isolating Feed from RAG or other modules.
 */
@Component
@ConditionalOnProperty(name = "ppv.feed.scheduler.enabled", havingValue = "true")
public class FeedSchedulerLifecycle implements ApplicationListener<ApplicationReadyEvent>, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(FeedSchedulerLifecycle.class);

    public static final int THREAD_POOL_SIZE = 1;
    public static final String THREAD_NAME_PREFIX = "feed-scheduler-";
    public static final Duration INITIAL_DELAY = Duration.ofSeconds(60);
    public static final Duration FIXED_DELAY = Duration.ofSeconds(60);
    public static final long DEFAULT_SHUTDOWN_AWAIT_SECONDS = 75L;

    private final FeedScheduledRefreshService refreshService;
    private final ApplicationContext applicationContext;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final long shutdownAwaitSeconds;

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private volatile ScheduledFuture<?> scheduledFuture;

    @Autowired
    public FeedSchedulerLifecycle(
            FeedScheduledRefreshService refreshService,
            ApplicationContext applicationContext
    ) {
        this(refreshService, applicationContext, createUninitializedScheduler(DEFAULT_SHUTDOWN_AWAIT_SECONDS), DEFAULT_SHUTDOWN_AWAIT_SECONDS);
    }

    public FeedSchedulerLifecycle(
            FeedScheduledRefreshService refreshService,
            ApplicationContext applicationContext,
            ThreadPoolTaskScheduler taskScheduler,
            long shutdownAwaitSeconds
    ) {
        this.refreshService = refreshService;
        this.applicationContext = applicationContext;
        this.taskScheduler = taskScheduler;
        this.shutdownAwaitSeconds = shutdownAwaitSeconds;
    }

    private static ThreadPoolTaskScheduler createUninitializedScheduler(long awaitTerminationSeconds) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(THREAD_POOL_SIZE);
        scheduler.setThreadNamePrefix(THREAD_NAME_PREFIX);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds((int) awaitTerminationSeconds);
        return scheduler;
    }

    @Override
    public synchronized void onApplicationEvent(ApplicationReadyEvent event) {
        if (event.getApplicationContext() != this.applicationContext) {
            log.debug("Ignoring ApplicationReadyEvent from non-matching context");
            return;
        }

        if (stopped.get()) {
            log.warn("Feed scheduler was stopped before readiness; skipping registration");
            return;
        }

        if (started.get()) {
            log.debug("Feed scheduler already started; skipping duplicate registration");
            return;
        }

        try {
            if (initialized.compareAndSet(false, true)) {
                try {
                    taskScheduler.getScheduledThreadPoolExecutor();
                } catch (IllegalStateException ex) {
                    taskScheduler.initialize();
                }
            }
            Instant startInstant = taskScheduler.getClock().instant().plus(INITIAL_DELAY);
            this.scheduledFuture = taskScheduler.scheduleWithFixedDelay(
                    refreshService::executeTick,
                    startInstant,
                    FIXED_DELAY
            );
            started.set(true);
            log.info("Feed scheduled refresh registered: first run at {}, fixed delay {}", startInstant, FIXED_DELAY);
        } catch (Exception ex) {
            log.error("Failed to register Feed scheduled refresh task; cleaning up scheduler", ex);
            stop();
            throw ex;
        }
    }

    @Override
    public void destroy() {
        stop();
    }

    public synchronized boolean stop() {
        if (stopped.get()) {
            return isTerminated();
        }
        stopped.set(true);

        log.info("Stopping Feed scheduler lifecycle");
        refreshService.stop();

        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }

        if (initialized.get()) {
            taskScheduler.destroy();
        }

        boolean clean = isTerminated();
        if (!clean) {
            log.warn("Feed scheduler did not terminate within {} seconds timeout", shutdownAwaitSeconds);
        }
        return clean;
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    public boolean isTerminated() {
        if (!initialized.get() || taskScheduler == null) {
            return true;
        }
        try {
            var exec = taskScheduler.getScheduledThreadPoolExecutor();
            return exec == null || exec.isTerminated();
        } catch (IllegalStateException ex) {
            return true;
        }
    }

    public boolean isStarted() {
        return started.get();
    }

    public boolean isStopped() {
        return stopped.get();
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    public ThreadPoolTaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    public long getShutdownAwaitSeconds() {
        return shutdownAwaitSeconds;
    }
}
