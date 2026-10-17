package com.vhvkhangg.personalprivatevault.rag.internal.domain;

import com.vhvkhangg.personalprivatevault.rag.source.RagAccessState;
import com.vhvkhangg.personalprivatevault.rag.source.RagClassification;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "sources", schema = "rag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "approval_instance")
    private java.util.UUID approvalInstance;

    @Column(name = "approved_owner_reset_version")
    private Long approvedOwnerResetVersion;

    public void bindOwnerResetVersion(long version) {
        if (version <= 0) throw new IllegalArgumentException("Invalid owner consent history");
        this.approvedOwnerResetVersion = version;
    }

    public void configureClassification(RagClassification classification, Instant now) {
        Objects.requireNonNull(classification);
        if (this.classification != classification) {
            this.classification = classification;
            revoke(now);
        }
    }

    public void bindApprovalInstance(java.util.UUID instance) {
        this.approvalInstance = Objects.requireNonNull(instance);
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private RagSourceType sourceType;

    @Column(name = "source_id", nullable = false, length = 255)
    private String sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "classification", nullable = false, length = 32)
    private RagClassification classification;

    @Column(name = "authorization_epoch", nullable = false)
    private long authorizationEpoch;

    @Column(name = "field_policy_version", nullable = false)
    private int fieldPolicyVersion;

    @Column(name = "approved_fields_text", nullable = false)
    private String approvedFieldsText;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_state", nullable = false, length = 32)
    private RagAccessState accessState;

    @Column(name = "source_revision_hash", nullable = false, length = 128)
    private String sourceRevisionHash;

    @Column(name = "description_revision_hash", nullable = false, length = 128)
    private String descriptionRevisionHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "file_identity", length = 128)
    private String fileIdentity;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public void setFileIdentity(String fileIdentity) {
        this.fileIdentity = fileIdentity;
    }

    public RagSource(
            RagSourceType sourceType,
            String sourceId,
            RagClassification classification,
            long authorizationEpoch,
            int fieldPolicyVersion,
            Set<String> approvedFields,
            RagAccessState accessState,
            String sourceRevisionHash,
            String descriptionRevisionHash,
            Instant now) {
        this.sourceType = Objects.requireNonNull(sourceType, "sourceType must not be null");
        this.sourceId = Objects.requireNonNull(sourceId, "sourceId must not be null");
        this.classification = Objects.requireNonNull(classification, "classification must not be null");
        this.authorizationEpoch = authorizationEpoch;
        this.fieldPolicyVersion = fieldPolicyVersion;
        this.approvedFieldsText = encodeFields(approvedFields);
        this.accessState = Objects.requireNonNull(accessState, "accessState must not be null");
        this.sourceRevisionHash = Objects.requireNonNull(sourceRevisionHash, "sourceRevisionHash must not be null");
        this.descriptionRevisionHash = Objects.requireNonNull(descriptionRevisionHash, "descriptionRevisionHash must not be null");
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
        this.updatedAt = now;
    }

    public Set<String> getApprovedFields() {
        if (approvedFieldsText == null || approvedFieldsText.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(approvedFieldsText.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    public void approve(
            Set<String> fields,
            String sourceRevisionHash,
            String descriptionRevisionHash,
            Instant now) {
        if (this.classification != RagClassification.NON_PRIVATE) {
            throw new IllegalStateException("Private or unclassified source cannot be approved");
        }
        this.accessState = RagAccessState.APPROVED;
        this.approvedFieldsText = encodeFields(fields);
        this.sourceRevisionHash = Objects.requireNonNull(sourceRevisionHash, "sourceRevisionHash must not be null");
        this.descriptionRevisionHash = Objects.requireNonNull(descriptionRevisionHash, "descriptionRevisionHash must not be null");
        this.authorizationEpoch = Math.incrementExact(this.authorizationEpoch);
        this.fieldPolicyVersion = Math.incrementExact(this.fieldPolicyVersion);
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
        this.revokedAt = null;
    }

    public void revoke(Instant now) {
        this.accessState = RagAccessState.REVOKED;
        this.authorizationEpoch = Math.incrementExact(this.authorizationEpoch);
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
        this.revokedAt = now;
        if(sourceType==RagSourceType.EXTERNAL_FILE) {
            sourceId="disabled:"+id;fileIdentity=null;sourceRevisionHash="DISABLED";
            descriptionRevisionHash="DISABLED";approvedFieldsText="";classification=RagClassification.UNKNOWN;
        }
    }

    public void advanceTopologyEpoch(Instant now) {
        this.authorizationEpoch = Math.incrementExact(this.authorizationEpoch);
        this.updatedAt = Objects.requireNonNull(now);
    }

    public void updateHashes(String sourceRevisionHash, String descriptionRevisionHash, Instant now) {
        this.sourceRevisionHash = Objects.requireNonNull(sourceRevisionHash, "sourceRevisionHash must not be null");
        this.descriptionRevisionHash = Objects.requireNonNull(descriptionRevisionHash, "descriptionRevisionHash must not be null");
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
    }

    private static String encodeFields(Set<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return "";
        }
        return fields.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .sorted()
                .collect(Collectors.joining(","));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RagSource other)) return false;
        return sourceType == other.sourceType && Objects.equals(sourceId, other.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceType, sourceId);
    }
}
