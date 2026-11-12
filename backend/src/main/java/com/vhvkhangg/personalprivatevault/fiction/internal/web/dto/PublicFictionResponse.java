package com.vhvkhangg.personalprivatevault.fiction.internal.web.dto;

import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PublicFictionResponse", description = "Public fiction work detail", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public record PublicFictionResponse(
        @Schema(description = "Fiction ID (identical to underlying vault entry ID)", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(description = "Primary title", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @Schema(description = "Optional original/native title", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String originalTitle,
        @Schema(description = "Optional 2-character country code", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String nationalityCode,
        @Schema(description = "Format of the work (NOVEL or COMIC)", requiredMode = Schema.RequiredMode.REQUIRED)
        FictionFormat format,
        @Schema(description = "Whether the work is marked NSFW (always false on public success)", allowableValues = {"false"}, requiredMode = Schema.RequiredMode.REQUIRED)
        boolean isNsfw
) {
}
