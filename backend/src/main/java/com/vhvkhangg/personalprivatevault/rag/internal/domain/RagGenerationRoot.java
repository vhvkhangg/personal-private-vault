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

@Entity
@Table(name = "generation_roots", schema = "rag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagGenerationRoot {

    @Id
    @Column(name = "generation_id", nullable = false)
    private Long generationId;

    @Column(name = "root_key", nullable = false, length = 255)
    private String rootKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "exact_dependency_manifest", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> exactDependencyManifest;

    @Column(name = "model_artifact_hash", nullable = false, length = 128)
    private String modelArtifactHash;

    @Column(name = "model_version", nullable = false, length = 128)
    private String modelVersion;

    @Column(name = "tokenizer_version", nullable = false, length = 128)
    private String tokenizerVersion;

    @Column(name = "parser_version", nullable = false, length = 128)
    private String parserVersion;

    @Column(name = "chunker_version", nullable = false, length = 128)
    private String chunkerVersion;

    @Column(name = "ocr_pipeline_version", nullable = false, length = 128)
    private String ocrPipelineVersion;

    @Column(name = "vector_dimensions", nullable = false)
    private int vectorDimensions;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RagGenerationRoot(
            Long generationId,
            String rootKey,
            Map<String, Object> exactDependencyManifest,
            String modelArtifactHash,
            String modelVersion,
            String tokenizerVersion,
            String parserVersion,
            String chunkerVersion,
            String ocrPipelineVersion,
            int vectorDimensions,
            Instant now
    ) {
        this.generationId = Objects.requireNonNull(generationId, "generationId must not be null");
        this.rootKey = Objects.requireNonNull(rootKey, "rootKey must not be null");
        this.exactDependencyManifest = exactDependencyManifest != null ? Map.copyOf(exactDependencyManifest) : Map.of();
        this.modelArtifactHash = Objects.requireNonNull(modelArtifactHash, "modelArtifactHash must not be null");
        this.modelVersion = Objects.requireNonNull(modelVersion, "modelVersion must not be null");
        this.tokenizerVersion = Objects.requireNonNull(tokenizerVersion, "tokenizerVersion must not be null");
        this.parserVersion = Objects.requireNonNull(parserVersion, "parserVersion must not be null");
        this.chunkerVersion = Objects.requireNonNull(chunkerVersion, "chunkerVersion must not be null");
        this.ocrPipelineVersion = Objects.requireNonNull(ocrPipelineVersion, "ocrPipelineVersion must not be null");
        this.vectorDimensions = vectorDimensions;
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
    }
}
