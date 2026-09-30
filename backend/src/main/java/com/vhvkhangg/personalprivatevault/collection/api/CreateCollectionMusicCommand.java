package com.vhvkhangg.personalprivatevault.collection.api;

/**
 * Public parent collection command to create a music track.
 */
public record CreateCollectionMusicCommand(
        String title,
        CollectionMusicVersion version,
        Long platformId,
        String url
) {}
