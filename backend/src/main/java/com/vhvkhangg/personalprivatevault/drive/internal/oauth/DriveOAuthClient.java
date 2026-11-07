package com.vhvkhangg.personalprivatevault.drive.internal.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveCapacityExhaustedException;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveProviderException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class DriveOAuthClient implements AutoCloseable {
    public static final int MAX_RESPONSE_BYTES = 64 * 1024;
    public static final int MAX_JWKS_BYTES = 256 * 1024;
    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    public static final Duration READ_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration CALLBACK_TIMEOUT = Duration.ofSeconds(30);
    public static final Duration REVOKE_TIMEOUT = Duration.ofSeconds(8);
    public static final String REQUIRED_DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.file";
    public static final String REQUIRED_USERINFO_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private static final Set<String> ALLOWED_SCOPES = Set.of(
            "openid", "email", REQUIRED_DRIVE_SCOPE, REQUIRED_USERINFO_EMAIL_SCOPE);
    private static final Set<String> GOOGLE_ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

    /** Monotonic whole-operation budget, with at most two fixed-endpoint JWKS fetches per callback. */
    public static final class CallbackBudget {
        private final long deadline;
        private JWKSet keys;
        private int keyFetches;
        private java.util.function.Consumer<Runnable> enqueue = Runnable::run;

        public CallbackBudget(Duration duration) { deadline = System.nanoTime() + duration.toNanos(); }

        public void check() { remaining(); }

        private Duration remaining() {
            long nanos = deadline - System.nanoTime();
            if (nanos <= 0) throw timeout();
            return Duration.ofNanos(nanos);
        }
    }

    private final String clientId;
    private final String clientSecret;
    private final String tokenEndpoint;
    private final String revokeEndpoint;
    private final String jwksUri;
    private final Clock clock;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final JWKSource<SecurityContext> testKeys;
    // Four processing callbacks plus two local disconnects; no waiting work queue.
    private final Semaphore requests = new Semaphore(6);

    public DriveOAuthClient(String clientId, String clientSecret, String tokenEndpoint, String revokeEndpoint,
            String jwksUri, Clock clock, JWKSource<SecurityContext> testKeys) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenEndpoint = tokenEndpoint;
        this.revokeEndpoint = revokeEndpoint;
        this.jwksUri = jwksUri;
        this.clock = clock;
        this.testKeys = testKeys;
        httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER).version(HttpClient.Version.HTTP_1_1).build();
    }

    public CallbackBudget beginCallback() { return new CallbackBudget(CALLBACK_TIMEOUT); }

    public DriveTokenResponse exchangeAuthorizationCode(String code, String verifier, String redirect) {
        return exchangeAuthorizationCode(code, verifier, redirect, beginCallback());
    }

    public DriveTokenResponse exchangeAuthorizationCode(String code, String verifier, String redirect, CallbackBudget budget) {
        if (code == null || code.isBlank() || code.length() > 4096) {
            throw new DriveProviderException("DRIVE_INVALID_CODE", "Invalid authorization code", 400);
        }
        return tokenRequest("grant_type=authorization_code&code=" + encode(code) + "&redirect_uri=" + encode(redirect)
                + "&client_id=" + encode(clientId) + "&client_secret=" + encode(clientSecret)
                + "&code_verifier=" + encode(verifier), budget);
    }

    public DriveTokenResponse refreshAccessToken(String token) { return refreshAccessToken(token, beginCallback()); }

    public DriveTokenResponse refreshAccessToken(String token, CallbackBudget budget) {
        return refreshAccessTokenForFiles(token, budget, Runnable::run);
    }

    /** Enqueue hook lets the Drive-private file lease linearize dispatch with the existing S1 guard. */
    public DriveTokenResponse refreshAccessTokenForFiles(String token, CallbackBudget budget,
            java.util.function.Consumer<Runnable> enqueue) {
        budget.enqueue = enqueue;
        if (token == null || token.isBlank()) {
            throw new DriveProviderException("DRIVE_INVALID_REFRESH_TOKEN", "Missing refresh credential", 400);
        }
        return tokenRequest("grant_type=refresh_token&refresh_token=" + encode(token)
                + "&client_id=" + encode(clientId) + "&client_secret=" + encode(clientSecret), budget, enqueue);
    }

    public RemoteRevocationStatus revokeToken(String token) {
        if (token == null || token.isBlank()) return RemoteRevocationStatus.FAILED;
        try {
            var budget = new CallbackBudget(REVOKE_TIMEOUT);
            var response = sendBounded(revokeEndpoint, "token=" + encode(token), MAX_RESPONSE_BYTES, budget);
            return response.statusCode() == 200 ? RemoteRevocationStatus.REQUEST_ACCEPTED : RemoteRevocationStatus.FAILED;
        } catch (RuntimeException e) {
            return RemoteRevocationStatus.UNCERTAIN;
        }
    }

    public DriveIdTokenClaims validateIdToken(String token, String nonce) {
        return validateIdToken(token, nonce, beginCallback());
    }

    public DriveIdTokenClaims validateIdToken(String token, String nonce, CallbackBudget budget) {
        if (token == null || token.isBlank()) throw invalidIdentity("ID token is missing");
        try {
            budget.check();
            JWKSource<SecurityContext> source = testKeys != null ? testKeys : (selector, context) -> {
                if (budget.keys == null) fetchKeys(budget);
                var selected = selector.select(budget.keys);
                if (selected.isEmpty() && budget.keyFetches < 2) {
                    fetchKeys(budget);
                    selected = selector.select(budget.keys);
                }
                return selected;
            };
            var processor = new DefaultJWTProcessor<SecurityContext>();
            processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, source));
            // Claims use the injected UTC clock below; Nimbus still owns signature/algorithm/key verification.
            processor.setJWTClaimsSetVerifier((claims, context) -> {});
            var claims = processor.process(token, null);
            if (!GOOGLE_ISSUERS.contains(claims.getIssuer())) throw invalidIdentity("Untrusted ID token issuer");
            var audience = claims.getAudience();
            String azp = claims.getStringClaim("azp");
            if (audience == null || !audience.contains(clientId)) throw invalidIdentity("Audience does not match client ID");
            if ((azp != null && !clientId.equals(azp)) || (audience.size() > 1 && !clientId.equals(azp))) {
                throw invalidIdentity("Authorized party does not match client ID");
            }
            var now = clock.instant();
            var expiry = claims.getExpirationTime();
            if (expiry == null || !expiry.toInstant().isAfter(now)) throw invalidIdentity("ID token expired");
            if (claims.getNotBeforeTime() != null && claims.getNotBeforeTime().toInstant().isAfter(now)) {
                throw invalidIdentity("ID token is not yet valid");
            }
            String tokenNonce = claims.getStringClaim("nonce");
            if (nonce != null && !nonce.equals(tokenNonce)) throw invalidIdentity("ID token nonce mismatch");
            String sub = claims.getSubject();
            if (sub == null || sub.isBlank() || sub.length() > 255) throw invalidIdentity("Invalid ID token subject");
            String email = claims.getStringClaim("email");
            if (email != null && email.length() > 320) throw invalidIdentity("Invalid display email");
            budget.check();
            return new DriveIdTokenClaims(sub, email, expiry.toInstant(), tokenNonce);
        } catch (DriveProviderException e) {
            throw e;
        } catch (Exception e) {
            // Parsing/JOSE exceptions can contain the supplied token; never retain them as causes.
            throw invalidIdentity("ID token signature or claims verification failed");
        }
    }

    private void fetchKeys(CallbackBudget budget) {
        if (++budget.keyFetches > 2) throw invalidIdentity("Signing key acquisition limit exceeded");
        var response = sendBounded(jwksUri, null, MAX_JWKS_BYTES, budget);
        if (response.statusCode() != 200) {
            throw new DriveProviderException("DRIVE_JWKS_FAILURE", "Signing key endpoint refused the request", 502);
        }
        try {
            budget.keys = JWKSet.parse(new String(response.body(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new DriveProviderException("DRIVE_JWKS_FAILURE", "Invalid signing key response", 502);
        }
    }

    public String verifyAndCanonicalizeScopes(String scope) {
        if (scope == null || scope.isBlank()) {
            throw new DriveProviderException("DRIVE_MISSING_SCOPE", "Granted scopes are missing", 400);
        }
        Set<String> scopes = Arrays.stream(scope.strip().split("\\s+")).collect(Collectors.toSet());
        if (!ALLOWED_SCOPES.containsAll(scopes)) {
            throw new DriveProviderException("DRIVE_PROHIBITED_SCOPE", "Unapproved scope granted", 400);
        }
        if (!scopes.contains(REQUIRED_DRIVE_SCOPE)) throw missingScope(REQUIRED_DRIVE_SCOPE);
        if (!scopes.contains("openid")) throw missingScope("openid");
        if (!scopes.contains("email") && !scopes.contains(REQUIRED_USERINFO_EMAIL_SCOPE)) throw missingScope("email");
        // Google's userinfo.email alias grants the same selected capability.
        var canonical = new TreeSet<>(scopes);
        canonical.remove(REQUIRED_USERINFO_EMAIL_SCOPE);
        canonical.add("email");
        return String.join(" ", canonical);
    }

    public void validateRefreshResponse(DriveTokenResponse response, String sub, String nonce,
            String grantedScopes, CallbackBudget budget) {
        var identity = validateRefreshAuthority(response, sub, grantedScopes, budget);
        if (identity != null && identity.nonce() != null && !nonce.equals(identity.nonce())) {
            throw invalidIdentity("Refresh identity changed");
        }
    }

    /** Resource-operation refresh has no new authorization nonce; signature/audience/subject still apply. */
    public void validateFileRefreshResponse(DriveTokenResponse response, String sub,
            String grantedScopes, CallbackBudget budget) {
        validateRefreshAuthority(response, sub, grantedScopes, budget);
    }

    private DriveIdTokenClaims validateRefreshAuthority(DriveTokenResponse response, String sub,
            String grantedScopes, CallbackBudget budget) {
        requireUsableAccessToken(response);
        if (response.scope() != null && !verifyAndCanonicalizeScopes(response.scope()).equals(grantedScopes)) {
            throw invalidIdentity("Refresh scopes changed");
        }
        if (response.idToken() != null) {
            var identity = validateIdToken(response.idToken(), null, budget);
            if (!sub.equals(identity.sub())) {
                throw invalidIdentity("Refresh identity changed");
            }
            budget.check();
            return identity;
        }
        budget.check();
        return null;
    }

    private DriveTokenResponse tokenRequest(String form, CallbackBudget budget) {
        return tokenRequest(form, budget, Runnable::run);
    }

    private DriveTokenResponse tokenRequest(String form, CallbackBudget budget, java.util.function.Consumer<Runnable> enqueue) {
        var response = sendBounded(tokenEndpoint, form, MAX_RESPONSE_BYTES, budget, enqueue);
        try {
            if (response.statusCode() != 200) {
                JsonNode body;
                try { body = mapper.readTree(response.body()); } catch (Exception e) { body = null; }
                if ((response.statusCode() == 400 || response.statusCode() == 401) && body != null
                        && "invalid_grant".equals(body.path("error").asText())) {
                    throw new DriveProviderException("DRIVE_INVALID_GRANT", "Provider returned invalid_grant", 400);
                }
                throw new DriveProviderException("DRIVE_PROVIDER_REFUSAL", "Provider refused token request", 502);
            }
            JsonNode body = mapper.readTree(response.body());
            if (body == null || !body.isObject() || !body.path("access_token").isTextual()
                    || !body.path("token_type").isTextual() || !body.path("expires_in").isIntegralNumber()
                    || !body.path("expires_in").canConvertToLong()) throw invalidResponse();
            var parsed = new DriveTokenResponse(body.path("access_token").textValue(), optionalString(body, "refresh_token"),
                    optionalString(body, "id_token"), optionalString(body, "scope"),
                    body.path("expires_in").longValue(), body.path("token_type").textValue());
            requireUsableAccessToken(parsed);
            budget.check();
            return parsed;
        } catch (DriveProviderException e) {
            throw e;
        } catch (Exception e) {
            throw invalidResponse();
        }
    }

    private String optionalString(JsonNode body, String name) {
        var value = body.get(name);
        if (value == null || value.isNull()) return null;
        if (!value.isTextual() || value.textValue().isBlank()) throw invalidResponse();
        return value.textValue();
    }

    private void requireUsableAccessToken(DriveTokenResponse response) {
        if (response == null || response.accessToken() == null || response.accessToken().isBlank()
                || !"Bearer".equalsIgnoreCase(response.tokenType()) || response.expiresIn() == null
                || response.expiresIn() <= 0) throw invalidResponse();
    }

    private HttpResponse<byte[]> sendBounded(String endpoint, String form, int byteLimit, CallbackBudget budget) {
        return sendBounded(endpoint, form, byteLimit, budget, budget.enqueue);
    }

    private HttpResponse<byte[]> sendBounded(String endpoint, String form, int byteLimit, CallbackBudget budget,
            java.util.function.Consumer<Runnable> enqueue) {
        if (!requests.tryAcquire()) throw new DriveCapacityExhaustedException("Provider request capacity exhausted");
        java.util.concurrent.CompletableFuture<HttpResponse<byte[]>> future = null;
        try {
            Duration remaining = budget.remaining();
            Duration timeout = remaining.compareTo(READ_TIMEOUT) < 0 ? remaining : READ_TIMEOUT;
            var builder = HttpRequest.newBuilder(URI.create(endpoint)).timeout(timeout);
            if (form == null) builder.GET();
            else builder.header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8));
            // This body completes only at EOF. The caller's timed future covers headers AND the entire body.
            var request = builder.build();
            var admitted = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.CompletableFuture<HttpResponse<byte[]>>>();
            enqueue.accept(() -> admitted.set(httpClient.sendAsync(request, info ->
                    HttpResponse.BodySubscribers.limiting(HttpResponse.BodySubscribers.ofByteArray(), byteLimit))));
            future = java.util.Objects.requireNonNull(admitted.get());
            var response = future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
            budget.check();
            return response;
        } catch (TimeoutException e) {
            throw timeout();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw timeout();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof HttpTimeoutException) throw timeout();
            throw new DriveProviderException("DRIVE_PROVIDER_COMMUNICATION_FAILURE", "Provider response failed or exceeded its byte limit", 502);
        } catch (DriveProviderException e) {
            throw e;
        } catch (com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new DriveProviderException("DRIVE_PROVIDER_COMMUNICATION_FAILURE", "Provider request failed", 502);
        } finally {
            if (future != null && !future.isDone()) future.cancel(true);
            requests.release();
        }
    }

    private static DriveProviderException timeout() {
        return new DriveProviderException("DRIVE_PROVIDER_TIMEOUT", "Provider deadline exceeded", 504);
    }

    private DriveProviderException invalidIdentity(String message) {
        return new DriveProviderException("DRIVE_INVALID_ID_TOKEN", message, 400);
    }

    private DriveProviderException invalidResponse() {
        return new DriveProviderException("DRIVE_INVALID_TOKEN_RESPONSE", "Invalid provider token response", 502);
    }

    private DriveProviderException missingScope(String scope) {
        return new DriveProviderException("DRIVE_MISSING_SCOPE", "Missing required scope: " + scope, 400);
    }

    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }

    @Override public void close() { httpClient.shutdownNow(); }
}
