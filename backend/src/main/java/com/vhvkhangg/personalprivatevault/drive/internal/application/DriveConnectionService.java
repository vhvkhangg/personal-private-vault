package com.vhvkhangg.personalprivatevault.drive.internal.application;

import com.vhvkhangg.personalprivatevault.drive.internal.config.DriveProperties;
import com.vhvkhangg.personalprivatevault.drive.internal.crypto.DriveCredentialCrypto;
import com.vhvkhangg.personalprivatevault.drive.internal.crypto.DriveCryptoException;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.*;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.*;
import com.vhvkhangg.personalprivatevault.drive.internal.persistence.*;
import com.vhvkhangg.personalprivatevault.drive.internal.web.dto.*;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DriveConnectionService {
    // PPV is permanently single-user. The HTTP adapter supplies the authenticated JWT subject.
    private static final String OWNER_ID = "1";
    private final DriveProperties properties;
    private final DriveConnectionRepository repository;
    private final DriveCredentialCrypto crypto;
    private final OAuthAttemptManager attempts;
    private final SubjectLifecycleManager lifecycle;
    private final DriveOAuthClient oauth;
    private final Clock clock;
    private final TransactionTemplate transactions;

    public record AuthorizeResult(DriveAuthorizeResponse response, String cookieName, String cookieValue, boolean secure) {
        @Override public String toString() { return "AuthorizeResult[REDACTED]"; }
    }
    public record CallbackResult(int httpStatus, short slot, boolean success) {}
    private record CallbackStart(OAuthAttempt attempt, DriveConnectionEntity prior) {}
    private record DisconnectCapture(String sub, String token, RemoteRevocationStatus retained) {
        @Override public String toString() { return "DisconnectCapture[REDACTED]"; }
    }

    public DriveConnectionService(DriveProperties properties, DriveConnectionRepository repository,
            DriveCredentialCrypto crypto, OAuthAttemptManager attempts, SubjectLifecycleManager lifecycle,
            DriveOAuthClient oauth, Clock clock, PlatformTransactionManager transactionManager) {
        this.properties = properties;
        this.repository = repository;
        this.crypto = crypto;
        this.attempts = attempts;
        this.lifecycle = lifecycle;
        this.oauth = oauth;
        this.clock = clock;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    public AuthorizeResult authorize(int slotNumber, String ownerId) {
        short slot = requireSlot(slotNumber);
        if (!OWNER_ID.equals(ownerId)) throw new DriveException("DRIVE_INVALID_OWNER", "Authenticated Owner required", 401);
        return guarded(() -> {
            lifecycle.checkCanAuthorize(slot);
            String redirect = properties.getOauth().getRedirectUri();
            var created = attempts.createAttempt(slot, ownerId, redirect);
            var a = created.attempt();
            String url = properties.getOauth().getAuthorizationEndpoint()
                    + "?client_id=" + encode(properties.getOauth().getClientId())
                    + "&redirect_uri=" + encode(redirect) + "&response_type=code"
                    + "&scope=" + encode("openid email https://www.googleapis.com/auth/drive.file")
                    + "&access_type=offline&state=" + encode(a.getState())
                    + "&code_challenge=" + encode(a.getCodeChallenge()) + "&code_challenge_method=S256"
                    + "&nonce=" + encode(a.getNonce()) + "&prompt=" + encode("consent select_account");
            return new AuthorizeResult(new DriveAuthorizeResponse(slotNumber, url, a.getExpiresAt()),
                    "PPV_DRIVE_BINDING_" + slot, created.plainCookieProof(), redirect.startsWith("https://"));
        });
    }

    public Optional<OAuthAttempt> findCallbackAttempt(String state) { return attempts.findAttempt(state); }

    public boolean shouldClearCookie(OAuthAttempt attempt, String proof) {
        return guarded(() -> attempts.shouldClearCookie(attempt, proof));
    }

    public CallbackResult callback(String state, String code, String error, String errorDescription, String proof) {
        // The adapter additionally checks cardinalities before scalar values reach this boundary.
        boolean hasCode = code != null;
        boolean hasError = error != null;
        if (hasCode == hasError || (hasCode && (code.isBlank() || code.length() > 4096))
                || (hasError && (error.isBlank() || error.length() > 256))) {
            throw new DriveException("DRIVE_INVALID_CALLBACK", "Invalid callback parameters", 400);
        }
        CallbackStart start = guarded(() -> {
            var attempt = attempts.consumeAndVerifyAttempt(state, proof, OWNER_ID, properties.getOauth().getRedirectUri())
                    .orElseThrow(() -> new DriveException("DRIVE_INVALID_STATE_OR_COOKIE", "Invalid callback proof", 400));
            try {
                return new CallbackStart(attempt, repository.findById(attempt.getSlot()).orElse(null));
            } catch (DataAccessException e) {
                attempts.failAttempt(attempt);
                throw new DrivePersistenceException("Could not read connection authority");
            }
        });
        var a = start.attempt();
        boolean success = false;
        try {
            if (hasError) throw new DriveProviderException("DRIVE_PROVIDER_DENIAL", "Provider denied authorization", 400);
            var budget = oauth.beginCallback();
            DriveTokenResponse grant = oauth.exchangeAuthorizationCode(code, a.getCodeVerifier(), a.getRedirectUri(), budget);
            var identity = oauth.validateIdToken(grant.idToken(), a.getNonce(), budget);
            String scopes = oauth.verifyAndCanonicalizeScopes(grant.scope());
            String candidate = grant.refreshToken();
            boolean fromStored = false;
            var prior = start.prior();
            if (candidate == null || candidate.isBlank()) {
                if (prior != null && prior.getGoogleSub().equals(identity.sub())
                        && prior.getStatus() == DriveConnectionStatus.CONNECTED) {
                    try {
                        candidate = crypto.decrypt(a.getSlot(), identity.sub(), prior.getCredentialEpoch(), prior.getEncryptedRefreshToken());
                        fromStored = true;
                    } catch (DriveCryptoException e) {
                        candidate = null;
                    }
                }
            }
            if (candidate == null || candidate.isBlank()) {
                commitResult(start, identity, scopes, null, budget);
                return new CallbackResult(400, a.getSlot(), false);
            }
            try {
                // Exactly one refresh, including same-subject grants which omit a refresh token.
                var validated = oauth.refreshAccessToken(candidate, budget);
                oauth.validateRefreshResponse(validated, identity.sub(), a.getNonce(), scopes, budget);
                if (validated.refreshToken() != null) candidate = validated.refreshToken();
            } catch (DriveProviderException e) {
                boolean unusable = "DRIVE_INVALID_GRANT".equals(e.getErrorCode())
                        || "DRIVE_INVALID_TOKEN_RESPONSE".equals(e.getErrorCode());
                if (unusable && (prior == null || fromStored)) {
                    // Downgrade only the captured matching generation, never a different valid credential.
                    commitResult(start, identity, scopes, null, budget);
                    return new CallbackResult(400, a.getSlot(), false);
                }
                throw e;
            }
            commitResult(start, identity, scopes, candidate, budget);
            success = true;
            return new CallbackResult(200, a.getSlot(), true);
        } catch (DriveException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new DriveProviderException("DRIVE_CALLBACK_PROCESSING_FAILED", "Could not complete authorization", 502);
        } finally {
            boolean completed = success;
            guarded(() -> {
                if (completed) attempts.completeAttempt(a); else attempts.failAttempt(a);
                lifecycle.retireCompletedCutoffs();
                return null;
            });
        }
    }

    /** The same final admission/transaction protects both CONNECTED and RECONNECT_REQUIRED. */
    private void commitResult(CallbackStart start, DriveIdTokenClaims identity, String scopes,
            String token, DriveOAuthClient.CallbackBudget budget) {
        guarded(() -> {
            var a = start.attempt();
            attempts.verifyCurrent(a, OWNER_ID, properties.getOauth().getRedirectUri());
            budget.check();
            lifecycle.checkCanCommit(a.getSlot(), identity.sub(), a.getInitiationOrder());
            try {
                transactions.executeWithoutResult(tx -> {
                    var current = repository.findById(a.getSlot());
                    UUID expected = start.prior() == null ? null : start.prior().getCredentialEpoch();
                    if (!Objects.equals(current.map(DriveConnectionEntity::getCredentialEpoch).orElse(null), expected)) {
                        throw new DriveStaleAttemptException("Connection generation changed during authorization");
                    }
                    if (current.isPresent() && !current.get().getGoogleSub().equals(identity.sub())) {
                        throw new DriveDifferentSubjectOccupiedException("Slot is already occupied by a different subject");
                    }
                    var other = repository.findByGoogleSub(identity.sub());
                    if (other.isPresent() && other.get().getSlot() != a.getSlot()) {
                        throw new DriveDuplicateSubjectException("Verified subject already occupies the other slot");
                    }
                    UUID epoch = UUID.randomUUID();
                    byte[] encrypted = token == null ? null : crypto.encrypt(a.getSlot(), identity.sub(), epoch, token);
                    var status = token == null ? DriveConnectionStatus.RECONNECT_REQUIRED : DriveConnectionStatus.CONNECTED;
                    var now = clock.instant();
                    // Expiry is checked again immediately before mutation, still under the commit guard.
                    attempts.verifyCurrent(a, OWNER_ID, properties.getOauth().getRedirectUri());
                    budget.check();
                    int changed = current.isEmpty()
                            ? repository.insertIfAbsent(a.getSlot(), identity.sub(), identity.email(), scopes,
                                    status.name(), encrypted, token == null ? null : crypto.getKeyId(), epoch, now)
                            : repository.updateWithEpochCas(a.getSlot(), expected, epoch, status, encrypted,
                                    token == null ? null : crypto.getKeyId(), scopes, identity.email(), now);
                    if (changed != 1) throw new DriveStaleAttemptException("Connection write lost current authority");
                });
            } catch (DataAccessException | TransactionException e) {
                throw new DrivePersistenceException("Could not persist connection authority");
            }
            lifecycle.recordConnected(a.getSlot(), identity.sub());
            return null;
        });
    }

    public DriveConnectionListResponse listConnections() {
        return guarded(() -> {
            var rows = repository.findAll().stream().collect(Collectors.toMap(DriveConnectionEntity::getSlot, e -> e));
            var items = new ArrayList<DriveConnectionItemResponse>(2);
            for (short slot = 1; slot <= 2; slot++) {
                var row = rows.get(slot);
                var remote = row == null ? lifecycle.getSlotRemoteRevocation(slot) : null;
                items.add(new DriveConnectionItemResponse(slot, row == null ? "DISCONNECTED" : row.getStatus().name(),
                        row == null ? null : row.getDisplayEmail(), remote == null ? null : remote.name()));
            }
            return new DriveConnectionListResponse(items);
        });
    }

    public DriveDisconnectResponse disconnect(int slotNumber) {
        short slot = requireSlot(slotNumber);
        DisconnectCapture captured = guarded(() -> {
            var found = repository.findById(slot);
            if (found.isEmpty()) {
                attempts.invalidateSlotAttempts(slot);
                lifecycle.retireCompletedCutoffs();
                return new DisconnectCapture(null, null, lifecycle.getSlotRemoteRevocation(slot));
            }
            var row = found.get();
            var reservation = lifecycle.reserveRevokeFence(slot, row.getGoogleSub());
            String token = null;
            if (row.getEncryptedRefreshToken() != null) {
                try {
                    token = crypto.decrypt(slot, row.getGoogleSub(), row.getCredentialEpoch(), row.getEncryptedRefreshToken());
                } catch (DriveCryptoException e) {
                    // Local invalidation still succeeds; no fabricated token is dispatched.
                }
            }
            try {
                transactions.executeWithoutResult(tx -> {
                    if (repository.deleteBySlotAndCredentialEpoch(slot, row.getCredentialEpoch()) != 1) {
                        throw new DriveStaleAttemptException("Connection generation changed during disconnect");
                    }
                });
            } catch (RuntimeException e) {
                lifecycle.rollbackRevokeFence(slot, row.getGoogleSub(), reservation);
                if (e instanceof DriveException drive) throw drive;
                throw new DrivePersistenceException("Could not remove local connection authority");
            }
            attempts.invalidateSlotAttempts(slot);
            return new DisconnectCapture(row.getGoogleSub(), token, null);
        });
        var outcome = captured.retained();
        if (captured.sub() != null) {
            try {
                outcome = captured.token() == null ? RemoteRevocationStatus.FAILED : oauth.revokeToken(captured.token());
            } catch (RuntimeException e) {
                outcome = RemoteRevocationStatus.UNCERTAIN;
            }
            lifecycle.completeRevoke(slot, captured.sub(), outcome);
        }
        return new DriveDisconnectResponse(slotNumber, "DISCONNECTED", outcome == null ? null : outcome.name());
    }

    private short requireSlot(int slot) {
        if (slot != 1 && slot != 2) throw new DriveInvalidSlotException(slot);
        return (short) slot;
    }

    private <T> T guarded(Supplier<T> operation) {
        lifecycle.getLock().lock();
        try { return operation.get(); } finally { lifecycle.getLock().unlock(); }
    }

    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
}
