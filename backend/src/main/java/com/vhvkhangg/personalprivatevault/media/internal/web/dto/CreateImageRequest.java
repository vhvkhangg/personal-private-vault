package com.vhvkhangg.personalprivatevault.media.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateImageRequest(
        Long albumId,
        @NotBlank(message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
        @NotBlank(message = "Image type must not be blank")
        @Size(max = 50, message = "Image type must not exceed 50 characters")
        String imageType,
        @NotBlank(message = "Object key must not be blank")
        @Size(max = 1024, message = "Object key must not exceed 1024 characters")
        String objectKey,
        @Size(max = 2048, message = "Source URL must not exceed 2048 characters")
        String sourceUrl,
        @NotBlank(message = "MIME type must not be blank")
        @Size(max = 100, message = "MIME type must not exceed 100 characters")
        String mimeType,
        @NotNull(message = "Size in bytes must not be null")
        Long sizeBytes,
        Integer widthPx,
        Integer heightPx,
        @NotBlank(message = "SHA-256 checksum must not be blank")
        @Size(max = 64, message = "Checksum must not exceed 64 characters")
        String checksumSha256,
        Instant capturedAt,
        String locationText
) {
}
