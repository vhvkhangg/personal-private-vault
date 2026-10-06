package com.vhvkhangg.personalprivatevault.film.internal.web.dto;

import com.vhvkhangg.personalprivatevault.film.enums.FilmCreditRole;

public record FilmCreditResponse(
        Long id,
        Long filmId,
        Long personId,
        FilmCreditRole role,
        String characterName,
        String note
) {
}
