package com.vhvkhangg.personalprivatevault.location.category;

/**
 * Command to create a new Location Category.
 */
public record CreateLocationCategoryCommand(
        String name,
        String description
) {
}
