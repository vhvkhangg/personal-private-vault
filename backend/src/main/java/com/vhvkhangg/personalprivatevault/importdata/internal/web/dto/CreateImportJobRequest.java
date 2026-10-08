package com.vhvkhangg.personalprivatevault.importdata.internal.web.dto;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportFormat;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateImportJobRequest(
        @NotNull ImportTargetType targetType,
        @NotNull ImportFormat format,
        @NotBlank @Size(max = 500) String originalFileName,
        @Size(max = 1024) String rawFileObjectKey
) {}
