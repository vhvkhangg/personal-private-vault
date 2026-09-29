package com.vhvkhangg.personalprivatevault.media.view;

/**
 * Immutable view of an Album, including its derived image count.
 */
public record AlbumView(
        Long id,
        String title,
        String description,
        long imageCount
) {
}
