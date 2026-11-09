package com.vhvkhangg.personalprivatevault.authentication.fixture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

/** Controlled native-browser consumer; evidence contains booleans/status, never raw proofs. */
public final class GoogleSignInBrowserConsumer implements AutoCloseable {
    public enum Scenario {
        POSITIVE, SCOPE, MISSING_BINDING, REPLACED_BINDING, WRONG_BINDING,
        EXPIRED_COOKIE, EXPIRED_ATTEMPT, WRONG_VERIFIER_THEN_REPLAY, CANCEL_PRESET, CANCEL_IN_FLIGHT
    }

    public record CookieEvidence(boolean hostOnly, boolean httpOnly, boolean sameSiteLax,
                                 boolean expectedPath, boolean expectedMaxAge, boolean localHttp) {}
    public record RedemptionEvidence(int status, int refreshRows, int pendingTickets, boolean sessionInstalled) {}
    public record CompletionEvidence(boolean cleanUrl, boolean ticketReceived, boolean bindingAbsent,
                                     List<RedemptionEvidence> redemptions, boolean sessionInstalled) {}

    private static final String BIND = "ppv_google_signin_bind";
    private static final String TICKET = "ppv_google_handoff";
    private static final String CALLBACK = "/api/v1/auth/google/callback";
    private static final String COMPLETE = "/api/bff/auth/google/complete";
    private final HttpServer server;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2))
            .followRedirects(HttpClient.Redirect.NEVER).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String springBaseUrl;
    private final FakeGoogleSignInServer fakeGoogle;
    private final Scenario scenario;
    private final Runnable expireAttempt;
    private final IntSupplier refreshCount;
    private final IntSupplier pendingTicketCount;
    private final List<String> secrets = new CopyOnWriteArrayList<>();
    private final CountDownLatch releaseAuthorization;
    public final CountDownLatch authorizationReached = new CountDownLatch(1);
    public final CountDownLatch browserReported = new CountDownLatch(1);
    public volatile CookieEvidence bindingAttributes;
    public volatile CompletionEvidence completion;
    public volatile boolean coveredPathDelivered;
    public volatile boolean outsidePathExcluded;
    public volatile boolean otherHostExcluded;
    public volatile boolean crossSiteResourceExcluded;
    public volatile boolean crossSiteResourceObserved;
    public volatile boolean laxNavigationDelivered;
    public volatile boolean crossSiteNavigationObserved;
    public volatile boolean bindingScriptHidden;
    public volatile boolean ticketScriptHidden;
    public volatile boolean ticketOutsidePathExcluded;
    public volatile boolean callbackTargetExact;
    public volatile boolean tokenExchangeCallbackExact;
    public volatile boolean nativeFixtureHostExact = true;
    public volatile boolean cancelInFlight;
    public volatile boolean revokedRefreshRejected;
    public volatile String fixtureFailure;
    private String verifier;
    private String authorizationUrl;
    private boolean sessionInstalled;
    private volatile String expectedNativeBinding;

    public GoogleSignInBrowserConsumer(int consumerPort, String springBaseUrl, FakeGoogleSignInServer fakeGoogle,
                                      Scenario scenario, Runnable expireAttempt, IntSupplier refreshCount,
                                      IntSupplier pendingTicketCount) throws IOException {
        URI spring = URI.create(springBaseUrl);
        if (consumerPort != 3000 || !"http".equals(spring.getScheme()) || !"localhost".equals(spring.getHost())
                || !List.of(19080, 19081, 28081).contains(spring.getPort())) {
            throw new IllegalArgumentException("D31 fixed localhost test authority required");
        }
        this.springBaseUrl = springBaseUrl;
        this.fakeGoogle = fakeGoogle;
        this.scenario = scenario;
        this.expireAttempt = expireAttempt;
        this.refreshCount = refreshCount;
        this.pendingTicketCount = pendingTicketCount;
        releaseAuthorization = new CountDownLatch(scenario == Scenario.CANCEL_IN_FLIGHT ? 1 : 0);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", consumerPort), 0);
        server.setExecutor(executor);
        server.createContext("/", this::handle);
        server.start();
    }

    public void releaseAuthorization() { releaseAuthorization.countDown(); }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String host = exchange.getRequestHeaders().getFirst("Host");
            boolean fixtureHost = List.of("localhost:3000", "proof.localhost:3000", "127.0.0.1:3000").contains(host);
            nativeFixtureHostExact &= fixtureHost;
            if (!fixtureHost) { exchange.sendResponseHeaders(400, -1); return; }
            switch (path) {
                case "/start-flow" -> start(exchange);
                case "/fake-auth" -> authorize(exchange);
                case CALLBACK + "/scope" -> {
                    coveredPathDelivered = hasCookie(exchange, BIND);
                    html(exchange, "<script>location.replace('/outside-scope?readable='+document.cookie.includes('" + BIND + "='))</script>");
                }
                case "/outside-scope" -> {
                    outsidePathExcluded = !hasCookie(exchange, BIND);
                    bindingScriptHidden = "false".equals(query(exchange).get("readable"));
                    redirect(exchange, "http://proof.localhost:3000" + CALLBACK + "/host");
                }
                case CALLBACK + "/host" -> {
                    otherHostExcluded = "proof.localhost:3000".equals(host) && !hasCookie(exchange, BIND);
                    redirect(exchange, "http://127.0.0.1:3000/cross-site");
                }
                case "/cross-site" -> html(exchange, "<script>(async()=>{await fetch('http://localhost:3000" + CALLBACK
                        + "/resource',{mode:'no-cors',credentials:'include',cache:'no-store'});"
                        + "location.replace('http://localhost:3000" + CALLBACK + "/navigation')})()</script>");
                case CALLBACK + "/resource" -> {
                    crossSiteResourceObserved = "cross-site".equals(exchange.getRequestHeaders().getFirst("Sec-Fetch-Site"));
                    crossSiteResourceExcluded = !hasCookie(exchange, BIND);
                    exchange.sendResponseHeaders(204, -1);
                }
                case CALLBACK + "/navigation" -> {
                    crossSiteNavigationObserved = "cross-site".equals(exchange.getRequestHeaders().getFirst("Sec-Fetch-Site"));
                    laxNavigationDelivered = hasCookie(exchange, BIND);
                    redirect(exchange, authorizationUrl);
                }
                case COMPLETE -> complete(exchange);
                case "/browser-report" -> {
                    ticketOutsidePathExcluded = !hasCookie(exchange, TICKET);
                    ticketScriptHidden = "false".equals(query(exchange).get("ticketReadable"));
                    browserReported.countDown();
                    exchange.sendResponseHeaders(204, -1);
                }
                default -> exchange.sendResponseHeaders(404, -1);
            }
        } catch (Exception failure) {
            fixtureFailure = failure.getClass().getSimpleName();
            html(exchange, "<div id=\"status\">FIXTURE_ERROR</div>");
        } finally {
            exchange.close();
        }
    }

    private void start(HttpExchange exchange) throws Exception {
        verifier = secret(GoogleSignInTestSupport.generate43CharToken());
        HttpResponse<String> response = startLogin(verifier);
        if (response.statusCode() != 200) throw new IllegalStateException("Synthetic start rejected");
        authorizationUrl = mapper.readTree(response.body()).path("data").path("authorizationUrl").asText();
        URI authorization = URI.create(authorizationUrl);
        Map<String, String> parameters = parseQuery(authorization.getRawQuery());
        callbackTargetExact = (springBaseUrl + CALLBACK).equals(parameters.get("redirect_uri"));
        if (!"localhost".equals(authorization.getHost()) || authorization.getPort() != 3000
                || !"/fake-auth".equals(authorization.getPath()) || !callbackTargetExact) {
            throw new IllegalStateException("Synthetic authorization authority mismatch");
        }
        for (String name : List.of("state", "nonce", "code_challenge")) secret(parameters.get(name));
        relayBinding(exchange, response);
        redirect(exchange, scenario == Scenario.SCOPE ? "http://localhost:3000" + CALLBACK + "/scope" : authorizationUrl);
    }

    private HttpResponse<String> startLogin(String handoffVerifier) throws Exception {
        return post("/api/v1/auth/google/start", Map.of("handoffChallenge", GoogleSignInTestSupport.sha256Hex(handoffVerifier)));
    }

    private void relayBinding(HttpExchange exchange, HttpResponse<?> response) {
        String header = response.headers().allValues("Set-Cookie").stream()
                .filter(value -> value.startsWith(BIND + "=")).findFirst().orElseThrow();
        bindingAttributes = inspectCookie(header, CALLBACK, 300);
        expectedNativeBinding = secret(cookie(header, BIND));
        exchange.getResponseHeaders().add("Set-Cookie", header);
    }

    private void authorize(HttpExchange exchange) throws Exception {
        authorizationReached.countDown();
        if (!releaseAuthorization.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Authorization latch timeout");
        Map<String, String> parameters = query(exchange);
        String code = secret("synthetic-" + GoogleSignInTestSupport.generate43CharToken());
        fakeGoogle.registerCode(code, parameters.get("nonce"));
        switch (scenario) {
            case MISSING_BINDING -> mutateCookie(exchange, "", "Max-Age=0");
            case WRONG_BINDING -> mutateCookie(exchange, secret(GoogleSignInTestSupport.generate43CharToken()), "Max-Age=300");
            case REPLACED_BINDING -> {
                HttpResponse<String> second = startLogin(secret(GoogleSignInTestSupport.generate43CharToken()));
                if (second.statusCode() != 200) throw new IllegalStateException("Replacement start rejected");
                relayBinding(exchange, second);
            }
            case EXPIRED_COOKIE -> mutateCookie(exchange, "", "Expires=Thu, 01 Jan 1970 00:00:00 GMT");
            case EXPIRED_ATTEMPT -> expireAttempt.run();
            default -> { }
        }
        redirect(exchange, springBaseUrl + CALLBACK + "?code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(parameters.get("state"), StandardCharsets.UTF_8));
    }

    private void mutateCookie(HttpExchange exchange, String value, String expiry) {
        expectedNativeBinding = value.isEmpty() ? null : value;
        exchange.getResponseHeaders().add("Set-Cookie", BIND + "=" + value + "; Path=" + CALLBACK
                + "; HttpOnly; SameSite=Lax; " + expiry);
    }

    private void complete(HttpExchange exchange) throws Exception {
        String ticket = secret(cookie(exchange.getRequestHeaders().getFirst("Cookie"), TICKET));
        List<RedemptionEvidence> attempts = new ArrayList<>();
        if (ticket != null) {
            if (scenario == Scenario.WRONG_VERIFIER_THEN_REPLAY) {
                HttpResponse<String> wrong = redeem(ticket, secret(GoogleSignInTestSupport.generate43CharToken()));
                attempts.add(new RedemptionEvidence(wrong.statusCode(), refreshCount.getAsInt(), pendingTicketCount.getAsInt(), sessionInstalled));
            }
            HttpResponse<String> valid = redeem(ticket, verifier);
            boolean canceled = scenario == Scenario.CANCEL_PRESET || cancelInFlight;
            if (valid.statusCode() == 200) {
                JsonNode tokens = mapper.readTree(valid.body()).path("data");
                secret(tokens.path("accessToken").asText());
                String refresh = secret(tokens.path("refreshToken").asText());
                if (canceled) {
                    int revoke = post("/api/v1/auth/revoke", Map.of("refreshToken", refresh)).statusCode();
                    revokedRefreshRejected = revoke == 200 && post("/api/v1/auth/refresh", Map.of("refreshToken", refresh)).statusCode() == 401;
                } else {
                    sessionInstalled = tokens.hasNonNull("accessToken") && tokens.hasNonNull("refreshToken");
                }
            }
            attempts.add(new RedemptionEvidence(valid.statusCode(), refreshCount.getAsInt(), pendingTicketCount.getAsInt(), sessionInstalled));
            if (scenario == Scenario.WRONG_VERIFIER_THEN_REPLAY) {
                HttpResponse<String> replay = redeem(ticket, verifier);
                attempts.add(new RedemptionEvidence(replay.statusCode(), refreshCount.getAsInt(), pendingTicketCount.getAsInt(), sessionInstalled));
            }
        }
        tokenExchangeCallbackExact = fakeGoogle.receivedTokenRequests.stream()
                .allMatch(request -> (springBaseUrl + CALLBACK).equals(request.redirectUri()));
        completion = new CompletionEvidence(exchange.getRequestURI().getRawQuery() == null,
                ticket != null, !hasCookie(exchange, BIND), List.copyOf(attempts), sessionInstalled);
        String outcome = sessionInstalled ? "SUCCESS" :
                (scenario == Scenario.CANCEL_PRESET || cancelInFlight) ? "CANCELLED" : "REJECTED";
        html(exchange, "<div id=\"status\">WAITING</div><script>(async()=>{"
                + "await fetch('/browser-report?ticketReadable='+document.cookie.includes('" + TICKET + "='),{cache:'no-store'});"
                + "document.getElementById('status').textContent='" + outcome + "'})()</script>");
    }

    private HttpResponse<String> redeem(String ticket, String handoffVerifier) throws Exception {
        return post("/api/v1/auth/google/handoff", Map.of("ticket", ticket, "handoffVerifier", handoffVerifier));
    }

    private HttpResponse<String> post(String path, Map<String, String> payload) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(springBaseUrl + path)).timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private String secret(String value) {
        if (value != null && !value.isEmpty()) secrets.add(value);
        return value;
    }

    public boolean domContainsNoProof(String dom) { return secrets.stream().noneMatch(dom::contains); }
    public boolean matchesExpectedNativeBinding(String observed) { return java.util.Objects.equals(expectedNativeBinding, observed); }
    public static CookieEvidence inspectCookie(String header, String path, int maxAge) {
        List<String> attributes = java.util.Arrays.stream(header.split(";")).skip(1)
                .map(value -> value.trim().toLowerCase(java.util.Locale.ROOT)).toList();
        return new CookieEvidence(attributes.stream().noneMatch(value -> value.startsWith("domain=")),
                attributes.contains("httponly"), attributes.contains("samesite=lax"),
                attributes.contains("path=" + path), attributes.contains("max-age=" + maxAge), !attributes.contains("secure"));
    }
    public String redact(String text) {
        for (String value : secrets) text = text.replace(value, "[REDACTED]");
        return text;
    }

    private static boolean hasCookie(HttpExchange exchange, String name) {
        return cookie(exchange.getRequestHeaders().getFirst("Cookie"), name) != null;
    }
    private static String cookie(String header, String name) {
        if (header == null) return null;
        for (String pair : header.split(";")) {
            String trimmed = pair.trim();
            if (trimmed.startsWith(name + "=")) {
                String value = trimmed.substring(name.length() + 1);
                return value.isEmpty() ? null : value;
            }
        }
        return null;
    }
    private static Map<String, String> query(HttpExchange exchange) { return parseQuery(exchange.getRequestURI().getRawQuery()); }
    private static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new java.util.HashMap<>();
        if (query != null) for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) result.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
        }
        return result;
    }
    private static void redirect(HttpExchange exchange, String target) throws IOException {
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer");
        exchange.getResponseHeaders().set("Location", target);
        exchange.sendResponseHeaders(303, -1);
    }
    private static void html(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = ("<!DOCTYPE html><html><head><meta charset=\"utf-8\"></head><body>" + body + "</body></html>").getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer");
        exchange.getResponseHeaders().set("Content-Security-Policy", "default-src 'none'; script-src 'unsafe-inline'; connect-src http://localhost:3000");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    @Override public void close() {
        releaseAuthorization.countDown();
        server.stop(0);
        executor.shutdownNow();
        http.close();
    }
}
