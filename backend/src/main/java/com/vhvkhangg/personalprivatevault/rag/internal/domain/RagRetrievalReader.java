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
@Table(name = "retrieval_readers", schema = "rag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagRetrievalReader {

    @Id
    @Column(name = "reader_id", nullable = false)
    private UUID readerId;

    @Column(name = "reservation_token", nullable = false, unique = true)
    private UUID reservationToken;

    @Column(name = "scope_digest", nullable = false, length = 64)
    private String scopeDigest;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scope_payload", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> scopePayload;

    @Column(name = "state", nullable = false, length = 32)
    private String state;

    @Column(name = "stop_requested", nullable = false)
    private boolean stopRequested;

    @Column(name = "emission_started", nullable = false)
    private boolean emissionStarted;

    @Column(name = "emission_disposition", nullable = false, length = 32)
    private String emissionDisposition;

    @Column(name = "controller_instance", nullable = false, length = 255)
    private String controllerInstance;

    @Column(name = "worker_pid")
    private Long workerPid;

    @Column(name = "worker_created_at")
    private Instant workerCreatedAt;

    @Column(name = "worker_creation_file_time")
    private Long workerCreationFileTime;

    @Column(name = "finish_disposition", length = 32)
    private String finishDisposition;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quiescence_evidence", columnDefinition = "jsonb")
    private Map<String, Object> quiescenceEvidence;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public RagRetrievalReader(
            UUID readerId,
            UUID reservationToken,
            String scopeDigest,
            Map<String, Object> scopePayload,
            String state,
            String controllerInstance,
            Instant now
    ) {
        this.readerId = Objects.requireNonNull(readerId, "readerId must not be null");
        this.reservationToken = Objects.requireNonNull(reservationToken, "reservationToken must not be null");
        this.scopeDigest = Objects.requireNonNull(scopeDigest, "scopeDigest must not be null");
        this.scopePayload = scopePayload != null ? Map.copyOf(scopePayload) : Map.of();
        this.state = Objects.requireNonNull(state, "state must not be null");
        this.controllerInstance = Objects.requireNonNull(controllerInstance, "controllerInstance must not be null");
        this.stopRequested = false;
        this.emissionStarted = false;
        this.emissionDisposition = "PENDING";
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
        this.updatedAt = now;
    }

    public void bindWorker(long pid, Instant createdAt, Instant now) {
        this.workerPid = pid;
        this.workerCreatedAt = createdAt;
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
    }

    public void requestStop(Instant now) {
        this.stopRequested = true;
        if ("ADMITTED".equals(this.state) || "CONSUMING".equals(this.state)) {
            this.state = "STOP_REQUESTED";
        }
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
    }
}
