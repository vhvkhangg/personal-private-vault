package com.vhvkhangg.personalprivatevault.importdata.view;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportFormat;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportJobStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;

import java.time.Instant;

/**
 * Immutable read model for an import job.
 */
public record ImportJobView(
        Long id,
        ImportTargetType targetType,
        ImportFormat format,
        String originalFileName,
        String fileHash,
        String rawFileObjectKey,
        ImportJobStatus status,
        int totalItems,
        int validItems,
        int duplicateItems,
        int invalidItems,
        int importedItems,
        Instant createdAt,
        Instant updatedAt
) {
}
