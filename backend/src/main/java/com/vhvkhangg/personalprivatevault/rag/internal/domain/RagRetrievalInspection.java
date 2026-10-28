package com.vhvkhangg.personalprivatevault.rag.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "retrieval_inspections", schema = "rag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagRetrievalInspection {

    @Id
    @Column(name = "inspection_id", nullable = false)
    private UUID inspectionId;

    @Column(name = "reader_id")
    private UUID readerId;

    @Column(name = "producer", nullable = false, length = 64)
    private String producer;

    @Column(name = "scope_digest", nullable = false, length = 64)
    private String scopeDigest;

    @Column(name = "affected_file_source_id", nullable = false)
    private UUID affectedFileSourceId;

    @Column(name = "affected_revision_id")
    private UUID affectedRevisionId;

    @Column(name = "state", nullable = false, length = 32)
    private String state;

    @Column(name = "deny_new_admission", nullable = false)
    private boolean denyNewAdmission;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="inspection_proof",columnDefinition="jsonb",nullable=false)
    private Map<String,Object> inspectionProof=Map.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb")
    private Map<String, Object> details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public RagRetrievalInspection(
            UUID inspectionId,
            UUID readerId,
            String producer,
            String scopeDigest,
            UUID affectedFileSourceId,
            UUID affectedRevisionId,
            boolean denyNewAdmission,
            Instant now
    ) {
        this.inspectionId = Objects.requireNonNull(inspectionId, "inspectionId must not be null");
        this.readerId = readerId;
        this.producer = Objects.requireNonNull(producer, "producer must not be null");
        this.scopeDigest = Objects.requireNonNull(scopeDigest, "scopeDigest must not be null");
        this.affectedFileSourceId = Objects.requireNonNull(affectedFileSourceId, "affectedFileSourceId must not be null");
        this.affectedRevisionId = affectedRevisionId;
        this.state = "UNRESOLVED";
        this.denyNewAdmission = denyNewAdmission;
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
        this.updatedAt = now;
    }

    public void recordClassification(String state, boolean denyNewAdmission, Map<String, Object> details, Instant now) {
        this.state = Objects.requireNonNull(state, "state must not be null");
        this.denyNewAdmission = denyNewAdmission;
        this.details = details != null ? Map.copyOf(details) : null;
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
    }
}
