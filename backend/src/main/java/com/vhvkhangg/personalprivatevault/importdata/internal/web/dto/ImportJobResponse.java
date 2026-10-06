package com.vhvkhangg.personalprivatevault.importdata.internal.web.dto;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportFormat;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportJobStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;

import java.time.Instant;

public record ImportJobResponse(
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
) {}
