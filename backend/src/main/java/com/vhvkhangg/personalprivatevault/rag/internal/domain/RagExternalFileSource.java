package com.vhvkhangg.personalprivatevault.rag.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "external_file_sources", schema = "rag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagExternalFileSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_source_id", nullable = false, unique = true)
    private UUID fileSourceId;

    @Column(name = "canonical_identity_digest", nullable = false, length = 128)
    private String canonicalIdentityDigest;

    @Column(name = "source_version", nullable = false)
    private long sourceVersion;

    @Column(name = "consent_reset_version", nullable = false)
    private long consentResetVersion;

    @Column(name = "authorization_epoch", nullable = false)
    private long authorizationEpoch;

    @Column(name = "field_policy_version", nullable = false)
    private int fieldPolicyVersion;

    @Column(name = "eligible", nullable = false)
    private boolean eligible;

    @Column(name = "reserved_by")
    private UUID reservedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public RagExternalFileSource(
            UUID fileSourceId,
            String canonicalIdentityDigest,
            long sourceVersion,
            long consentResetVersion,
            long authorizationEpoch,
            int fieldPolicyVersion,
            boolean eligible,
            Instant now
    ) {
        this.fileSourceId = Objects.requireNonNull(fileSourceId, "fileSourceId must not be null");
        this.canonicalIdentityDigest = Objects.requireNonNull(canonicalIdentityDigest, "canonicalIdentityDigest must not be null");
        this.sourceVersion = sourceVersion > 0 ? sourceVersion : 1;
        this.consentResetVersion = consentResetVersion > 0 ? consentResetVersion : 1;
        this.authorizationEpoch = authorizationEpoch > 0 ? authorizationEpoch : 1;
        this.fieldPolicyVersion = fieldPolicyVersion > 0 ? fieldPolicyVersion : 1;
        this.eligible = eligible;
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
        this.updatedAt = now;
    }

    public void advanceVersion(boolean resetConsent, Instant now) {
        this.sourceVersion = Math.incrementExact(this.sourceVersion);
        if (resetConsent) {
            this.consentResetVersion = this.sourceVersion;
            this.authorizationEpoch = Math.incrementExact(this.authorizationEpoch);
        }
        this.updatedAt = Objects.requireNonNull(now);
    }

    public void setEligible(boolean eligible, Instant now) {
        this.eligible = eligible;
        this.updatedAt = Objects.requireNonNull(now);
    }
}
