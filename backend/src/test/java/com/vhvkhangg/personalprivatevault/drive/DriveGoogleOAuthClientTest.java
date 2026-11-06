package com.vhvkhangg.personalprivatevault.drive;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleOAuthServer;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveProviderException;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.DriveIdTokenClaims;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.DriveOAuthClient;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.DriveTokenResponse;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.RemoteRevocationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriveGoogleOAuthClientTest {

    private FakeGoogleOAuthServer fakeServer;
    private DriveOAuthClient client;
    private final String clientId = "test-client-id.apps.googleusercontent.com";
    private final String clientSecret = "test-client-secret-123";

    @BeforeEach
    void setUp() throws Exception {
        fakeServer = new FakeGoogleOAuthServer();
        client = new DriveOAuthClient(
                clientId,
                clientSecret,
                fakeServer.getTokenEndpoint(),
                fakeServer.getRevokeEndpoint(),
                fakeServer.getJwksUri(),
                Clock.systemUTC(),
                new ImmutableJWKSet<>(new JWKSet(fakeServer.getPublicJwk()))
        );
    }

    @AfterEach
    void tearDown() {
        if (fakeServer != null) {
            fakeServer.close();
        }
    }

    @Test
    @DisplayName("Valid ID token is verified successfully and claims are extracted")
    void validIdTokenVerificationSuccess() throws Exception {
        Instant expiresAt = Instant.now().plusSeconds(3600);
        String idToken = fakeServer.createSignedIdToken("sub-9999", "test@example.com", clientId, "nonce-xyz", expiresAt);

        DriveIdTokenClaims claims = client.validateIdToken(idToken, "nonce-xyz");
        assertThat(claims.sub()).isEqualTo("sub-9999");
        assertThat(claims.email()).isEqualTo("test@example.com");
        assertThat(claims.nonce()).isEqualTo("nonce-xyz");
    }

    @Test
    @DisplayName("ID token signed by an untrusted key fails verification")
    void idTokenWithUntrustedKeyFails() throws Exception {
        RSAKey otherKey = new RSAKeyGenerator(2048).keyID("other-key").generate();
        FakeGoogleOAuthServer otherServer = new FakeGoogleOAuthServer();
        String idToken = otherServer.createSignedIdToken("sub-9999", "test@example.com", clientId, "nonce-xyz", Instant.now().plusSeconds(3600));
        otherServer.close();

        assertThatThrownBy(() -> client.validateIdToken(idToken, "nonce-xyz"))
                .isInstanceOf(DriveProviderException.class)
                .hasMessageContaining("ID token signature or claims verification failed");
    }

    @Test
    @DisplayName("ID token with wrong audience, wrong nonce, or expired timestamp fails verification")
    void idTokenWithInvalidClaimsFails() throws Exception {
        // Wrong audience
        String idTokenWrongAud = fakeServer.createSignedIdToken("sub-9999", "test@example.com", "wrong-aud", "nonce-xyz", Instant.now().plusSeconds(3600));
        assertThatThrownBy(() -> client.validateIdToken(idTokenWrongAud, "nonce-xyz"))
                .isInstanceOf(DriveProviderException.class)
                .hasMessageContaining("Audience does not match");

        // Wrong nonce
        String idTokenWrongNonce = fakeServer.createSignedIdToken("sub-9999", "test@example.com", clientId, "different-nonce", Instant.now().plusSeconds(3600));
        assertThatThrownBy(() -> client.validateIdToken(idTokenWrongNonce, "nonce-xyz"))
                .isInstanceOf(DriveProviderException.class)
                .hasMessageContaining("nonce mismatch");

        // Expired
        String expiredToken = fakeServer.createSignedIdToken("sub-9999", "test@example.com", clientId, "nonce-xyz", Instant.now().minusSeconds(10));
        assertThatThrownBy(() -> client.validateIdToken(expiredToken, "nonce-xyz"))
                .isInstanceOf(DriveProviderException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Scope verification accepts exact drive.file + openid + email and rejects missing or broad drive scopes")
    void scopeVerificationRules() {
        // Valid exact scopes
        String canonical = client.verifyAndCanonicalizeScopes("https://www.googleapis.com/auth/drive.file openid email");
        assertThat(canonical).isEqualTo("email https://www.googleapis.com/auth/drive.file openid");

        // Valid with userinfo.email
        String canonical2 = client.verifyAndCanonicalizeScopes("https://www.googleapis.com/auth/drive.file openid https://www.googleapis.com/auth/userinfo.email");
        assertThat(canonical2).contains("https://www.googleapis.com/auth/drive.file", "openid", "email");

        // Missing drive.file
        assertThatThrownBy(() -> client.verifyAndCanonicalizeScopes("openid email"))
                .isInstanceOf(DriveProviderException.class)
                .hasMessageContaining("Missing required scope: https://www.googleapis.com/auth/drive.file");

        // Missing openid
        assertThatThrownBy(() -> client.verifyAndCanonicalizeScopes("https://www.googleapis.com/auth/drive.file email"))
                .isInstanceOf(DriveProviderException.class)
                .hasMessageContaining("Missing required scope: openid");

        // Missing email
        assertThatThrownBy(() -> client.verifyAndCanonicalizeScopes("https://www.googleapis.com/auth/drive.file openid"))
                .isInstanceOf(DriveProviderException.class)
                .hasMessageContaining("Missing required scope: email");

        // Prohibited broad drive scope
        assertThatThrownBy(() -> client.verifyAndCanonicalizeScopes("https://www.googleapis.com/auth/drive.file openid email https://www.googleapis.com/auth/drive"))
                .isInstanceOf(DriveProviderException.class)
                .hasMessageContaining("Unapproved scope granted");

        assertThatThrownBy(() -> client.verifyAndCanonicalizeScopes("https://www.googleapis.com/auth/drive.file openid email https://www.googleapis.com/auth/drive.readonly"))
                .isInstanceOf(DriveProviderException.class)
                .hasMessageContaining("Unapproved scope granted");
    }

    @Test
    @DisplayName("exchangeAuthorizationCode and refreshAccessToken return tokens from provider")
    void codeExchangeAndRefreshSuccess() {
        DriveTokenResponse response = client.exchangeAuthorizationCode("test-code-123", "verifier-abc", "http://localhost:8080/callback");
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.idToken()).isNotBlank();

        DriveTokenResponse refreshed = client.refreshAccessToken(response.refreshToken());
        assertThat(refreshed.accessToken()).isNotBlank();
        assertThat(refreshed.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("Revoke endpoint returns REQUEST_ACCEPTED on 200 and FAILED on non-200")
    void revokeTokenOutcomes() {
        RemoteRevocationStatus status = client.revokeToken("valid-token-to-revoke");
        assertThat(status).isEqualTo(RemoteRevocationStatus.REQUEST_ACCEPTED);

        fakeServer.revokeEndpointStatus = 400;
        RemoteRevocationStatus failedStatus = client.revokeToken("failing-token");
        assertThat(failedStatus).isEqualTo(RemoteRevocationStatus.FAILED);
    }
}
