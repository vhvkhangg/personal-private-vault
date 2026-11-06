package com.vhvkhangg.personalprivatevault.drive.internal.oauth;

import java.time.Instant;
import java.util.UUID;

public class OAuthAttempt {

    public enum Status {
        PENDING,
        PROCESSING,
        COMPLETED,
        CANCELLED
    }

    private final UUID id;
    private final short slot;
    private final String ownerId;
    private final long slotGeneration;
    private final long initiationOrder;
    private final String state;
    private final String nonce;
    private final String codeVerifier;
    private final String codeChallenge;
    private final byte[] proofDigest;
    private final String redirectUri;
    private final Instant createdAt;
    private final Instant expiresAt;
    private volatile Status status;
    private volatile boolean processing;

    public OAuthAttempt(
            UUID id,
            short slot,
            String ownerId,
            long slotGeneration,
            long initiationOrder,
            String state,
            String nonce,
            String codeVerifier,
            String codeChallenge,
            byte[] proofDigest,
            String redirectUri,
            Instant createdAt,
            Instant expiresAt) {
        this.id = id;
        this.slot = slot;
        this.ownerId = ownerId;
        this.slotGeneration = slotGeneration;
        this.initiationOrder = initiationOrder;
        this.state = state;
        this.nonce = nonce;
        this.codeVerifier = codeVerifier;
        this.codeChallenge = codeChallenge;
        this.proofDigest = proofDigest;
        this.redirectUri = redirectUri;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = Status.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public short getSlot() {
        return slot;
    }

    public String getOwnerId() { return ownerId; }

    public long getSlotGeneration() { return slotGeneration; }

    public long getInitiationOrder() { return initiationOrder; }

    public boolean isProcessing() { return processing; }

    void setProcessing(boolean processing) { this.processing = processing; }

    public String getState() {
        return state;
    }

    public String getNonce() {
        return nonce;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public byte[] getProofDigest() {
        return proofDigest;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
