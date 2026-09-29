package com.vhvkhangg.personalprivatevault.location.category;

/**
 * Command to update an existing Location Category.
 */
public record UpdateLocationCategoryCommand(
        Long id,
        String name,
        String description
) {
}
