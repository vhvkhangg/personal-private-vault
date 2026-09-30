package com.vhvkhangg.personalprivatevault.collection.api;

/**
 * Public parent collection view of a music track.
 */
public record CollectionMusicView(
        Long id,
        String title,
        CollectionMusicVersion version,
        Long platformId,
        String url
) {}
