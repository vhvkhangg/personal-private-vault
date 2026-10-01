package com.vhvkhangg.personalprivatevault.importdata.internal.domain;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportFormat;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportJobStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

/**
 * Import job entity mapped to {@code import_jobs}.
 */
@Entity
@Table(name = "import_jobs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "target_type", nullable = false)
    private ImportTargetType targetType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "format", nullable = false)
    private ImportFormat format;

    @Column(name = "original_file_name", length = 500, nullable = false)
    private String originalFileName;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "raw_file_object_key", length = 1024)
    private String rawFileObjectKey;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private ImportJobStatus status;

    @Column(name = "total_items", nullable = false)
    private int totalItems;

    @Column(name = "valid_items", nullable = false)
    private int validItems;

    @Column(name = "duplicate_items", nullable = false)
    private int duplicateItems;

    @Column(name = "invalid_items", nullable = false)
    private int invalidItems;

    @Column(name = "imported_items", nullable = false)
    private int importedItems;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ImportJob(
            ImportTargetType targetType,
            ImportFormat format,
            String originalFileName,
            String rawFileObjectKey
    ) {
        this.targetType = Objects.requireNonNull(targetType, "targetType must not be null");
        this.format = Objects.requireNonNull(format, "format must not be null");
        this.originalFileName = Objects.requireNonNull(originalFileName, "originalFileName must not be null");
        this.rawFileObjectKey = rawFileObjectKey;
        this.status = ImportJobStatus.CREATED;
        this.totalItems = 0;
        this.validItems = 0;
        this.duplicateItems = 0;
        this.invalidItems = 0;
        this.importedItems = 0;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void transitionToParsed(String fileHash, int total, int valid, int invalid) {
        this.fileHash = fileHash;
        this.status = ImportJobStatus.PARSED;
        this.totalItems = total;
        this.validItems = valid;
        this.duplicateItems = 0;
        this.invalidItems = invalid;
        this.importedItems = 0;
    }

    public void transitionToValidated(int valid, int duplicate, int invalid) {
        this.status = ImportJobStatus.VALIDATED;
        this.validItems = valid;
        this.duplicateItems = duplicate;
        this.invalidItems = invalid;
    }

    public void transitionToImported(int importedCount) {
        this.status = ImportJobStatus.IMPORTED;
        this.importedItems = importedCount;
    }

    public void transitionToCancelled() {
        this.status = ImportJobStatus.CANCELLED;
    }

    public void transitionToFailed() {
        this.status = ImportJobStatus.FAILED;
    }

    @PrePersist
    void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = this.createdAt;
        }
    }

    @PreUpdate
    void onPreUpdate() {
        this.updatedAt = Instant.now();
    }
}
