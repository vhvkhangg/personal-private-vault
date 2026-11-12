package com.vhvkhangg.personalprivatevault.authentication.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ppv_login_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginSession {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private Short ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_profile", nullable = false, length = 7)
    private LoginProfile activeProfile;

    @Column(name = "generation", nullable = false)
    private Long generation;

    @Column(name = "public_credential_epoch")
    private Long publicCredentialEpoch;

    @Column(name = "current_refresh_token_id", nullable = false)
    private Long currentRefreshTokenId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public LoginSession(
            UUID id,
            Short ownerId,
            LoginProfile activeProfile,
            Long generation,
            Long publicCredentialEpoch,
            Long currentRefreshTokenId,
            Instant createdAt,
            Instant expiresAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId must not be null");
        this.activeProfile = Objects.requireNonNull(activeProfile, "activeProfile must not be null");
        this.generation = Objects.requireNonNull(generation, "generation must not be null");
        this.publicCredentialEpoch = publicCredentialEpoch;
        this.currentRefreshTokenId = Objects.requireNonNull(currentRefreshTokenId, "currentRefreshTokenId must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public void revoke(Instant now) {
        this.revokedAt = Objects.requireNonNull(now, "now must not be null");
    }

    public void updateCurrentRefresh(Long newRefreshTokenId, Instant newExpiresAt) {
        this.currentRefreshTokenId = Objects.requireNonNull(newRefreshTokenId, "newRefreshTokenId must not be null");
        this.expiresAt = Objects.requireNonNull(newExpiresAt, "newExpiresAt must not be null");
    }

    public void transitionProfile(LoginProfile newProfile, Long newRefreshTokenId, Instant newExpiresAt, Long newPublicCredentialEpoch) {
        this.activeProfile = Objects.requireNonNull(newProfile, "newProfile must not be null");
        if (this.generation == Long.MAX_VALUE) {
            throw new IllegalStateException("Session generation overflow");
        }
        this.generation++;
        this.currentRefreshTokenId = Objects.requireNonNull(newRefreshTokenId, "newRefreshTokenId must not be null");
        this.expiresAt = Objects.requireNonNull(newExpiresAt, "newExpiresAt must not be null");
        if (newProfile == LoginProfile.PUBLIC) {
            this.publicCredentialEpoch = Objects.requireNonNull(newPublicCredentialEpoch, "publicCredentialEpoch must not be null for PUBLIC");
        }
    }
}
