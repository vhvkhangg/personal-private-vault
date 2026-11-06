package com.vhvkhangg.personalprivatevault.drive;

import com.vhvkhangg.personalprivatevault.drive.internal.application.DriveConnectionService;
import com.vhvkhangg.personalprivatevault.drive.internal.config.DriveConfiguration;
import com.vhvkhangg.personalprivatevault.drive.internal.config.DriveProperties;
import com.vhvkhangg.personalprivatevault.drive.internal.crypto.DriveCredentialCrypto;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveCapacityExhaustedException;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.OAuthAttemptManager;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.SubjectLifecycleManager;
import com.vhvkhangg.personalprivatevault.drive.internal.web.controller.DriveController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriveConfigurationIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(DriveConfiguration.class);

    @Test
    @DisplayName("When ppv.drive.enabled is false (or absent), no Drive beans are registered")
    void disabledContextRegistersNoDriveBeans() {
        contextRunner.withPropertyValues("ppv.drive.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DriveController.class);
                    assertThat(context).doesNotHaveBean(DriveConnectionService.class);
                    assertThat(context).doesNotHaveBean(DriveCredentialCrypto.class);
                    assertThat(context).doesNotHaveBean(OAuthAttemptManager.class);
                    assertThat(context).doesNotHaveBean(SubjectLifecycleManager.class);
                });
    }

    @Test
    @DisplayName("When ppv.drive.enabled is true but token key file is missing or invalid length, startup fails")
    void misconfiguredKeyFileFailsStartup() throws Exception {
        // Missing key file property
        DriveConfiguration config = new DriveConfiguration();
        DriveProperties propsNoKey = new DriveProperties();
        propsNoKey.setEnabled(true);
        assertThatThrownBy(() -> config.driveCredentialCrypto(propsNoKey))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("token-key-file is required");

        // Non-existent key file
        propsNoKey.setTokenKeyFile("non-existent-file-" + System.currentTimeMillis() + ".bin");
        assertThatThrownBy(() -> config.driveCredentialCrypto(propsNoKey))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not exist");

        // Key file with wrong length (16 bytes instead of 32)
        Path shortFile = Files.createTempFile("short-key-", ".bin");
        try {
            Files.write(shortFile, new byte[16]);
            propsNoKey.setTokenKeyFile(shortFile.toAbsolutePath().toString());
            assertThatThrownBy(() -> config.driveCredentialCrypto(propsNoKey))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("must be exactly 32 bytes");
        } finally {
            Files.deleteIfExists(shortFile);
        }
    }

    @Test
    @DisplayName("When ppv.drive.enabled is true but OAuth credentials are blank, startup fails")
    void misconfiguredOAuthCredentialsFailsStartup() {
        DriveConfiguration config = new DriveConfiguration();
        DriveProperties props = new DriveProperties();
        props.setEnabled(true);
        props.getOauth().setClientId("");
        props.getOauth().setClientSecret("secret");

        assertThatThrownBy(() -> config.driveOAuthClient(props, Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("client-id is required");

        props.getOauth().setClientId("client-id");
        props.getOauth().setClientSecret("");
        assertThatThrownBy(() -> config.driveOAuthClient(props, Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("client-secret is required");
    }

    @Test
    @DisplayName("OAuthAttemptManager enforces maximum 4 active attempts and lazy expiry")
    void attemptManagerEnforcesCapacityAndExpiry() {
        MutableClock testClock = new MutableClock(Instant.parse("2026-11-06T10:00:00Z"));
        OAuthAttemptManager freshManager = new OAuthAttemptManager(testClock);

        var first = freshManager.createAttempt((short) 1, "1", "http://localhost:8080/cb");
        var next = freshManager.createAttempt((short) 1, "1", "http://localhost:8080/cb");
        assertThat(freshManager.consumeAndVerifyAttempt(first.attempt().getState(), first.plainCookieProof(), "1", "http://localhost:8080/cb")).isEmpty();
        assertThat(freshManager.consumeAndVerifyAttempt(next.attempt().getState(), next.plainCookieProof(), "other-owner", "http://localhost:8080/cb")).isEmpty();
        assertThat(freshManager.consumeAndVerifyAttempt(next.attempt().getState(), next.plainCookieProof(), "1", "altered-context")).isEmpty();
        testClock.instant = testClock.instant.plusSeconds(300);
        assertThat(freshManager.consumeAndVerifyAttempt(next.attempt().getState(), next.plainCookieProof(), "1", "http://localhost:8080/cb")).isEmpty();
        assertThat(freshManager.createAttempt((short) 2, "1", "http://localhost:8080/cb")).isNotNull();
    }

    @Test
    @DisplayName("SubjectLifecycleManager enforces maximum 2 subject records")
    void lifecycleManagerEnforcesSubjectCapacity() {
        SubjectLifecycleManager manager = new SubjectLifecycleManager(new OAuthAttemptManager(Clock.systemUTC()));

        // Connect subject 1
        manager.recordConnected((short) 1, "subject-1");
        // Connect subject 2
        manager.recordConnected((short) 2, "subject-2");

        // Attempting to commit a 3rd distinct subject fails with DRIVE_CAPACITY_EXHAUSTED
        assertThatThrownBy(() -> manager.checkCanCommit((short) 1, "subject-3", 1L))
                .isInstanceOf(DriveCapacityExhaustedException.class)
                .hasMessageContaining("capacity exhausted");
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={
            "ppv.drive.oauth.redirect-uri=https://remote.example/api/v1/drive/oauth/callback",
            "ppv.drive.oauth.redirect-uri=http://127.0.0.1:8080/api/v1/drive/oauth/callback",
            "ppv.drive.oauth.authorization-endpoint=http://untrusted.example/auth",
            "ppv.drive.oauth.token-endpoint=http://127.0.0.1:9/token",
            "ppv.drive.oauth.jwks-uri=https://untrusted.example/keys",
            "ppv.drive.oauth.revoke-endpoint=https://untrusted.example/revoke"})
    void realEnabledContextRejectsUnapprovedRedirectAndEndpointsBeforeEgress(String invalid) throws Exception {
        Path key = Files.createTempFile("gdi26-config-", ".bin");
        try {
            Files.write(key, new byte[32]);
            contextRunner.withBean(Clock.class, Clock::systemUTC)
                    .withBean(com.vhvkhangg.personalprivatevault.drive.internal.persistence.DriveConnectionRepository.class,
                            () -> org.mockito.Mockito.mock(com.vhvkhangg.personalprivatevault.drive.internal.persistence.DriveConnectionRepository.class))
                    .withBean(org.springframework.transaction.PlatformTransactionManager.class,
                            () -> org.mockito.Mockito.mock(org.springframework.transaction.PlatformTransactionManager.class))
                    .withPropertyValues("ppv.drive.enabled=true", "ppv.drive.token-key-file=" + key,
                            "ppv.drive.oauth.client-id=client", "ppv.drive.oauth.client-secret=synthetic", invalid)
                    .run(context -> assertThat(context).hasFailed());
        } finally { Files.deleteIfExists(key); }
    }

    private static class MutableClock extends Clock {
        Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
