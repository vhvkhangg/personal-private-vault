package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.ChangePinCommand;
import com.vhvkhangg.personalprivatevault.authentication.session.LoginCommand;
import com.vhvkhangg.personalprivatevault.authentication.view.AuthTokensView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecretRedactionTest {

    @Test
    @DisplayName("BootstrapCommand redacts password and PIN from toString output")
    void bootstrapCommandRedactsSecrets() {
        String rawPassword = "SecretPassword123!";
        String rawPin = "654321";
        BootstrapCommand command = new BootstrapCommand("user@vault.local", "user", rawPassword, rawPin);

        String representation = command.toString();

        boolean containsPassword = representation.contains(rawPassword);
        boolean containsPin = representation.contains(rawPin);

        assertThat(containsPassword).as("Password must not leak into toString()").isFalse();
        assertThat(containsPin).as("PIN must not leak into toString()").isFalse();
        assertThat(representation).contains("password=[REDACTED]", "pin=[REDACTED]");
    }

    @Test
    @DisplayName("LoginCommand redacts password from toString output")
    void loginCommandRedactsPassword() {
        String rawPassword = "SecretPassword123!";
        LoginCommand command = new LoginCommand("user", rawPassword);

        String representation = command.toString();
        boolean containsPassword = representation.contains(rawPassword);

        assertThat(containsPassword).as("Password must not leak into toString()").isFalse();
        assertThat(representation).contains("password=[REDACTED]");
    }

    @Test
    @DisplayName("ChangePinCommand redacts current and new PIN from toString output")
    void changePinCommandRedactsPins() {
        String rawCurrentPin = "111111";
        String rawNewPin = "222222";
        ChangePinCommand command = new ChangePinCommand(rawCurrentPin, rawNewPin);

        String representation = command.toString();
        boolean containsCurrent = representation.contains(rawCurrentPin);
        boolean containsNew = representation.contains(rawNewPin);

        assertThat(containsCurrent).as("Current PIN must not leak into toString()").isFalse();
        assertThat(containsNew).as("New PIN must not leak into toString()").isFalse();
        assertThat(representation).contains("currentPin=[REDACTED]", "newPin=[REDACTED]");
    }

    @Test
    @DisplayName("AuthTokensView redacts accessToken and refreshToken from toString output")
    void authTokensViewRedactsTokens() {
        String rawAccess = "secret-access-token-jwt-value";
        String rawRefresh = "secret-refresh-token-entropy-value";
        AuthTokensView tokens = new AuthTokensView(rawAccess, rawRefresh, "Bearer", 900L);

        String representation = tokens.toString();
        boolean containsAccess = representation.contains(rawAccess);
        boolean containsRefresh = representation.contains(rawRefresh);

        assertThat(containsAccess).as("Access token must not leak into toString()").isFalse();
        assertThat(containsRefresh).as("Refresh token must not leak into toString()").isFalse();
        assertThat(representation).contains("accessToken=[REDACTED]", "refreshToken=[REDACTED]");
    }
}
