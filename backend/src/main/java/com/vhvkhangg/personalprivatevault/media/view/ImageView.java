package com.vhvkhangg.personalprivatevault.media.view;

import java.time.Instant;

/**
 * Immutable view of an Image metadata record.
 */
public record ImageView(
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
