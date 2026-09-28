package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapOperations;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.UserAlreadyBootstrappedException;
import com.vhvkhangg.personalprivatevault.authentication.session.InvalidCredentialsException;
import com.vhvkhangg.personalprivatevault.authentication.session.LoginCommand;
import com.vhvkhangg.personalprivatevault.authentication.session.SessionOperations;
import com.vhvkhangg.personalprivatevault.authentication.view.AppUserView;
import com.vhvkhangg.personalprivatevault.authentication.view.AuthTokensView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticationBootstrapIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private BootstrapOperations bootstrapOperations;

    @Autowired
    private SessionOperations sessionOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM app_users");
    }

    @Test
    @DisplayName("Initializes singleton user, stores hashes at rest, and prevents duplicate bootstrap")
    void bootstrapsSingletonUserSafely() {
        assertThat(bootstrapOperations.isBootstrapped()).isFalse();

        BootstrapCommand command = new BootstrapCommand(
                "  Owner@Vault.Local  ",
                "  VaultOwner  ",
                "SecureMasterPassword123!",
                "123456"
        );

        AppUserView userView = bootstrapOperations.bootstrap(command);

        assertThat(bootstrapOperations.isBootstrapped()).isTrue();
        assertThat(userView.id()).isEqualTo((short) 1);
        assertThat(userView.email()).isEqualTo("Owner@Vault.Local");
        assertThat(userView.username()).isEqualTo("VaultOwner");
        assertThat(userView.createdAt()).isNotNull();

        // Verify database state: only hashes, no plaintext
        String passwordHash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM app_users WHERE id = 1", String.class);
        String pinHash = jdbcTemplate.queryForObject(
                "SELECT pin_hash FROM app_users WHERE id = 1", String.class);

        assertThat(passwordHash).isNotBlank().doesNotContain("SecureMasterPassword123!");
        assertThat(pinHash).isNotBlank().doesNotContain("123456");

        // Repeat bootstrap attempt fails with stable conflict outcome
        BootstrapCommand repeatCommand = new BootstrapCommand(
                "attacker@vault.local",
                "attacker",
                "AnotherPassword123!",
                "654321"
        );

        assertThatThrownBy(() -> bootstrapOperations.bootstrap(repeatCommand))
                .isInstanceOf(UserAlreadyBootstrappedException.class)
                .hasMessageContaining("already bootstrapped");

        // Confirm credentials were NOT overwritten
        String unchangedEmail = jdbcTemplate.queryForObject(
                "SELECT email FROM app_users WHERE id = 1", String.class);
        assertThat(unchangedEmail).isEqualTo("Owner@Vault.Local");
    }

    @Test
    @DisplayName("Concurrent bootstrap race produces exactly one winner and one stable conflict without poisoning context")
    void concurrentBootstrapRaceResolvesCleanly() throws Exception {
        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        try {
            Future<?> f1 = executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    bootstrapOperations.bootstrap(new BootstrapCommand(
                            "user1@vault.local", "user1", "MasterPassword123!", "111111"));
                    successCount.incrementAndGet();
                } catch (UserAlreadyBootstrappedException ex) {
                    conflictCount.incrementAndGet();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            Future<?> f2 = executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    bootstrapOperations.bootstrap(new BootstrapCommand(
                            "user2@vault.local", "user2", "MasterPassword123!", "222222"));
                    successCount.incrementAndGet();
                } catch (UserAlreadyBootstrappedException ex) {
                    conflictCount.incrementAndGet();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            readyLatch.await();
            startLatch.countDown();

            f1.get();
            f2.get();

            assertThat(successCount.get()).isEqualTo(1);
            assertThat(conflictCount.get()).isEqualTo(1);

            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM app_users", Integer.class);
            assertThat(count).isEqualTo(1);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("Enforces PostgreSQL singleton check constraint: chk_app_users_singleton_id")
    void enforcesSingletonIdConstraint() {
        assertThatThrownBy(() -> jdbcTemplate.execute("""
                INSERT INTO app_users (id, email, username, password_hash, pin_hash)
                VALUES (2, 'user2@vault.local', 'user2', 'hash', 'hash')
                """))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_app_users_singleton_id");
    }

    @Test
    @DisplayName("Case-insensitive login succeeds with username or email; bad credentials fail with generic error")
    void caseInsensitiveLoginAndGenericFailure() {
        bootstrapOperations.bootstrap(new BootstrapCommand(
                "Owner@Vault.Local",
                "VaultOwner",
                "SuperSecretPassword123!",
                "123456"
        ));

        // Login with lowercase username
        AuthTokensView tokens1 = sessionOperations.login(new LoginCommand("vaultowner", "SuperSecretPassword123!"));
        boolean hasAccess1 = tokens1.accessToken() != null && !tokens1.accessToken().isBlank();
        boolean hasRefresh1 = tokens1.refreshToken() != null && !tokens1.refreshToken().isBlank();
        assertThat(hasAccess1).as("Access token must be non-blank").isTrue();
        assertThat(hasRefresh1).as("Refresh token must be non-blank").isTrue();
        assertThat(tokens1.tokenType()).isEqualTo("Bearer");
        assertThat(tokens1.expiresIn()).isEqualTo(900L); // 15m in seconds

        // Login with mixed case email
        AuthTokensView tokens2 = sessionOperations.login(new LoginCommand("oWnEr@vAuLt.lOcAl", "SuperSecretPassword123!"));
        boolean hasAccess2 = tokens2.accessToken() != null && !tokens2.accessToken().isBlank();
        boolean hasRefresh2 = tokens2.refreshToken() != null && !tokens2.refreshToken().isBlank();
        assertThat(hasAccess2).as("Access token must be non-blank").isTrue();
        assertThat(hasRefresh2).as("Refresh token must be non-blank").isTrue();

        // Login with wrong password
        assertThatThrownBy(() -> sessionOperations.login(new LoginCommand("vaultowner", "WrongPassword123!")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");

        // Login with non-existent identifier
        assertThatThrownBy(() -> sessionOperations.login(new LoginCommand("nonexistent@vault.local", "SuperSecretPassword123!")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
    }
}
