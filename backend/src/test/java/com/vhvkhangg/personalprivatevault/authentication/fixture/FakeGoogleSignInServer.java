package com.vhvkhangg.personalprivatevault.authentication.fixture;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Synthetic OIDC provider for Google Sign-In backend testing.
 * Provides /certs (JWKS) and /token endpoints with RS256 signing and deterministic test controls.
 */
public class FakeGoogleSignInServer implements AutoCloseable {

    public record TokenRequest(
            String code,
            String codeVerifier,
            String clientId,
            String clientSecret,
            String redirectUri,
            String grantType
    ) {
        @Override
        public String toString() {
            return "TokenRequest[code=[REDACTED], codeVerifier=[REDACTED], clientId=" + clientId +
                    ", clientSecret=[REDACTED], redirectUri=" + redirectUri + ", grantType=" + grantType + "]";
        }
    }

    private final HttpServer server;
    private final RSAKey rsaJwk;
    private final RSAKey altJwk;
    private final ObjectMapper mapper = new ObjectMapper();
    private final java.util.concurrent.ExecutorService executor = Executors.newCachedThreadPool();

    public final AtomicInteger tokenRequestCount = new AtomicInteger();
    public final AtomicInteger jwksRequestCount = new AtomicInteger();
    public final ConcurrentLinkedQueue<TokenRequest> receivedTokenRequests = new ConcurrentLinkedQueue<>();
    public final Map<String, String> nonceByCode = new ConcurrentHashMap<>();
    public final Map<String, String> subjectByCode = new ConcurrentHashMap<>();

    // Test controls
    public volatile int tokenStatus = 200;
    public volatile String tokenError = null;
    public volatile int jwksStatus = 200;
    public volatile String customIdToken = null;
    public volatile String forcedSubject = "109876543210987654321";
    public volatile String forcedEmail = "owner@vault.local";
    public volatile String forcedIssuer = "https://accounts.google.com";
    public volatile String forcedAudience = "test-google-client-id";
    public volatile String forcedAzp = null;
    public volatile String forcedNonce = null;
    public volatile Instant forcedExpiry = null;
    public volatile Instant forcedIat = null;
    public volatile boolean omitIat = false;
    public volatile Object forcedScope = null;
    public volatile RSAKey forcedSigningKey = null;
    public volatile boolean corruptJwks = false;

    public FakeGoogleSignInServer() throws Exception {
        this.rsaJwk = new RSAKeyGenerator(2048).keyID("test-google-kid").generate();
        this.altJwk = new RSAKeyGenerator(2048).keyID("alt-google-kid").generate();
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        this.server.setExecutor(executor);

        server.createContext("/certs", this::handleCerts);
        server.createContext("/token", this::handleToken);
        server.start();
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    public String getJwksUri() {
        return "http://127.0.0.1:" + getPort() + "/certs";
    }

    public String getTokenUri() {
        return "http://127.0.0.1:" + getPort() + "/token";
    }

    public RSAKey getRsaJwk() {
        return rsaJwk;
    }

    public RSAKey getAltJwk() {
        return altJwk;
    }

    public void registerCode(String code, String nonce) {
        registerCode(code, nonce, null);
    }

    public void registerCode(String code, String nonce, String subject) {
        if (nonce != null) {
            nonceByCode.put(code, nonce);
        }
        if (subject != null) {
            subjectByCode.put(code, subject);
        }
    }

    public String issueIdToken(
            String sub,
            String email,
            String nonce,
            String iss,
            String aud,
            String azp,
            Instant iat,
            Instant exp,
            RSAKey signingKey
    ) {
        try {
            RSAKey key = (signingKey != null) ? signingKey : rsaJwk;
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(key.getKeyID())
                    .build();

            JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                    .issuer(iss != null ? iss : forcedIssuer)
                    .subject(sub != null ? sub : forcedSubject)
                    .audience(aud != null ? List.of(aud) : List.of(forcedAudience))
                    .expirationTime(Date.from(exp != null ? exp : Instant.now().plusSeconds(3600)))
                    .claim("email", email != null ? email : forcedEmail)
                    .claim("email_verified", true);

            if (!omitIat) {
                claims.issueTime(Date.from(iat != null ? iat : Instant.now()));
            }

            if (nonce != null) {
                claims.claim("nonce", nonce);
            }
            if (azp != null) {
                claims.claim("azp", azp);
            }

            SignedJWT signedJWT = new SignedJWT(header, claims.build());
            signedJWT.sign(new RSASSASigner(key.toRSAPrivateKey()));
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to issue test ID token", e);
        }
    }

    private void handleCerts(HttpExchange exchange) throws IOException {
        jwksRequestCount.incrementAndGet();
        if (jwksStatus != 200) {
            byte[] err = "{\"error\":\"jwks_unavailable\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(jwksStatus, err.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(err);
            }
            return;
        }

        if (corruptJwks) {
            byte[] body = "{\"keys\":[{\"corrupted\":true}]}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            return;
        }

        JWKSet jwkSet = new JWKSet(rsaJwk.toPublicJWK());
        byte[] body = jwkSet.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private void handleToken(HttpExchange exchange) throws IOException {
        tokenRequestCount.incrementAndGet();
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Map<String, String> form = parseFormData(exchange.getRequestBody());
        TokenRequest req = new TokenRequest(
                form.get("code"),
                form.get("code_verifier"),
                form.get("client_id"),
                form.get("client_secret"),
                form.get("redirect_uri"),
                form.get("grant_type")
        );
        receivedTokenRequests.add(req);

        if (tokenStatus != 200) {
            String errJson = "{\"error\":\"" + (tokenError != null ? tokenError : "invalid_grant") + "\"}";
            byte[] body = errJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(tokenStatus, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            return;
        }

        String idToken;
        if (customIdToken != null) {
            idToken = customIdToken;
        } else {
            String nonce = (req.code() != null && nonceByCode.containsKey(req.code()))
                    ? nonceByCode.get(req.code())
                    : forcedNonce;
            String subject = (req.code() != null && subjectByCode.containsKey(req.code()))
                    ? subjectByCode.get(req.code())
                    : forcedSubject;
            idToken = issueIdToken(
                    subject,
                    forcedEmail,
                    nonce,
                    forcedIssuer,
                    forcedAudience,
                    forcedAzp,
                    forcedIat,
                    forcedExpiry,
                    forcedSigningKey
            );
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("access_token", "fake-google-access-token-" + UUID.randomUUID());
        resp.put("token_type", "Bearer");
        resp.put("expires_in", 3600);
        resp.put("id_token", idToken);
        if (forcedScope != null) {
            resp.put("scope", forcedScope);
        }

        byte[] body = mapper.writeValueAsBytes(resp);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private Map<String, String> parseFormData(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int r;
        while ((r = is.read(buf)) != -1) {
            baos.write(buf, 0, r);
        }
        String body = baos.toString(StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        if (body.isBlank()) {
            return map;
        }
        for (String pair : body.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(k, v);
            }
        }
        return map;
    }

    public void reset() {
        tokenStatus = 200;
        tokenError = null;
        jwksStatus = 200;
        customIdToken = null;
        forcedSubject = "109876543210987654321";
        forcedEmail = "owner@vault.local";
        forcedIssuer = "https://accounts.google.com";
        forcedAudience = "test-google-client-id";
        forcedAzp = null;
        forcedNonce = null;
        forcedExpiry = null;
        forcedIat = null;
        omitIat = false;
        forcedScope = null;
        forcedSigningKey = null;
        corruptJwks = false;
        receivedTokenRequests.clear();
        nonceByCode.clear();
        subjectByCode.clear();
    }

    @Override
    public void close() {
        server.stop(0);
        executor.shutdownNow();
    }
}
