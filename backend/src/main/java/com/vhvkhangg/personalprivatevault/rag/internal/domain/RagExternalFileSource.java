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

    @Column(name = "selected_revision_id")
    private UUID selectedRevisionId;

    @Column(name = "selected_revision_hash", length = 128)
    private String selectedRevisionHash;

    @Column(name = "selected_byte_length")
    private Long selectedByteLength;

    @Column(name = "snapshot_integrity_failed", nullable = false)
    private boolean snapshotIntegrityFailed;

    public void denySnapshotIntegrity(Instant now) {
        this.snapshotIntegrityFailed = true;
        this.updatedAt = Objects.requireNonNull(now);
    }

    @Column(name = "pending_revision_id")
    private UUID pendingRevisionId;

    @Column(name = "approved_pending_revision_id")
    private UUID approvedPendingRevisionId;

    public void approvePendingRevision(UUID revisionId, Instant now) {
        if (!Objects.equals(pendingRevisionId, revisionId)) throw new IllegalStateException("Pending revision changed");
        this.approvedPendingRevisionId=Objects.requireNonNull(revisionId);
        this.updatedAt=Objects.requireNonNull(now);
    }

    @Column(name = "pending_consent_context", length = 160)
    private String pendingConsentContext;

    public void bindPendingConsent(long sourceEpoch, int sourcePolicy) {
        this.pendingConsentContext=consentContext(sourceEpoch,sourcePolicy);
    }

    public boolean pendingConsentMatches(long sourceEpoch, int sourcePolicy) {
        return Objects.equals(this.pendingConsentContext,consentContext(sourceEpoch,sourcePolicy))
                && Objects.equals(expectedSourceVersion,sourceVersion)
                && Objects.equals(expectedSelectedRevisionId,selectedRevisionId);
    }

    private String consentContext(long sourceEpoch,int sourcePolicy) {
        return sourceEpoch+":"+sourcePolicy+":"+consentResetVersion+":"+authorizationEpoch+":"+fieldPolicyVersion;
    }

    @Column(name = "pending_revision_hash", length = 128)
    private String pendingRevisionHash;

    @Column(name = "pending_byte_length")
    private Long pendingByteLength;

    @Column(name = "pending_sealed_at")
    private Instant pendingSealedAt;

    @Column(name = "expected_selected_revision_id")
    private UUID expectedSelectedRevisionId;

    @Column(name = "expected_source_version")
    private Long expectedSourceVersion;

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

    public void setPendingRevision(
            UUID revisionId,
            String sha256,
            long byteLength,
            Instant sealedAt,
            UUID expectedSelectedRevisionId,
            Long expectedSourceVersion,
            Instant now
    ) {
        this.pendingConsentContext = null;
        this.approvedPendingRevisionId = null;
        this.pendingRevisionId = Objects.requireNonNull(revisionId, "revisionId must not be null");
        this.pendingRevisionHash = Objects.requireNonNull(sha256, "sha256 must not be null");
        this.pendingByteLength = byteLength;
        this.pendingSealedAt = Objects.requireNonNull(sealedAt, "sealedAt must not be null");
        this.expectedSelectedRevisionId = expectedSelectedRevisionId;
        this.expectedSourceVersion = expectedSourceVersion;
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
    }

    public void clearPendingRevision(Instant now) {
        this.pendingConsentContext = null;
        this.approvedPendingRevisionId = null;
        this.pendingRevisionId = null;
        this.pendingRevisionHash = null;
        this.pendingByteLength = null;
        this.pendingSealedAt = null;
        this.expectedSelectedRevisionId = null;
        this.expectedSourceVersion = null;
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
    }

    public void setSelectedRevision(UUID revisionId, String sha256, long byteLength, Instant now) {
        this.selectedRevisionId = revisionId;
        this.selectedRevisionHash = Objects.requireNonNull(sha256);
        this.selectedByteLength = byteLength;
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
    }

    public void promotePendingToSelected(Instant now) {
        if (this.pendingRevisionId == null) {
            throw new IllegalStateException("Cannot promote null pending revision");
        }
        this.selectedRevisionId = this.pendingRevisionId;
        this.selectedRevisionHash = this.pendingRevisionHash;
        this.selectedByteLength = this.pendingByteLength;
        this.sourceVersion = Math.incrementExact(this.sourceVersion);
        this.clearPendingRevision(now);
    }
}
