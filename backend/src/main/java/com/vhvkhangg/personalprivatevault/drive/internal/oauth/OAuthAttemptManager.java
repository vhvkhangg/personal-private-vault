package com.vhvkhangg.personalprivatevault.drive.internal.oauth;

import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveCapacityExhaustedException;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveStaleAttemptException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Bounded ephemeral authority; cancelled processing work retains admission until its caller finishes. */
public class OAuthAttemptManager {
    public static final int MAX_ACTIVE_ATTEMPTS = 4;
    public static final Duration ATTEMPT_TTL = Duration.ofSeconds(300);

    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, OAuthAttempt> attempts = new HashMap<>();
    private final long[] slotGenerations = new long[3];
    private long initiationOrder;

    public record CreationResult(OAuthAttempt attempt, String plainCookieProof) {
        @Override public String toString() { return "CreationResult[REDACTED]"; }
    }

    public OAuthAttemptManager(Clock clock) { this.clock = clock; }

    public synchronized CreationResult createAttempt(short slot, String ownerId, String redirectUri) {
        purgeExpired();
        long retained = attempts.values().stream()
                .filter(a -> a.isProcessing() || a.getSlot() != slot).count();
        if (retained >= MAX_ACTIVE_ATTEMPTS) {
            throw new DriveCapacityExhaustedException("Maximum active OAuth authorization attempts reached");
        }
        invalidateSlotAttempts(slot);
        String proof = randomToken();
        var now = clock.instant();
        String verifier = randomToken();
        OAuthAttempt attempt = new OAuthAttempt(UUID.randomUUID(), slot, ownerId,
                slotGenerations[slot], ++initiationOrder, randomToken(), randomToken(), verifier,
                Base64.getUrlEncoder().withoutPadding().encodeToString(sha256(verifier)),
                sha256(proof), redirectUri, now, now.plus(ATTEMPT_TTL));
        attempts.put(attempt.getState(), attempt);
        return new CreationResult(attempt, proof);
    }

    public synchronized Optional<OAuthAttempt> findAttempt(String state) {
        purgeExpired();
        return validState(state) ? Optional.ofNullable(attempts.get(state)) : Optional.empty();
    }

    public synchronized Optional<OAuthAttempt> consumeAndVerifyAttempt(
            String state, String proof, String ownerId, String redirectUri) {
        Optional<OAuthAttempt> found = findAttempt(state);
        if (found.isEmpty()) return Optional.empty();
        OAuthAttempt a = found.get();
        if (a.getStatus() != OAuthAttempt.Status.PENDING || !isCurrent(a, ownerId, redirectUri)
                || !matchesProof(a, proof)) return Optional.empty();
        a.setStatus(OAuthAttempt.Status.PROCESSING);
        a.setProcessing(true);
        return found;
    }

    /** Caller holds the application lifecycle guard through the short database commit. */
    public synchronized void verifyCurrent(OAuthAttempt attempt, String ownerId, String redirectUri) {
        if (attempt.getStatus() != OAuthAttempt.Status.PROCESSING || !attempt.isProcessing()
                || !isCurrent(attempt, ownerId, redirectUri)) {
            throw new DriveStaleAttemptException("OAuth attempt expired, was replaced or lost slot authority");
        }
    }

    private boolean isCurrent(OAuthAttempt a, String ownerId, String redirectUri) {
        return attempts.get(a.getState()) == a && a.getSlotGeneration() == slotGenerations[a.getSlot()]
                && a.getOwnerId().equals(ownerId) && a.getRedirectUri().equals(redirectUri)
                && clock.instant().isBefore(a.getExpiresAt());
    }

    public synchronized void completeAttempt(OAuthAttempt a) { finish(a, OAuthAttempt.Status.COMPLETED); }

    public synchronized void failAttempt(OAuthAttempt a) { finish(a, OAuthAttempt.Status.CANCELLED); }

    private void finish(OAuthAttempt a, OAuthAttempt.Status status) {
        a.setStatus(status);
        a.setProcessing(false);
        attempts.remove(a.getState(), a);
    }

    public synchronized void invalidateSlotAttempts(short slot) {
        ++slotGenerations[slot];
        attempts.values().removeIf(a -> {
            if (a.getSlot() != slot) return false;
            a.setStatus(OAuthAttempt.Status.CANCELLED);
            return !a.isProcessing();
        });
    }

    public synchronized boolean shouldClearCookie(OAuthAttempt a, String proof) {
        return a != null && !a.isProcessing()
                && (a.getStatus() == OAuthAttempt.Status.COMPLETED || a.getStatus() == OAuthAttempt.Status.CANCELLED)
                && a.getSlotGeneration() == slotGenerations[a.getSlot()] && matchesProof(a, proof);
    }

    public synchronized long currentInitiationOrder() { return initiationOrder; }

    public synchronized boolean hasAttemptsAtOrBefore(long cutoff) {
        purgeExpired();
        return attempts.values().stream().anyMatch(a -> a.getInitiationOrder() <= cutoff);
    }

    private void purgeExpired() {
        var now = clock.instant();
        attempts.values().removeIf(a -> {
            if (!now.isBefore(a.getExpiresAt())) {
                a.setStatus(OAuthAttempt.Status.CANCELLED);
                return !a.isProcessing();
            }
            return false;
        });
    }

    private boolean validState(String state) { return state != null && !state.isBlank() && state.length() <= 128; }

    private boolean matchesProof(OAuthAttempt a, String proof) {
        return proof != null && !proof.isBlank() && proof.length() <= 128
                && MessageDigest.isEqual(a.getProofDigest(), sha256(proof));
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] sha256(String input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable");
        }
    }
}
