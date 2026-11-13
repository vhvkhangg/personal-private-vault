package com.vhvkhangg.personalprivatevault.film.internal.web.dto;

import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Public film creation request DTO bounded to exactly 5 scalar properties.
 * No relationship, NSFW, or internal progress fields permitted.
 */
@Schema(
        name = "PublicCreateFilmRequest",
        description = "Public film creation request payload (strictly 5 properties, no client-supplied isNsfw, relational, or consumption fields)",
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE
)
public record PublicCreateFilmRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 500, message = "Title must not exceed 500 characters")
        @Schema(description = "Primary title", example = "Sample Film", maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Size(max = 500, message = "Original title must not exceed 500 characters")
        @Schema(description = "Optional original/native title", example = "Sample Original Title", maxLength = 500, nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String originalTitle,

        @Size(max = 2, message = "Nationality code must not exceed 2 characters")
        @Schema(description = "Optional 2-character country code", example = "VN", maxLength = 2, nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String nationalityCode,

        @NotNull(message = "Format must not be null")
        @Schema(description = "Format of the work (MOVIE or SERIES)", example = "MOVIE", requiredMode = Schema.RequiredMode.REQUIRED)
        FilmFormat format,

        @NotNull(message = "Production style must not be null")
        @Schema(description = "Production style (LIVE_ACTION or ANIMATION)", example = "LIVE_ACTION", requiredMode = Schema.RequiredMode.REQUIRED)
        FilmProductionStyle productionStyle
) {
}
