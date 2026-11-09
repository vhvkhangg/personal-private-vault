package com.vhvkhangg.personalprivatevault.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.authentication.fixture.GoogleSignInBrowserConsumer;
import com.vhvkhangg.personalprivatevault.authentication.fixture.GoogleSignInBrowserConsumer.Scenario;
import com.vhvkhangg.personalprivatevault.authentication.fixture.GoogleSignInTestSupport;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInService;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.controller.GoogleSignInController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/** D31: one preflight-selected fixed Spring listener, never a random/8080/18080 fallback. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=19080")
@Import(GoogleSignInBrowserIntegrationTest.BrowserObservationConfig.class)
class GoogleSignInBrowserIntegrationTest extends GoogleSignInTestSupport {
    private static final int SPRING_PORT = 19080;
    private static final String SPRING_BASE = "http://localhost:" + SPRING_PORT;
    private static final Path EDGE = Path.of("C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe");
    private final ObjectMapper mapper = new ObjectMapper();

    @LocalServerPort private int port;
    @Autowired private GoogleSignInService googleSignInService;
    @Autowired private GoogleSignInController controller;
    @Autowired private NativeCallbackObservation observation;
    private GoogleSignInBrowserConsumer consumer;
    private Path profile;
    private Process browser;
    private int refreshBefore;
    private int providerBefore;

    @BeforeEach
    void setUpBrowserFixture() {
        assertThat(port).isEqualTo(SPRING_PORT);
        ensureOwnerBootstrapped();
        seedOwnerGoogleIdentity("https://accounts.google.com", "browser-owner-sub-1001");
        // Existing controlled seams only: strict Host parser and shipping defaults remain unchanged.
        controller.setAllowedPort(SPRING_PORT);
        googleSignInService.setCustomAuthEndpoint("http://localhost:3000/fake-auth");
        googleSignInService.setCustomCallbackUri(SPRING_BASE + "/api/v1/auth/google/callback");
        fakeGoogle().forcedSubject = "browser-owner-sub-1001";
        refreshBefore = refreshCount();
        providerBefore = fakeGoogle().tokenRequestCount.get();
        observation.reset();
    }

    @AfterEach
    void tearDownBrowserFixture() throws Exception {
        if (consumer != null) consumer.releaseAuthorization();
        if (browser != null && browser.isAlive()) {
            List<ProcessHandle> ownedChildren = browser.descendants().toList();
            ownedChildren.reversed().forEach(ProcessHandle::destroyForcibly);
            browser.destroyForcibly();
            assertThat(browser.waitFor(5, TimeUnit.SECONDS)).isTrue();
        }
        if (consumer != null) consumer.close();
        if (profile != null && Files.exists(profile)) {
            Path ownedRoot = profile.toAbsolutePath().normalize();
            if (!ownedRoot.getFileName().toString().startsWith("edge-auth-owned-")) {
                throw new IllegalStateException("Unexpected browser profile cleanup target");
            }
            try (var paths = Files.walk(ownedRoot)) {
                for (Path candidate : paths.sorted(Comparator.reverseOrder()).toList()) {
                    if (!candidate.toAbsolutePath().normalize().startsWith(ownedRoot)) {
                        throw new IllegalStateException("Browser cleanup escaped owned profile");
                    }
                    Files.deleteIfExists(candidate);
                }
            }
        }
    }

    @Test void nativeEdgePositiveRoundtrip() throws Exception {
        runNative(Scenario.POSITIVE, "SUCCESS");
        assertThat(consumer.completion.ticketReceived()).isTrue();
        assertThat(consumer.completion.sessionInstalled()).isTrue();
        assertThat(consumer.completion.redemptions()).extracting(GoogleSignInBrowserConsumer.RedemptionEvidence::status)
                .containsExactly(200);
        assertThat(fakeGoogle().tokenRequestCount.get() - providerBefore).isEqualTo(1);
        assertThat(refreshCount() - refreshBefore).isEqualTo(1);
        assertThat(consumer.tokenExchangeCallbackExact).isTrue();
    }

    @Test void nativeMissingBindingRejectedBeforeSession() throws Exception { rejectBinding(Scenario.MISSING_BINDING); }
    @Test void nativeReplacedBindingRejectedBeforeSession() throws Exception { rejectBinding(Scenario.REPLACED_BINDING); }
    @Test void nativeWrongBindingRejectedBeforeSession() throws Exception { rejectBinding(Scenario.WRONG_BINDING); }
    @Test void nativeExpiredCookieRejectedBeforeSession() throws Exception { rejectBinding(Scenario.EXPIRED_COOKIE); }
    @Test void nativeExpiredAttemptWithRetainedCookieRejectedBeforeSession() throws Exception { rejectBinding(Scenario.EXPIRED_ATTEMPT); }

    @Test void nativeCookieScopeHttpOnlyAndLaxBehavior() throws Exception {
        runNative(Scenario.SCOPE, "SUCCESS");
        assertThat(consumer.coveredPathDelivered).as("Native Cookie header on covered path").isTrue();
        assertThat(consumer.bindingScriptHidden).as("Native document.cookie on covered path excludes HttpOnly binding").isTrue();
        assertThat(consumer.outsidePathExcluded).as("Native request outside Path excludes binding").isTrue();
        assertThat(consumer.otherHostExcluded).as("Native proof.localhost request excludes host-only localhost binding").isTrue();
        assertThat(consumer.crossSiteResourceExcluded).as("Cross-site subresource excludes Lax cookie under current Edge policy").isTrue();
        assertThat(consumer.crossSiteResourceObserved).as("Native Sec-Fetch-Site confirms cross-site subresource").isTrue();
        assertThat(consumer.laxNavigationDelivered).as("Cross-site top-level safe navigation delivers Lax cookie").isTrue();
        assertThat(consumer.crossSiteNavigationObserved).as("Native Sec-Fetch-Site confirms cross-site top-level navigation").isTrue();
        assertThat(consumer.completion.sessionInstalled()).isTrue();
    }

    @Test void nativeLiveTicketWrongVerifierThenSingleRedemptionAndReplay() throws Exception {
        runNative(Scenario.WRONG_VERIFIER_THEN_REPLAY, "SUCCESS");
        var redemptions = consumer.completion.redemptions();
        assertThat(redemptions).hasSize(3);
        assertThat(redemptions.get(0).status()).isEqualTo(401);
        assertThat(redemptions.get(0).pendingTickets()).as("Wrong verifier tested on still-live unconsumed ticket").isEqualTo(1);
        assertThat(redemptions.get(0).refreshRows()).isEqualTo(refreshBefore);
        assertThat(redemptions.get(0).sessionInstalled()).isFalse();
        assertThat(redemptions.get(1).status()).isEqualTo(200);
        assertThat(redemptions.get(1).pendingTickets()).isZero();
        assertThat(redemptions.get(1).sessionInstalled()).isTrue();
        assertThat(redemptions.get(2).status()).isEqualTo(401);
        assertThat(redemptions.get(2).refreshRows()).isEqualTo(redemptions.get(1).refreshRows());
        assertThat(refreshCount() - refreshBefore).isEqualTo(1);
    }

    @Test void consumerInFlightCancellationRevokesLateRefreshToken() throws Exception {
        runNative(Scenario.CANCEL_IN_FLIGHT, "CANCELLED");
        assertThat(consumer.completion.sessionInstalled()).isFalse();
        assertThat(consumer.revokedRefreshRejected).isTrue();
    }

    @Test void consumerPresetCancellationRevokesLateRefreshToken() throws Exception {
        runNative(Scenario.CANCEL_PRESET, "CANCELLED");
        assertThat(consumer.completion.sessionInstalled()).isFalse();
        assertThat(consumer.revokedRefreshRejected).isTrue();
    }

    private void rejectBinding(Scenario scenario) throws Exception {
        runNative(scenario, "REJECTED");
        assertThat(consumer.completion.ticketReceived()).isFalse();
        assertThat(consumer.completion.sessionInstalled()).isFalse();
        assertThat(consumer.completion.redemptions()).isEmpty();
        assertThat(fakeGoogle().tokenRequestCount.get() - providerBefore).as("No provider exchange on rejected proof").isZero();
        assertThat(refreshCount()).as("No token persistence/session installation on rejected proof").isEqualTo(refreshBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM ppv_google_handoff_tickets", Integer.class)).isZero();
        boolean expectedPresent = scenario != Scenario.MISSING_BINDING && scenario != Scenario.EXPIRED_COOKIE;
        assertThat(observation.bindingPresent).as("Actual native callback Cookie presence for " + scenario).isEqualTo(expectedPresent);
    }

    private void runNative(Scenario scenario, String expectedOutcome) throws Exception {
        consumer = new GoogleSignInBrowserConsumer(3000, SPRING_BASE, fakeGoogle(), scenario,
                () -> jdbcTemplate.update("""
                        UPDATE ppv_google_login_attempts
                        SET created_at = created_at - INTERVAL '301 seconds',
                            expires_at = expires_at - INTERVAL '301 seconds'
                        WHERE status = 'PENDING'
                        """), this::refreshCount, this::pendingTicketCount);
        observation.proofMatcher = consumer::matchesExpectedNativeBinding;
        assertThat(Files.isRegularFile(EDGE) && Files.isExecutable(EDGE)).as("Installed native Edge required").isTrue();
        Path repository = Path.of("").toAbsolutePath().normalize();
        if (!Files.exists(repository.resolve(".git"))) repository = repository.getParent();
        if (repository == null || !Files.isRegularFile(repository.resolve("scripts/run-bounded-command.py"))) {
            throw new IllegalStateException("Repository root required for browser evidence");
        }
        Path allowedEvidence = repository.resolve("logs/gdi26/h1-auth").normalize();
        Path evidenceRoot = Path.of(System.getProperty("ppv.auth.browserEvidenceDir", allowedEvidence.resolve("browser-evidence").toString()))
                .toAbsolutePath().normalize();
        if (!evidenceRoot.startsWith(allowedEvidence)) throw new IllegalArgumentException("Browser evidence must stay in ignored scope logs");
        Files.createDirectories(evidenceRoot);
        Path run = Files.createDirectory(evidenceRoot.resolve(scenario.name().toLowerCase() + "-" + UUID.randomUUID()));
        profile = Files.createTempDirectory("edge-auth-owned-").toAbsolutePath().normalize();
        Path rawDom = Files.createTempFile("edge-auth-dom-", ".tmp");
        Path rawError = Files.createTempFile("edge-auth-error-", ".tmp");
        String dom;
        List<String> command = List.of(EDGE.toString(), "--headless=new", "--no-first-run", "--no-default-browser-check",
                "--disable-gpu", "--disable-background-networking", "--disable-component-update",
                "--user-data-dir=" + profile, "--virtual-time-budget=10000", "--dump-dom", "http://localhost:3000/start-flow");
        try {
            browser = new ProcessBuilder(command).redirectOutput(rawDom.toFile()).redirectError(rawError.toFile()).start();
            if (scenario == Scenario.CANCEL_IN_FLIGHT) {
                assertThat(consumer.authorizationReached.await(10, TimeUnit.SECONDS)).as("Browser authorization is in flight").isTrue();
                consumer.cancelInFlight = true;
                consumer.releaseAuthorization();
            }
            boolean finished = browser.waitFor(30, TimeUnit.SECONDS);
            assertThat(finished).as("Owned Edge has finite deadline").isTrue();
            dom = Files.readString(rawDom, StandardCharsets.UTF_8);
            String stderr = Files.readString(rawError, StandardCharsets.UTF_8);
            Files.writeString(run.resolve("dom.html"), consumer.redact(dom), StandardOpenOption.CREATE_NEW);
            Files.writeString(run.resolve("browser.stderr.log"), consumer.redact(stderr), StandardOpenOption.CREATE_NEW);
            Map<String, Object> evidence = new LinkedHashMap<>();
            evidence.put("utc", Instant.now().toString());
            evidence.put("scenario", scenario.name());
            evidence.put("springPort", port);
            evidence.put("consumerPort", 3000);
            evidence.put("edgeExecutable", EDGE.toString());
            evidence.put("flags", command.subList(1, command.size() - 1));
            evidence.put("browserExit", browser.exitValue());
            evidence.put("cookieAttributes", consumer.bindingAttributes);
            evidence.put("completion", consumer.completion);
            evidence.put("scope", Map.of("coveredPathDelivered", consumer.coveredPathDelivered,
                    "bindingScriptHidden", consumer.bindingScriptHidden, "outsidePathExcluded", consumer.outsidePathExcluded,
                    "otherHostExcluded", consumer.otherHostExcluded, "crossSiteResourceExcluded", consumer.crossSiteResourceExcluded,
                    "laxNavigationDelivered", consumer.laxNavigationDelivered));
            evidence.put("ticketScriptHidden", consumer.ticketScriptHidden);
            evidence.put("ticketOutsidePathExcluded", consumer.ticketOutsidePathExcluded);
            evidence.put("crossSiteResourceObserved", consumer.crossSiteResourceObserved);
            evidence.put("crossSiteNavigationObserved", consumer.crossSiteNavigationObserved);
            evidence.put("nativeCallback", Map.of("exactHost", observation.exactHost, "nativeNavigation", observation.nativeNavigation,
                    "bindingPresent", observation.bindingPresent, "bindingMatchesScenario", observation.bindingMatchesScenario,
                    "clean303", observation.clean303));
            evidence.put("handoffCookieAttributes", observation.handoffAttributes);
            evidence.put("providerDispatchDelta", fakeGoogle().tokenRequestCount.get() - providerBefore);
            evidence.put("refreshRowDelta", refreshCount() - refreshBefore);
            evidence.put("callbackTargetExact", consumer.callbackTargetExact);
            evidence.put("tokenExchangeCallbackExact", consumer.tokenExchangeCallbackExact);
            evidence.put("fixtureFailure", consumer.fixtureFailure);
            evidence.put("domContainsNoProof", consumer.domContainsNoProof(dom));
            mapper.writeValue(run.resolve("evidence.json").toFile(), evidence);
            assertThat(browser.exitValue()).isZero();
            assertThat(consumer.fixtureFailure).as("Fixture failure category only, without credentials").isNull();
            assertThat(consumer.browserReported.await(2, TimeUnit.SECONDS)).as("Native JavaScript completed cookie observation").isTrue();
            assertThat(dom.contains("<div id=\"status\">" + expectedOutcome + "</div>")).as("Final native DOM marker").isTrue();
            assertThat(consumer.domContainsNoProof(dom)).as("Native DOM contains no raw code/token/ticket/verifier/proof").isTrue();
        } finally {
            Files.deleteIfExists(rawDom);
            Files.deleteIfExists(rawError);
        }
        assertThat(consumer.bindingAttributes).isEqualTo(new GoogleSignInBrowserConsumer.CookieEvidence(true, true, true, true, true, true));
        assertThat(consumer.completion).isNotNull();
        assertThat(consumer.completion.cleanUrl()).isTrue();
        assertThat(consumer.completion.bindingAbsent()).as("Callback-scoped binding not sent to completion path").isTrue();
        assertThat(consumer.ticketScriptHidden).as("Native completion document.cookie excludes HttpOnly ticket").isTrue();
        assertThat(consumer.ticketOutsidePathExcluded).as("Native report request outside ticket Path excludes ticket").isTrue();
        assertThat(consumer.callbackTargetExact).isTrue();
        assertThat(consumer.nativeFixtureHostExact).isTrue();
        assertThat(observation.exactHost).isTrue();
        assertThat(observation.nativeNavigation).as("Actual native callback navigation, not Java HttpClient").isTrue();
        assertThat(observation.bindingMatchesScenario).as("Native jar delivered the specifically mutated/current proof").isTrue();
        assertThat(observation.clean303).isTrue();
        if (consumer.completion.ticketReceived()) {
            assertThat(observation.handoffAttributes).isEqualTo(new GoogleSignInBrowserConsumer.CookieEvidence(true, true, true, true, true, true));
        }
    }

    private int refreshCount() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM refresh_tokens", Integer.class);
    }

    private int pendingTicketCount() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM ppv_google_handoff_tickets WHERE status = 'PENDING'", Integer.class);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class BrowserObservationConfig {
        @Bean NativeCallbackObservation nativeCallbackObservation() { return new NativeCallbackObservation(); }
    }

    /** Passive test-only observation: invokes the unmodified real filter/controller chain exactly once. */
    static final class NativeCallbackObservation implements Filter {
        volatile Predicate<String> proofMatcher = value -> false;
        volatile boolean exactHost, nativeNavigation, bindingPresent, bindingMatchesScenario, clean303;
        volatile GoogleSignInBrowserConsumer.CookieEvidence handoffAttributes;

        void reset() {
            proofMatcher = value -> false;
            exactHost = nativeNavigation = bindingPresent = bindingMatchesScenario = clean303 = false;
            handoffAttributes = null;
        }

        @Override public void doFilter(ServletRequest input, ServletResponse output, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest request = (HttpServletRequest) input;
            HttpServletResponse response = (HttpServletResponse) output;
            boolean callback = "/api/v1/auth/google/callback".equals(request.getRequestURI());
            if (callback) {
                exactHost = ("localhost:" + SPRING_PORT).equals(request.getHeader("Host"));
                nativeNavigation = "navigate".equals(request.getHeader("Sec-Fetch-Mode"));
                String proof = null;
                if (request.getCookies() != null) for (Cookie cookie : request.getCookies()) {
                    if (GoogleSignInController.BIND_COOKIE_NAME.equals(cookie.getName())) proof = cookie.getValue();
                }
                bindingPresent = proof != null && !proof.isEmpty();
                bindingMatchesScenario = proofMatcher.test(proof);
            }
            chain.doFilter(input, output);
            if (callback) {
                clean303 = response.getStatus() == 303
                        && GoogleSignInController.BFF_COMPLETE_URL.equals(response.getHeader("Location"))
                        && "no-store".equals(response.getHeader("Cache-Control"))
                        && "no-referrer".equals(response.getHeader("Referrer-Policy"));
                for (String header : response.getHeaders("Set-Cookie")) {
                    if (header.startsWith(GoogleSignInController.HANDOFF_COOKIE_NAME + "=")) {
                        var inspected = GoogleSignInBrowserConsumer.inspectCookie(header, "/api/bff/auth/google/complete", 60);
                        if (inspected.expectedMaxAge()) handoffAttributes = inspected;
                    }
                }
            }
        }
    }
}
