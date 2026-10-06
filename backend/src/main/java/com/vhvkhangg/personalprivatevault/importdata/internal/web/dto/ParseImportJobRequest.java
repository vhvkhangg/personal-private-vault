package com.vhvkhangg.personalprivatevault.importdata.internal.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ParseImportJobRequest(
        @NotBlank String rawText
) {}
