package com.vhvkhangg.personalprivatevault.fiction.internal.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(name = "PublicFictionDetailResponse", description = "Public fiction work detail with author name", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public record PublicFictionDetailResponse(
        @Schema(description = "Fiction ID (identical to underlying vault entry ID)", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(description = "Primary title", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @Schema(description = "Optional original/native title", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String originalTitle,
        @Schema(description = "Optional 2-character country code", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String nationalityCode,
        @Schema(description = "Optional cover or poster image URL", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String posterUrl,
        @Schema(description = "Format of the work (NOVEL or COMIC)", requiredMode = Schema.RequiredMode.REQUIRED)
        FictionFormat format,
        @Schema(description = "Whether the work is marked NSFW (always false on public success)", allowableValues = {"false"}, requiredMode = Schema.RequiredMode.REQUIRED)
        boolean isNsfw,
        @Schema(description = "ID of the fiction-owned genre", requiredMode = Schema.RequiredMode.REQUIRED)
        Long genreId,
        @Schema(description = "ID of the author person (mutually exclusive with authorGroupId)", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Long authorPersonId,
        @Schema(description = "ID of the author creator group (mutually exclusive with authorPersonId)", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Long authorGroupId,
        @Schema(description = "Optional description / synopsis", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String description,
        @Schema(description = "Optional total chapter count", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Integer totalChapters,
        @Schema(description = "Publication progress status", requiredMode = Schema.RequiredMode.REQUIRED)
        ProgressStatus progressStatus,
        @Schema(description = "Personal reading/consumption status", requiredMode = Schema.RequiredMode.REQUIRED)
        ConsumptionStatus consumptionStatus,
        @Schema(description = "Optional progress description text", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String currentProgressText,
        @Schema(description = "Optional personal review notes", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String review,
        @Schema(description = "Set of assigned story archetype IDs", requiredMode = Schema.RequiredMode.REQUIRED)
        Set<Long> storyArchetypeIds,
        @Schema(description = "Set of assigned world setting IDs", requiredMode = Schema.RequiredMode.REQUIRED)
        Set<Long> worldSettingIds,
        @Schema(description = "Resolved author person or creator group name", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String authorName
) {
}
