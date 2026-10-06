package com.vhvkhangg.personalprivatevault.media.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateImageRequest(
        Long albumId,
        @NotBlank(message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
        @NotBlank(message = "Image type must not be blank")
        @Size(max = 50, message = "Image type must not exceed 50 characters")
        String imageType,
        @Size(max = 2048, message = "Source URL must not exceed 2048 characters")
        String sourceUrl,
        @NotBlank(message = "MIME type must not be blank")
        @Size(max = 100, message = "MIME type must not exceed 100 characters")
        String mimeType,
        @NotNull(message = "Size in bytes must not be null")
        Long sizeBytes,
        Integer widthPx,
        Integer heightPx,
        Instant capturedAt,
        String locationText
) {
}
