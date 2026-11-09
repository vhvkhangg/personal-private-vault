package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInService;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.oauth.GoogleSignInProviderClient;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInConfiguration;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInProperties;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInResources;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInTransactionCrypto;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.controller.GoogleSignInController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleSignInConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(GoogleSignInConfiguration.class)
            .withBean(Clock.class, Clock::systemUTC);

    @Test
    @DisplayName("A-01: When ppv.security.google-signin.enabled is false (or absent), no Google beans are registered")
    void disabledContextRegistersNoGoogleBeans() {
        contextRunner.withPropertyValues("ppv.security.google-signin.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(GoogleSignInConfiguration.class);
                    assertThat(context).doesNotHaveBean(GoogleSignInResources.class);
                    assertThat(context).doesNotHaveBean(GoogleSignInTransactionCrypto.class);
                    assertThat(context).doesNotHaveBean(GoogleSignInProviderClient.class);
                    assertThat(context).doesNotHaveBean(GoogleSignInService.class);
                    assertThat(context).doesNotHaveBean(GoogleSignInController.class);
                });

        // Default absent property
        new ApplicationContextRunner()
                .withUserConfiguration(GoogleSignInConfiguration.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(GoogleSignInConfiguration.class);
                    assertThat(context).doesNotHaveBean(GoogleSignInResources.class);
                });
    }

    @Test
    @DisplayName("A-01: When ppv.security.google-signin.enabled is true and key is valid, registers infrastructure beans")
    void enabledContextRegistersGoogleBeans() throws Exception {
        Path keyFile = Files.createTempFile("test-google-key-", ".bin");
        byte[] key32 = new byte[32];
        new SecureRandom().nextBytes(key32);
        Files.write(keyFile, key32);
        keyFile.toFile().deleteOnExit();

        contextRunner.withPropertyValues(
                "ppv.security.google-signin.enabled=true",
                "ppv.security.google-signin.client-id=test-client-id",
                "ppv.security.google-signin.client-secret=test-client-secret",
                "ppv.security.google-signin.txn-key-file=" + keyFile.toAbsolutePath()
        ).run(context -> {
            assertThat(context).hasSingleBean(GoogleSignInConfiguration.class);
            assertThat(context).hasSingleBean(GoogleSignInProperties.class);
            assertThat(context).hasSingleBean(GoogleSignInResources.class);
            assertThat(context).hasSingleBean(GoogleSignInTransactionCrypto.class);
            assertThat(context).hasSingleBean(GoogleSignInProviderClient.class);

            GoogleSignInProperties props = context.getBean(GoogleSignInProperties.class);
            assertThat(props.isEnabled()).isTrue();
            assertThat(props.getClientId()).isEqualTo("test-client-id");
            assertThat(props.getClientSecret()).isEqualTo("test-client-secret");
        });
    }

    @Test
    @DisplayName("AUTH-FR-01: Fail-fast when enabled=true but credentials are blank")
    void blankCredentialsFailWhenEnabled() throws Exception {
        Path keyFile = Files.createTempFile("test-google-key-", ".bin");
        Files.write(keyFile, new byte[32]);
        keyFile.toFile().deleteOnExit();

        // Missing client-id
        contextRunner.withPropertyValues(
                "ppv.security.google-signin.enabled=true",
                "ppv.security.google-signin.client-id=",
                "ppv.security.google-signin.client-secret=test-secret",
                "ppv.security.google-signin.txn-key-file=" + keyFile.toAbsolutePath()
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalStateException.class)
                    .hasRootCauseMessage("ppv.security.google-signin.client-id must not be blank when enabled");
        });

        // Missing client-secret
        contextRunner.withPropertyValues(
                "ppv.security.google-signin.enabled=true",
                "ppv.security.google-signin.client-id=test-id",
                "ppv.security.google-signin.client-secret=",
                "ppv.security.google-signin.txn-key-file=" + keyFile.toAbsolutePath()
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalStateException.class)
                    .hasRootCauseMessage("ppv.security.google-signin.client-secret must not be blank when enabled");
        });
    }

    @Test
    @DisplayName("Startup validation fails if txnKeyFile is missing, nonexistent, or not 32 bytes")
    void invalidKeyFileFailsInitialization() throws Exception {
        GoogleSignInProperties props = new GoogleSignInProperties();
        props.setEnabled(true);

        // Missing key file
        props.setTxnKeyFile("");
        assertThatThrownBy(() -> new GoogleSignInTransactionCrypto(props))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be configured");

        // Non-existent key file
        props.setTxnKeyFile("non-existent-" + System.currentTimeMillis() + ".key");
        assertThatThrownBy(() -> new GoogleSignInTransactionCrypto(props))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not exist");

        // Wrong size (16 bytes instead of 32)
        Path shortKey = Files.createTempFile("short-key-", ".bin");
        Files.write(shortKey, new byte[16]);
        shortKey.toFile().deleteOnExit();
        props.setTxnKeyFile(shortKey.toAbsolutePath().toString());

        assertThatThrownBy(() -> new GoogleSignInTransactionCrypto(props))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 32 bytes");
    }
}
