package com.vhvkhangg.personalprivatevault.importdata.internal.web.dto;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;

import java.util.Map;

public record ImportJobItemResponse(
        Long id,
        Long importJobId,
        int itemIndex,
        Map<String, Object> parsedPayload,
        ImportItemStatus status,
        Long duplicateVaultEntryId,
        String errorMessage,
        Long importedVaultEntryId
) {}
