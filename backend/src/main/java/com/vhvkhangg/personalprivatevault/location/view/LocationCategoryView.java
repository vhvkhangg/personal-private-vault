package com.vhvkhangg.personalprivatevault.location.view;

/**
 * Immutable view of a Location Category.
 */
public record LocationCategoryView(
        Long id,
        String name,
        String description
) {
}
