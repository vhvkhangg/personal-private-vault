package com.vhvkhangg.personalprivatevault.collection.api;

/**
 * Public parent collection view of a music credit.
 */
public record CollectionMusicCreditView(
        Long musicId,
        Long personId,
        CollectionMusicCreditRole role
) {}
