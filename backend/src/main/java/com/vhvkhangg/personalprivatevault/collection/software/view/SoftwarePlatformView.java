package com.vhvkhangg.personalprivatevault.collection.software.view;

/**
 * Immutable view of a supported platform on a software item.
 */
public record SoftwarePlatformView(
        Long softwareId,
        Long platformId
) {}
