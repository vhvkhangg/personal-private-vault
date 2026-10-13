package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapOperations;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.ChangePinCommand;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.InvalidPinException;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.PinVerificationException;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.PrivatePinOperations;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.UnauthenticatedAccessException;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SBA-005 regression tests:
 * Verifies atomic current-PIN verification and PIN update under pessimistic row lock on app_users singleton,
 * rejection of superseded/stale current PINs in cached persistence contexts,
 * deterministic contention between concurrent PIN changes where exactly one winner succeeds,
 * preservation of username/email/password hash, and format/unbootstrapped/auth constraints.
 */
class PrivatePinConcurrencyTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private BootstrapOperations bootstrapOperations;

    @Autowired
    private PrivatePinOperations privatePinOperations;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM app_users");
    }

    private void bootstrapFixture(String pin) {
        bootstrapOperations.bootstrap(new BootstrapCommand(
                "owner@vault.local",
                "vaultowner",
                "VaultPassword123!",
                pin
        ));
    }

    private void setAuthenticatedContext(String userId) {
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userId, "password", AuthorityUtils.NO_AUTHORITIES);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private TransactionTemplate requiresNewTx() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return tx;
    }

    private int getCurrentBackendPid() {
        Integer pid = jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class);
        return pid != null ? pid : 0;
    }

    private void awaitCompetitorBlockedBy(int holderPid, AtomicInteger waiterPidRef, String expectedTable, Duration timeout) {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            int waiterPid = waiterPidRef.get();
            if (waiterPid > 0) {
                List<Integer> blockedPids = jdbcTemplate.query(
                        """
                        SELECT a.pid FROM pg_locks l
                        JOIN pg_stat_activity a ON a.pid = l.pid
                        WHERE NOT l.granted
                          AND a.pid = ?
                          AND ? = ANY(pg_blocking_pids(a.pid))
                          AND a.wait_event_type = 'Lock'
                          AND a.query ILIKE ?
                        """,
                        (rs, rowNum) -> rs.getInt("pid"),
                        waiterPid,
                        holderPid,
                        "%" + expectedTable + "%"
                );
                if (!blockedPids.isEmpty()) {
                    return;
                }
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Lock observer interrupted", e);
            }
        }
        throw new AssertionError("Timed out waiting for waiter PID " + waiterPidRef.get()
                + " on table " + expectedTable + " to be blocked by holder PID " + holderPid);
    }

    @Test
    @DisplayName("SBA-005: Cached user/current-PIN proof followed by competing committed change rejects stale old PIN")
    void cachedUserCompetingCommittedChangeRejectsStaleOldPin() throws Exception {
        bootstrapFixture("123456");
        setAuthenticatedContext("1");

        Map<String, Object> baseline = jdbcTemplate.queryForMap(
                "SELECT email, username, password_hash, created_at FROM app_users WHERE id = 1");

        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        try {
            assertThatThrownBy(() -> requiresNewTx().executeWithoutResult(status -> {
                // Verify initial PIN in outer transaction (loads user in persistence context)
                assertThat(privatePinOperations.verifyPin("123456")).isTrue();

                // Competing transaction in separate thread changes PIN to 654321 and commits
                try {
                    singleThreadExecutor.submit(() -> {
                        setAuthenticatedContext("1");
                        try {
                            requiresNewTx().executeWithoutResult(s2 ->
                                    privatePinOperations.changePin(new ChangePinCommand("123456", "654321")));
                        } finally {
                            SecurityContextHolder.clearContext();
                        }
                    }).get(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // Outer tx attempts change using superseded old PIN 123456 -> must fail authoritatively
                privatePinOperations.changePin(new ChangePinCommand("123456", "234567"));
            })).isInstanceOf(PinVerificationException.class)
                    .hasMessageContaining("Current PIN is incorrect");

            // Persisted PIN in DB is the winner 654321, NOT superseded 234567 or initial 123456
            assertThat(privatePinOperations.verifyPin("654321")).isTrue();
            assertThat(privatePinOperations.verifyPin("123456")).isFalse();
            assertThat(privatePinOperations.verifyPin("234567")).isFalse();

            // Unrelated credentials match baseline exactly
            Map<String, Object> finalUser = jdbcTemplate.queryForMap(
                    "SELECT email, username, password_hash, created_at, pin_hash FROM app_users WHERE id = 1");
            assertThat(finalUser.get("email")).isEqualTo(baseline.get("email"));
            assertThat(finalUser.get("username")).isEqualTo(baseline.get("username"));
            assertThat(finalUser.get("password_hash")).isEqualTo(baseline.get("password_hash"));
            assertThat(finalUser.get("created_at")).isEqualTo(baseline.get("created_at"));
            assertThat(passwordEncoder.matches("654321", (String) finalUser.get("pin_hash"))).isTrue();
        } finally {
            SecurityContextHolder.clearContext();
            singleThreadExecutor.shutdown();
            assertThat(singleThreadExecutor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    @DisplayName("SBA-005: Two competing transactions with same old PIN and distinct new PINs serialize on lock, exactly one succeeds")
    void competingPinChangesSerializeOnLockExactlyOneSucceeds() throws Exception {
        bootstrapFixture("111111");

        Map<String, Object> baseline = jdbcTemplate.queryForMap(
                "SELECT email, username, password_hash, created_at FROM app_users WHERE id = 1");

        CountDownLatch t1LockAcquired = new CountDownLatch(1);
        CountDownLatch t1Proceed = new CountDownLatch(1);
        AtomicInteger holderPidRef = new AtomicInteger();
        AtomicInteger waiterPidRef = new AtomicInteger();
        AtomicBoolean t1Success = new AtomicBoolean(false);
        AtomicBoolean t2Success = new AtomicBoolean(false);
        AtomicBoolean t2FailedWithPinVerification = new AtomicBoolean(false);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> f1 = executor.submit(() -> {
                setAuthenticatedContext("1");
                try {
                    requiresNewTx().executeWithoutResult(status -> {
                        holderPidRef.set(getCurrentBackendPid());
                        privatePinOperations.changePin(new ChangePinCommand("111111", "222222"));
                        t1LockAcquired.countDown();
                        try {
                            if (!t1Proceed.await(5, TimeUnit.SECONDS)) {
                                throw new AssertionError("Holder timed out waiting for t1Proceed");
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new AssertionError("Holder thread was interrupted", e);
                        }
                    });
                    t1Success.set(true);
                } finally {
                    SecurityContextHolder.clearContext();
                }
            });

            assertThat(t1LockAcquired.await(5, TimeUnit.SECONDS)).isTrue();

            Future<?> f2 = executor.submit(() -> {
                setAuthenticatedContext("1");
                try {
                    requiresNewTx().executeWithoutResult(status -> {
                        waiterPidRef.set(getCurrentBackendPid());
                        privatePinOperations.changePin(new ChangePinCommand("111111", "333333"));
                    });
                    t2Success.set(true);
                } catch (PinVerificationException ex) {
                    t2FailedWithPinVerification.set(true);
                } finally {
                    SecurityContextHolder.clearContext();
                }
            });

            // Observe T2 blocked specifically by holderPidRef
            awaitCompetitorBlockedBy(holderPidRef.get(), waiterPidRef, "app_users", Duration.ofSeconds(5));
            t1Proceed.countDown();

            f1.get(5, TimeUnit.SECONDS);
            f2.get(5, TimeUnit.SECONDS);

            // Exactly one succeeds, the competing loser fails with PinVerificationException
            assertThat(t1Success.get()).isTrue();
            assertThat(t2Success.get()).isFalse();
            assertThat(t2FailedWithPinVerification.get()).isTrue();

            // Winner PIN 222222 matches; loser PIN 333333 and old PIN 111111 fail
            setAuthenticatedContext("1");
            try {
                assertThat(privatePinOperations.verifyPin("222222")).isTrue();
                assertThat(privatePinOperations.verifyPin("111111")).isFalse();
                assertThat(privatePinOperations.verifyPin("333333")).isFalse();
            } finally {
                SecurityContextHolder.clearContext();
            }

            // Unrelated credentials match baseline exactly
            Map<String, Object> userRow = jdbcTemplate.queryForMap(
                    "SELECT email, username, password_hash, created_at, pin_hash FROM app_users WHERE id = 1");
            assertThat(userRow.get("email")).isEqualTo(baseline.get("email"));
            assertThat(userRow.get("username")).isEqualTo(baseline.get("username"));
            assertThat(userRow.get("password_hash")).isEqualTo(baseline.get("password_hash"));
            assertThat(userRow.get("created_at")).isEqualTo(baseline.get("created_at"));
            assertThat(passwordEncoder.matches("222222", (String) userRow.get("pin_hash"))).isTrue();
        } finally {
            t1Proceed.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    @DisplayName("SBA-005: Competing PIN changes in reverse winner order serialize with exactly one winner")
    void competingPinChangesReverseWinnerOrder() throws Exception {
        bootstrapFixture("444444");

        Map<String, Object> baseline = jdbcTemplate.queryForMap(
                "SELECT email, username, password_hash, created_at FROM app_users WHERE id = 1");

        CountDownLatch t2LockAcquired = new CountDownLatch(1);
        CountDownLatch t2Proceed = new CountDownLatch(1);
        AtomicInteger holderPidRef = new AtomicInteger();
        AtomicInteger waiterPidRef = new AtomicInteger();
        AtomicBoolean t1Success = new AtomicBoolean(false);
        AtomicBoolean t2Success = new AtomicBoolean(false);
        AtomicBoolean t1FailedWithPinVerification = new AtomicBoolean(false);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> f2 = executor.submit(() -> {
                setAuthenticatedContext("1");
                try {
                    requiresNewTx().executeWithoutResult(status -> {
                        holderPidRef.set(getCurrentBackendPid());
                        privatePinOperations.changePin(new ChangePinCommand("444444", "666666"));
                        t2LockAcquired.countDown();
                        try {
                            if (!t2Proceed.await(5, TimeUnit.SECONDS)) {
                                throw new AssertionError("Holder timed out waiting for t2Proceed");
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new AssertionError("Holder thread was interrupted", e);
                        }
                    });
                    t2Success.set(true);
                } finally {
                    SecurityContextHolder.clearContext();
                }
            });

            assertThat(t2LockAcquired.await(5, TimeUnit.SECONDS)).isTrue();

            Future<?> f1 = executor.submit(() -> {
                setAuthenticatedContext("1");
                try {
                    requiresNewTx().executeWithoutResult(status -> {
                        waiterPidRef.set(getCurrentBackendPid());
                        privatePinOperations.changePin(new ChangePinCommand("444444", "555555"));
                    });
                    t1Success.set(true);
                } catch (PinVerificationException ex) {
                    t1FailedWithPinVerification.set(true);
                } finally {
                    SecurityContextHolder.clearContext();
                }
            });

            awaitCompetitorBlockedBy(holderPidRef.get(), waiterPidRef, "app_users", Duration.ofSeconds(5));
            t2Proceed.countDown();

            f2.get(5, TimeUnit.SECONDS);
            f1.get(5, TimeUnit.SECONDS);

            assertThat(t2Success.get()).isTrue();
            assertThat(t1Success.get()).isFalse();
            assertThat(t1FailedWithPinVerification.get()).isTrue();

            setAuthenticatedContext("1");
            try {
                assertThat(privatePinOperations.verifyPin("666666")).isTrue();
                assertThat(privatePinOperations.verifyPin("444444")).isFalse();
                assertThat(privatePinOperations.verifyPin("555555")).isFalse();
            } finally {
                SecurityContextHolder.clearContext();
            }

            // Unrelated credentials match baseline exactly
            Map<String, Object> userRow = jdbcTemplate.queryForMap(
                    "SELECT email, username, password_hash, created_at, pin_hash FROM app_users WHERE id = 1");
            assertThat(userRow.get("email")).isEqualTo(baseline.get("email"));
            assertThat(userRow.get("username")).isEqualTo(baseline.get("username"));
            assertThat(userRow.get("password_hash")).isEqualTo(baseline.get("password_hash"));
            assertThat(userRow.get("created_at")).isEqualTo(baseline.get("created_at"));
            assertThat(passwordEncoder.matches("666666", (String) userRow.get("pin_hash"))).isTrue();
        } finally {
            t2Proceed.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    @DisplayName("SBA-005: Wrong current PIN fails with PinVerificationException and leaves PIN unchanged")
    void wrongCurrentPinFailsAndLeavesPinUnchanged() {
        bootstrapFixture("123456");
        setAuthenticatedContext("1");

        assertThatThrownBy(() -> privatePinOperations.changePin(new ChangePinCommand("999999", "654321")))
                .isInstanceOf(PinVerificationException.class)
                .hasMessage("Current PIN is incorrect");

        assertThat(privatePinOperations.verifyPin("123456")).isTrue();
        assertThat(privatePinOperations.verifyPin("654321")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12345", "1234567", "abcdef", "12 456", "12345\n"})
    @DisplayName("SBA-005: Invalid PIN format fails with InvalidPinException before query/lock")
    void invalidPinFormatFails(String invalidPin) {
        bootstrapFixture("123456");
        setAuthenticatedContext("1");

        assertThatThrownBy(() -> privatePinOperations.changePin(new ChangePinCommand("123456", invalidPin)))
                .isInstanceOf(InvalidPinException.class);

        assertThatThrownBy(() -> privatePinOperations.changePin(new ChangePinCommand(invalidPin, "654321")))
                .isInstanceOf(InvalidPinException.class);
    }

    @Test
    @DisplayName("SBA-005: Unbootstrapped database throws IllegalStateException")
    void unbootstrappedThrowsException() {
        setAuthenticatedContext("1");

        assertThatThrownBy(() -> privatePinOperations.verifyPin("123456"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not bootstrapped");

        assertThatThrownBy(() -> privatePinOperations.changePin(new ChangePinCommand("123456", "654321")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not bootstrapped");
    }

    @Test
    @DisplayName("SBA-005: Anonymous or unauthenticated access throws UnauthenticatedAccessException")
    void unauthenticatedAccessThrowsException() {
        bootstrapFixture("123456");

        // No authentication
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> privatePinOperations.verifyPin("123456"))
                .isInstanceOf(UnauthenticatedAccessException.class);

        assertThatThrownBy(() -> privatePinOperations.changePin(new ChangePinCommand("123456", "654321")))
                .isInstanceOf(UnauthenticatedAccessException.class);

        // Anonymous authentication
        AnonymousAuthenticationToken anon = new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext().setAuthentication(anon);

        assertThatThrownBy(() -> privatePinOperations.verifyPin("123456"))
                .isInstanceOf(UnauthenticatedAccessException.class);

        assertThatThrownBy(() -> privatePinOperations.changePin(new ChangePinCommand("123456", "654321")))
                .isInstanceOf(UnauthenticatedAccessException.class);
    }
}
