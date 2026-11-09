package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.fixture.GoogleSignInTestSupport;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInException;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInService;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInStore;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.session.SessionService;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.token.JwtTokenService;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.AppUser;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.GoogleHandoffTicket;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.GoogleLoginAttempt;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.OwnerGoogleIdentity;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.GoogleHandoffTicketRepository;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.GoogleLoginAttemptRepository;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.OwnerGoogleIdentityRepository;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.RefreshTokenRepository;
import com.vhvkhangg.personalprivatevault.authentication.view.AuthTokensView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleSignInProtocolTest extends GoogleSignInTestSupport {

    @Autowired
    private GoogleSignInService service;

    @Autowired
    private OwnerGoogleIdentityRepository identityRepository;

    @Autowired
    private GoogleLoginAttemptRepository attemptRepository;

    @Autowired
    private GoogleHandoffTicketRepository ticketRepository;

    @Autowired
    private GoogleSignInStore store;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoSpyBean
    private SessionService sessionService;

    @MockitoSpyBean
    private JwtTokenService jwtTokenService;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        ensureOwnerBootstrapped();
    }

    @Test
    @DisplayName("A-02: Unlinked LOGIN fails with ownerNotReady error")
    void unlinkedLoginFails() {
        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        // When unlinked, startLogin fails immediately with ownerNotReady
        assertThatThrownBy(() -> service.startLogin(challenge))
                .isInstanceOf(GoogleSignInException.class);
    }

    @Test
    @DisplayName("A-02: LINK with wrong password records failure rate event; 5 failures trigger rate limit")
    void linkWrongPasswordRecordsFailureAndRateLimits() {
        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        // 5 consecutive wrong passwords
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> service.startLink(challenge, "WrongPassword!"))
                    .isInstanceOf(GoogleSignInException.class);
        }

        // 6th attempt is blocked by the 5/15-minute rate limit
        assertThatThrownBy(() -> service.startLink(challenge, DEFAULT_RAW_PASSWORD))
                .isInstanceOf(GoogleSignInException.class);

        // Verify failure records in database
        long failures = attemptRepository.findAll().stream()
                .filter(a -> GoogleLoginAttempt.KIND_FAILURE.equals(a.getRecordKind()))
                .count();
        assertThat(failures).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("A-02: LINK with valid password starts successfully and sets owner_id = 1")
    void linkWithCorrectPasswordStarts() {
        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        var start = service.startLink(challenge, DEFAULT_RAW_PASSWORD);
        assertThat(start.attemptId()).isNotNull();
        assertThat(start.authorizationUrl()).contains("client_id=");
        assertThat(start.bindingSecret()).hasSize(43);

        var attempt = attemptRepository.findById(start.attemptId()).orElseThrow();
        assertThat(attempt.getPurpose()).isEqualTo(GoogleLoginAttempt.PURPOSE_LINK);
        assertThat(attempt.getOwner()).isNotNull();
        assertThat(attempt.getOwner().getId()).isEqualTo((short) 1);
    }

    @Test
    @DisplayName("A-03: Successfully links identity and second link with same sub is idempotent")
    void linkCompletionIsIdempotentForSameSub() {
        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        var start = service.startLink(challenge, DEFAULT_RAW_PASSWORD);
        String state = extractQueryParam(start.authorizationUrl(), "state");
        String nonce = extractQueryParam(start.authorizationUrl(), "nonce");

        String code = "code-link-1";
        fakeGoogle().forcedSubject = "google-sub-owner-1";
        fakeGoogle().registerCode(code, nonce);

        var outcome = service.processCallback(state, code, null, start.bindingSecret());
        assertThat(outcome.success()).as("Outcome 1 failed with: " + outcome.errorMessage()).isTrue();
        assertThat(outcome.ticket()).isNotNull();

        // Check identity created
        OwnerGoogleIdentity id = identityRepository.findById((short) 1).orElseThrow();
        assertThat(id.getGoogleSub()).isEqualTo("google-sub-owner-1");

        // Second link with same sub also succeeds (idempotent)
        String verifier2 = generate43CharToken();
        String challenge2 = sha256Hex(verifier2);
        var start2 = service.startLink(challenge2, DEFAULT_RAW_PASSWORD);
        String state2 = extractQueryParam(start2.authorizationUrl(), "state");
        String nonce2 = extractQueryParam(start2.authorizationUrl(), "nonce");
        String code2 = "code-link-2";
        fakeGoogle().registerCode(code2, nonce2);

        var outcome2 = service.processCallback(state2, code2, null, start2.bindingSecret());
        assertThat(outcome2.success()).as("Outcome 2 failed with: " + outcome2.errorMessage()).isTrue();
    }

    @Test
    @DisplayName("A-03: Re-link attempt with DIFFERENT Google subject fails with identity conflict")
    void relinkWithDifferentSubFailsConflict() {
        seedOwnerGoogleIdentity("https://accounts.google.com", "sub-existing-owner");

        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        var start = service.startLink(challenge, DEFAULT_RAW_PASSWORD);
        String state = extractQueryParam(start.authorizationUrl(), "state");
        String nonce = extractQueryParam(start.authorizationUrl(), "nonce");

        String code = "code-different-sub";
        fakeGoogle().forcedSubject = "sub-different-attacker";
        fakeGoogle().registerCode(code, nonce);

        var outcome = service.processCallback(state, code, null, start.bindingSecret());
        assertThat(outcome.success()).isFalse();
        assertThat(outcome.errorMessage()).contains("GOOGLE_IDENTITY_CONFLICT");

        // Identity sub remained unchanged
        OwnerGoogleIdentity id = identityRepository.findById((short) 1).orElseThrow();
        assertThat(id.getGoogleSub()).isEqualTo("sub-existing-owner");
    }

    @Test
    @DisplayName("A-04: Callback fails closed when state or binding cookie is missing, replayed, or invalid")
    void callbackFailsClosedOnInvalidStateOrBinding() {
        seedOwnerGoogleIdentity("https://accounts.google.com", "sub-owner-1");

        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        var start = service.startLogin(challenge);
        String state = extractQueryParam(start.authorizationUrl(), "state");

        // Missing code
        assertThat(service.processCallback(state, null, null, start.bindingSecret()).success()).isFalse();

        // Missing state
        assertThat(service.processCallback(null, "code", null, start.bindingSecret()).success()).isFalse();

        // Missing binding secret
        assertThat(service.processCallback(state, "code", null, null).success()).isFalse();

        // Wrong binding secret
        assertThat(service.processCallback(state, "code", null, generate43CharToken()).success()).isFalse();
    }

    @Test
    @DisplayName("A-05: ID token validation rejects wrong issuer, wrong aud, expired token, or forged signature")
    void idTokenValidationRejectsInvalidTokens() {
        seedOwnerGoogleIdentity("https://accounts.google.com", "sub-owner-1");

        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        // 1. Wrong issuer
        var start1 = service.startLogin(challenge);
        String state1 = extractQueryParam(start1.authorizationUrl(), "state");
        String nonce1 = extractQueryParam(start1.authorizationUrl(), "nonce");
        String code1 = "code-bad-iss";
        fakeGoogle().forcedSubject = "sub-owner-1";
        fakeGoogle().forcedIssuer = "https://rogue-issuer.example.com";
        fakeGoogle().registerCode(code1, nonce1);
        assertThat(service.processCallback(state1, code1, null, start1.bindingSecret()).success()).isFalse();

        // 2. Wrong audience
        fakeGoogle().forcedIssuer = "https://accounts.google.com";
        var start2 = service.startLogin(challenge);
        String state2 = extractQueryParam(start2.authorizationUrl(), "state");
        String nonce2 = extractQueryParam(start2.authorizationUrl(), "nonce");
        String code2 = "code-bad-aud";
        fakeGoogle().forcedAudience = "attacker-client-id";
        fakeGoogle().registerCode(code2, nonce2);
        assertThat(service.processCallback(state2, code2, null, start2.bindingSecret()).success()).isFalse();

        // 3. Forged signature (signed by alt key not in JWKS)
        fakeGoogle().forcedAudience = "test-google-client-id";
        var start3 = service.startLogin(challenge);
        String state3 = extractQueryParam(start3.authorizationUrl(), "state");
        String nonce3 = extractQueryParam(start3.authorizationUrl(), "nonce");
        String code3 = "code-forged";
        fakeGoogle().forcedSigningKey = fakeGoogle().getAltJwk();
        fakeGoogle().registerCode(code3, nonce3);
        assertThat(service.processCallback(state3, code3, null, start3.bindingSecret()).success()).isFalse();

        // 4. Expired token (> 60s past expiry)
        fakeGoogle().forcedSigningKey = null;
        var start4 = service.startLogin(challenge);
        String state4 = extractQueryParam(start4.authorizationUrl(), "state");
        String nonce4 = extractQueryParam(start4.authorizationUrl(), "nonce");
        String code4 = "code-expired";
        fakeGoogle().forcedExpiry = Instant.now().minusSeconds(120);
        fakeGoogle().registerCode(code4, nonce4);
        assertThat(service.processCallback(state4, code4, null, start4.bindingSecret()).success()).isFalse();
    }

    @Test
    @DisplayName("A-06: Atomic handoff redemption issues access and refresh tokens; replay is rejected")
    void handoffTicketAtomicRedemption() {
        seedOwnerGoogleIdentity("https://accounts.google.com", "sub-owner-1");

        String handoffVerifier = generate43CharToken();
        String challenge = sha256Hex(handoffVerifier);

        var start = service.startLogin(challenge);
        String state = extractQueryParam(start.authorizationUrl(), "state");
        String nonce = extractQueryParam(start.authorizationUrl(), "nonce");

        String code = "code-handoff-success";
        fakeGoogle().forcedSubject = "sub-owner-1";
        fakeGoogle().registerCode(code, nonce);

        var outcome = service.processCallback(state, code, null, start.bindingSecret());
        assertThat(outcome.success()).isTrue();
        String ticket = outcome.ticket();

        // Wrong verifier fails
        assertThatThrownBy(() -> service.redeemHandoff(ticket, generate43CharToken()))
                .isInstanceOf(GoogleSignInException.class);

        // Correct verifier succeeds
        AuthTokensView tokens = service.redeemHandoff(ticket, handoffVerifier);
        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();

        // Replaying redeemed ticket is rejected
        assertThatThrownBy(() -> service.redeemHandoff(ticket, handoffVerifier))
                .isInstanceOf(GoogleSignInException.class);
    }

    @Test
    @DisplayName("A-06: Two concurrent redemption attempts issue at most one token pair")
    void concurrentRedemptionsIssueAtMostOnce() throws Exception {
        seedOwnerGoogleIdentity("https://accounts.google.com", "sub-owner-1");

        String handoffVerifier = generate43CharToken();
        String challenge = sha256Hex(handoffVerifier);

        var start = service.startLogin(challenge);
        String state = extractQueryParam(start.authorizationUrl(), "state");
        String nonce = extractQueryParam(start.authorizationUrl(), "nonce");

        String code = "code-concurrent-handoff";
        fakeGoogle().forcedSubject = "sub-owner-1";
        fakeGoogle().registerCode(code, nonce);

        var outcome = service.processCallback(state, code, null, start.bindingSecret());
        assertThat(outcome.success()).isTrue();
        String ticket = outcome.ticket();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    startLatch.await();
                    service.redeemHandoff(ticket, handoffVerifier);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            });
        }

        ready.await();
        startLatch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("A-03: Two concurrent first LINK attempts with DIFFERENT Google subjects commit only one binding")
    void concurrentLinkDifferentSubConflict() throws Exception {
        String verifier1 = generate43CharToken();
        String challenge1 = sha256Hex(verifier1);
        var start1 = service.startLink(challenge1, DEFAULT_RAW_PASSWORD);
        String state1 = extractQueryParam(start1.authorizationUrl(), "state");
        String nonce1 = extractQueryParam(start1.authorizationUrl(), "nonce");

        String verifier2 = generate43CharToken();
        String challenge2 = sha256Hex(verifier2);
        var start2 = service.startLink(challenge2, DEFAULT_RAW_PASSWORD);
        String state2 = extractQueryParam(start2.authorizationUrl(), "state");
        String nonce2 = extractQueryParam(start2.authorizationUrl(), "nonce");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch fire = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        executor.submit(() -> {
            ready.countDown();
            try {
                fire.await();
                fakeGoogle().registerCode("code-race-1", nonce1, "sub-race-1");
                var outcome1 = service.processCallback(state1, "code-race-1", null, start1.bindingSecret());
                if (outcome1.success()) {
                    successCount.incrementAndGet();
                } else if ("GOOGLE_IDENTITY_CONFLICT".equals(outcome1.errorMessage())) {
                    conflictCount.incrementAndGet();
                }
            } catch (Exception ignored) {}
        });

        executor.submit(() -> {
            ready.countDown();
            try {
                fire.await();
                fakeGoogle().registerCode("code-race-2", nonce2, "sub-race-2");
                var outcome2 = service.processCallback(state2, "code-race-2", null, start2.bindingSecret());
                if (outcome2.success()) {
                    successCount.incrementAndGet();
                } else if ("GOOGLE_IDENTITY_CONFLICT".equals(outcome2.errorMessage())) {
                    conflictCount.incrementAndGet();
                }
            } catch (Exception ignored) {}
        });

        ready.await();
        fire.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(1);
        assertThat(identityRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("A-05: Missing iat, wrong azp, or unauthorized scope are rejected")
    void tokenValidationStrictChecks() {
        seedOwnerGoogleIdentity("https://accounts.google.com", "sub-owner-strict");

        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        // 1. Missing iat
        var start1 = service.startLogin(challenge);
        String state1 = extractQueryParam(start1.authorizationUrl(), "state");
        String nonce1 = extractQueryParam(start1.authorizationUrl(), "nonce");
        String code1 = "code-no-iat";
        fakeGoogle().forcedSubject = "sub-owner-strict";
        fakeGoogle().omitIat = true;
        fakeGoogle().registerCode(code1, nonce1);
        assertThat(service.processCallback(state1, code1, null, start1.bindingSecret()).success()).isFalse();
        fakeGoogle().reset();

        // 2. Wrong azp
        var start2 = service.startLogin(challenge);
        String state2 = extractQueryParam(start2.authorizationUrl(), "state");
        String nonce2 = extractQueryParam(start2.authorizationUrl(), "nonce");
        String code2 = "code-wrong-azp";
        fakeGoogle().forcedSubject = "sub-owner-strict";
        fakeGoogle().forcedAzp = "wrong-client-id";
        fakeGoogle().registerCode(code2, nonce2);
        assertThat(service.processCallback(state2, code2, null, start2.bindingSecret()).success()).isFalse();
        fakeGoogle().reset();

        // 3. Unauthorized scope: profile
        var start3 = service.startLogin(challenge);
        String state3 = extractQueryParam(start3.authorizationUrl(), "state");
        String nonce3 = extractQueryParam(start3.authorizationUrl(), "nonce");
        String code3 = "code-scope-profile";
        fakeGoogle().forcedSubject = "sub-owner-strict";
        fakeGoogle().forcedScope = "openid email profile";
        fakeGoogle().registerCode(code3, nonce3);
        assertThat(service.processCallback(state3, code3, null, start3.bindingSecret()).success()).isFalse();
        fakeGoogle().reset();

        // 4. Unauthorized scope: drive
        var start4 = service.startLogin(challenge);
        String state4 = extractQueryParam(start4.authorizationUrl(), "state");
        String nonce4 = extractQueryParam(start4.authorizationUrl(), "nonce");
        String code4 = "code-scope-drive";
        fakeGoogle().forcedSubject = "sub-owner-strict";
        fakeGoogle().forcedScope = "openid email https://www.googleapis.com/auth/drive.readonly";
        fakeGoogle().registerCode(code4, nonce4);
        assertThat(service.processCallback(state4, code4, null, start4.bindingSecret()).success()).isFalse();
        fakeGoogle().reset();

        // 5. Non-textual scope: numeric
        var start5 = service.startLogin(challenge);
        String state5 = extractQueryParam(start5.authorizationUrl(), "state");
        String nonce5 = extractQueryParam(start5.authorizationUrl(), "nonce");
        String code5 = "code-scope-numeric";
        fakeGoogle().forcedSubject = "sub-owner-strict";
        fakeGoogle().forcedScope = 12345;
        fakeGoogle().registerCode(code5, nonce5);
        assertThat(service.processCallback(state5, code5, null, start5.bindingSecret()).success()).isFalse();
        fakeGoogle().reset();

        // 6. Authorized scope alias: userinfo.email succeeds
        var start6 = service.startLogin(challenge);
        String state6 = extractQueryParam(start6.authorizationUrl(), "state");
        String nonce6 = extractQueryParam(start6.authorizationUrl(), "nonce");
        String code6 = "code-scope-alias";
        fakeGoogle().forcedSubject = "sub-owner-strict";
        fakeGoogle().forcedScope = "openid https://www.googleapis.com/auth/userinfo.email";
        fakeGoogle().registerCode(code6, nonce6);
        assertThat(service.processCallback(state6, code6, null, start6.bindingSecret()).success()).isTrue();
        fakeGoogle().reset();
    }

    @Test
    @DisplayName("AUTH-FR-06: Concurrent password failures commit failure records under row lock and enforce rate lockout")
    void concurrentPasswordFailuresCommitRateRecords() throws Exception {
        // Run 4 initial failures sequentially
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> service.startLink(sha256Hex(generate43CharToken()), "WrongPassword!"))
                    .isInstanceOf(GoogleSignInException.class);
        }

        // 2 concurrent threads submit the 5th and 6th failure
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch fire = new CountDownLatch(1);
        AtomicInteger failures = new AtomicInteger();

        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    fire.await();
                    service.startLink(sha256Hex(generate43CharToken()), "WrongPassword!");
                } catch (GoogleSignInException e) {
                    failures.incrementAndGet();
                } catch (Exception ignored) {}
            });
        }

        ready.await();
        fire.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(failures.get()).isEqualTo(2);

        // Verify that subsequent link attempts are completely locked out
        assertThatThrownBy(() -> service.startLink(sha256Hex(generate43CharToken()), DEFAULT_RAW_PASSWORD))
                .isInstanceOf(GoogleSignInException.class);
    }

    @Test
    @DisplayName("AUTH-FR-11: Simultaneous first LINK with same Google subject succeeds idempotently")
    void simultaneousFirstLinkSameSubSucceedsIdempotently() throws Exception {
        String verifier1 = generate43CharToken();
        String challenge1 = sha256Hex(verifier1);
        var start1 = service.startLink(challenge1, DEFAULT_RAW_PASSWORD);
        String state1 = extractQueryParam(start1.authorizationUrl(), "state");
        String nonce1 = extractQueryParam(start1.authorizationUrl(), "nonce");

        String verifier2 = generate43CharToken();
        String challenge2 = sha256Hex(verifier2);
        var start2 = service.startLink(challenge2, DEFAULT_RAW_PASSWORD);
        String state2 = extractQueryParam(start2.authorizationUrl(), "state");
        String nonce2 = extractQueryParam(start2.authorizationUrl(), "nonce");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch fire = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        executor.submit(() -> {
            ready.countDown();
            try {
                fire.await();
                fakeGoogle().registerCode("code-same-1", nonce1, "same-sub-race-owner");
                var outcome1 = service.processCallback(state1, "code-same-1", null, start1.bindingSecret());
                if (outcome1.success()) {
                    successCount.incrementAndGet();
                }
            } catch (Exception ignored) {}
        });

        executor.submit(() -> {
            ready.countDown();
            try {
                fire.await();
                fakeGoogle().registerCode("code-same-2", nonce2, "same-sub-race-owner");
                var outcome2 = service.processCallback(state2, "code-same-2", null, start2.bindingSecret());
                if (outcome2.success()) {
                    successCount.incrementAndGet();
                }
            } catch (Exception ignored) {}
        });

        ready.await();
        fire.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(successCount.get()).isEqualTo(2);
        assertThat(identityRepository.count()).isEqualTo(1);
        assertThat(identityRepository.findById((short) 1).orElseThrow().getGoogleSub())
                .isEqualTo("same-sub-race-owner");
    }

    @Test
    @DisplayName("A-08: In-flight PROCESSING attempt crossing lock-boundary TTL fails closed and replay is rejected")
    void inFlightProcessingAttemptFailsClosedAndReplayRejected() {
        seedOwnerGoogleIdentity("https://accounts.google.com", "sub-inflight-test");

        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        var start = service.startLogin(challenge);
        String state = extractQueryParam(start.authorizationUrl(), "state");
        String stateHash = sha256Hex(state);
        String bindingHash = sha256Hex(start.bindingSecret());

        // Step 1: Claim the attempt transition PENDING -> PROCESSING
        var claimed = store.claimAttempt(stateHash, bindingHash, Instant.now());
        assertThat(claimed.attemptId()).isEqualTo(start.attemptId());

        // Step 2: Replay claim on PROCESSING attempt fails closed
        assertThatThrownBy(() -> store.claimAttempt(stateHash, bindingHash, Instant.now()))
                .isInstanceOf(GoogleSignInException.class);

        // Step 3: Age attempt past 300s TTL while in status PROCESSING
        jdbcTemplate.update("""
                UPDATE ppv_google_login_attempts
                SET created_at = NOW() - INTERVAL '310 seconds',
                    expires_at = NOW() - INTERVAL '10 seconds'
                WHERE attempt_id = ?
                """, start.attemptId());

        // Step 4: Finalization across lock boundary fails closed due to expired TTL
        String ticket = generate43CharToken();
        String ticketHash = sha256Hex(ticket);
        assertThatThrownBy(() -> store.finalizeCompletion(
                claimed.attemptId(),
                claimed.claimEpoch(),
                "https://accounts.google.com",
                "sub-inflight-test",
                ticketHash
        )).isInstanceOf(GoogleSignInException.class);

        // Step 5: Verify no ticket was issued
        assertThat(ticketRepository.findById(ticketHash)).isEmpty();
    }

    @Test
    @DisplayName("A-08: Lock-boundary TTL crossing on handoff ticket redemption fails closed")
    void ticketRedemptionLockBoundaryTtlCrossingFailsClosed() {
        seedOwnerGoogleIdentity("https://accounts.google.com", "sub-ticket-ttl");

        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        var start = service.startLogin(challenge);
        String state = extractQueryParam(start.authorizationUrl(), "state");
        String nonce = extractQueryParam(start.authorizationUrl(), "nonce");

        fakeGoogle().forcedSubject = "sub-ticket-ttl";
        fakeGoogle().registerCode("code-ticket-ttl", nonce);

        var outcome = service.processCallback(state, "code-ticket-ttl", null, start.bindingSecret());
        assertThat(outcome.success()).isTrue();
        String ticket = outcome.ticket();
        String ticketHash = sha256Hex(ticket);

        // Age ticket past 60s TTL in database
        jdbcTemplate.update("""
                UPDATE ppv_google_handoff_tickets
                SET issued_at = NOW() - INTERVAL '70 seconds',
                    expires_at = NOW() - INTERVAL '10 seconds'
                WHERE ticket_hash = ?
                """, ticketHash);

        // Redemption fails closed
        assertThatThrownBy(() -> service.redeemHandoff(ticket, verifier))
                .isInstanceOf(GoogleSignInException.class);

        // Ticket remains unconsumed (not marked CONSUMED)
        var ticketEntity = ticketRepository.findById(ticketHash).orElseThrow();
        assertThat(ticketEntity.getStatus()).isNotEqualTo(GoogleHandoffTicket.STATUS_CONSUMED);
    }

    @Test
    @DisplayName("AUTH-FR-11: Forced rollback rolls back ticket update and refresh token write atomically")
    void forcedRollbackLeavesTicketUnredeemed() {
        seedOwnerGoogleIdentity("https://accounts.google.com", "sub-rollback");

        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        var start = service.startLogin(challenge);
        String state = extractQueryParam(start.authorizationUrl(), "state");
        String nonce = extractQueryParam(start.authorizationUrl(), "nonce");

        fakeGoogle().forcedSubject = "sub-rollback";
        fakeGoogle().registerCode("code-rollback", nonce);

        var outcome = service.processCallback(state, "code-rollback", null, start.bindingSecret());
        assertThat(outcome.success()).isTrue();
        String ticket = outcome.ticket();
        String ticketHash = sha256Hex(ticket);

        long refreshTokensBefore = refreshTokenRepository.count();

        // Stub jwtTokenService to fail AFTER refreshTokenRepository.save executes inside issueForVerifiedOwner
        new org.springframework.transaction.support.TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            org.mockito.Mockito.doThrow(new IllegalStateException("Simulated token signing failure after refresh token row insert"))
                    .when(jwtTokenService).issueAccessToken();
        });

        try {
            assertThatThrownBy(() -> service.redeemHandoff(ticket, verifier))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Simulated token signing failure after refresh token row insert");

            // 1. Verify ticket is still PENDING in database (rollback of ticket consumption)
            var ticketEntity = ticketRepository.findById(ticketHash);
            assertThat(ticketEntity).isPresent();
            assertThat(ticketEntity.get().getStatus()).isEqualTo(GoogleHandoffTicket.STATUS_PENDING);
            assertThat(ticketEntity.get().getConsumedAt()).isNull();

            // 2. Verify refresh token row write was rolled back (row count unchanged)
            assertThat(refreshTokenRepository.count()).isEqualTo(refreshTokensBefore);
        } finally {
            org.mockito.Mockito.reset(jwtTokenService);
        }

        // Retry after resetting failure: redemption now succeeds
        var tokens = service.redeemHandoff(ticket, verifier);
        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(refreshTokenRepository.count()).isEqualTo(refreshTokensBefore + 1);
    }

    private static String extractQueryParam(String url, String param) {
        for (String pair : url.substring(url.indexOf('?') + 1).split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0 && pair.substring(0, idx).equals(param)) {
                return pair.substring(idx + 1);
            }
        }
        return null;
    }
}
