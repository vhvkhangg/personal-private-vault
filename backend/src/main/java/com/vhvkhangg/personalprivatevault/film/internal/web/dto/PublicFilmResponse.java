package com.vhvkhangg.personalprivatevault.film.internal.web.dto;

import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PublicFilmResponse", description = "Public film work detail", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
public record PublicFilmResponse(
        @Schema(description = "Film ID (identical to underlying vault entry ID)", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(description = "Primary title", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @Schema(description = "Optional original/native title", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String originalTitle,
        @Schema(description = "Optional 2-character country code", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
        String nationalityCode,
        @Schema(description = "Format of the work (MOVIE or SERIES)", requiredMode = Schema.RequiredMode.REQUIRED)
        FilmFormat format,
        @Schema(description = "Production style (LIVE_ACTION or ANIMATION)", requiredMode = Schema.RequiredMode.REQUIRED)
        FilmProductionStyle productionStyle,
        @Schema(description = "Whether the work is marked NSFW (always false on public success)", allowableValues = {"false"}, requiredMode = Schema.RequiredMode.REQUIRED)
        boolean isNsfw
) {
}
