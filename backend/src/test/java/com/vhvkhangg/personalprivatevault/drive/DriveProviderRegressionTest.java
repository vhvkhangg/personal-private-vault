package com.vhvkhangg.personalprivatevault.drive;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleOAuthServer;
import com.vhvkhangg.personalprivatevault.drive.internal.application.DriveConnectionService;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveProviderException;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.*;
import com.vhvkhangg.personalprivatevault.drive.internal.web.dto.DriveAuthorizeResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.*;

@Timeout(15)
class DriveProviderRegressionTest {
    FakeGoogleOAuthServer provider;
    DriveOAuthClient client;
    @BeforeEach void setup() throws Exception {
        provider = new FakeGoogleOAuthServer();
        client = new DriveOAuthClient("client", "secret", provider.getTokenEndpoint(), provider.getRevokeEndpoint(),
                provider.getJwksUri(), Clock.systemUTC(), null);
    }
    @AfterEach void close() { client.close(); provider.close(); }

    @ParameterizedTest
    @CsvSource({"token,HEADERS_ONLY", "token,PARTIAL", "token,TRICKLE", "jwks,HEADERS_ONLY",
            "jwks,PARTIAL", "jwks,TRICKLE", "revoke,HEADERS_ONLY", "revoke,PARTIAL", "revoke,TRICKLE"})
    void fullResponseDeadlineCoversHeadersAndTrickledBody(String endpoint, FakeGoogleOAuthServer.BodyMode mode) throws Exception {
        provider.bodyRelease = new CountDownLatch(1);
        provider.tokenBodyMode = endpoint.equals("token") ? mode : FakeGoogleOAuthServer.BodyMode.NORMAL;
        provider.jwksBodyMode = endpoint.equals("jwks") ? mode : FakeGoogleOAuthServer.BodyMode.NORMAL;
        provider.revokeBodyMode = endpoint.equals("revoke") ? mode : FakeGoogleOAuthServer.BodyMode.NORMAL;
        String id = provider.createSignedIdToken("U", "u@example.com", "client", "nonce", Instant.now().plusSeconds(3600));
        long began = System.nanoTime();
        if (endpoint.equals("revoke")) assertThat(client.revokeToken("synthetic-token")).isEqualTo(RemoteRevocationStatus.UNCERTAIN);
        else assertThatThrownBy(() -> {
            if (endpoint.equals("jwks")) client.validateIdToken(id, "nonce");
            else client.exchangeAuthorizationCode("code", "verifier", "http://localhost:8080/api/v1/drive/oauth/callback");
        }).isInstanceOfSatisfying(DriveProviderException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(504));
        assertThat(Duration.ofNanos(System.nanoTime() - began)).isLessThan(Duration.ofSeconds(7));
        provider.bodyRelease.countDown();
        provider.tokenBodyMode = provider.jwksBodyMode = provider.revokeBodyMode = FakeGoogleOAuthServer.BodyMode.NORMAL;
        // Cancellation releases admission; a new normal request completes immediately on the same client.
        assertThat(client.exchangeAuthorizationCode("fresh-code", "verifier", "http://localhost:8080/api/v1/drive/oauth/callback").accessToken()).isNotBlank();
    }

    @Test void everyStepUsesTheRemainingWholeCallbackBudget() {
        provider.tokenBodyMode = FakeGoogleOAuthServer.BodyMode.PARTIAL;
        provider.bodyRelease = new CountDownLatch(1);
        var budget = new DriveOAuthClient.CallbackBudget(Duration.ofMillis(200));
        long began = System.nanoTime();
        assertThatThrownBy(() -> client.exchangeAuthorizationCode("code", "v", "http://localhost:8080/api/v1/drive/oauth/callback", budget))
                .isInstanceOfSatisfying(DriveProviderException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(504));
        assertThat(Duration.ofNanos(System.nanoTime() - began)).isLessThan(Duration.ofSeconds(2));
        assertThatThrownBy(() -> client.refreshAccessToken("token", budget)).isInstanceOf(DriveProviderException.class);
        assertThat(provider.refreshTokenCount).hasValue(0);
    }

    @ParameterizedTest @CsvSource({"token,OVERSIZED", "token,REDIRECT", "token,MALFORMED",
            "jwks,OVERSIZED", "jwks,REDIRECT", "jwks,MALFORMED"})
    void byteCapsMalformedBodiesAndRedirectsFailWithoutReplay(String endpoint, FakeGoogleOAuthServer.BodyMode mode) throws Exception {
        if (endpoint.equals("token")) provider.tokenBodyMode = mode; else provider.jwksBodyMode = mode;
        String id = provider.createSignedIdToken("U", "u@example.com", "client", "n", Instant.now().plusSeconds(3600));
        assertThatThrownBy(() -> {
            if (endpoint.equals("token")) client.exchangeAuthorizationCode("code", "v", "http://localhost:8080/cb");
            else client.validateIdToken(id, "n");
        }).isInstanceOf(DriveProviderException.class).hasNoCause();
        assertThat(provider.tokenExchangeCount.get() + provider.jwksCount.get()).isEqualTo(1);
    }

    @ParameterizedTest @ValueSource(ints={401, 403, 429, 500, 503})
    void providerRefusalsNeverReplayMutation(int status) {
        provider.tokenEndpointStatus = status;
        provider.tokenEndpointError = "{\"error\":\"synthetic-secret-marker\"}";
        assertThatThrownBy(() -> client.exchangeAuthorizationCode("code", "v", "http://localhost:8080/cb"))
                .isInstanceOf(DriveProviderException.class).hasNoCause().hasMessageNotContaining("synthetic-secret-marker");
        assertThat(provider.tokenExchangeCount).hasValue(1);
        assertThat(provider.wireRequests).allSatisfy(w -> assertThat(w.query()).isNull());
    }

    @ParameterizedTest @CsvSource({"wrong,client,false", "client,wrong,false", "multiple,absent,false",
            "multiple,client,true", "client,absent,true", "client,client,true"})
    void audienceIsRequiredAndAzpCannotSubstitute(String audience, String azp, boolean valid) throws Exception {
        var claims = new JWTClaimsSet.Builder().issuer("https://accounts.google.com").subject("U")
                .audience(audience.equals("multiple") ? List.of("client", "other") : List.of(audience))
                .claim("nonce", "n").expirationTime(Date.from(Instant.now().plusSeconds(3600)));
        if (!azp.equals("absent")) claims.claim("azp", azp);
        String token = provider.sign(claims.build(), JWSAlgorithm.RS256);
        if (valid) assertThat(client.validateIdToken(token, "n").sub()).isEqualTo("U");
        else assertThatThrownBy(() -> client.validateIdToken(token, "n")).isInstanceOf(DriveProviderException.class);
    }

    @Test void signatureAlgorithmIssuerExpiryNonceAndSubjectAreVerified() throws Exception {
        var base = new JWTClaimsSet.Builder().issuer("https://accounts.google.com").subject("U")
                .audience("client").claim("nonce", "n").expirationTime(Date.from(Instant.now().plusSeconds(3600))).build();
        for (var claims : List.of(new JWTClaimsSet.Builder(base).issuer("https://attacker.example").build(),
                new JWTClaimsSet.Builder(base).subject(" ").build(), new JWTClaimsSet.Builder(base).subject("X".repeat(256)).build(),
                new JWTClaimsSet.Builder(base).expirationTime(new Date(0)).build(),
                new JWTClaimsSet.Builder(base).claim("nonce", "wrong").build())) {
            String token = provider.sign(claims, JWSAlgorithm.RS256);
            assertThatThrownBy(() -> client.validateIdToken(token, "n")).isInstanceOf(DriveProviderException.class);
        }
        String wrongAlgorithm = provider.sign(base, JWSAlgorithm.RS512);
        assertThatThrownBy(() -> client.validateIdToken(wrongAlgorithm, "n")).isInstanceOf(DriveProviderException.class);
    }

    @Test void jwksIsReusedWithinChainAndUnknownKeysFetchAtMostTwice() throws Exception {
        var budget = client.beginCallback();
        String id = provider.createSignedIdToken("U", "u@example.com", "client", "n", Instant.now().plusSeconds(3600));
        client.validateIdToken(id, "n", budget);
        client.validateRefreshResponse(new DriveTokenResponse("A", null, id, null, 3600L, "Bearer"), "U", "n",
                client.verifyAndCanonicalizeScopes(provider.forcedScopes), budget);
        assertThat(provider.jwksCount).hasValue(1);
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("unknown").build(), SignedJWT.parse(id).getJWTClaimsSet());
        jwt.sign(new RSASSASigner(provider.getRsaJwk()));
        for (int i=0; i<2; i++) assertThatThrownBy(() -> client.validateIdToken(jwt.serialize(), "n", budget)).isInstanceOf(DriveProviderException.class);
        assertThat(provider.jwksCount).hasValue(2);
    }

    @ParameterizedTest @ValueSource(strings={"https://www.googleapis.com/auth/drive.activity", "https://www.googleapis.com/auth/drive.appdata",
            "profile", "https://www.googleapis.com/auth/userinfo.profile", "unapproved"})
    void effectiveScopeAllowlistRejectsEveryUnselectedCapability(String added) {
        assertThatThrownBy(() -> client.verifyAndCanonicalizeScopes(provider.forcedScopes + " " + added)).isInstanceOf(DriveProviderException.class);
    }

    @Test void secretCarriersAndProviderFailuresHaveSafeDiagnosticStrings() {
        String marker = "SYNTHETIC_SECRET_MARKER";
        var attempt = new OAuthAttemptManager(Clock.systemUTC()).createAttempt((short) 1, "1", "http://localhost:8080/cb");
        var response = new DriveAuthorizeResponse(1, marker, Instant.now());
        assertThat(new DriveTokenResponse(marker, marker, marker, marker, 1L, marker).toString()).doesNotContain(marker);
        assertThat(new DriveIdTokenClaims(marker, marker, Instant.now(), marker).toString()).doesNotContain(marker);
        assertThat(new DriveConnectionService.AuthorizeResult(response, marker, marker, false).toString()).doesNotContain(marker);
        assertThat(response.toString()).doesNotContain(marker);
        assertThat(attempt.toString()).doesNotContain(attempt.plainCookieProof());
        provider.tokenBodyMode = FakeGoogleOAuthServer.BodyMode.MALFORMED;
        assertThatThrownBy(() -> client.exchangeAuthorizationCode(marker, marker, "http://localhost:8080/cb"))
                .isInstanceOf(DriveProviderException.class).hasNoCause().hasMessageNotContaining(marker);
    }
}
