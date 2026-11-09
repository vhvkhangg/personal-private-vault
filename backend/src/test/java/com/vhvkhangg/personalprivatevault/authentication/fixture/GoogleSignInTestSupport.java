package com.vhvkhangg.personalprivatevault.authentication.fixture;

import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapOperations;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.oauth.GoogleSignInProviderClient;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInProperties;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInResources;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;

/**
 * Base test support for Google Sign-In backend integration tests.
 * Manages Testcontainers PG, synthetic Google OIDC server, and transaction crypto key file.
 */
@Import(GoogleSignInTestSupport.GoogleSignInTestConfig.class)
public abstract class GoogleSignInTestSupport extends AbstractPostgresIntegrationTest {

    @TestConfiguration
    public static class GoogleSignInTestConfig {
        @Bean
        public GoogleSignInProviderClient testGoogleSignInProviderClient(
                GoogleSignInProperties properties,
                Clock clock,
                GoogleSignInResources resources
        ) {
            return new GoogleSignInProviderClient(
                    properties,
                    clock,
                    URI.create(fakeGoogleServer.getTokenUri()),
                    URI.create(fakeGoogleServer.getJwksUri()),
                    resources
            );
        }
    }

    public static final String DEFAULT_RAW_PASSWORD = "MasterVaultPassword123!";
    public static final String DEFAULT_PIN = "123456";
    public static final String DEFAULT_USERNAME = "vaultowner";
    public static final String DEFAULT_EMAIL = "owner@vault.local";

    private static final Path tempKeyFile;
    private static final FakeGoogleSignInServer fakeGoogleServer;

    static {
        try {
            byte[] keyBytes = new byte[32];
            new SecureRandom().nextBytes(keyBytes);
            tempKeyFile = Files.createTempFile("ppv-google-txn-", ".key");
            Files.write(tempKeyFile, keyBytes);
            tempKeyFile.toFile().deleteOnExit();

            fakeGoogleServer = new FakeGoogleSignInServer();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize GoogleSignInTestSupport", e);
        }
    }

    @DynamicPropertySource
    public static void configureGoogleSignInProperties(DynamicPropertyRegistry registry) {
        registry.add("ppv.security.google-signin.enabled", () -> "true");
        registry.add("ppv.security.google-signin.client-id", () -> "test-google-client-id");
        registry.add("ppv.security.google-signin.client-secret", () -> "test-google-client-secret");
        registry.add("ppv.security.google-signin.txn-key-file", () -> tempKeyFile.toAbsolutePath().toString());
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    protected BootstrapOperations bootstrapOperations;

    @Autowired(required = false)
    protected PasswordEncoder passwordEncoder;

    public static FakeGoogleSignInServer fakeGoogle() {
        return fakeGoogleServer;
    }

    protected static Path txnKeyPath() {
        return tempKeyFile;
    }

    @BeforeEach
    protected void resetGoogleTestState() {
        fakeGoogleServer.reset();
        cleanupGoogleTables();
    }

    @AfterEach
    protected void cleanupAfterEach() {
        fakeGoogleServer.reset();
        cleanupGoogleTables();
    }

    protected void cleanupGoogleTables() {
        if (jdbcTemplate != null) {
            jdbcTemplate.execute("TRUNCATE TABLE ppv_google_handoff_tickets, ppv_google_login_attempts, ppv_owner_google_identities CASCADE");
        }
    }

    protected void ensureOwnerBootstrapped() {
        if (bootstrapOperations != null && !bootstrapOperations.isBootstrapped()) {
            bootstrapOperations.bootstrap(new BootstrapCommand(
                    DEFAULT_EMAIL,
                    DEFAULT_USERNAME,
                    DEFAULT_RAW_PASSWORD,
                    DEFAULT_PIN
            ));
        }
    }

    protected void seedOwnerGoogleIdentity(String issuer, String sub) {
        ensureOwnerBootstrapped();
        jdbcTemplate.update("""
                INSERT INTO ppv_owner_google_identities (owner_id, issuer, google_sub, linked_at)
                VALUES (1, ?, ?, NOW())
                ON CONFLICT (owner_id) DO NOTHING
                """, issuer, sub);
    }

    public static String generate43CharToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
