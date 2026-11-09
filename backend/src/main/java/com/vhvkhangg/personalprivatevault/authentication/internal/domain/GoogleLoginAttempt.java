package com.vhvkhangg.personalprivatevault.authentication.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Ephemeral OIDC transaction attempt or durable failed LINK-password rate event.
 */
@Entity
@Table(name = "ppv_google_login_attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoogleLoginAttempt {

    public static final String KIND_OIDC = "OIDC";
    public static final String KIND_FAILURE = "LINK_PASSWORD_FAILURE";

    public static final String PURPOSE_LOGIN = "LOGIN";
    public static final String PURPOSE_LINK = "LINK";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    @Id
    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @Column(name = "record_kind", nullable = false, length = 24)
    private String recordKind;

    @Column(name = "purpose", nullable = false, length = 8)
    private String purpose;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @JdbcTypeCode(Types.CHAR)
    @Column(name = "state_hash", length = 64)
    private String stateHash;

    @JdbcTypeCode(Types.CHAR)
    @Column(name = "binding_hash", length = 64)
    private String bindingHash;

    @JdbcTypeCode(Types.CHAR)
    @Column(name = "nonce_hash", length = 64)
    private String nonceHash;

    @JdbcTypeCode(Types.CHAR)
    @Column(name = "handoff_challenge_hash", length = 64)
    private String handoffChallengeHash;

    @Column(name = "pkce_nonce")
    private byte[] pkceNonce;

    @Column(name = "pkce_ciphertext")
    private byte[] pkceCiphertext;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "claim_epoch")
    private UUID claimEpoch;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public static GoogleLoginAttempt createOidc(
            UUID attemptId,
            String purpose,
            AppUser owner,
            String stateHash,
            String bindingHash,
            String nonceHash,
            String handoffChallengeHash,
            byte[] pkceNonce,
            byte[] pkceCiphertext,
            Instant createdAt
    ) {
        GoogleLoginAttempt a = new GoogleLoginAttempt();
        a.attemptId = Objects.requireNonNull(attemptId, "attemptId must not be null");
        a.recordKind = KIND_OIDC;
        a.purpose = Objects.requireNonNull(purpose, "purpose must not be null");
        a.owner = owner;
        a.stateHash = Objects.requireNonNull(stateHash, "stateHash must not be null");
        a.bindingHash = Objects.requireNonNull(bindingHash, "bindingHash must not be null");
        a.nonceHash = Objects.requireNonNull(nonceHash, "nonceHash must not be null");
        a.handoffChallengeHash = Objects.requireNonNull(handoffChallengeHash, "handoffChallengeHash must not be null");
        a.pkceNonce = Objects.requireNonNull(pkceNonce, "pkceNonce must not be null");
        a.pkceCiphertext = Objects.requireNonNull(pkceCiphertext, "pkceCiphertext must not be null");
        a.status = STATUS_PENDING;
        a.claimEpoch = null;
        a.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        a.updatedAt = createdAt;
        a.expiresAt = createdAt.plusSeconds(300);
        a.completedAt = null;
        return a;
    }

    public static GoogleLoginAttempt createPasswordFailure(UUID attemptId, AppUser owner, Instant now) {
        GoogleLoginAttempt a = new GoogleLoginAttempt();
        a.attemptId = Objects.requireNonNull(attemptId, "attemptId must not be null");
        a.recordKind = KIND_FAILURE;
        a.purpose = PURPOSE_LINK;
        a.owner = Objects.requireNonNull(owner, "owner must not be null");
        a.stateHash = null;
        a.bindingHash = null;
        a.nonceHash = null;
        a.handoffChallengeHash = null;
        a.pkceNonce = null;
        a.pkceCiphertext = null;
        a.status = STATUS_FAILED;
        a.claimEpoch = null;
        a.createdAt = Objects.requireNonNull(now, "now must not be null");
        a.updatedAt = now;
        a.expiresAt = now;
        a.completedAt = now;
        return a;
    }

    public void claim(UUID claimEpoch, Instant now) {
        if (!STATUS_PENDING.equals(this.status)) {
            throw new IllegalStateException("Cannot claim attempt in status: " + this.status);
        }
        this.status = STATUS_PROCESSING;
        this.claimEpoch = Objects.requireNonNull(claimEpoch, "claimEpoch must not be null");
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
    }

    public void complete(Instant now) {
        if (!STATUS_PROCESSING.equals(this.status)) {
            throw new IllegalStateException("Cannot complete attempt in status: " + this.status);
        }
        this.status = STATUS_COMPLETED;
        this.completedAt = Objects.requireNonNull(now, "now must not be null");
        this.updatedAt = now;
        this.pkceNonce = null;
        this.pkceCiphertext = null;
    }

    public void fail(Instant now) {
        this.status = STATUS_FAILED;
        this.completedAt = Objects.requireNonNull(now, "now must not be null");
        this.updatedAt = now;
        this.pkceNonce = null;
        this.pkceCiphertext = null;
    }

    public void expire(Instant now) {
        this.status = STATUS_EXPIRED;
        this.completedAt = Objects.requireNonNull(now, "now must not be null");
        this.updatedAt = now;
        this.pkceNonce = null;
        this.pkceCiphertext = null;
    }
}
