package com.vhvkhangg.personalprivatevault.media.image;

import java.time.Instant;

/**
 * Command to update metadata attributes of an existing Image.
 */
public record UpdateImageMetadataCommand(
        Long id,
        Long albumId,
        String title,
        String imageType,
        String sourceUrl,
        String mimeType,
        Long sizeBytes,
        Integer widthPx,
        Integer heightPx,
        Instant capturedAt,
        String locationText
) {
}
