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

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
}
