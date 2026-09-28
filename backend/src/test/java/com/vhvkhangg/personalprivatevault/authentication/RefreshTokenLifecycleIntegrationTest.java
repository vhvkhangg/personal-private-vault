package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapOperations;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.token.TokenGenerator;
import com.vhvkhangg.personalprivatevault.authentication.session.InvalidRefreshTokenException;
import com.vhvkhangg.personalprivatevault.authentication.session.LoginCommand;
import com.vhvkhangg.personalprivatevault.authentication.session.SessionOperations;
import com.vhvkhangg.personalprivatevault.authentication.view.AuthTokensView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.RefreshTokenRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenLifecycleIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private BootstrapOperations bootstrapOperations;

    @Autowired
    private SessionOperations sessionOperations;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM app_users");

        bootstrapOperations.bootstrap(new BootstrapCommand(
                "owner@vault.local",
                "owner",
                "SuperSecretPassword123!",
                "123456"
        ));
    }

    @Test
    @DisplayName("Hashes refresh token at rest, rotates safely, and tracks replacement")
    void hashesAtRestAndRotatesSuccessfully() {
        AuthTokensView loginTokens = sessionOperations.login(new LoginCommand("owner", "SuperSecretPassword123!"));
        String rawRefresh1 = loginTokens.refreshToken();

        // Verify hash at rest
        String expectedHash = tokenGenerator.hashToken(rawRefresh1);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM refresh_tokens WHERE token_hash = ?", Integer.class, expectedHash);
        assertThat(count).isEqualTo(1);

        // Verify raw token is NOT in database
        Integer rawCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM refresh_tokens WHERE token_hash = ?", Integer.class, rawRefresh1);
        assertThat(rawCount).isZero();

        // Rotate token
        AuthTokensView rotatedTokens = sessionOperations.rotate(rawRefresh1);
        String rawRefresh2 = rotatedTokens.refreshToken();

        boolean hasNonBlankSuccessor = rawRefresh2 != null && !rawRefresh2.isBlank();
        boolean distinctFromPredecessor = !Objects.equals(rawRefresh2, rawRefresh1);
        assertThat(hasNonBlankSuccessor).as("Rotated refresh token must be non-blank").isTrue();
        assertThat(distinctFromPredecessor).as("Rotated refresh token must differ from predecessor").isTrue();

        boolean hasNonBlankAccessToken = rotatedTokens.accessToken() != null && !rotatedTokens.accessToken().isBlank();
        assertThat(hasNonBlankAccessToken).as("Rotated access token must be non-blank").isTrue();

        // Verify predecessor state in DB: revoked_at IS NOT NULL and replaced_by_token_id is successor ID
        Long predecessorReplacedBy = jdbcTemplate.queryForObject(
                "SELECT replaced_by_token_id FROM refresh_tokens WHERE token_hash = ?", Long.class, expectedHash);
        assertThat(predecessorReplacedBy).isNotNull();

        String successorHash = tokenGenerator.hashToken(rawRefresh2);
        Long successorId = jdbcTemplate.queryForObject(
                "SELECT id FROM refresh_tokens WHERE token_hash = ?", Long.class, successorHash);
        assertThat(predecessorReplacedBy).isEqualTo(successorId);
    }

    @Test
    @DisplayName("Fails closed on replay: rotating an already replaced token is rejected")
    void rejectsReplayedToken() {
        AuthTokensView loginTokens = sessionOperations.login(new LoginCommand("owner", "SuperSecretPassword123!"));
        String rawRefresh1 = loginTokens.refreshToken();

        // First rotation succeeds
        sessionOperations.rotate(rawRefresh1);

        // Second rotation with the same (now replaced) token fails closed
        assertThatThrownBy(() -> sessionOperations.rotate(rawRefresh1))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("Fails closed when rotating an expired refresh token")
    void rejectsExpiredToken() {
        AuthTokensView loginTokens = sessionOperations.login(new LoginCommand("owner", "SuperSecretPassword123!"));
        String rawRefresh = loginTokens.refreshToken();

        // Expire token in database
        String tokenHash = tokenGenerator.hashToken(rawRefresh);
        jdbcTemplate.update("UPDATE refresh_tokens SET expires_at = now() - interval '1 hour' WHERE token_hash = ?", tokenHash);

        assertThatThrownBy(() -> sessionOperations.rotate(rawRefresh))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("Fails closed when rotating a token expired at the current database timestamp boundary")
    void rejectsTokenAtDatabaseCurrentTimestampBoundary() {
        AuthTokensView loginTokens = sessionOperations.login(new LoginCommand("owner", "SuperSecretPassword123!"));
        String rawRefresh = loginTokens.refreshToken();

        String tokenHash = tokenGenerator.hashToken(rawRefresh);
        jdbcTemplate.update("UPDATE refresh_tokens SET expires_at = now() WHERE token_hash = ?", tokenHash);

        assertThatThrownBy(() -> sessionOperations.rotate(rawRefresh))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("Fails closed when rotating a revoked refresh token")
    void rejectsRevokedToken() {
        AuthTokensView loginTokens = sessionOperations.login(new LoginCommand("owner", "SuperSecretPassword123!"));
        String rawRefresh = loginTokens.refreshToken();

        sessionOperations.revoke(rawRefresh);

        assertThatThrownBy(() -> sessionOperations.rotate(rawRefresh))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("Revocation is idempotent and does not fail on repeated calls or unknown tokens")
    void revocationIsIdempotent() {
        AuthTokensView loginTokens = sessionOperations.login(new LoginCommand("owner", "SuperSecretPassword123!"));
        String rawRefresh = loginTokens.refreshToken();

        sessionOperations.revoke(rawRefresh);
        // Repeated revocation
        sessionOperations.revoke(rawRefresh);
        // Unknown token revocation
        sessionOperations.revoke("non-existent-refresh-token");

        String tokenHash = tokenGenerator.hashToken(rawRefresh);
        Boolean isRevoked = jdbcTemplate.queryForObject(
                "SELECT revoked_at IS NOT NULL FROM refresh_tokens WHERE token_hash = ?", Boolean.class, tokenHash);
        assertThat(isRevoked).isTrue();
    }

    @Test
    @DisplayName("Concurrent rotation with same token yields exactly one successor due to pessimistic locking")
    void concurrentRotationYieldsAtMostOneSuccessor() throws Exception {
        AuthTokensView loginTokens = sessionOperations.login(new LoginCommand("owner", "SuperSecretPassword123!"));
        String rawRefresh = loginTokens.refreshToken();

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        try {
            Future<?> f1 = executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    sessionOperations.rotate(rawRefresh);
                    successCount.incrementAndGet();
                } catch (InvalidRefreshTokenException ex) {
                    failureCount.incrementAndGet();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            Future<?> f2 = executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    sessionOperations.rotate(rawRefresh);
                    successCount.incrementAndGet();
                } catch (InvalidRefreshTokenException ex) {
                    failureCount.incrementAndGet();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            readyLatch.await();
            startLatch.countDown();

            f1.get();
            f2.get();

            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failureCount.get()).isEqualTo(1);

            // Exactly 2 tokens in DB: initial + 1 successor
            Integer totalTokens = jdbcTemplate.queryForObject("SELECT count(*) FROM refresh_tokens", Integer.class);
            assertThat(totalTokens).isEqualTo(2);
        } finally {
            executor.shutdown();
        }
    }

    private void awaitCompetingLock(Duration timeout) throws Exception {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM pg_locks l " +
                    "JOIN pg_stat_activity a ON l.pid = a.pid " +
                    "WHERE NOT l.granted " +
                    "  AND a.pid != pg_backend_pid() " +
                    "  AND a.query ILIKE '%refresh_tokens%'",
                    Integer.class
            );
            if (count != null && count > 0) {
                return;
            }
            Thread.sleep(10);
        }
        throw new AssertionError("Timed out waiting for competing transaction to reach PostgreSQL row lock on refresh_tokens");
    }

    @Test
    @DisplayName("Concurrent rotation-wins: replacement link is preserved when revocation races with rotation")
    void concurrentRotationWinsOverRevocationPreservingReplacementLink() throws Exception {
        AuthTokensView loginTokens = sessionOperations.login(new LoginCommand("owner", "SuperSecretPassword123!"));
        String rawRefresh = loginTokens.refreshToken();
        String tokenHash = tokenGenerator.hashToken(rawRefresh);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch rotationLockedPredecessor = new CountDownLatch(1);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: Acquires row lock on predecessor and performs rotation
            Future<AuthTokensView> rotateFuture = executor.submit(() -> txTemplate.execute(status -> {
                // 1. Explicitly acquire pessimistic lock on the predecessor row
                refreshTokenRepository.findByTokenHashWithLock(tokenHash).orElseThrow();
                rotationLockedPredecessor.countDown();

                // 2. Wait until revoke thread is confirmed actively blocked in PostgreSQL on the row lock
                try {
                    awaitCompetingLock(Duration.ofSeconds(5));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // 3. Complete rotation within this transaction
                return sessionOperations.rotate(rawRefresh);
            }));

            // Thread 2: Concurrently calls revoke on the same raw token
            Future<?> revokeFuture = executor.submit(() -> {
                try {
                    // Wait until rotation thread has locked predecessor
                    boolean locked = rotationLockedPredecessor.await(5, TimeUnit.SECONDS);
                    assertThat(locked).isTrue();
                    // This call will block on PostgreSQL row lock until Thread 1 commits
                    sessionOperations.revoke(rawRefresh);
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            AuthTokensView rotatedTokens = rotateFuture.get(10, TimeUnit.SECONDS);
            revokeFuture.get(10, TimeUnit.SECONDS);

            assertThat(rotatedTokens).isNotNull();
            String successorRaw = rotatedTokens.refreshToken();

            // Non-sensitive property assertions: do not print raw token strings in diagnostic failures
            boolean hasNonBlankSuccessor = successorRaw != null && !successorRaw.isBlank();
            boolean distinctFromPredecessor = !Objects.equals(successorRaw, rawRefresh);
            assertThat(hasNonBlankSuccessor).as("Rotated refresh token must be non-blank").isTrue();
            assertThat(distinctFromPredecessor).as("Rotated refresh token must differ from predecessor").isTrue();

            // Verify database state:
            // 1. Predecessor is revoked
            Boolean predecessorRevoked = jdbcTemplate.queryForObject(
                    "SELECT revoked_at IS NOT NULL FROM refresh_tokens WHERE token_hash = ?", Boolean.class, tokenHash);
            assertThat(predecessorRevoked).as("Predecessor must be revoked").isTrue();

            // 2. Predecessor's replaced_by_token_id was NOT erased by concurrent revoke and points to the successor
            String successorHash = tokenGenerator.hashToken(successorRaw);
            Long successorId = jdbcTemplate.queryForObject(
                    "SELECT id FROM refresh_tokens WHERE token_hash = ?", Long.class, successorHash);
            Long predecessorReplacedBy = jdbcTemplate.queryForObject(
                    "SELECT replaced_by_token_id FROM refresh_tokens WHERE token_hash = ?", Long.class, tokenHash);
            assertThat(predecessorReplacedBy).as("Predecessor replaced_by_token_id must match successor id").isNotNull().isEqualTo(successorId);

            // 3. Exactly 2 tokens in DB: predecessor + 1 successor
            Integer totalTokens = jdbcTemplate.queryForObject("SELECT count(*) FROM refresh_tokens", Integer.class);
            assertThat(totalTokens).as("Must have exactly 2 refresh tokens").isEqualTo(2);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Concurrent revocation-wins: rotation fails closed when revocation acquires lock first")
    void concurrentRevocationWinsOverRotationCausingRotationToFailClosed() throws Exception {
        AuthTokensView loginTokens = sessionOperations.login(new LoginCommand("owner", "SuperSecretPassword123!"));
        String rawRefresh = loginTokens.refreshToken();
        String tokenHash = tokenGenerator.hashToken(rawRefresh);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch revokeLockedPredecessor = new CountDownLatch(1);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: Acquires row lock on predecessor and revokes it
            Future<?> revokeFuture = executor.submit(() -> txTemplate.execute(status -> {
                // 1. Explicitly acquire pessimistic lock on the predecessor row
                refreshTokenRepository.findByTokenHashWithLock(tokenHash).orElseThrow();
                revokeLockedPredecessor.countDown();

                // 2. Wait until rotate thread is confirmed actively blocked in PostgreSQL on the row lock
                try {
                    awaitCompetingLock(Duration.ofSeconds(5));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // 3. Revoke predecessor within this transaction
                sessionOperations.revoke(rawRefresh);
                return null;
            }));

            // Thread 2: Concurrently calls rotate on the same raw token
            Future<AuthTokensView> rotateFuture = executor.submit(() -> {
                boolean locked = revokeLockedPredecessor.await(5, TimeUnit.SECONDS);
                assertThat(locked).isTrue();
                // This call will block on PostgreSQL row lock until Thread 1 commits
                return sessionOperations.rotate(rawRefresh);
            });

            revokeFuture.get(10, TimeUnit.SECONDS);

            // Rotate must fail closed because predecessor was already revoked
            assertThatThrownBy(() -> {
                try {
                    rotateFuture.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException ex) {
                    throw ex.getCause();
                }
            }).isInstanceOf(InvalidRefreshTokenException.class);

            // Verify database state:
            // 1. Predecessor is revoked
            Boolean predecessorRevoked = jdbcTemplate.queryForObject(
                    "SELECT revoked_at IS NOT NULL FROM refresh_tokens WHERE token_hash = ?", Boolean.class, tokenHash);
            assertThat(predecessorRevoked).as("Predecessor must be revoked").isTrue();

            // 2. Predecessor was never replaced
            Long predecessorReplacedBy = jdbcTemplate.queryForObject(
                    "SELECT replaced_by_token_id FROM refresh_tokens WHERE token_hash = ?", Long.class, tokenHash);
            assertThat(predecessorReplacedBy).as("Predecessor replaced_by_token_id must remain null").isNull();

            // 3. Exactly 1 token in DB (no successor created)
            Integer totalTokens = jdbcTemplate.queryForObject("SELECT count(*) FROM refresh_tokens", Integer.class);
            assertThat(totalTokens).as("Must have exactly 1 refresh token (no successor created)").isEqualTo(1);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
