package com.vhvkhangg.personalprivatevault.authentication.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @jakarta.persistence.Column(name = "session_id")
    private java.util.UUID sessionId;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @jakarta.persistence.Column(name = "profile", length = 7)
    private LoginProfile profile;

    @jakarta.persistence.Column(name = "session_generation")
    private Long sessionGeneration;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token_id", unique = true)
    private Long replacedByTokenId;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public RefreshToken(
            AppUser user,
            java.util.UUID sessionId,
            LoginProfile profile,
            Long sessionGeneration,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt
    ) {
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId must not be null");
        this.profile = Objects.requireNonNull(profile, "profile must not be null");
        this.sessionGeneration = Objects.requireNonNull(sessionGeneration, "sessionGeneration must not be null");
        this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    @Deprecated
    public RefreshToken(AppUser user, String tokenHash, Instant expiresAt, Instant createdAt) {
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isReplaced() {
        return replacedByTokenId != null;
    }

    public boolean isValid(Instant now) {
        return !isExpired(now) && !isRevoked() && !isReplaced();
    }

    public void revoke(Instant now) {
        this.revokedAt = Objects.requireNonNull(now, "now must not be null");
    }

    public void replaceWith(Long successorId, Instant now) {
        this.replacedByTokenId = Objects.requireNonNull(successorId, "successorId must not be null");
        this.revokedAt = Objects.requireNonNull(now, "now must not be null");
    }
}
