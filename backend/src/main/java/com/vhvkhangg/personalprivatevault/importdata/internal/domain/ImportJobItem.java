package com.vhvkhangg.personalprivatevault.importdata.internal.domain;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJsonSnapshot;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import com.vhvkhangg.personalprivatevault.importdata.internal.infrastructure.persistence.ImportPayloadJsonType;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.Objects;

/**
 * Import job item entity mapped to {@code import_job_items}.
 */
@Entity
@Table(name = "import_job_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImportJobItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "import_job_id", nullable = false)
    private Long importJobId;

    @Column(name = "item_index", nullable = false)
    private int itemIndex;

    @Type(ImportPayloadJsonType.class)
    @Column(name = "parsed_payload", columnDefinition = "jsonb")
    private Map<String, Object> parsedPayload;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private ImportItemStatus status;

    @Column(name = "duplicate_vault_entry_id")
    private Long duplicateVaultEntryId;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "imported_vault_entry_id")
    private Long importedVaultEntryId;

    public ImportJobItem(
            Long importJobId,
            int itemIndex,
            Map<String, Object> parsedPayload,
            ImportItemStatus status,
            String errorMessage
    ) {
        this.importJobId = Objects.requireNonNull(importJobId, "importJobId must not be null");
        this.itemIndex = itemIndex;
        this.parsedPayload = ImportJsonSnapshot.deepCopy(parsedPayload);
        this.status = Objects.requireNonNull(status, "ImportItemStatus must not be null");
        this.errorMessage = errorMessage;
    }

    public void markDuplicate(Long duplicateVaultEntryId) {
        this.status = ImportItemStatus.DUPLICATE;
        this.duplicateVaultEntryId = duplicateVaultEntryId;
    }

    public void markImported(Long importedVaultEntryId) {
        this.status = ImportItemStatus.IMPORTED;
        this.importedVaultEntryId = importedVaultEntryId;
    }

    public void markUpdated(Long importedVaultEntryId) {
        this.status = ImportItemStatus.UPDATED;
        this.importedVaultEntryId = importedVaultEntryId;
    }

    public void markSkipped() {
        this.status = ImportItemStatus.SKIPPED;
    }

    public Map<String, Object> getParsedPayload() {
        return ImportJsonSnapshot.toUnmodifiableSnapshot(this.parsedPayload);
    }
}
