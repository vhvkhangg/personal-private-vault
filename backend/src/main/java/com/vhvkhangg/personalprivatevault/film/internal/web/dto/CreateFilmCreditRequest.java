package com.vhvkhangg.personalprivatevault.film.internal.web.dto;

import com.vhvkhangg.personalprivatevault.film.enums.FilmCreditRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFilmCreditRequest(
        @NotNull(message = "Person ID must not be null")
        Long personId,
        @NotNull(message = "Role must not be null")
        FilmCreditRole role,
        @Size(max = 255, message = "Character name must not exceed 255 characters")
        String characterName,
        String note
) {
}
