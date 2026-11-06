package com.vhvkhangg.personalprivatevault.drive;

import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleOAuthServer;
import com.vhvkhangg.personalprivatevault.drive.internal.application.DriveConnectionService;
import com.vhvkhangg.personalprivatevault.drive.internal.config.DriveProperties;
import com.vhvkhangg.personalprivatevault.drive.internal.crypto.DriveCredentialCrypto;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.*;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.*;
import com.vhvkhangg.personalprivatevault.drive.internal.persistence.*;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@Timeout(25)
class DriveGrantRevocationIntegrationTest extends AbstractPostgresIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired DriveConnectionRepository repository;
    @Autowired PlatformTransactionManager transactions;
    FakeGoogleOAuthServer provider;
    DriveConnectionService service;
    OAuthAttemptManager attempts;
    SubjectLifecycleManager lifecycle;
    DriveOAuthClient oauth;
    DriveCredentialCrypto crypto;
    DriveProperties properties;
    MutableClock clock;
    private record Consent(DriveConnectionService.AuthorizeResult auth, String state, String code) {
        @Override public String toString() { return "Consent[REDACTED]"; }
    }

    @BeforeEach void setup() throws Exception {
        jdbc.update("DELETE FROM drive_connections");
        provider = new FakeGoogleOAuthServer();
        clock = new MutableClock(Instant.now());
        attempts = new OAuthAttemptManager(clock);
        lifecycle = new SubjectLifecycleManager(attempts);
        properties = new DriveProperties();
        properties.getOauth().setClientId("test-client");
        properties.getOauth().setClientSecret("test-secret");
        byte[] key = new byte[32]; new java.security.SecureRandom().nextBytes(key);
        crypto = new DriveCredentialCrypto(key);
        oauth = new DriveOAuthClient("test-client", "test-secret", provider.getTokenEndpoint(),
                provider.getRevokeEndpoint(), provider.getJwksUri(), clock, null);
        service = service(lifecycle);
    }

    private DriveConnectionService service(SubjectLifecycleManager guard) {
        return new DriveConnectionService(properties, repository, crypto, attempts, guard, oauth, clock, transactions);
    }

    @AfterEach void cleanup() { oauth.close(); provider.close(); }

    private Consent start(int slot, String sub) {
        var auth = service.authorize(slot, "1");
        String url = auth.response().authorizationUrl();
        String code = UUID.randomUUID().toString();
        provider.issueCode(code, sub, param(url, "nonce"), "credential-" + code);
        return new Consent(auth, param(url, "state"), code);
    }
    private String param(String url, String name) { return url.split(name + "=")[1].split("&")[0]; }
    private DriveConnectionService.CallbackResult complete(Consent c) {
        return service.callback(c.state(), c.code(), null, null, c.auth().cookieValue());
    }
    private void connect(int slot, String sub) { assertThat(complete(start(slot, sub)).success()).isTrue(); }
    private String token(int slot) {
        var row = repository.findById((short) slot).orElseThrow();
        return crypto.decrypt((short) slot, row.getGoogleSub(), row.getCredentialEpoch(), row.getEncryptedRefreshToken());
    }
    private void entered(CountDownLatch latch) throws Exception { assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue(); }
    private Throwable failure(Future<?> result) throws Exception {
        try { result.get(8, TimeUnit.SECONDS); return null; }
        catch (ExecutionException e) { return e.getCause(); }
    }

    @Test void replacedPendingAttemptNeverExchanges() {
        var old = start(1, "U"); start(1, "V");
        assertThatThrownBy(() -> complete(old)).isInstanceOf(DriveException.class);
        assertThat(provider.tokenExchangeCount).hasValue(0);
        assertThat(repository.findAll()).isEmpty();
    }

    @ParameterizedTest @ValueSource(strings={"replace", "disconnect", "expire", "missing-replace", "missing-disconnect", "missing-expire"})
    void cancelledOrExpiredProcessingCannotInsertConnectedOrRecovery(String action) throws Exception {
        provider.omitRefreshTokenOnCodeExchange = action.startsWith("missing");
        var consent = start(1, "U");
        provider.tokenPauseLatch = new CountDownLatch(1); provider.tokenEnteredLatch = new CountDownLatch(1);
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            var result = workers.submit(() -> complete(consent));
            entered(provider.tokenEnteredLatch);
            if (action.endsWith("replace")) start(1, "V");
            if (action.endsWith("disconnect")) assertThat(service.disconnect(1).status()).isEqualTo("DISCONNECTED");
            if (action.endsWith("expire")) clock.advance(Duration.ofSeconds(300));
            // A parallel read acquires the guard and SQL connection while Google is paused.
            assertThat(workers.submit(service::listConnections).get(1, TimeUnit.SECONDS).connections()).hasSize(2);
            provider.tokenPauseLatch.countDown();
            assertThat(failure(result)).isInstanceOf(DriveStaleAttemptException.class);
            assertThat(repository.findAll()).isEmpty();
        } finally { provider.tokenPauseLatch.countDown(); }
    }

    @Test void differentSubjectWinnerCannotBeOverwrittenByOldEmptySlotSnapshot() throws Exception {
        var old = start(1, "U");
        var pause = new CountDownLatch(1);
        provider.tokenPauseLatch = pause; provider.tokenEnteredLatch = new CountDownLatch(1);
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            var result = workers.submit(() -> complete(old));
            entered(provider.tokenEnteredLatch);
            provider.tokenPauseLatch = null;
            connect(1, "V");
            var winner = repository.findById((short) 1).orElseThrow();
            pause.countDown();
            assertThat(failure(result)).isInstanceOf(DriveStaleAttemptException.class);
            var after = repository.findById((short) 1).orElseThrow();
            assertThat(after.getGoogleSub()).isEqualTo("V");
            assertThat(after.getCredentialEpoch()).isEqualTo(winner.getCredentialEpoch());
        } finally { pause.countDown(); }
    }

    @Test void cancelledProcessingRetainsAllFourAdmissionsUntilFinishedEvenAfterTtl() throws Exception {
        provider.tokenPauseLatch = new CountDownLatch(1);
        var results = new java.util.ArrayList<Future<?>>();
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 4; i++) {
                provider.tokenEnteredLatch = new CountDownLatch(1);
                var consent = start(1, "U");
                results.add(workers.submit(() -> complete(consent)));
                entered(provider.tokenEnteredLatch);
            }
            service.disconnect(1); clock.advance(Duration.ofSeconds(301));
            assertThatThrownBy(() -> start(2, "V")).isInstanceOf(DriveCapacityExhaustedException.class);
            provider.tokenPauseLatch.countDown();
            for (var result : results) assertThat(failure(result)).isInstanceOf(DriveStaleAttemptException.class);
            assertThat(start(2, "V")).isNotNull();
            assertThat(repository.findAll()).isEmpty();
        } finally { provider.tokenPauseLatch.countDown(); }
    }

    @Test void changedDatabaseEpochDuringNetworkCannotBeOverwritten() throws Exception {
        connect(1, "U"); var consent = start(1, "U");
        var prior = repository.findById((short) 1).orElseThrow();
        provider.tokenPauseLatch = new CountDownLatch(1); provider.tokenEnteredLatch = new CountDownLatch(1);
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            var result = workers.submit(() -> complete(consent));
            entered(provider.tokenEnteredLatch);
            UUID newer = UUID.randomUUID();
            assertThat(repository.updateWithEpochCas((short) 1, prior.getCredentialEpoch(), newer,
                    DriveConnectionStatus.CONNECTED, crypto.encrypt((short) 1, "U", newer, "new-valid-token"),
                    crypto.getKeyId(), prior.getGrantedScopes(), prior.getDisplayEmail(), clock.instant())).isEqualTo(1);
            provider.tokenPauseLatch.countDown();
            assertThat(failure(result)).isInstanceOf(DriveStaleAttemptException.class);
            assertThat(repository.findById((short) 1).orElseThrow().getCredentialEpoch()).isEqualTo(newer);
        } finally { provider.tokenPauseLatch.countDown(); }
    }

    @Test void duringRevokeAttemptStaysStaleAfter200AndFreshReconnectWhileVRemainsIndependent() throws Exception {
        connect(1, "U");
        provider.revokePauseLatch = new CountDownLatch(1); provider.revokeEnteredLatch = new CountDownLatch(1);
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            var disconnect = workers.submit(() -> service.disconnect(1));
            entered(provider.revokeEnteredLatch);
            assertThat(repository.findById((short) 1)).isEmpty();
            assertThat(service.listConnections().connections().getFirst().remoteRevocation()).isEqualTo("PENDING");
            assertThatThrownBy(() -> service.authorize(1, "1")).isInstanceOf(DriveRevocationPendingException.class);
            // Retain this during-revoke cross-slot attempt until after fresh U reconnect.
            var during = start(2, "U");
            provider.revokePauseLatch.countDown();
            assertThat(disconnect.get(3, TimeUnit.SECONDS).remoteRevocation()).isEqualTo("REQUEST_ACCEPTED");
            connect(1, "U");
            assertThatThrownBy(() -> complete(during)).isInstanceOf(DriveStaleAttemptException.class);
            connect(2, "V");
            assertThat(oauth.refreshAccessToken(token(2)).accessToken()).isNotBlank();
            assertThat(provider.wireRequests.stream().filter(w -> w.path().equals("/revoke")).count()).isEqualTo(1);
        } finally { provider.revokePauseLatch.countDown(); }
    }

    @Test void duringRevokeAttemptCompletedAfter200IsRejectedBeforeAnyDurableRow() throws Exception {
        connect(1, "U");
        provider.revokePauseLatch = new CountDownLatch(1); provider.revokeEnteredLatch = new CountDownLatch(1);
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            var disconnect = workers.submit(() -> service.disconnect(1)); entered(provider.revokeEnteredLatch);
            var during = start(2, "U");
            provider.revokePauseLatch.countDown(); disconnect.get(3, TimeUnit.SECONDS);
            assertThatThrownBy(() -> complete(during)).isInstanceOf(DriveStaleAttemptException.class);
            assertThat(repository.findAll()).isEmpty();
            connect(1, "U");
        } finally { provider.revokePauseLatch.countDown(); }
    }

    @Test void fenceRejectsIssuedR2BeforeOldProjectGrantRevokeCanInvalidateIt() throws Exception {
        // This test-only seam bypasses just final subject admission for the separately recorded mutant run.
        if (Boolean.getBoolean("gdi26.test.disableFence")) {
            lifecycle = new SubjectLifecycleManager(attempts) {
                @Override public void checkCanCommit(short slot, String sub, long order) {}
            };
            service = service(lifecycle);
        }
        connect(1, "U");
        provider.revokePauseLatch = new CountDownLatch(1); provider.revokeEnteredLatch = new CountDownLatch(1);
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            var disconnect = workers.submit(() -> service.disconnect(1)); entered(provider.revokeEnteredLatch);
            var during = start(2, "U");
            Throwable rejected = catchThrowable(() -> complete(during));
            String incorrectlyCommitted = repository.findById((short) 2).isPresent() ? token(2) : null;
            // Change the mutable default to V: revoke must still target its captured U credential.
            provider.forcedSubject = "V";
            provider.revokePauseLatch.countDown(); disconnect.get(3, TimeUnit.SECONDS);
            if (incorrectlyCommitted != null) {
                assertThatThrownBy(() -> oauth.refreshAccessToken(incorrectlyCommitted))
                        .isInstanceOf(DriveProviderException.class).hasMessageContaining("invalid_grant");
            }
            assertThat(provider.providerGrantValidBySubject.get("U")).isFalse();
            assertThat(rejected).as("Fence must reject R2 before the old grant-wide revoke invalidates its credential")
                    .isInstanceOf(DriveRevocationPendingException.class);
            assertThat(repository.findById((short) 2)).isEmpty();
        } finally { provider.revokePauseLatch.countDown(); }
    }

    @Test void revokeUsesCapturedSubjectWhileConcurrentVRemainsUsable() throws Exception {
        connect(1, "U");
        provider.revokePauseLatch = new CountDownLatch(1); provider.revokeEnteredLatch = new CountDownLatch(1);
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            var result = workers.submit(() -> service.disconnect(1)); entered(provider.revokeEnteredLatch);
            connect(2, "V"); String vToken = token(2);
            provider.forcedSubject = "V";
            provider.revokePauseLatch.countDown(); result.get(3, TimeUnit.SECONDS);
            assertThat(oauth.refreshAccessToken(vToken).accessToken()).isNotBlank();
            assertThat(provider.providerGrantValidBySubject.get("U")).isFalse();
            assertThat(provider.providerGrantValidBySubject.get("V")).isTrue();
        } finally { provider.revokePauseLatch.countDown(); }
    }

    @ParameterizedTest @ValueSource(strings={"refusal", "timeout"})
    void definitiveFailureAndRealBodyTimeoutQuarantineWithoutRetry(String mode) {
        connect(1, "U");
        if (mode.equals("refusal")) provider.revokeEndpointStatus = 500;
        else { provider.revokeBodyMode = FakeGoogleOAuthServer.BodyMode.PARTIAL; provider.bodyRelease = new CountDownLatch(1); }
        long began = System.nanoTime();
        var result = service.disconnect(1);
        assertThat(Duration.ofNanos(System.nanoTime() - began)).isLessThan(Duration.ofSeconds(8));
        assertThat(result.status()).isEqualTo("DISCONNECTED");
        assertThat(result.remoteRevocation()).isEqualTo(mode.equals("refusal") ? "FAILED" : "UNCERTAIN");
        assertThat(repository.findAll()).isEmpty();
        assertThatThrownBy(() -> service.authorize(1, "1")).isInstanceOf(DriveRevokeUncertainException.class);
        var cross = start(2, "U");
        assertThatThrownBy(() -> complete(cross)).isInstanceOf(DriveRevokeUncertainException.class);
        assertThat(service.disconnect(1).remoteRevocation()).isEqualTo(result.remoteRevocation());
        assertThat(provider.revokeCount).hasValue(1);
        connect(2, "V");
        provider.bodyRelease.countDown();
    }

    @Test void completedCutoffsRetireButRecoveryRowsAlwaysReserveDisconnectCapacity() {
        for (String sub : List.of("U", "V", "W")) { connect(1, sub); service.disconnect(1); }
        provider.omitRefreshTokenOnCodeExchange = true;
        assertThat(complete(start(1, "recovery-U")).httpStatus()).isEqualTo(400);
        provider.omitRefreshTokenOnCodeExchange = false; connect(2, "V");
        int priorRevokes = provider.revokeCount.get();
        assertThat(service.disconnect(1).remoteRevocation()).isEqualTo("FAILED");
        assertThat(provider.revokeCount).hasValue(priorRevokes);
        assertThat(repository.findById((short) 1)).isEmpty();
        // Two retained uncertain subjects eventually exhaust admission but never block existing local removal.
        provider.revokeEndpointStatus = 500;
        assertThat(service.disconnect(2).remoteRevocation()).isEqualTo("FAILED");
    }

    @Test void localDeleteFailureRestoresFenceAndSendsNoRevoke() {
        connect(1, "U"); var epoch = repository.findById((short) 1).orElseThrow().getCredentialEpoch();
        jdbc.execute("CREATE FUNCTION gdi26_reject_delete() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'synthetic delete failure'; END $$");
        jdbc.execute("CREATE TRIGGER gdi26_reject_delete BEFORE DELETE ON drive_connections FOR EACH ROW EXECUTE FUNCTION gdi26_reject_delete()");
        try {
            assertThatThrownBy(() -> service.disconnect(1)).isInstanceOf(DrivePersistenceException.class)
                    .hasMessage("Could not remove local connection authority").hasNoCause();
            assertThat(provider.revokeCount).hasValue(0);
            assertThat(repository.findById((short) 1).orElseThrow().getCredentialEpoch()).isEqualTo(epoch);
            assertThat(service.authorize(1, "1")).isNotNull();
        } finally {
            jdbc.execute("DROP TRIGGER gdi26_reject_delete ON drive_connections");
            jdbc.execute("DROP FUNCTION gdi26_reject_delete()");
        }
    }

    @Test void restartLosesAttemptsAndRemoteKnowledgeWithoutResurrectingDeletedCredentials() {
        connect(1, "U"); var old = start(2, "V"); service.disconnect(1);
        attempts = new OAuthAttemptManager(clock); lifecycle = new SubjectLifecycleManager(attempts);
        lifecycle.initializeFromDatabase(repository.findAll()); service = service(lifecycle);
        int before = provider.tokenExchangeCount.get();
        assertThatThrownBy(() -> complete(old)).isInstanceOf(DriveException.class);
        assertThat(provider.tokenExchangeCount).hasValue(before);
        assertThat(repository.findAll()).isEmpty();
        assertThat(service.listConnections().connections().getFirst().remoteRevocation()).isNull();
    }

    @Test void sameSubjectOmissionUsesOneRefreshAndRetainsValidatedCandidateOrRotation() {
        connect(1, "U"); String stored = token(1);
        provider.omitRefreshTokenOnCodeExchange = true; provider.omitRefreshTokenOnRefresh = true;
        provider.omitRefreshScopes = true;
        int before = provider.refreshTokenCount.get();
        assertThat(complete(start(1, "U")).success()).isTrue();
        assertThat(provider.refreshTokenCount).hasValue(before + 1);
        assertThat(token(1)).isEqualTo(stored);
        provider.omitRefreshTokenOnRefresh = false;
        assertThat(complete(start(1, "U")).success()).isTrue();
        assertThat(provider.refreshTokenCount).hasValue(before + 2);
        assertThat(token(1)).isNotEqualTo(stored);
    }

    @ParameterizedTest @ValueSource(strings={"{}", "{\"access_token\":\"\", \"expires_in\":3600, \"token_type\":\"Bearer\"}",
            "{\"access_token\":\"A\", \"expires_in\":0, \"token_type\":\"Bearer\"}",
            "{\"access_token\":\"A\", \"expires_in\":3600, \"token_type\":\"Other\"}", "not-json"})
    void invalidRefreshSuccessNeverCreatesConnectedAuthority(String body) {
        provider.refreshResponseBody = body;
        assertThat(complete(start(1, "U")).httpStatus()).isEqualTo(400);
        var row = repository.findById((short) 1).orElseThrow();
        assertThat(row.getStatus()).isEqualTo(DriveConnectionStatus.RECONNECT_REQUIRED);
        assertThat(row.getEncryptedRefreshToken()).isNull(); assertThat(row.getKeyId()).isNull();
        assertThat(provider.refreshTokenCount).hasValue(1);
    }

    @Test void changedRefreshIdentityOrScopeCannotPersistAndCannotDestroyPriorValidGeneration() {
        connect(1, "U"); UUID epoch = repository.findById((short) 1).orElseThrow().getCredentialEpoch();
        provider.refreshSubject = "V";
        assertThatThrownBy(() -> complete(start(1, "U"))).isInstanceOf(DriveProviderException.class);
        assertThat(repository.findById((short) 1).orElseThrow().getCredentialEpoch()).isEqualTo(epoch);
        provider.refreshSubject = null; provider.refreshScopes = "openid email https://www.googleapis.com/auth/drive.activity";
        assertThatThrownBy(() -> complete(start(2, "W"))).isInstanceOf(DriveProviderException.class);
        assertThat(repository.findById((short) 2)).isEmpty();
        assertThat(repository.findById((short) 1).orElseThrow().getCredentialEpoch()).isEqualTo(epoch);
    }

    @Test void delayedProjectRevocationDowngradesOnlyValidatedStoredEpochOnInvalidGrant() {
        connect(1, "U"); provider.delayRevocationEffect = true; service.disconnect(1);
        connect(1, "U"); UUID prior = repository.findById((short) 1).orElseThrow().getCredentialEpoch();
        provider.applyDelayedRevocations();
        provider.omitRefreshTokenOnCodeExchange = true;
        assertThat(complete(start(1, "U")).httpStatus()).isEqualTo(400);
        var row = repository.findById((short) 1).orElseThrow();
        assertThat(row.getCredentialEpoch()).isNotEqualTo(prior);
        assertThat(row.getStatus()).isEqualTo(DriveConnectionStatus.RECONNECT_REQUIRED);
        assertThat(row.getEncryptedRefreshToken()).isNull();
    }

    @Test void sameSubjectConcurrentSlotsHaveOneWinnerAndNoDuplicateAuthority() throws Exception {
        var one = start(1, "U"); var two = start(2, "U");
        provider.tokenPauseLatch = new CountDownLatch(1); provider.tokenEnteredLatch = new CountDownLatch(2);
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = workers.submit(() -> complete(one)); var second = workers.submit(() -> complete(two));
            entered(provider.tokenEnteredLatch); provider.tokenPauseLatch.countDown();
            var errors = java.util.stream.Stream.of(failure(first), failure(second)).filter(java.util.Objects::nonNull).toList();
            assertThat(errors).hasSize(1).allMatch(e -> e instanceof DriveDuplicateSubjectException);
            assertThat(repository.findAll()).hasSize(1);
        } finally { provider.tokenPauseLatch.countDown(); }
    }

    @Test void differentSubjectReconnectCannotReplaceOccupiedSlot() {
        connect(1, "U"); UUID prior = repository.findById((short) 1).orElseThrow().getCredentialEpoch();
        assertThatThrownBy(() -> complete(start(1, "V"))).isInstanceOf(DriveDifferentSubjectOccupiedException.class);
        assertThat(repository.findById((short) 1).orElseThrow().getCredentialEpoch()).isEqualTo(prior);
    }

    @Test void oldProcessingCrossSlotAttemptStaysStaleAfterRevokeAndFreshGrant() throws Exception {
        connect(1, "U"); var old = start(2, "U");
        var pause = new CountDownLatch(1);
        provider.tokenPauseLatch = pause; provider.tokenEnteredLatch = new CountDownLatch(1);
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            var result = workers.submit(() -> complete(old)); entered(provider.tokenEnteredLatch);
            provider.tokenPauseLatch = null;
            service.disconnect(1); connect(1, "U");
            pause.countDown();
            assertThat(failure(result)).isInstanceOf(DriveStaleAttemptException.class);
            assertThat(repository.findById((short) 2)).isEmpty();
        } finally { pause.countDown(); }
    }

    @Test void uncertainCapacityRejectsNewSubjectWithoutBlockingExistingLocalDisconnect() {
        connect(1, "U"); connect(2, "V");
        provider.revokeEndpointStatus = 500;
        service.disconnect(1);
        assertThatThrownBy(() -> complete(start(2, "W"))).isInstanceOf(DriveCapacityExhaustedException.class);
        assertThat(service.disconnect(2).remoteRevocation()).isEqualTo("FAILED");
        assertThat(repository.findAll()).isEmpty();
        assertThat(provider.revokeCount).hasValue(2);
    }

    @Test void lostKeyStillAllowsLocalDisconnectWithoutFabricatedRevoke() {
        connect(1, "U");
        crypto = new DriveCredentialCrypto(new byte[32]); service = service(lifecycle);
        assertThat(service.disconnect(1).remoteRevocation()).isEqualTo("FAILED");
        assertThat(repository.findAll()).isEmpty();
        assertThat(provider.revokeCount).hasValue(0);
        assertThatThrownBy(() -> service.authorize(1, "1")).isInstanceOf(DriveRevokeUncertainException.class);
    }

    @Test void transientRefreshFailurePreservesPriorCredentialAndNeverRetries() {
        connect(1, "U"); UUID prior = repository.findById((short) 1).orElseThrow().getCredentialEpoch();
        provider.omitRefreshTokenOnCodeExchange = true; provider.refreshStatus = 503;
        int before = provider.refreshTokenCount.get();
        assertThatThrownBy(() -> complete(start(1, "U"))).isInstanceOf(DriveProviderException.class);
        assertThat(provider.refreshTokenCount).hasValue(before + 1);
        assertThat(repository.findById((short) 1).orElseThrow().getCredentialEpoch()).isEqualTo(prior);
    }

    @Test void wrongOwnerAndAlteredRedirectContextFailBeforeProviderDispatch() {
        assertThatThrownBy(() -> service.authorize(1, "different-owner")).isInstanceOf(DriveException.class);
        var consent = start(1, "U");
        properties.getOauth().setRedirectUri("https://unapproved.example/callback");
        assertThatThrownBy(() -> complete(consent)).isInstanceOf(DriveException.class);
        assertThat(provider.tokenExchangeCount).hasValue(0);
        assertThat(repository.findAll()).isEmpty();
    }

    private static class MutableClock extends Clock {
        private Instant now;
        MutableClock(Instant now) { this.now = now; }
        void advance(Duration duration) { now = now.plus(duration); }
        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }
}
