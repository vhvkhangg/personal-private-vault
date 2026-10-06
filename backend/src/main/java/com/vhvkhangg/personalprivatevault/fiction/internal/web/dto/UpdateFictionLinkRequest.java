package com.vhvkhangg.personalprivatevault.fiction.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFictionLinkRequest(
        @Size(max = 10, message = "Language code must not exceed 10 characters")
        String languageCode,
        @NotBlank(message = "Link type must not be blank")
        @Size(max = 50, message = "Link type must not exceed 50 characters")
        String linkType,
        @Size(max = 255, message = "Label must not exceed 255 characters")
        String label,
        @NotBlank(message = "URL must not be blank")
        @Size(max = 2048, message = "URL must not exceed 2048 characters")
        String url,
        Boolean isPrimary
) {
}
