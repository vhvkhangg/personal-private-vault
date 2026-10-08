package com.vhvkhangg.personalprivatevault.audit;

import com.vhvkhangg.personalprivatevault.location.address.AddressOperations;
import com.vhvkhangg.personalprivatevault.location.address.CreateAddressCommand;
import com.vhvkhangg.personalprivatevault.location.enums.DayOfWeek;
import com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursIntervalInput;
import com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursOperations;
import com.vhvkhangg.personalprivatevault.location.hours.ReplaceBusinessHoursScheduleCommand;
import com.vhvkhangg.personalprivatevault.location.location.CreateLocationCommand;
import com.vhvkhangg.personalprivatevault.location.location.LocationOperations;
import com.vhvkhangg.personalprivatevault.location.location.UpdateLocationCommand;
import com.vhvkhangg.personalprivatevault.location.view.BusinessHoursScheduleView;
import com.vhvkhangg.personalprivatevault.location.view.LocationView;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BA15-9 regression test:
 * Verifies serialized concurrency between Location scalar mutations and BusinessHours schedule mutations,
 * including prior managed reads, unknown schedule transitions, and forced PostgreSQL lock contention.
 */
class LocationConcurrencyIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private LocationOperations locationOperations;

    @Autowired
    private BusinessHoursOperations businessHoursOperations;

    @Autowired
    private AddressOperations addressOperations;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newCachedThreadPool();
    }

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("BA15-9: Concurrent scalar update and schedule replacement preserve both updates without lost updates")
    void concurrentScalarAndScheduleUpdatesSucceed() throws Exception {
        var addr = addressOperations.create(new CreateAddressCommand("Addr", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));
        LocationView location = locationOperations.create(new CreateLocationCommand(
                null,
                addr.id(),
                "Original Name",
                null,
                null,
                null,
                null,
                BigDecimal.valueOf(10.00),
                BigDecimal.valueOf(20.00),
                "USD",
                "Original review"
        ));
        Long locationId = location.id();

        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Writer 1: Update scalar fields
        Future<?> scalarFuture = executor.submit(() -> {
            try {
                barrier.await(5, TimeUnit.SECONDS);
                locationOperations.update(new UpdateLocationCommand(
                        locationId,
                        null,
                        addr.id(),
                        "Updated Scalar Name",
                        null,
                        null,
                        null,
                        null,
                        BigDecimal.valueOf(25.50),
                        BigDecimal.valueOf(50.00),
                        "USD",
                        "Updated Notes"
                ));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Writer 2: Update schedule
        Future<?> scheduleFuture = executor.submit(() -> {
            try {
                barrier.await(5, TimeUnit.SECONDS);
                businessHoursOperations.replaceSchedule(new ReplaceBusinessHoursScheduleCommand(
                        locationId,
                        true,
                        List.of(new BusinessHoursIntervalInput(
                                DayOfWeek.MONDAY,
                                LocalTime.of(9, 0),
                                LocalTime.of(17, 0)
                        ))
                ));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        scalarFuture.get(10, TimeUnit.SECONDS);
        scheduleFuture.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Verify both updates took effect and neither clobbered the other
        LocationView finalLocation = locationOperations.findById(locationId);
        assertThat(finalLocation.name()).isEqualTo("Updated Scalar Name");
        assertThat(finalLocation.minPrice()).isEqualByComparingTo(BigDecimal.valueOf(25.50));
        assertThat(finalLocation.review()).isEqualTo("Updated Notes");

        BusinessHoursScheduleView finalSchedule = businessHoursOperations.getSchedule(locationId);
        assertThat(finalSchedule.intervals()).hasSize(1);
        assertThat(finalSchedule.intervals().get(0).dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("BA15-9: Prior managed read in scalar transaction does not overwrite schedule committed concurrently")
    void priorManagedReadDoesNotOverwriteConcurrentSchedule() throws Exception {
        var addr = addressOperations.create(new CreateAddressCommand("Addr 2", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));
        LocationView location = locationOperations.create(new CreateLocationCommand(
                null, addr.id(), "Initial Name", null, null, null, null, null, null, null, null
        ));
        Long locationId = location.id();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        CountDownLatch t1ReadLatch = new CountDownLatch(1);
        CountDownLatch t2CommitLatch = new CountDownLatch(1);

        Future<?> f1 = executor.submit(() -> {
            txTemplate.execute(status -> {
                // T1 performs prior managed read: sees businessHoursKnown = false
                LocationView priorRead = locationOperations.findById(locationId);
                assertThat(priorRead.businessHoursKnown()).isFalse();

                t1ReadLatch.countDown();
                try {
                    t2CommitLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // T1 updates scalar field. Refresh under pessimistic lock ensures fresh state
                locationOperations.update(new UpdateLocationCommand(
                        locationId, null, addr.id(), "Scalar Name Updated", null, null, null, null, null, null, null, "Updated Review"
                ));
                return null;
            });
            return null;
        });

        assertThat(t1ReadLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // T2 replaces schedule to known = true with Tuesday interval and commits
        txTemplate.execute(status -> {
            businessHoursOperations.replaceSchedule(new ReplaceBusinessHoursScheduleCommand(
                    locationId,
                    true,
                    List.of(new BusinessHoursIntervalInput(
                            DayOfWeek.TUESDAY,
                            LocalTime.of(10, 0),
                            LocalTime.of(18, 0)
                    ))
            ));
            return null;
        });
        t2CommitLatch.countDown();

        f1.get(10, TimeUnit.SECONDS);

        // Verification: Both scalar name and schedule are intact, businessHoursKnown remains true!
        LocationView finalLocation = locationOperations.findById(locationId);
        assertThat(finalLocation.name()).isEqualTo("Scalar Name Updated");
        assertThat(finalLocation.review()).isEqualTo("Updated Review");
        assertThat(finalLocation.businessHoursKnown()).isTrue();

        BusinessHoursScheduleView finalSchedule = businessHoursOperations.getSchedule(locationId);
        assertThat(finalSchedule.businessHoursKnown()).isTrue();
        assertThat(finalSchedule.intervals()).hasSize(1);
        assertThat(finalSchedule.intervals().get(0).dayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
    }

    @Test
    @DisplayName("BA15-9: Scalar update vs unknown schedule replacement transitions correctly and serializes lock")
    void scalarUpdateVsUnknownScheduleReplacementSerializes() throws Exception {
        var addr = addressOperations.create(new CreateAddressCommand("Addr 3", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));
        LocationView location = locationOperations.create(new CreateLocationCommand(
                null, addr.id(), "Initial Branch", null, null, null, null, null, null, null, null
        ));
        Long locationId = location.id();

        // Initially known schedule
        businessHoursOperations.replaceSchedule(new ReplaceBusinessHoursScheduleCommand(
                locationId,
                true,
                List.of(new BusinessHoursIntervalInput(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)))
        ));

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        CountDownLatch t1LockLatch = new CountDownLatch(1);
        CountDownLatch t2BlockedLatch = new CountDownLatch(1);
        CountDownLatch t1ReleaseLatch = new CountDownLatch(1);
        AtomicBoolean t2FinishedBeforeT1Commit = new AtomicBoolean(false);

        Future<?> f1 = executor.submit(() -> {
            txTemplate.execute(status -> {
                // T1 updates scalar holding row lock
                locationOperations.update(new UpdateLocationCommand(
                        locationId, null, addr.id(), "Name Held Under Lock", null, null, null, null, null, null, null, null
                ));
                t1LockLatch.countDown();
                try {
                    t1ReleaseLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            return null;
        });

        Future<?> f2 = executor.submit(() -> {
            try {
                t1LockLatch.await(5, TimeUnit.SECONDS);
                t2BlockedLatch.countDown();
                txTemplate.execute(status -> {
                    // T2 replaces schedule with unknown schedule (empty intervals)
                    businessHoursOperations.replaceSchedule(new ReplaceBusinessHoursScheduleCommand(
                            locationId, false, List.of()
                    ));
                    return null;
                });
                if (t1ReleaseLatch.getCount() > 0) {
                    t2FinishedBeforeT1Commit.set(true);
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertThat(t1LockLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(t2BlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Deterministically verify T2 is blocked on PostgreSQL row lock held by T1
        awaitCompetingLock(java.time.Duration.ofSeconds(5));
        assertThat(t2FinishedBeforeT1Commit.get()).isFalse();

        // Release T1 to commit
        t1ReleaseLatch.countDown();
        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);

        // Verify final state: scalar name was updated, schedule was marked unknown with empty intervals
        LocationView finalLocation = locationOperations.findById(locationId);
        assertThat(finalLocation.name()).isEqualTo("Name Held Under Lock");
        assertThat(finalLocation.businessHoursKnown()).isFalse();

        BusinessHoursScheduleView finalSchedule = businessHoursOperations.getSchedule(locationId);
        assertThat(finalSchedule.businessHoursKnown()).isFalse();
        assertThat(finalSchedule.intervals()).isEmpty();
    }

    @Test
    @DisplayName("BA15-9: Opposite order - Schedule replacement holds lock, scalar writer serializes and both commit")
    void scheduleReplacementVsScalarUpdateSerializes() throws Exception {
        var addr = addressOperations.create(new CreateAddressCommand("Addr 4", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));
        LocationView location = locationOperations.create(new CreateLocationCommand(
                null, addr.id(), "Initial Opposite Name", null, null, null, null, null, null, null, null
        ));
        Long locationId = location.id();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        CountDownLatch t1LockLatch = new CountDownLatch(1);
        CountDownLatch t2BlockedLatch = new CountDownLatch(1);
        CountDownLatch t1ReleaseLatch = new CountDownLatch(1);
        AtomicBoolean t2FinishedBeforeT1Commit = new AtomicBoolean(false);

        Future<?> f1 = executor.submit(() -> {
            txTemplate.execute(status -> {
                // T1 updates schedule holding row lock
                businessHoursOperations.replaceSchedule(new ReplaceBusinessHoursScheduleCommand(
                        locationId,
                        true,
                        List.of(new BusinessHoursIntervalInput(DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)))
                ));
                t1LockLatch.countDown();
                try {
                    t1ReleaseLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            return null;
        });

        Future<?> f2 = executor.submit(() -> {
            try {
                t1LockLatch.await(5, TimeUnit.SECONDS);
                t2BlockedLatch.countDown();
                txTemplate.execute(status -> {
                    // T2 updates scalar fields
                    locationOperations.update(new UpdateLocationCommand(
                            locationId, null, addr.id(), "Opposite Scalar Updated", null, null, null, null, null, null, null, null
                    ));
                    return null;
                });
                if (t1ReleaseLatch.getCount() > 0) {
                    t2FinishedBeforeT1Commit.set(true);
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertThat(t1LockLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(t2BlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Deterministically verify T2 is blocked on PostgreSQL row lock held by T1
        awaitCompetingLock(java.time.Duration.ofSeconds(5));
        assertThat(t2FinishedBeforeT1Commit.get()).isFalse();

        // Release T1 to commit
        t1ReleaseLatch.countDown();
        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);

        // Verify both updates committed cleanly
        LocationView finalLocation = locationOperations.findById(locationId);
        assertThat(finalLocation.name()).isEqualTo("Opposite Scalar Updated");
        assertThat(finalLocation.businessHoursKnown()).isTrue();

        BusinessHoursScheduleView finalSchedule = businessHoursOperations.getSchedule(locationId);
        assertThat(finalSchedule.businessHoursKnown()).isTrue();
        assertThat(finalSchedule.intervals()).hasSize(1);
        assertThat(finalSchedule.intervals().get(0).dayOfWeek()).isEqualTo(DayOfWeek.THURSDAY);
    }

    @Test
    @DisplayName("BA15-9: Prior managed read in schedule transaction does not overwrite concurrent scalar update")
    void priorManagedReadInScheduleTxDoesNotOverwriteConcurrentScalar() throws Exception {
        var addr = addressOperations.create(new CreateAddressCommand("Addr 5", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));
        LocationView location = locationOperations.create(new CreateLocationCommand(
                null, addr.id(), "Initial Prior Read Name", null, null, null, null, null, null, null, null
        ));
        Long locationId = location.id();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        CountDownLatch t1ReadLatch = new CountDownLatch(1);
        CountDownLatch t2CommitLatch = new CountDownLatch(1);

        Future<?> f1 = executor.submit(() -> {
            txTemplate.execute(status -> {
                // T1 performs prior managed read of location
                LocationView priorRead = locationOperations.findById(locationId);
                assertThat(priorRead.name()).isEqualTo("Initial Prior Read Name");

                t1ReadLatch.countDown();
                try {
                    t2CommitLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // T1 replaces schedule; refresh under lock preserves concurrent scalar mutations
                businessHoursOperations.replaceSchedule(new ReplaceBusinessHoursScheduleCommand(
                        locationId,
                        true,
                        List.of(new BusinessHoursIntervalInput(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(16, 0)))
                ));
                return null;
            });
            return null;
        });

        assertThat(t1ReadLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // T2 concurrently updates scalar name
        txTemplate.execute(status -> {
            locationOperations.update(new UpdateLocationCommand(
                    locationId, null, addr.id(), "Updated By Concurrent Scalar Writer", null, null, null, null, null, null, null, "Review"
            ));
            return null;
        });
        t2CommitLatch.countDown();

        f1.get(10, TimeUnit.SECONDS);

        // Verification: Both scalar name and schedule are intact
        LocationView finalLocation = locationOperations.findById(locationId);
        assertThat(finalLocation.name()).isEqualTo("Updated By Concurrent Scalar Writer");
        assertThat(finalLocation.businessHoursKnown()).isTrue();

        BusinessHoursScheduleView finalSchedule = businessHoursOperations.getSchedule(locationId);
        assertThat(finalSchedule.businessHoursKnown()).isTrue();
        assertThat(finalSchedule.intervals()).hasSize(1);
        assertThat(finalSchedule.intervals().get(0).dayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
    }

    @Test
    @DisplayName("BA15-9: Prior-managed Location reader blocked by unknown->known schedule writer returns coherent fresh state")
    void priorManagedReaderDuringUnknownToKnownWriterContentionSerializesCoherentState() throws Exception {
        var addr = addressOperations.create(new CreateAddressCommand("Addr Reader 1", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));
        LocationView location = locationOperations.create(new CreateLocationCommand(
                null, addr.id(), "Location Reader Test 1", null, null, null, null, null, null, null, null
        ));
        Long locationId = location.id();

        // Initial state: unknown schedule, 0 intervals
        BusinessHoursScheduleView initialSchedule = businessHoursOperations.getSchedule(locationId);
        assertThat(initialSchedule.businessHoursKnown()).isFalse();
        assertThat(initialSchedule.intervals()).isEmpty();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        CountDownLatch t1PriorReadLatch = new CountDownLatch(1);
        CountDownLatch t2HoldingLockLatch = new CountDownLatch(1);
        CountDownLatch t2ReleaseLatch = new CountDownLatch(1);
        AtomicBoolean readerFinishedBeforeWriterCommit = new AtomicBoolean(false);

        Future<?> writerFuture = executor.submit(() -> {
            txTemplate.execute(status -> {
                try {
                    t1PriorReadLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Writer replaces schedule under exclusive lock (findByIdForUpdate)
                businessHoursOperations.replaceSchedule(new ReplaceBusinessHoursScheduleCommand(
                        locationId,
                        true,
                        List.of(new BusinessHoursIntervalInput(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)))
                ));

                t2HoldingLockLatch.countDown();
                try {
                    t2ReleaseLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            return null;
        });

        Future<BusinessHoursScheduleView> readerFuture = executor.submit(() -> {
            return txTemplate.execute(status -> {
                // T1 performs prior managed read: loads location into EntityManager persistence context
                LocationView priorRead = locationOperations.findById(locationId);
                assertThat(priorRead.businessHoursKnown()).isFalse();
                t1PriorReadLatch.countDown();

                try {
                    t2HoldingLockLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // T1 calls getSchedule: issues findByIdForShare, blocking behind writer's lock
                BusinessHoursScheduleView schedule = businessHoursOperations.getSchedule(locationId);
                if (t2ReleaseLatch.getCount() > 0) {
                    readerFinishedBeforeWriterCommit.set(true);
                }
                return schedule;
            });
        });

        assertThat(t1PriorReadLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(t2HoldingLockLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Deterministically verify reader T1 is blocked on PostgreSQL row lock held by writer T2
        awaitCompetingLock(java.time.Duration.ofSeconds(5));
        assertThat(readerFinishedBeforeWriterCommit.get()).isFalse();

        // Release writer to commit
        t2ReleaseLatch.countDown();
        writerFuture.get(10, TimeUnit.SECONDS);
        BusinessHoursScheduleView readerSchedule = readerFuture.get(10, TimeUnit.SECONDS);

        // Verification: reader received fresh, coherent state (businessHoursKnown=true and 1 interval)
        assertThat(readerSchedule.businessHoursKnown()).isTrue();
        assertThat(readerSchedule.intervals()).hasSize(1);
        assertThat(readerSchedule.intervals().get(0).dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);

        // Fresh transaction confirms coherent state
        BusinessHoursScheduleView freshSchedule = businessHoursOperations.getSchedule(locationId);
        assertThat(freshSchedule.businessHoursKnown()).isTrue();
        assertThat(freshSchedule.intervals()).hasSize(1);
        assertThat(freshSchedule.intervals().get(0).dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("BA15-9: Prior-managed Location reader blocked by known->unknown schedule writer returns coherent fresh state")
    void priorManagedReaderDuringKnownToUnknownWriterContentionSerializesCoherentState() throws Exception {
        var addr = addressOperations.create(new CreateAddressCommand("Addr Reader 2", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));
        LocationView location = locationOperations.create(new CreateLocationCommand(
                null, addr.id(), "Location Reader Test 2", null, null, null, null, null, null, null, null
        ));
        Long locationId = location.id();

        // Initial state: known schedule with 1 interval
        businessHoursOperations.replaceSchedule(new ReplaceBusinessHoursScheduleCommand(
                locationId,
                true,
                List.of(new BusinessHoursIntervalInput(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(16, 0)))
        ));
        BusinessHoursScheduleView initialSchedule = businessHoursOperations.getSchedule(locationId);
        assertThat(initialSchedule.businessHoursKnown()).isTrue();
        assertThat(initialSchedule.intervals()).hasSize(1);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        CountDownLatch t1PriorReadLatch = new CountDownLatch(1);
        CountDownLatch t2HoldingLockLatch = new CountDownLatch(1);
        CountDownLatch t2ReleaseLatch = new CountDownLatch(1);
        AtomicBoolean readerFinishedBeforeWriterCommit = new AtomicBoolean(false);

        Future<?> writerFuture = executor.submit(() -> {
            txTemplate.execute(status -> {
                try {
                    t1PriorReadLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Writer replaces schedule to unknown with 0 intervals under exclusive lock
                businessHoursOperations.replaceSchedule(new ReplaceBusinessHoursScheduleCommand(
                        locationId,
                        false,
                        List.of()
                ));

                t2HoldingLockLatch.countDown();
                try {
                    t2ReleaseLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            return null;
        });

        Future<BusinessHoursScheduleView> readerFuture = executor.submit(() -> {
            return txTemplate.execute(status -> {
                // T1 performs prior managed read: loads location (businessHoursKnown=true) into EntityManager
                LocationView priorRead = locationOperations.findById(locationId);
                assertThat(priorRead.businessHoursKnown()).isTrue();
                t1PriorReadLatch.countDown();

                try {
                    t2HoldingLockLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // T1 calls getSchedule: issues findByIdForShare, blocking behind writer's lock
                BusinessHoursScheduleView schedule = businessHoursOperations.getSchedule(locationId);
                if (t2ReleaseLatch.getCount() > 0) {
                    readerFinishedBeforeWriterCommit.set(true);
                }
                return schedule;
            });
        });

        assertThat(t1PriorReadLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(t2HoldingLockLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Deterministically verify reader T1 is blocked on PostgreSQL row lock held by writer T2
        awaitCompetingLock(java.time.Duration.ofSeconds(5));
        assertThat(readerFinishedBeforeWriterCommit.get()).isFalse();

        // Release writer to commit
        t2ReleaseLatch.countDown();
        writerFuture.get(10, TimeUnit.SECONDS);
        BusinessHoursScheduleView readerSchedule = readerFuture.get(10, TimeUnit.SECONDS);

        // Verification: reader received fresh, coherent state (businessHoursKnown=false and 0 intervals)
        assertThat(readerSchedule.businessHoursKnown()).isFalse();
        assertThat(readerSchedule.intervals()).isEmpty();

        // Fresh transaction confirms coherent state
        BusinessHoursScheduleView freshSchedule = businessHoursOperations.getSchedule(locationId);
        assertThat(freshSchedule.businessHoursKnown()).isFalse();
        assertThat(freshSchedule.intervals()).isEmpty();
    }
}
