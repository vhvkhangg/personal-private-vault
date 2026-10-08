package com.vhvkhangg.personalprivatevault.media.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAlbumRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 500, message = "Title must not exceed 500 characters")
        String title,
        String description
) {
}
