package com.vhvkhangg.personalprivatevault.media.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateImageRequest(
        Long albumId,
        @Size(max = 500, message = "Title must not exceed 500 characters")
        String title,
        @Size(max = 100, message = "Image type must not exceed 100 characters")
        String imageType,
        @Size(max = 2048, message = "Source URL must not exceed 2048 characters")
        String sourceUrl,
        @Size(max = 100, message = "MIME type must not exceed 100 characters")
        String mimeType,
        Long sizeBytes,
        Integer widthPx,
        Integer heightPx,
        Instant capturedAt,
        @Size(max = 500, message = "Location text must not exceed 500 characters")
        String locationText
) {
}
