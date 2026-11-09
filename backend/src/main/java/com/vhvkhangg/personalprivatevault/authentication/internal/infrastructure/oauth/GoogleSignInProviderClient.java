package com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.oauth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInException;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInService;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInProperties;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInResources;
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestParametersConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.DefaultMapOAuth2AccessTokenResponseConverter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class GoogleSignInProviderClient {

    public static final String CANONICAL_ISSUER = "https://accounts.google.com";
    public static final String LEGACY_ISSUER = "accounts.google.com";
    public static final URI PRODUCTION_TOKEN_URI = URI.create("https://oauth2.googleapis.com/token");
    public static final URI PRODUCTION_JWKS_URI = URI.create("https://www.googleapis.com/oauth2/v3/certs");
    public static final long OVERALL_BUDGET_NANOS = TimeUnit.SECONDS.toNanos(10);

    private static final Set<String> ALLOWED_SCOPES = Set.of(
            "openid",
            "email",
            "https://www.googleapis.com/auth/userinfo.email"
    );

    private final GoogleSignInProperties properties;
    private final Clock clock;
    private final URI tokenUri;
    private final URI jwksUri;
    private final GoogleSignInResources resources;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    private final Object jwksLock = new Object();
    private volatile JWKSet cachedJwks;
    private volatile Instant jwksFetchTime = Instant.EPOCH;
    private final AtomicLong lastJwksRefreshAttempt = new AtomicLong(0);

    public record ValidatedIdToken(String issuer, String sub, String email) {
        @Override
        public String toString() {
            return "ValidatedIdToken[issuer=" + issuer + ", sub=[REDACTED], email=[REDACTED]]";
        }
    }

    public GoogleSignInProviderClient(
            GoogleSignInProperties properties,
            Clock clock,
            GoogleSignInResources resources
    ) {
        this(
                properties,
                clock,
                PRODUCTION_TOKEN_URI,
                PRODUCTION_JWKS_URI,
                resources
        );
    }

    // Controlled constructor for test fixtures only
    public GoogleSignInProviderClient(
            GoogleSignInProperties properties,
            Clock clock,
            URI tokenUri,
            URI jwksUri,
            GoogleSignInResources resources
    ) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.tokenUri = Objects.requireNonNull(tokenUri, "tokenUri must not be null");
        this.jwksUri = Objects.requireNonNull(jwksUri, "jwksUri must not be null");
        this.resources = Objects.requireNonNull(resources, "resources must not be null");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    private static long remainingNanos(long startNano, long budgetNanos) {
        long elapsed = System.nanoTime() - startNano;
        long remaining = budgetNanos - elapsed;
        if (remaining <= 0) {
            throw GoogleSignInException.providerFailure();
        }
        return remaining;
    }

    public ValidatedIdToken exchangeCodeAndValidate(
            String code,
            String codeVerifier,
            String expectedNonceHash,
            String redirectUri
    ) {
        long startNano = System.nanoTime();
        long budgetNanos = OVERALL_BUDGET_NANOS;
        try (var slot = resources.acquireProvider()) {
            String idTokenString = exchangeAuthorizationCode(code, codeVerifier, redirectUri, startNano, budgetNanos);
            return validateIdToken(idTokenString, expectedNonceHash, startNano, budgetNanos);
        } catch (GoogleSignInException e) {
            throw e;
        } catch (Exception e) {
            throw GoogleSignInException.providerFailure();
        }
    }

    private String exchangeAuthorizationCode(
            String code,
            String codeVerifier,
            String redirectUri,
            long startNano,
            long budgetNanos
    ) {
        long remaining = remainingNanos(startNano, budgetNanos);

        try {
            ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                    .clientId(properties.getClientId())
                    .clientSecret(properties.getClientSecret())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri(redirectUri)
                    .scope("openid", "email")
                    .authorizationUri(GoogleSignInService.DEFAULT_AUTH_ENDPOINT)
                    .tokenUri(tokenUri.toString())
                    .jwkSetUri(jwksUri.toString())
                    .clientName("Google")
                    .build();

            OAuth2AuthorizationRequest authzRequest = OAuth2AuthorizationRequest.authorizationCode()
                    .clientId(properties.getClientId())
                    .authorizationUri(GoogleSignInService.DEFAULT_AUTH_ENDPOINT)
                    .redirectUri(redirectUri)
                    .scopes(Set.of("openid", "email"))
                    .state("ignored")
                    .attributes(attrs -> attrs.put(PkceParameterNames.CODE_VERIFIER, codeVerifier))
                    .build();

            OAuth2AuthorizationResponse authzResponse = OAuth2AuthorizationResponse.success(code)
                    .redirectUri(redirectUri)
                    .state("ignored")
                    .build();

            OAuth2AuthorizationExchange exchange = new OAuth2AuthorizationExchange(authzRequest, authzResponse);
            OAuth2AuthorizationCodeGrantRequest grantRequest = new OAuth2AuthorizationCodeGrantRequest(registration, exchange);

            DefaultOAuth2TokenRequestParametersConverter<OAuth2AuthorizationCodeGrantRequest> parametersConverter =
                    new DefaultOAuth2TokenRequestParametersConverter<>();
            MultiValueMap<String, String> formParams = parametersConverter.convert(grantRequest);

            StringBuilder formBuilder = new StringBuilder();
            if (formParams != null) {
                for (var entry : formParams.entrySet()) {
                    for (String value : entry.getValue()) {
                        if (formBuilder.length() > 0) {
                            formBuilder.append("&");
                        }
                        formBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                                .append("=")
                                .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                    }
                }
            }

            Duration requestTimeout = Duration.ofNanos(Math.min(remaining, TimeUnit.SECONDS.toNanos(10)));
            HttpRequest request = HttpRequest.newBuilder(tokenUri)
                    .timeout(requestTimeout)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(formBuilder.toString()))
                    .build();

            CompletableFuture<HttpResponse<byte[]>> future = httpClient.sendAsync(
                    request,
                    responseInfo -> HttpResponse.BodySubscribers.limiting(
                            HttpResponse.BodySubscribers.ofByteArray(),
                            64 * 1024 + 1
                    )
            );

            HttpResponse<byte[]> response;
            try {
                response = future.get(remaining, TimeUnit.NANOSECONDS);
            } catch (TimeoutException te) {
                future.cancel(true);
                throw GoogleSignInException.providerFailure();
            } catch (Exception e) {
                future.cancel(true);
                throw GoogleSignInException.providerFailure();
            }

            if (response.statusCode() != 200) {
                throw GoogleSignInException.providerFailure();
            }

            byte[] body = response.body();
            if (body.length > 64 * 1024) {
                throw GoogleSignInException.providerFailure();
            }

            JsonNode root = mapper.readTree(body);

            // Scope validation: if scope is present in token response, ensure it is textual and strictly within allowlist
            JsonNode scopeNode = root.get("scope");
            if (scopeNode != null && !scopeNode.isNull()) {
                if (!scopeNode.isTextual()) {
                    throw GoogleSignInException.providerFailure();
                }
                String grantedScope = scopeNode.asText().trim();
                if (grantedScope.isEmpty()) {
                    throw GoogleSignInException.providerFailure();
                }
                String[] tokens = grantedScope.split("\\s+");
                for (String s : tokens) {
                    if (!ALLOWED_SCOPES.contains(s)) {
                        throw GoogleSignInException.providerFailure();
                    }
                }
            }

            // Convert to Spring Security BOM-managed OAuth2AccessTokenResponse
            Map<String, Object> tokenResponseMap = mapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            DefaultMapOAuth2AccessTokenResponseConverter tokenResponseConverter = new DefaultMapOAuth2AccessTokenResponseConverter();
            OAuth2AccessTokenResponse tokenResponse = tokenResponseConverter.convert(tokenResponseMap);

            Object idTokenObj = tokenResponse.getAdditionalParameters().get("id_token");
            if (!(idTokenObj instanceof String idTokenString) || idTokenString.isBlank() || idTokenString.length() > 16 * 1024) {
                throw GoogleSignInException.providerFailure();
            }

            return idTokenString;
        } catch (GoogleSignInException e) {
            throw e;
        } catch (Exception e) {
            throw GoogleSignInException.providerFailure();
        }
    }

    public ValidatedIdToken validateIdToken(String idTokenString, String expectedNonceHash) {
        return validateIdToken(idTokenString, expectedNonceHash, System.nanoTime(), OVERALL_BUDGET_NANOS);
    }

    public ValidatedIdToken validateIdToken(
            String idTokenString,
            String expectedNonceHash,
            long startNano,
            long budgetNanos
    ) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(idTokenString);

            if (!JWSAlgorithm.RS256.equals(signedJwt.getHeader().getAlgorithm())) {
                throw GoogleSignInException.providerFailure();
            }

            String kid = signedJwt.getHeader().getKeyID();
            if (kid == null || kid.isBlank()) {
                throw GoogleSignInException.providerFailure();
            }

            // Check jku/x5u headers (must not be present/redirecting)
            if (signedJwt.getHeader().getJWKURL() != null || signedJwt.getHeader().getX509CertURL() != null) {
                throw GoogleSignInException.providerFailure();
            }

            RSAKey rsaKey = getRsaKey(kid, startNano, budgetNanos);
            if (rsaKey == null) {
                throw GoogleSignInException.providerFailure();
            }

            RSASSAVerifier verifier = new RSASSAVerifier(rsaKey);
            if (!signedJwt.verify(verifier)) {
                throw GoogleSignInException.providerFailure();
            }

            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            Instant now = clock.instant();

            // Validate Issuer
            String issuer = claims.getIssuer();
            if (!CANONICAL_ISSUER.equals(issuer) && !LEGACY_ISSUER.equals(issuer)) {
                throw GoogleSignInException.providerFailure();
            }

            // Validate Audience
            List<String> audience = claims.getAudience();
            if (audience == null || !audience.contains(properties.getClientId())) {
                throw GoogleSignInException.providerFailure();
            }

            // Validate Authorized Party (azp):
            // If azp is present, it must equal clientId.
            // If multiple audiences are present, azp is required and must equal clientId.
            String azp = claims.getStringClaim("azp");
            if (azp != null && !properties.getClientId().equals(azp)) {
                throw GoogleSignInException.providerFailure();
            }
            if (audience.size() > 1 && (azp == null || !properties.getClientId().equals(azp))) {
                throw GoogleSignInException.providerFailure();
            }

            // Validate timestamps with 60s clock skew
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.toInstant().isBefore(now.minusSeconds(60))) {
                throw GoogleSignInException.providerFailure();
            }

            Date nbf = claims.getNotBeforeTime();
            if (nbf != null && nbf.toInstant().isAfter(now.plusSeconds(60))) {
                throw GoogleSignInException.providerFailure();
            }

            // Issued-at (iat) is required by OIDC and local contract
            Date iat = claims.getIssueTime();
            if (iat == null || iat.toInstant().isAfter(now.plusSeconds(60))) {
                throw GoogleSignInException.providerFailure();
            }

            // Validate Nonce against stored nonce hash
            String nonce = claims.getStringClaim("nonce");
            if (nonce == null || !sha256Hex(nonce).equals(expectedNonceHash)) {
                throw GoogleSignInException.providerFailure();
            }

            // Validate Subject
            String sub = claims.getSubject();
            if (sub == null || sub.isBlank() || sub.length() > 255 || !sub.matches("^[\\x21-\\x7E]+$")) {
                throw GoogleSignInException.providerFailure();
            }

            // Ensure entire execution did not exceed the overall budget
            remainingNanos(startNano, budgetNanos);

            String email = claims.getStringClaim("email");
            return new ValidatedIdToken(CANONICAL_ISSUER, sub, email);
        } catch (GoogleSignInException e) {
            throw e;
        } catch (Exception e) {
            throw GoogleSignInException.providerFailure();
        }
    }

    private RSAKey getRsaKey(String kid, long startNano, long budgetNanos) {
        Instant now = clock.instant();

        // 1. Check existing cached keys under lock
        synchronized (jwksLock) {
            if (cachedJwks != null && Duration.between(jwksFetchTime, now).toMinutes() < 5) {
                JWK key = cachedJwks.getKeyByKeyId(kid);
                if (key instanceof RSAKey rsaKey) {
                    return rsaKey;
                }
            }

            // Cooldown check applies to BOTH cold and populated caches: at most once per 60 seconds
            long nowMs = clock.millis();
            long lastAttempt = lastJwksRefreshAttempt.get();
            if ((nowMs - lastAttempt) < 60_000L) {
                return null;
            }
            lastJwksRefreshAttempt.set(nowMs);
        }

        // 2. Network I/O is performed OUTSIDE the synchronized block
        long remaining;
        try {
            remaining = remainingNanos(startNano, budgetNanos);
        } catch (Exception e) {
            return null;
        }

        Duration requestTimeout = Duration.ofNanos(Math.min(remaining, TimeUnit.SECONDS.toNanos(10)));
        try {
            HttpRequest request = HttpRequest.newBuilder(jwksUri)
                    .timeout(requestTimeout)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            CompletableFuture<HttpResponse<byte[]>> future = httpClient.sendAsync(
                    request,
                    responseInfo -> HttpResponse.BodySubscribers.limiting(
                            HttpResponse.BodySubscribers.ofByteArray(),
                            64 * 1024 + 1
                    )
            );

            HttpResponse<byte[]> response;
            try {
                response = future.get(remaining, TimeUnit.NANOSECONDS);
            } catch (Exception e) {
                future.cancel(true);
                return null;
            }

            if (response.statusCode() != 200) {
                return null;
            }

            byte[] body = response.body();
            if (body.length > 64 * 1024) {
                return null;
            }

            JWKSet jwkSet = JWKSet.parse(new String(body, StandardCharsets.UTF_8));
            if (jwkSet.getKeys().size() > 8) {
                return null;
            }

            synchronized (jwksLock) {
                this.cachedJwks = jwkSet;
                this.jwksFetchTime = clock.instant();
                JWK key = jwkSet.getKeyByKeyId(kid);
                if (key instanceof RSAKey rsaKey) {
                    return rsaKey;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.US_ASCII));
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
