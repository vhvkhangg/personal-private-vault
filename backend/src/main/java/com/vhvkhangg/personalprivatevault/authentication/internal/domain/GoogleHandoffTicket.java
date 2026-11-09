package com.vhvkhangg.personalprivatevault.authentication.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.Instant;
import java.util.Objects;

/**
 * Ephemeral one-use handover ticket redeemed for canonical PPV tokens.
 */
@Entity
@Table(name = "ppv_google_handoff_tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoogleHandoffTicket {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONSUMED = "CONSUMED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    @Id
    @JdbcTypeCode(Types.CHAR)
    @Column(name = "ticket_hash", nullable = false, length = 64)
    private String ticketHash;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false, unique = true)
    private GoogleLoginAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Column(name = "purpose", nullable = false, length = 8)
    private String purpose;

    @JdbcTypeCode(Types.CHAR)
    @Column(name = "handoff_challenge_hash", nullable = false, length = 64)
    private String handoffChallengeHash;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    public GoogleHandoffTicket(
            String ticketHash,
            GoogleLoginAttempt attempt,
            AppUser owner,
            String purpose,
            String handoffChallengeHash,
            Instant issuedAt
    ) {
        this.ticketHash = Objects.requireNonNull(ticketHash, "ticketHash must not be null");
        this.attempt = Objects.requireNonNull(attempt, "attempt must not be null");
        this.owner = Objects.requireNonNull(owner, "owner must not be null");
        this.purpose = Objects.requireNonNull(purpose, "purpose must not be null");
        this.handoffChallengeHash = Objects.requireNonNull(handoffChallengeHash, "handoffChallengeHash must not be null");
        this.status = STATUS_PENDING;
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt must not be null");
        this.expiresAt = issuedAt.plusSeconds(60);
        this.consumedAt = null;
    }

    public void consume(Instant now) {
        if (!STATUS_PENDING.equals(this.status)) {
            throw new IllegalStateException("Cannot consume ticket in status: " + this.status);
        }
        this.status = STATUS_CONSUMED;
        this.consumedAt = Objects.requireNonNull(now, "now must not be null");
    }

    public void expire(Instant now) {
        this.status = STATUS_EXPIRED;
    }
}
