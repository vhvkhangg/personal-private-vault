package com.vhvkhangg.personalprivatevault.collection.api;

/**
 * Public parent collection command to update a music track with full scalar replacement semantics.
 */
public record UpdateCollectionMusicCommand(
        String title,
        CollectionMusicVersion version,
        Long platformId,
        String url
) {}
