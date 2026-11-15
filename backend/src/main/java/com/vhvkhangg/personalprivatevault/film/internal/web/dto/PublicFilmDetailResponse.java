package com.vhvkhangg.personalprivatevault.film.internal.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(name = "PublicFilmDetailResponse", description = "Public film work detail with director name", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public record PublicFilmDetailResponse(
        @Schema(description = "Film ID (identical to underlying vault entry ID)", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(description = "Primary title", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @Schema(description = "Optional original/native title", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String originalTitle,
        @Schema(description = "Optional 2-character country code", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String nationalityCode,
        @Schema(description = "Optional cover or poster image URL", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String posterUrl,
        @Schema(description = "Format of the work (MOVIE or SERIES)", requiredMode = Schema.RequiredMode.REQUIRED)
        FilmFormat format,
        @Schema(description = "Production style (LIVE_ACTION or ANIMATION)", requiredMode = Schema.RequiredMode.REQUIRED)
        FilmProductionStyle productionStyle,
        @Schema(description = "Whether the work is marked NSFW (always false on public success)", allowableValues = {"false"}, requiredMode = Schema.RequiredMode.REQUIRED)
        boolean isNsfw,
        @Schema(description = "Optional ID of the director person", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Long directorPersonId,
        @Schema(description = "Optional description / synopsis", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String description,
        @Schema(description = "Optional total episode count", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        Integer totalEpisodes,
        @Schema(description = "Publication/release progress status", requiredMode = Schema.RequiredMode.REQUIRED)
        ProgressStatus progressStatus,
        @Schema(description = "Personal viewing/consumption status", requiredMode = Schema.RequiredMode.REQUIRED)
        ConsumptionStatus consumptionStatus,
        @Schema(description = "Optional progress description text", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String currentProgressText,
        @Schema(description = "Optional personal review notes", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String review,
        @Schema(description = "Set of assigned film genre IDs", requiredMode = Schema.RequiredMode.REQUIRED)
        Set<Long> genreIds,
        @Schema(description = "Set of assigned story archetype IDs", requiredMode = Schema.RequiredMode.REQUIRED)
        Set<Long> storyArchetypeIds,
        @Schema(description = "Set of assigned world setting IDs", requiredMode = Schema.RequiredMode.REQUIRED)
        Set<Long> worldSettingIds,
        @Schema(description = "Resolved director person name", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String directorName
) {
}
