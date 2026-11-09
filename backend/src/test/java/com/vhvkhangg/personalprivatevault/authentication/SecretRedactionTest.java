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

    @Test
    @DisplayName("Google Sign-In DTOs and internal records redact passwords, tickets, and verifiers")
    void googleSignInSecretCarriersRedacted() {
        var linkReq = new com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.GoogleSignInDtos.StartGoogleLinkRequest(
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                "SuperSecretPassword999!"
        );
        assertThat(linkReq.toString()).doesNotContain("SuperSecretPassword999!");
        assertThat(linkReq.toString()).contains("password=[REDACTED]");

        var redeemReq = new com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.GoogleSignInDtos.RedeemHandoffRequest(
                "ticket12345678901234567890123456789012345",
                "verifier1234567890123456789012345678901234"
        );
        assertThat(redeemReq.toString()).doesNotContain("ticket12345678901234567890123456789012345");
        assertThat(redeemReq.toString()).doesNotContain("verifier1234567890123456789012345678901234");
        assertThat(redeemReq.toString()).contains("ticket=[REDACTED]", "handoffVerifier=[REDACTED]");

        String rawStateSentinel = "synthetic-state-sentinel-xyz";
        String rawNonceSentinel = "synthetic-nonce-sentinel-123";
        String authUrlWithSentinels = "https://accounts.google.com/o/oauth2/v2/auth?state=" + rawStateSentinel + "&nonce=" + rawNonceSentinel;

        var startResult = new com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInService.StartResult(
                java.util.UUID.randomUUID(),
                authUrlWithSentinels,
                "bindingSecret12345678901234567890123456789",
                300
        );
        assertThat(startResult.toString()).doesNotContain("bindingSecret12345678901234567890123456789");
        assertThat(startResult.toString()).doesNotContain(rawStateSentinel);
        assertThat(startResult.toString()).doesNotContain(rawNonceSentinel);
        assertThat(startResult.toString()).contains("bindingSecret=[REDACTED]", "authorizationUrl=[REDACTED]");

        var startResp = new com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.GoogleSignInDtos.StartGoogleSignInResponse(
                java.util.UUID.randomUUID(),
                authUrlWithSentinels,
                300
        );
        assertThat(startResp.toString()).doesNotContain(rawStateSentinel);
        assertThat(startResp.toString()).doesNotContain(rawNonceSentinel);
        assertThat(startResp.toString()).contains("authorizationUrl=[REDACTED]");

        var callbackOutcome = new com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInService.CallbackOutcome(
                true,
                "ticketSecret123456789012345678901234567890",
                null
        );
        assertThat(callbackOutcome.toString()).doesNotContain("ticketSecret123456789012345678901234567890");
        assertThat(callbackOutcome.toString()).contains("ticket=[REDACTED]");

        var validatedIdToken = new com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.oauth.GoogleSignInProviderClient.ValidatedIdToken(
                "https://accounts.google.com",
                "secret-sub-12345",
                "secret-email@example.com"
        );
        assertThat(validatedIdToken.toString()).doesNotContain("secret-sub-12345");
        assertThat(validatedIdToken.toString()).doesNotContain("secret-email@example.com");
        assertThat(validatedIdToken.toString()).contains("sub=[REDACTED]", "email=[REDACTED]");

        var tokenRequest = new com.vhvkhangg.personalprivatevault.authentication.fixture.FakeGoogleSignInServer.TokenRequest(
                "code-xyz",
                "verifier-abc",
                "client-123",
                "secret-def",
                "http://localhost:8080/callback",
                "authorization_code"
        );
        assertThat(tokenRequest.toString()).doesNotContain("code-xyz");
        assertThat(tokenRequest.toString()).doesNotContain("verifier-abc");
        assertThat(tokenRequest.toString()).doesNotContain("secret-def");
        assertThat(tokenRequest.toString()).contains("code=[REDACTED]", "codeVerifier=[REDACTED]", "clientSecret=[REDACTED]");
    }
}
