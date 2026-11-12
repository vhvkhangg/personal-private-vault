package com.vhvkhangg.personalprivatevault.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.context.annotation.Import(ProfileSessionConcurrencyIntegrationTest.ClockConfig.class)
class ProfileSessionConcurrencyIntegrationTest extends AbstractWebIntegrationTest {
    @org.springframework.beans.factory.annotation.Autowired private com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.MutableClock controlledClock;
    @org.springframework.beans.factory.annotation.Autowired private com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.JwtProperties jwtProperties;
    @org.springframework.beans.factory.annotation.Autowired private org.springframework.web.context.WebApplicationContext webContext;
    @org.springframework.beans.factory.annotation.Autowired @org.springframework.beans.factory.annotation.Qualifier("requestMappingHandlerMapping")
    private org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping mappings;
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
    private com.vhvkhangg.personalprivatevault.authentication.internal.application.token.JwtTokenService failingIssuer;

    @org.junit.jupiter.api.BeforeEach void resetClock() { controlledClock.set(java.time.Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS)); }
    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods = false)
    static class ClockConfig {
        @org.springframework.context.annotation.Bean @org.springframework.context.annotation.Primary
        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.MutableClock profileClock() {
            return new com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.MutableClock();
        }
    }


    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    private static final String DEFAULT_PRIVATE_PASS = "master-vault-password-1234";
    private static final String DEFAULT_PUBLIC_PASS = "public-profile-password-5678";

    private void bootstrapAndConfigure() throws Exception {
        String bootstrapPayload = """
                {
                  "email": "owner@vault.local",
                  "username": "owner",
                  "password": "%s",
                  "pin": "123456"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);

        mockMvc.perform(post("/api/v1/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bootstrapPayload))
                .andExpect(status().isCreated());

        // Login private to get bearer
        String loginPayload = """
                {
                  "identifier": "owner",
                  "password": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);

        String resp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(resp).path("data").path("accessToken").asText();

        // Configure public credential
        String setupPayload = """
                {
                  "privatePassword": "%s",
                  "publicPassword": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS, DEFAULT_PUBLIC_PASS);

        mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(setupPayload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DPA-08: Two competing refreshes on the same token yield exactly one 200 and one 401")
    void competingRefreshesYieldOneWinner() throws Exception {
        bootstrapAndConfigure();

        // Login to get fresh tokens
        String loginPayload = """
                {
                  "identifier": "owner",
                  "password": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);

        String loginResp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginResp).path("data").path("refreshToken").asText();

        String refreshPayload = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch fire = new CountDownLatch(1);
        AtomicInteger okCount = new AtomicInteger();
        AtomicInteger unauthCount = new AtomicInteger();

        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    fire.await();
                    int status = mockMvc.perform(post("/api/v1/auth/refresh")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(refreshPayload))
                            .andReturn()
                            .getResponse()
                            .getStatus();
                    if (status == 200) {
                        okCount.incrementAndGet();
                    } else if (status == 401) {
                        unauthCount.incrementAndGet();
                    }
                } catch (Exception ignored) {}
            });
        }

        ready.await();
        fire.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(okCount.get()).isEqualTo(1);
        assertThat(unauthCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("DPA-08: Rotate then revoke(old predecessor) leaves successor valid")
    void rotateThenRevokePredecessorLeavesSuccessorValid() throws Exception {
        bootstrapAndConfigure();

        String loginPayload = """
                {
                  "identifier": "owner",
                  "password": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);

        String loginResp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String oldRefreshToken = objectMapper.readTree(loginResp).path("data").path("refreshToken").asText();

        // Rotate
        String refreshPayload = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(oldRefreshToken);

        String rotateResp = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String successorRefreshToken = objectMapper.readTree(rotateResp).path("data").path("refreshToken").asText();
        String successorAccessToken = objectMapper.readTree(rotateResp).path("data").path("accessToken").asText();

        // Revoke old predecessor -> 200 no-op
        mockMvc.perform(post("/api/v1/auth/revoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isOk());

        // Successor remains valid: can rotate again
        String rotateSuccessorPayload = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(successorRefreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rotateSuccessorPayload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DPA-08: Revoke(current) then rotate fails with 401")
    void revokeCurrentThenRotateFails() throws Exception {
        bootstrapAndConfigure();

        String loginPayload = """
                {
                  "identifier": "owner",
                  "password": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);

        String loginResp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String currentRefreshToken = objectMapper.readTree(loginResp).path("data").path("refreshToken").asText();
        String currentAccessToken = objectMapper.readTree(loginResp).path("data").path("accessToken").asText();

        String revokePayload = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(currentRefreshToken);

        // Revoke current -> 200
        mockMvc.perform(post("/api/v1/auth/revoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revokePayload))
                .andExpect(status().isOk());

        // Rotate fails with 401
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revokePayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_REFRESH_TOKEN"));

        // Access token is also immediately invalidated
        mockMvc.perform(post("/api/v1/auth/sessions/downgrade-public")
                        .header("Authorization", "Bearer " + currentAccessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DPA-09: Competing refresh and profile downgrade serialize cleanly under lineage lock")
    void competingRefreshAndDowngradeSerialize() throws Exception {
        bootstrapAndConfigure();

        String loginPayload = """
                {
                  "identifier": "owner",
                  "password": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);

        String loginResp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String initialAccessToken = objectMapper.readTree(loginResp).path("data").path("accessToken").asText();
        String initialRefreshToken = objectMapper.readTree(loginResp).path("data").path("refreshToken").asText();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch fire = new CountDownLatch(1);
        AtomicInteger rotateStatus = new AtomicInteger();
        AtomicInteger downgradeStatus = new AtomicInteger();

        executor.submit(() -> {
            ready.countDown();
            try {
                fire.await();
                int st = mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"refreshToken\":\"" + initialRefreshToken + "\"}"))
                        .andReturn()
                        .getResponse()
                        .getStatus();
                rotateStatus.set(st);
            } catch (Exception ignored) {}
        });

        executor.submit(() -> {
            ready.countDown();
            try {
                fire.await();
                int st = mockMvc.perform(post("/api/v1/auth/sessions/downgrade-public")
                                .header("Authorization", "Bearer " + initialAccessToken))
                        .andReturn()
                        .getResponse()
                        .getStatus();
                downgradeStatus.set(st);
            } catch (Exception ignored) {}
        });

        ready.await();
        fire.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // One of them must succeed with 200; the loser must fail cleanly without 500 or deadlock
        assertThat(rotateStatus.get()).isIn(200, 401);
        assertThat(downgradeStatus.get()).isIn(200, 401);
        assertThat(rotateStatus.get() == 200 || downgradeStatus.get() == 200).isTrue();
    }

    @Test
    @DisplayName("FR-DPA-01: Stale ever-PUBLIC elevated caller is rejected on concurrent/subsequent reset under credential lock")
    void staleEverPublicCallerRejectedOnResetUnderLock() throws Exception {
        bootstrapAndConfigure();

        // 1. Session A: PUBLIC login, then elevate to PRIVATE (has publicCredentialEpoch = 1)
        String pubLoginPayload = """
                {
                  "profile": "PUBLIC",
                  "password": "%s"
                }
                """.formatted(DEFAULT_PUBLIC_PASS);
        String pubResp = mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pubLoginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String pubToken = objectMapper.readTree(pubResp).path("data").path("accessToken").asText();

        String elevatePayload = """
                {
                  "privatePassword": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);
        String elevatedResp = mockMvc.perform(post("/api/v1/auth/sessions/elevate-private")
                        .header("Authorization", "Bearer " + pubToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(elevatePayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String sessionAElevatedToken = objectMapper.readTree(elevatedResp).path("data").path("accessToken").asText();

        // 2. Session B: Pure PRIVATE login
        String privLoginPayload = """
                {
                  "identifier": "owner",
                  "password": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);
        String privResp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(privLoginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String sessionBPrivateToken = objectMapper.readTree(privResp).path("data").path("accessToken").asText();

        // 3. Session B resets public credential -> epoch advances 1 -> 2
        String resetBPayload = """
                {
                  "privatePassword": "%s",
                  "publicPassword": "new-public-pass-from-b"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);
        mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                        .header("Authorization", "Bearer " + sessionBPrivateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetBPayload))
                .andExpect(status().isOk());

        Long epochAfterB = jdbcTemplate.queryForObject(
                "SELECT credential_epoch FROM ppv_public_login_credential WHERE owner_id = 1", Long.class);
        assertThat(epochAfterB).isEqualTo(2L);

        // 4. Session A (retained publicCredentialEpoch = 1) attempts to reset credential
        // Must be rejected with 401 UNAUTHORIZED because caller epoch (1) != credential epoch (2) (FR-DPA-01)
        String resetAPayload = """
                {
                  "privatePassword": "%s",
                  "publicPassword": "malicious-stale-reset"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);
        mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                        .header("Authorization", "Bearer " + sessionAElevatedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetAPayload))
                .andExpect(status().isUnauthorized());

        // Epoch must remain 2 (not advance to 3)
        Long epochFinal = jdbcTemplate.queryForObject(
                "SELECT credential_epoch FROM ppv_public_login_credential WHERE owner_id = 1", Long.class);
        assertThat(epochFinal).isEqualTo(2L);
    }

    @Test
    @DisplayName("FR-DPA-02: Expiry during lock contention rejects rotation after unblocking")
    void expiryDuringLockContentionRejectsRotation() throws Exception {
        bootstrapAndConfigure();

        // Login to get a valid refresh token
        String loginPayload = """
                {
                  "identifier": "owner",
                  "password": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);
        String loginResp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(loginResp).path("data").path("refreshToken").asText();

        // Hold row lock on credential in a transaction
        org.springframework.transaction.support.TransactionTemplate tx =
                new org.springframework.transaction.support.TransactionTemplate(fixtureTransactions);
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        AtomicInteger rotateStatus = new AtomicInteger();

        Thread holderThread = new Thread(() -> {
            tx.executeWithoutResult(status -> {
                jdbcTemplate.queryForObject("SELECT owner_id FROM ppv_public_login_credential WHERE owner_id = 1 FOR UPDATE", Object.class);
                lockAcquired.countDown();
                try {
                    releaseLock.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });
        holderThread.start();

        lockAcquired.await();

        // In a worker thread, attempt refresh (it will block on credential lock)
        Thread refreshThread = new Thread(() -> {
            try {
                int st = mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                        .andReturn().getResponse().getStatus();
                rotateStatus.set(st);
            } catch (Exception ignored) {}
        });
        refreshThread.start();

        // Wait deterministically until refreshThread is observed waiting for the lock in PostgreSQL pg_locks
        long deadline = System.currentTimeMillis() + 5000;
        boolean lockWaitObserved = false;
        while (System.currentTimeMillis() < deadline) {
            Integer waiting = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM pg_locks WHERE NOT granted", Integer.class);
            if (waiting != null && waiting > 0) {
                lockWaitObserved = true;
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        }
        assertThat(lockWaitObserved).as("Worker thread must be observed waiting on PostgreSQL lock").isTrue();

        // Advance only the injected Clock while the real refresh transaction waits. A stale
        // pre-lock time would still admit this unchanged, genuinely issued token.
        var expiry = jdbcTemplate.queryForObject("SELECT expires_at FROM ppv_login_sessions WHERE id=?", java.sql.Timestamp.class,
                java.util.UUID.fromString(jwtDecoder.decode(objectMapper.readTree(loginResp).path("data").path("accessToken").asText()).getClaimAsString("sid")));
        controlledClock.set(expiry.toInstant());

        // Release the lock
        releaseLock.countDown();
        holderThread.join(5000);
        refreshThread.join(5000);

        // After lock acquisition, authoritative state is re-read; expired token is rejected with 401
        assertThat(rotateStatus.get()).isEqualTo(401);
    }

    @Test
    @DisplayName("FR-DPA-01, DPA-09: Concurrent reset barrier: waiting reset evaluates caller epoch under lock and rejects with 401")
    void concurrentStaleCredentialResetRaceFailsUnderLockBarrier() throws Exception {
        bootstrapAndConfigure();

        // 1. Session A: configured PUBLIC login, then elevate to PRIVATE (retaining publicCredentialEpoch = 1)
        String pubLoginPayload = """
                {
                  "profile": "PUBLIC",
                  "password": "%s"
                }
                """.formatted(DEFAULT_PUBLIC_PASS);
        String pubResp = mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pubLoginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String sessionAPublicToken = objectMapper.readTree(pubResp).path("data").path("accessToken").asText();

        String elevatePayload = """
                {
                  "privatePassword": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);
        String elevateResp = mockMvc.perform(post("/api/v1/auth/sessions/elevate-private")
                        .header("Authorization", "Bearer " + sessionAPublicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(elevatePayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String sessionAElevatedToken = objectMapper.readTree(elevateResp).path("data").path("accessToken").asText();

        // 2. Session B: login private
        String privLoginPayload = """
                {
                  "identifier": "owner",
                  "password": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);
        String privResp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(privLoginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String sessionBPrivateToken = objectMapper.readTree(privResp).path("data").path("accessToken").asText();

        // 3. Holder transaction acquires FOR UPDATE lock on public credential
        org.springframework.transaction.support.TransactionTemplate tx =
                new org.springframework.transaction.support.TransactionTemplate(fixtureTransactions);
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        AtomicInteger waitingResetStatus = new AtomicInteger();

        Thread holder = new Thread(() -> {
            tx.executeWithoutResult(status -> {
                // Lock credential and advance epoch 1 -> 2
                jdbcTemplate.queryForObject("SELECT owner_id FROM ppv_public_login_credential WHERE owner_id = 1 FOR UPDATE", Short.class);
                lockAcquired.countDown();
                try {
                    releaseLock.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                jdbcTemplate.update("UPDATE ppv_public_login_credential SET credential_epoch = 2 WHERE owner_id = 1");
            });
        });
        holder.start();
        lockAcquired.await();

        // 4. Session A attempts reset while lock is held (blocks waiting for the row lock)
        Thread waitingResetThread = new Thread(() -> {
            try {
                String resetPayload = """
                        {
                          "privatePassword": "%s",
                          "publicPassword": "new-public-pass-from-a"
                        }
                        """.formatted(DEFAULT_PRIVATE_PASS);
                int st = mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                                .header("Authorization", "Bearer " + sessionAElevatedToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(resetPayload))
                        .andReturn().getResponse().getStatus();
                waitingResetStatus.set(st);
            } catch (Exception ignored) {}
        });
        waitingResetThread.start();

        // 5. Deterministically wait until Session A's thread is waiting on pg_locks
        long deadline = System.currentTimeMillis() + 5000;
        boolean lockWaitObserved = false;
        while (System.currentTimeMillis() < deadline) {
            Integer waiting = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM pg_locks WHERE NOT granted", Integer.class);
            if (waiting != null && waiting > 0) {
                lockWaitObserved = true;
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        }
        assertThat(lockWaitObserved).as("Session A reset must be observed waiting on PostgreSQL lock").isTrue();

        // 6. Release lock so holder commits epoch = 2; Session A unblocks under lock
        releaseLock.countDown();
        holder.join(5000);
        waitingResetThread.join(5000);

        // 7. Session A evaluates caller's epoch (1) != fresh under-lock epoch (2) -> fails with 401
        assertThat(waitingResetStatus.get()).isEqualTo(401);

        // Epoch remains 2 (not overwritten or advanced)
        Long epochFinal = jdbcTemplate.queryForObject(
                "SELECT credential_epoch FROM ppv_public_login_credential WHERE owner_id = 1", Long.class);
        assertThat(epochFinal).isEqualTo(2L);
    }


    private com.fasterxml.jackson.databind.JsonNode loginPair(boolean publicProfile) throws Exception {
        String body = publicProfile
                ? "{\"profile\":\"PUBLIC\",\"password\":\"" + DEFAULT_PUBLIC_PASS + "\"}"
                : "{\"identifier\":\"owner\",\"password\":\"" + DEFAULT_PRIVATE_PASS + "\"}";
        return objectMapper.readTree(mockMvc.perform(post(publicProfile ? "/api/v1/auth/profiles/login" : "/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).path("data");
    }

    private org.springframework.test.web.servlet.MvcResult action(String operation, JsonNode target, JsonNode admin) throws Exception {
        var req = switch (operation) {
            case "refresh" -> post("/api/v1/auth/refresh").content("{\"refreshToken\":\"" + target.path("refreshToken").asText() + "\"}");
            case "elevate" -> post("/api/v1/auth/sessions/elevate-private").header("Authorization", "Bearer " + target.path("accessToken").asText())
                    .content("{\"privatePassword\":\"" + DEFAULT_PRIVATE_PASS + "\"}");
            case "downgrade" -> post("/api/v1/auth/sessions/downgrade-public").header("Authorization", "Bearer " + target.path("accessToken").asText());
            case "reset" -> post("/api/v1/auth/profiles/public-credential").header("Authorization", "Bearer " + admin.path("accessToken").asText())
                    .content("{\"privatePassword\":\"" + DEFAULT_PRIVATE_PASS + "\",\"publicPassword\":\"replacement-public-pass-1234\"}");
            default -> throw new IllegalArgumentException(operation);
        };
        return mockMvc.perform(req.contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "DPA-09 {0} commits before blocked {1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "reset,refresh,401", "refresh,reset,200", "reset,elevate,401", "elevate,reset,200",
            "reset,downgrade,401", "downgrade,reset,200", "refresh,elevate,200", "elevate,refresh,401",
            "refresh,downgrade,200", "downgrade,refresh,401", "elevate,elevate,403", "downgrade,downgrade,403",
            "reset,reset,200"})
    void forcedCommitSerializations(String first, String second, int expectedSecond) throws Exception {
        bootstrapAndConfigure();
        JsonNode admin = loginPair(false);
        JsonNode target = loginPair(true);
        if (first.equals("downgrade") || second.equals("downgrade")) {
            var elevated = action("elevate", target, admin);
            assertThat(elevated.getResponse().getStatus()).isEqualTo(200);
            target = objectMapper.readTree(elevated.getResponse().getContentAsString()).path("data");
        }
        final JsonNode lineage = target;
        var executor = Executors.newFixedThreadPool(2);
        var firstHasWritten = new CountDownLatch(1);
        var commitFirst = new CountDownLatch(1);
        var firstPid = new AtomicInteger();
        var tx = new org.springframework.transaction.support.TransactionTemplate(fixtureTransactions);
        try {
            var a = executor.submit(() -> tx.execute(st -> {
                try {
                    firstPid.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
                    var result = action(first, lineage, admin);
                    assertThat(result.getResponse().getStatus()).isEqualTo(200);
                    firstHasWritten.countDown();
                    assertThat(commitFirst.await(10, TimeUnit.SECONDS)).isTrue();
                    return result;
                } catch (Exception ex) { throw new RuntimeException(ex); }
            }));
            assertThat(firstHasWritten.await(10, TimeUnit.SECONDS)).isTrue();
            var b = executor.submit(() -> action(second, lineage, admin));
            long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            boolean blockedByFirst = false;
            while (System.nanoTime() < until) {
                int blocked = jdbcTemplate.queryForObject("SELECT count(*) FROM pg_stat_activity WHERE ? = ANY(pg_blocking_pids(pid))", Integer.class, firstPid.get());
                if (blocked > 0) { blockedByFirst = true; break; }
                Thread.onSpinWait();
            }
            assertThat(blockedByFirst).as("second operation actually waits for first transaction, not a simultaneous-start guess").isTrue();
            assertThat(b.isDone()).isFalse();
            commitFirst.countDown();
            var firstResult = a.get(10, TimeUnit.SECONDS);
            var secondResult = b.get(10, TimeUnit.SECONDS);
            assertThat(secondResult.getResponse().getStatus()).isEqualTo(expectedSecond);
            if (first.equals("reset") || second.equals("reset")) {
                assertThat(jdbcTemplate.queryForObject("SELECT credential_epoch FROM ppv_public_login_credential", Long.class))
                        .isEqualTo(first.equals("reset") && second.equals("reset") ? 3L : 2L);
                mockMvc.perform(get("/api/v1/reference/countries").header("Authorization", "Bearer " + lineage.path("accessToken").asText()))
                        .andExpect(status().isUnauthorized());
                if (!first.equals("reset")) {
                    var issued = objectMapper.readTree(firstResult.getResponse().getContentAsString()).path("data");
                    mockMvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"" + issued.path("refreshToken").asText() + "\"}"))
                            .andExpect(status().isUnauthorized());
                }
            } else {
                if (first.equals("refresh")) {
                    var superseded = objectMapper.readTree(firstResult.getResponse().getContentAsString()).path("data");
                    assertThat(action("refresh", superseded, admin).getResponse().getStatus()).isEqualTo(401);
                    mockMvc.perform(get("/api/v1/reference/countries").header("Authorization", "Bearer " + superseded.path("accessToken").asText()))
                            .andExpect(status().isUnauthorized());
                }
                var last = objectMapper.readTree((expectedSecond == 200 ? secondResult : firstResult).getResponse().getContentAsString()).path("data");
                var jwt = jwtDecoder.decode(last.path("accessToken").asText());
                long expectedGeneration = first.equals("downgrade") || second.equals("downgrade") ? 3 : 2;
                assertThat(((Number) jwt.getClaim("generation")).longValue()).isEqualTo(expectedGeneration);
                assertThat(jdbcTemplate.queryForObject("SELECT generation FROM ppv_login_sessions WHERE id=?", Long.class,
                        java.util.UUID.fromString(jwt.getClaimAsString("sid")))).isEqualTo(expectedGeneration);
                assertThat(action("refresh", last, admin).getResponse().getStatus()).isEqualTo(200);
            }
        } finally {
            commitFirst.countDown(); executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    private java.util.Map<String, String> authorityRows() {
        var result = new java.util.LinkedHashMap<String, String>();
        for (String table : java.util.List.of("ppv_login_sessions", "refresh_tokens", "ppv_public_login_credential"))
            result.put(table, jdbcTemplate.queryForObject("SELECT jsonb_agg(v ORDER BY v::text)::text FROM (SELECT to_jsonb(t) v FROM " + table + " t) s", String.class));
        return result;
    }

    @Test
    void committedSwitchWithFailedResponseWriteRequiresFreshLogin() throws Exception {
        bootstrapAndConfigure();
        var pair = loginPair(false);
        var sid = java.util.UUID.fromString(jwtDecoder.decode(pair.path("accessToken").asText()).getClaimAsString("sid"));
        var lossyMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(webContext)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .addFilters((jakarta.servlet.Filter) (request, response, chain) -> chain.doFilter(request,
                        new jakarta.servlet.http.HttpServletResponseWrapper((jakarta.servlet.http.HttpServletResponse) response) {
                            @Override public jakarta.servlet.ServletOutputStream getOutputStream() {
                                return new jakarta.servlet.ServletOutputStream() {
                                    @Override public boolean isReady() { return true; }
                                    @Override public void setWriteListener(jakarta.servlet.WriteListener listener) { }
                                    @Override public void write(int b) throws java.io.IOException { throw new java.io.IOException("Synthetic lost reply after commit"); }
                                };
                            }
                        })).build();
        var lost = lossyMvc.perform(post("/api/v1/auth/sessions/downgrade-public")
                        .header("Authorization", "Bearer " + pair.path("accessToken").asText())).andReturn();
        assertThat(lost.getResolvedException()).hasStackTraceContaining("Synthetic lost reply after commit");
        assertThat(lost.getResponse().getContentAsByteArray()).as("no replacement pair reaches the client").isEmpty();
        assertThat(jdbcTemplate.queryForObject("SELECT generation FROM ppv_login_sessions WHERE id=?", Long.class, sid)).isEqualTo(2);
        mockMvc.perform(get("/api/v1/reference/countries").header("Authorization", "Bearer " + pair.path("accessToken").asText())).andExpect(status().isUnauthorized());
        assertThat(action("refresh", pair, pair).getResponse().getStatus()).isEqualTo(401);
        var recovered = loginPair(false);
        mockMvc.perform(get("/api/v1/reference/countries").header("Authorization", "Bearer " + recovered.path("accessToken").asText())).andExpect(status().isOk());
    }

    @Test
    void issuanceRollbackOnFailurePreservesPreviousAuthority() throws Exception {
        bootstrapAndConfigure();
        var pair = loginPair(false);
        var before = authorityRows();
        org.mockito.Mockito.clearInvocations(failingIssuer);
        org.mockito.Mockito.doThrow(new org.springframework.security.oauth2.jwt.JwtEncodingException("Synthetic issuance failure"))
                .when(failingIssuer).issueAccessToken(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyLong());
        try {
            var failed = action("downgrade", pair, pair);
            assertThat(failed.getResponse().getStatus()).isEqualTo(500);
            assertThat(failed.getResolvedException()).isInstanceOf(org.springframework.security.oauth2.jwt.JwtEncodingException.class);
            assertThat(objectMapper.readTree(failed.getResponse().getContentAsString()).path("data").isNull()).isTrue();
            org.mockito.Mockito.verify(failingIssuer).issueAccessToken(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyLong());
        } finally { org.mockito.Mockito.reset(failingIssuer); }
        assertThat(authorityRows()).isEqualTo(before);
        mockMvc.perform(get("/api/v1/reference/countries").header("Authorization", "Bearer " + pair.path("accessToken").asText())).andExpect(status().isOk());
        assertThat(action("refresh", pair, pair).getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void deferredCommitConstraintRollsBackSwitchAndAcceptsNoPair() throws Exception {
        bootstrapAndConfigure();
        var pair = loginPair(false);
        var before = authorityRows();
        jdbcTemplate.execute("CREATE FUNCTION public.dpa_test_commit_failure() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'Synthetic deferred commit failure' USING ERRCODE='23503'; END $$");
        jdbcTemplate.execute("CREATE CONSTRAINT TRIGGER dpa_test_commit_failure AFTER UPDATE ON ppv_login_sessions DEFERRABLE INITIALLY DEFERRED FOR EACH ROW WHEN (NEW.generation > OLD.generation) EXECUTE FUNCTION public.dpa_test_commit_failure()");
        try {
            var failed = action("downgrade", pair, pair);
            assertThat(failed.getResponse().getStatus()).isEqualTo(500);
            assertThat(failed.getResolvedException()).hasStackTraceContaining("Synthetic deferred commit failure");
            assertThat(objectMapper.readTree(failed.getResponse().getContentAsString()).path("data").isNull()).isTrue();
        } finally {
            jdbcTemplate.execute("DROP TRIGGER dpa_test_commit_failure ON ppv_login_sessions");
            jdbcTemplate.execute("DROP FUNCTION public.dpa_test_commit_failure()");
        }
        assertThat(authorityRows()).isEqualTo(before);
        mockMvc.perform(get("/api/v1/reference/countries").header("Authorization", "Bearer " + pair.path("accessToken").asText())).andExpect(status().isOk());
        assertThat(action("refresh", pair, pair).getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void actualAuthoritySqlFailureIsRedactedAndFailClosed() throws Exception {
        bootstrapAndConfigure();
        var pair = loginPair(false);
        var before = authorityRows();
        jdbcTemplate.execute("ALTER TABLE ppv_login_sessions RENAME TO dpa_test_unavailable_sessions");
        try {
            mockMvc.perform(get("/api/v1/reference/countries").header("Authorization", "Bearer " + pair.path("accessToken").asText()))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(r -> assertThat(r.getResponse().getContentAsString()).doesNotContain("SELECT", "relation", "SQL", "dpa_test"));
        } finally { jdbcTemplate.execute("ALTER TABLE dpa_test_unavailable_sessions RENAME TO ppv_login_sessions"); }
        assertThat(authorityRows()).isEqualTo(before);
        mockMvc.perform(get("/api/v1/reference/countries").header("Authorization", "Bearer " + pair.path("accessToken").asText())).andExpect(status().isOk());
    }

    @Test
    void controlledRollingLifetimeAndExactSessionExpiry() throws Exception {
        bootstrapAndConfigure();
        var t0 = controlledClock.instant();
        var pair = loginPair(false);
        var jwt = jwtDecoder.decode(pair.path("accessToken").asText());
        var sid = java.util.UUID.fromString(jwt.getClaimAsString("sid"));
        assertThat(jwt.getExpiresAt()).isEqualTo(t0.plus(jwtProperties.getAccessTokenLifetime()));
        assertThat(pair.path("expiresIn").asLong()).isEqualTo(jwtProperties.getAccessTokenLifetime().toSeconds());
        var originalExpiry = jdbcTemplate.queryForObject("SELECT expires_at FROM ppv_login_sessions WHERE id=?", java.sql.Timestamp.class, sid).toInstant();
        assertThat(originalExpiry).isEqualTo(t0.plus(jwtProperties.getRefreshTokenLifetime()));
        controlledClock.set(t0.plusSeconds(30));
        mockMvc.perform(get("/api/v1/reference/countries").header("Authorization", "Bearer " + pair.path("accessToken").asText())).andExpect(status().isOk());
        assertThat(jdbcTemplate.queryForObject("SELECT expires_at FROM ppv_login_sessions WHERE id=?", java.sql.Timestamp.class, sid).toInstant()).isEqualTo(originalExpiry);
        var rotated = action("refresh", pair, pair);
        assertThat(rotated.getResponse().getStatus()).isEqualTo(200);
        pair = objectMapper.readTree(rotated.getResponse().getContentAsString()).path("data");
        assertThat(((Number) jwtDecoder.decode(pair.path("accessToken").asText()).getClaim("generation")).longValue()).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT expires_at FROM ppv_login_sessions WHERE id=?", java.sql.Timestamp.class, sid).toInstant())
                .isEqualTo(t0.plusSeconds(30).plus(jwtProperties.getRefreshTokenLifetime()));
        controlledClock.set(t0.plusSeconds(60));
        var down = action("downgrade", pair, pair);
        assertThat(down.getResponse().getStatus()).isEqualTo(200);
        pair = objectMapper.readTree(down.getResponse().getContentAsString()).path("data");
        var expiry = t0.plusSeconds(60).plus(jwtProperties.getRefreshTokenLifetime());
        assertThat(jdbcTemplate.queryForObject("SELECT expires_at FROM ppv_login_sessions WHERE id=?", java.sql.Timestamp.class, sid).toInstant()).isEqualTo(expiry);
        assertThat(jwtDecoder.decode(pair.path("accessToken").asText()).getExpiresAt()).isEqualTo(t0.plusSeconds(60).plus(jwtProperties.getAccessTokenLifetime()));
        controlledClock.set(expiry.minusSeconds(1));
        String boundaryBearer = failingIssuer.issueAccessToken(com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile.PUBLIC, sid, 2);
        mockMvc.perform(get("/api/v1/reference/countries").header("Authorization", "Bearer " + boundaryBearer)).andExpect(status().isForbidden());
        controlledClock.set(expiry);
        mockMvc.perform(get("/api/v1/reference/countries").header("Authorization", "Bearer " + boundaryBearer)).andExpect(status().isUnauthorized());
        assertThat(action("refresh", pair, pair).getResponse().getStatus()).isEqualTo(401);
    }

    public static class AdmissionProbe {
        final CountDownLatch admitted = new CountDownLatch(3);
        final CountDownLatch release = new CountDownLatch(1);
        final JdbcTemplate jdbc;
        AdmissionProbe(JdbcTemplate jdbc) { this.jdbc = jdbc; }
        private void waitForCommit() throws InterruptedException {
            admitted.countDown();
            assertThat(release.await(10, TimeUnit.SECONDS)).isTrue();
        }
        @org.springframework.web.bind.annotation.ResponseBody
        public java.util.Map<String,String> read() throws Exception { waitForCommit(); return java.util.Map.of("value", "synthetic-private-read"); }
        @org.springframework.web.bind.annotation.ResponseBody
        public java.util.Map<String,String> write() throws Exception {
            waitForCommit(); assertThat(jdbc.update("UPDATE app_settings SET pagination_size=41 WHERE id=1")).isEqualTo(1); return java.util.Map.of("written", "true");
        }
        @org.springframework.web.bind.annotation.ResponseBody
        public void stream(jakarta.servlet.http.HttpServletResponse response) throws Exception {
            response.setContentType("application/octet-stream");
            response.getOutputStream().write(new byte[]{1,2}); response.flushBuffer();
            waitForCommit(); response.getOutputStream().write(new byte[]{3,4});
        }
    }

    @Test
    void admittedGetWriteAndStreamCompleteAcrossCommitButNewAdmissionsFail() throws Exception {
        bootstrapAndConfigure();
        jdbcTemplate.update("INSERT INTO app_settings(id,default_currency_code) VALUES (1,'USD')");
        var pair = loginPair(false);
        var probe = new AdmissionProbe(jdbcTemplate);
        var infos = new java.util.ArrayList<org.springframework.web.servlet.mvc.method.RequestMappingInfo>();
        for (String method : java.util.List.of("read", "write", "stream")) {
            var info = org.springframework.web.servlet.mvc.method.RequestMappingInfo.paths("/api/v1/dpa-test/" + method)
                    .methods(method.equals("write") ? org.springframework.web.bind.annotation.RequestMethod.POST : org.springframework.web.bind.annotation.RequestMethod.GET)
                    .options(mappings.getBuilderConfiguration()).build();
            infos.add(info);
            mappings.registerMapping(info, probe, method.equals("stream") ? AdmissionProbe.class.getMethod(method, jakarta.servlet.http.HttpServletResponse.class) : AdmissionProbe.class.getMethod(method));
        }
        var executor = Executors.newFixedThreadPool(3);
        try {
            var read = executor.submit(() -> mockMvc.perform(get("/api/v1/dpa-test/read").header("Authorization", "Bearer " + pair.path("accessToken").asText())).andReturn());
            var write = executor.submit(() -> mockMvc.perform(post("/api/v1/dpa-test/write").header("Authorization", "Bearer " + pair.path("accessToken").asText())).andReturn());
            var stream = executor.submit(() -> mockMvc.perform(get("/api/v1/dpa-test/stream").header("Authorization", "Bearer " + pair.path("accessToken").asText())).andReturn());
            assertThat(probe.admitted.await(10, TimeUnit.SECONDS)).isTrue();
            assertThat(action("downgrade", pair, pair).getResponse().getStatus()).isEqualTo(200);
            for (String endpoint : java.util.List.of("read", "stream")) {
                mockMvc.perform(get("/api/v1/dpa-test/" + endpoint).header("Authorization", "Bearer " + pair.path("accessToken").asText()))
                        .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
                        .andExpect(r -> assertThat(r.getResponse().getHeader("Content-Disposition")).isNull());
            }
            mockMvc.perform(post("/api/v1/dpa-test/write").header("Authorization", "Bearer " + pair.path("accessToken").asText())).andExpect(status().isUnauthorized());
            probe.release.countDown();
            assertThat(read.get(10,TimeUnit.SECONDS).getResponse().getStatus()).isEqualTo(200);
            assertThat(write.get(10,TimeUnit.SECONDS).getResponse().getStatus()).isEqualTo(200);
            assertThat(stream.get(10,TimeUnit.SECONDS).getResponse().getContentAsByteArray()).containsExactly(1,2,3,4);
            assertThat(jdbcTemplate.queryForObject("SELECT pagination_size FROM app_settings", Integer.class)).isEqualTo(41);
            // Discard replacement pair as a client with a lost reply would; only fresh credential login recovers.
            assertThat(action("refresh", pair, pair).getResponse().getStatus()).isEqualTo(401);
            var recovered = loginPair(false);
            mockMvc.perform(get("/api/v1/reference/countries").header("Authorization", "Bearer " + recovered.path("accessToken").asText())).andExpect(status().isOk());
        } finally {
            probe.release.countDown(); executor.shutdownNow();
            assertThat(executor.awaitTermination(10,TimeUnit.SECONDS)).isTrue();
            infos.forEach(mappings::unregisterMapping);
        }
    }
}
