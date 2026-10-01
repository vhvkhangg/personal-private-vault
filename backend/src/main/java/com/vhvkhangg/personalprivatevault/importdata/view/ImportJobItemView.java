package com.vhvkhangg.personalprivatevault.importdata.view;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;

import java.util.Map;

/**
 * Immutable read model for an individual item inside an import job.
 */
public record ImportJobItemView(
        Long id,
        Long importJobId,
        int itemIndex,
        Map<String, Object> parsedPayload,
        ImportItemStatus status,
        Long duplicateVaultEntryId,
        String errorMessage,
        Long importedVaultEntryId
) {
    public ImportJobItemView {
        parsedPayload = ImportJsonSnapshot.toUnmodifiableSnapshot(parsedPayload);
    }
}
