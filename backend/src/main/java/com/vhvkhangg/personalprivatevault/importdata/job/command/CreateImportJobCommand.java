package com.vhvkhangg.personalprivatevault.importdata.job.command;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportFormat;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;

/**
 * Command for creating an import job in CREATED status.
 */
public record CreateImportJobCommand(
        ImportTargetType targetType,
        ImportFormat format,
        String originalFileName,
        String rawFileObjectKey
) {
}
