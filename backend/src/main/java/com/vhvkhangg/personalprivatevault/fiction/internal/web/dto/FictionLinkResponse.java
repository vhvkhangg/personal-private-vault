package com.vhvkhangg.personalprivatevault.fiction.internal.web.dto;

import java.time.Instant;

public record FictionLinkResponse(
        Long id,
        Long fictionId,
        String languageCode,
        String linkType,
        String label,
        String url,
        boolean isPrimary,
        Instant createdAt
) {
}
