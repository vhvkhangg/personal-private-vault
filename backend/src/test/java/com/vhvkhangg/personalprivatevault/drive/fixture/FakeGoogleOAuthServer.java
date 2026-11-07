package com.vhvkhangg.personalprivatevault.drive.fixture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Synthetic HTTP provider: credentials belong to their issued subject/project, never mutable request defaults. */
public class FakeGoogleOAuthServer implements AutoCloseable {
    public enum BodyMode { NORMAL, HEADERS_ONLY, PARTIAL, TRICKLE, OVERSIZED, REDIRECT, MALFORMED }
    private record Grant(String sub, String project, String email, String scopes, String nonce) {}
    private record CodeGrant(String sub, String email, String scopes, String nonce, String token) {}
    private record Credential(Grant grant, AtomicBoolean valid) {}
    public record WireRequest(String path, String query, Map<String, String> form) {}

    private final HttpServer server;
    private final RSAKey rsaJwk = new RSAKeyGenerator(2048).keyID("test-key-id").generate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final java.util.concurrent.ExecutorService executor = Executors.newFixedThreadPool(12);
    private final Map<String, CodeGrant> codes = new ConcurrentHashMap<>();
    private final Set<String> consumedCodes = ConcurrentHashMap.newKeySet();
    private final Map<String, Credential> tokens = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Grant> delayedRevocations = new ConcurrentLinkedQueue<>();

    public volatile int tokenEndpointStatus = 200;
    public volatile String tokenEndpointError;
    public volatile String forcedSubject = "google-sub-1001";
    public volatile String forcedEmail = "user@example.com";
    public volatile String forcedScopes = "openid email https://www.googleapis.com/auth/drive.file";
    public volatile String issuedRefreshToken = "refresh-token";
    public volatile String currentNonce;
    public volatile boolean omitRefreshTokenOnCodeExchange;
    public volatile boolean omitRefreshTokenOnRefresh;
    public volatile String refreshResponseBody;
    public volatile String refreshSubject;
    public volatile String refreshScopes;
    public volatile boolean omitRefreshScopes;
    public volatile int refreshStatus = 200;
    public volatile int revokeEndpointStatus = 200;
    public volatile boolean delayRevocationEffect;
    public volatile CountDownLatch revokePauseLatch;
    public volatile CountDownLatch revokeEnteredLatch;
    public volatile CountDownLatch tokenPauseLatch;
    public volatile CountDownLatch tokenEnteredLatch;
    public volatile CountDownLatch bodyRelease = new CountDownLatch(0);
    public volatile CountDownLatch headersSent = new CountDownLatch(0);
    public volatile BodyMode tokenBodyMode = BodyMode.NORMAL;
    public volatile BodyMode jwksBodyMode = BodyMode.NORMAL;
    public volatile BodyMode revokeBodyMode = BodyMode.NORMAL;
    public final AtomicInteger tokenExchangeCount = new AtomicInteger();
    public final AtomicInteger refreshTokenCount = new AtomicInteger();
    public final AtomicInteger revokeCount = new AtomicInteger();
    public final AtomicInteger jwksCount = new AtomicInteger();
    public final Map<String, Boolean> providerGrantValidBySubject = new ConcurrentHashMap<>();
    public final Map<String, String> accessTokenSubjects = new ConcurrentHashMap<>();
    public final ConcurrentLinkedQueue<WireRequest> wireRequests = new ConcurrentLinkedQueue<>();

    public FakeGoogleOAuthServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(executor);
        server.createContext("/certs", e -> {
            jwksCount.incrementAndGet();
            wireRequests.add(new WireRequest("/certs", e.getRequestURI().getRawQuery(), Map.of()));
            respond(e, 200, new JWKSet(getPublicJwk()).toString().getBytes(StandardCharsets.UTF_8), jwksBodyMode, 256 * 1024);
        });
        server.createContext("/token", this::token);
        server.createContext("/revoke", this::revoke);
        server.start();
    }

    public int getPort() { return server.getAddress().getPort(); }
    public String getJwksUri() { return "http://127.0.0.1:" + getPort() + "/certs"; }
    public String getTokenEndpoint() { return "http://127.0.0.1:" + getPort() + "/token"; }
    public String getRevokeEndpoint() { return "http://127.0.0.1:" + getPort() + "/revoke"; }
    public RSAKey getRsaJwk() { return rsaJwk; }
    public RSAKey getPublicJwk() { return rsaJwk.toPublicJWK(); }

    public void issueCode(String code, String sub, String nonce, String token) {
        codes.put(code, new CodeGrant(sub, sub + "@example.com", forcedScopes, nonce, token));
    }

    public String createSignedIdToken(String sub, String email, String audience, String nonce, Instant expires) throws Exception {
        return sign(new JWTClaimsSet.Builder().issuer("https://accounts.google.com").subject(sub)
                .claim("email", email).audience(audience).claim("nonce", nonce)
                .expirationTime(Date.from(expires)).issueTime(new Date()).build(), JWSAlgorithm.RS256);
    }

    public String sign(JWTClaimsSet claims, JWSAlgorithm algorithm) throws Exception {
        var jwt = new SignedJWT(new JWSHeader.Builder(algorithm).keyID(rsaJwk.getKeyID()).build(), claims);
        jwt.sign(new RSASSASigner(rsaJwk));
        return jwt.serialize();
    }

    public void applyDelayedRevocations() {
        Grant grant;
        while ((grant = delayedRevocations.poll()) != null) invalidate(grant);
    }

    private void invalidate(Grant grant) {
        tokens.values().stream().filter(c -> c.grant().sub().equals(grant.sub())
                && c.grant().project().equals(grant.project())).forEach(c -> c.valid().set(false));
        providerGrantValidBySubject.put(grant.sub(), false);
    }

    public void reset() {
        codes.clear(); consumedCodes.clear(); tokens.clear(); delayedRevocations.clear(); wireRequests.clear();
        providerGrantValidBySubject.clear();
        accessTokenSubjects.clear();
        tokenExchangeCount.set(0); refreshTokenCount.set(0); revokeCount.set(0); jwksCount.set(0);
        tokenEndpointStatus = 200; tokenEndpointError = null; refreshStatus = 200; revokeEndpointStatus = 200;
        forcedSubject = "google-sub-1001"; forcedEmail = "user@example.com";
        forcedScopes = "openid email https://www.googleapis.com/auth/drive.file";
        issuedRefreshToken = "refresh-token"; currentNonce = null;
        omitRefreshTokenOnCodeExchange = false; omitRefreshTokenOnRefresh = false;
        refreshResponseBody = null; refreshSubject = null; refreshScopes = null; omitRefreshScopes = false;
        delayRevocationEffect = false; revokePauseLatch = null; revokeEnteredLatch = null;
        tokenPauseLatch = null; tokenEnteredLatch = null;
        bodyRelease = new CountDownLatch(0); headersSent = new CountDownLatch(0);
        tokenBodyMode = BodyMode.NORMAL; jwksBodyMode = BodyMode.NORMAL; revokeBodyMode = BodyMode.NORMAL;
    }

    private void token(HttpExchange e) throws IOException {
        Map<String, String> form = form(e);
        wireRequests.add(new WireRequest("/token", e.getRequestURI().getRawQuery(), Map.copyOf(form)));
        boolean code = "authorization_code".equals(form.get("grant_type"));
        if (code) tokenExchangeCount.incrementAndGet(); else refreshTokenCount.incrementAndGet();
        if (tokenEnteredLatch != null) tokenEnteredLatch.countDown();
        await(tokenPauseLatch);
        if (tokenEndpointStatus != 200) {
            respond(e, tokenEndpointStatus, bytes(tokenEndpointError == null ? "{\"error\":\"refused\"}" : tokenEndpointError), tokenBodyMode, 64 * 1024);
            return;
        }
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("access_token", "access-" + UUID.randomUUID());
            result.put("expires_in", 3600);
            result.put("token_type", "Bearer");
            if (code) {
                String codeValue = form.get("code");
                if (!consumedCodes.add(codeValue)) { invalidGrant(e); return; }
                CodeGrant spec = codes.remove(codeValue);
                if (spec == null) spec = new CodeGrant(forcedSubject, forcedEmail, forcedScopes,
                        currentNonce == null ? "expected-nonce" : currentNonce, issuedRefreshToken + "-" + UUID.randomUUID());
                Grant grant = new Grant(spec.sub(), form.get("client_id"), spec.email(), spec.scopes(), spec.nonce());
                result.put("scope", grant.scopes());
                result.put("id_token", createSignedIdToken(grant.sub(), grant.email(), grant.project(), grant.nonce(), Instant.now().plusSeconds(3600)));
                if (!omitRefreshTokenOnCodeExchange) {
                    tokens.put(spec.token(), new Credential(grant, new AtomicBoolean(true)));
                    result.put("refresh_token", spec.token());
                }
                providerGrantValidBySubject.put(grant.sub(), true);
                accessTokenSubjects.put((String)result.get("access_token"), grant.sub());
            } else {
                Credential credential = tokens.get(form.get("refresh_token"));
                if (credential == null || !credential.valid().get()
                        || !credential.grant().project().equals(form.get("client_id"))) { invalidGrant(e); return; }
                if (refreshStatus != 200) { respond(e, refreshStatus, bytes("{\"error\":\"invalid_grant\"}"), tokenBodyMode, 64 * 1024); return; }
                if (refreshResponseBody != null) { respond(e, 200, bytes(refreshResponseBody), tokenBodyMode, 64 * 1024); return; }
                var grant = credential.grant();
                accessTokenSubjects.put((String)result.get("access_token"), grant.sub());
                if (!omitRefreshScopes) result.put("scope", refreshScopes == null ? grant.scopes() : refreshScopes);
                if (refreshSubject != null) result.put("id_token", createSignedIdToken(refreshSubject, grant.email(), grant.project(), grant.nonce(), Instant.now().plusSeconds(3600)));
                if (!omitRefreshTokenOnRefresh) {
                    String rotated = "rotated-" + UUID.randomUUID();
                    tokens.put(rotated, new Credential(grant, new AtomicBoolean(true)));
                    credential.valid().set(false);
                    result.put("refresh_token", rotated);
                }
            }
            respond(e, 200, mapper.writeValueAsBytes(result), tokenBodyMode, 64 * 1024);
        } catch (Exception ex) {
            if (ex instanceof IOException io) throw io;
            respond(e, 500, bytes("{}"), BodyMode.NORMAL, 64 * 1024);
        }
    }

    private void revoke(HttpExchange e) throws IOException {
        Map<String, String> form = form(e);
        wireRequests.add(new WireRequest("/revoke", e.getRequestURI().getRawQuery(), Map.copyOf(form)));
        Credential captured = tokens.get(form.get("token"));
        revokeCount.incrementAndGet();
        if (revokeEnteredLatch != null) revokeEnteredLatch.countDown();
        await(revokePauseLatch);
        if (revokeEndpointStatus == 200 && captured != null) {
            if (delayRevocationEffect) delayedRevocations.add(captured.grant());
            else invalidate(captured.grant());
        }
        respond(e, revokeEndpointStatus, bytes("{}"), revokeBodyMode, 64 * 1024);
    }

    private void invalidGrant(HttpExchange e) throws IOException {
        respond(e, 400, bytes("{\"error\":\"invalid_grant\"}"), tokenBodyMode, 64 * 1024);
    }

    private Map<String, String> form(HttpExchange e) throws IOException {
        byte[] raw = e.getRequestBody().readNBytes(64 * 1024 + 1);
        if (raw.length > 64 * 1024) throw new IOException("Oversized synthetic request");
        Map<String, String> values = new HashMap<>();
        for (String entry : new String(raw, StandardCharsets.UTF_8).split("&")) {
            String[] pair = entry.split("=", 2);
            if (pair.length == 2) values.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
        }
        return values;
    }

    private void respond(HttpExchange e, int status, byte[] body, BodyMode mode, int limit) throws IOException {
        try (e) {
            e.getResponseHeaders().set("Content-Type", "application/json");
            if (mode == BodyMode.REDIRECT) {
                e.getResponseHeaders().set("Location", getTokenEndpoint());
                e.sendResponseHeaders(302, -1); return;
            }
            if (mode == BodyMode.OVERSIZED) body = new byte[limit + 1];
            if (mode == BodyMode.MALFORMED) body = bytes("{not-json");
            if (mode == BodyMode.TRICKLE && body.length < 40) {
                body = bytes(new String(body, StandardCharsets.UTF_8) + " ".repeat(64));
            }
            e.sendResponseHeaders(status, body.length);
            var output = e.getResponseBody();
            if (mode == BodyMode.PARTIAL) { output.write(body, 0, 1); output.flush(); }
            headersSent.countDown();
            if (mode == BodyMode.HEADERS_ONLY || mode == BodyMode.PARTIAL) {
                await(bodyRelease);
                if (mode == BodyMode.PARTIAL) output.write(body, 1, body.length - 1); else output.write(body);
            } else if (mode == BodyMode.TRICKLE) {
                // A controlled slow body keeps producing data without resetting the client's absolute deadline.
                for (byte b : body) {
                    output.write(b); output.flush();
                    if (awaitTick(bodyRelease)) break;
                }
            } else output.write(body);
        }
    }

    private boolean awaitTick(CountDownLatch latch) {
        try { return latch.await(200, TimeUnit.MILLISECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); return true; }
    }

    private void await(CountDownLatch latch) {
        if (latch == null) return;
        try {
            if (!latch.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("Synthetic provider barrier timed out");
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private byte[] bytes(String value) { return value.getBytes(StandardCharsets.UTF_8); }

    @Override public void close() {
        if (revokePauseLatch != null) revokePauseLatch.countDown();
        if (tokenPauseLatch != null) tokenPauseLatch.countDown();
        bodyRelease.countDown();
        server.stop(0);
        executor.shutdownNow();
    }
}
