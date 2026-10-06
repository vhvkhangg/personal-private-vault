package com.vhvkhangg.personalprivatevault.film.internal.web.dto;

import java.time.Instant;

public record FilmLinkResponse(
        Long id,
        Long filmId,
        String languageCode,
        String label,
        String url,
        boolean isPrimary,
        Instant createdAt
) {
}
