package com.vhvkhangg.personalprivatevault.feed.internal.application;

import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HackerNewsRefreshAdmissionTest {

    private HackerNewsRefreshAdmission admission;

    @BeforeEach
    void setUp() {
        admission = new HackerNewsRefreshAdmission();
    }

    @Test
    @DisplayName("Single-flight prevents overlapping refresh for the same source")
    void sameSourceSingleFlightEnforced() {
        admission.acquire(1L);
        assertThat(admission.isSourceActive(1L)).isTrue();
        assertThat(admission.getActiveCount()).isEqualTo(1);

        assertThatThrownBy(() -> admission.acquire(1L))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.REFRESH_ALREADY_RUNNING));

        admission.release(1L);
        assertThat(admission.isSourceActive(1L)).isFalse();
        assertThat(admission.getActiveCount()).isEqualTo(0);

        // Can acquire again after release
        admission.acquire(1L);
        admission.release(1L);
    }

    @Test
    @DisplayName("Global capacity limit of at most 2 active refreshes is enforced")
    void globalCapacityLimitEnforced() {
        admission.acquire(1L);
        admission.acquire(2L);
        assertThat(admission.getActiveCount()).isEqualTo(2);

        assertThatThrownBy(() -> admission.acquire(3L))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.REFRESH_CAPACITY_REACHED));

        assertThat(admission.getActiveCount()).isEqualTo(2);
        assertThat(admission.isSourceActive(3L)).isFalse();

        // Release source 1, then source 3 can be acquired
        admission.release(1L);
        assertThat(admission.getActiveCount()).isEqualTo(1);

        admission.acquire(3L);
        assertThat(admission.getActiveCount()).isEqualTo(2);

        admission.release(2L);
        admission.release(3L);
        assertThat(admission.getActiveCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Concurrent attempts on the same source admit exactly one thread")
    void concurrentAttemptsSameSourceAdmitOnlyOne() throws Exception {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger runningConflictCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    admission.acquire(42L);
                    successCount.incrementAndGet();
                } catch (FeedRefreshException ex) {
                    if (ex.getReason() == FeedRefreshFailureReason.REFRESH_ALREADY_RUNNING) {
                        runningConflictCount.incrementAndGet();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(runningConflictCount.get()).isEqualTo(threads - 1);

        admission.release(42L);
        assertThat(admission.getActiveCount()).isEqualTo(0);
    }
}
