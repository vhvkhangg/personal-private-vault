package com.vhvkhangg.personalprivatevault.film.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFilmLinkRequest(
        @Size(max = 10, message = "Language code must not exceed 10 characters")
        String languageCode,
        @Size(max = 255, message = "Label must not exceed 255 characters")
        String label,
        @NotBlank(message = "URL must not be blank")
        @Size(max = 2048, message = "URL must not exceed 2048 characters")
        String url,
        Boolean isPrimary
) {
}
