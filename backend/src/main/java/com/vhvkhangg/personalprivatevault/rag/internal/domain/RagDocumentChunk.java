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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "document_chunks", schema = "rag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagDocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chunkId;

    @Column(name = "generation_id", nullable = false)
    private Long generationId;

    @Column(name = "source_identity", nullable = false, length = 255)
    private String sourceIdentity;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "locator", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> locator;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structure_metadata", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> structureMetadata;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @org.hibernate.annotations.Array(length = 384)
    @Column(name = "embedding", columnDefinition = "vector(384)")
    private float[] embedding;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RagDocumentChunk(
            Long generationId,
            String sourceIdentity,
            String content,
            Map<String, Object> locator,
            Map<String, Object> structureMetadata,
            float[] embedding,
            Instant now
    ) {
        this.generationId = Objects.requireNonNull(generationId, "generationId must not be null");
        this.sourceIdentity = Objects.requireNonNull(sourceIdentity, "sourceIdentity must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.locator = locator != null ? Map.copyOf(locator) : Map.of();
        this.structureMetadata = structureMetadata != null ? Map.copyOf(structureMetadata) : Map.of();
        this.embedding = embedding != null ? embedding.clone() : null;
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
    }

    public RagDocumentChunk(
            Long generationId,
            String sourceIdentity,
            String content,
            Map<String, Object> locator,
            Map<String, Object> structureMetadata,
            Instant now
    ) {
        this(generationId, sourceIdentity, content, locator, structureMetadata, null, now);
    }
}

