package com.vhvkhangg.personalprivatevault.media.image;

import java.time.Instant;

/**
 * Command to create a new Image metadata record backed by a Vault Entry of type IMAGE.
 */
public record CreateImageCommand(
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
