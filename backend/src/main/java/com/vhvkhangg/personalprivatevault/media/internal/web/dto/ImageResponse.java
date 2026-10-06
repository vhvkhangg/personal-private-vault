package com.vhvkhangg.personalprivatevault.media.internal.web.dto;

import java.time.Instant;

public record ImageResponse(
        Long id,
        Long albumId,
        String title,
        String imageType,
        String objectKey,
        String sourceUrl,
        String mimeType,
        Long sizeBytes,
        Integer widthPx,
        Integer heightPx,
        String checksumSha256,
        Instant capturedAt,
        String locationText
) {
}
