package com.vhvkhangg.personalprivatevault.collection.api;

/**
 * Public parent collection view of a supported platform on a software item.
 */
public record CollectionSoftwarePlatformView(
        Long softwareId,
        Long platformId
) {}
