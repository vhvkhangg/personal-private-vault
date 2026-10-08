package com.vhvkhangg.personalprivatevault.fiction.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFictionGenreRequest(
        @NotBlank(message = "Genre name must not be blank")
        @Size(max = 150, message = "Genre name must not exceed 150 characters")
        String name,
        String description
) {
}
