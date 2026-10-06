package com.vhvkhangg.personalprivatevault.media.internal.web.dto;

public record AlbumResponse(
        Long id,
        String title,
        String description,
        long imageCount
) {
}
