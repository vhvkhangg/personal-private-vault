package com.vhvkhangg.personalprivatevault.location.category;

import com.vhvkhangg.personalprivatevault.location.view.LocationCategoryView;

import java.util.List;

/**
 * Public capability-oriented operations for Location Categories and Category assignments.
 */
public interface LocationCategoryOperations {

    /**
     * Creates a new Location Category with a case-insensitively unique name.
     *
     * @param command creation command
     * @return the created category view
     * @throws InvalidLocationCategoryException if input parameters are invalid
     * @throws LocationCategoryNameAlreadyExistsException if name already exists case-insensitively
     */
    LocationCategoryView create(CreateLocationCategoryCommand command);

    /**
     * Updates an existing Location Category.
     *
     * @param command update command
     * @return the updated category view
     * @throws LocationCategoryNotFoundException if the category does not exist
     * @throws InvalidLocationCategoryException if input parameters are invalid
     * @throws LocationCategoryNameAlreadyExistsException if name already exists case-insensitively
     */
    LocationCategoryView update(UpdateLocationCategoryCommand command);

    /**
     * Finds a Location Category by its ID.
     *
     * @param id category ID
     * @return the category view
     * @throws LocationCategoryNotFoundException if the category does not exist
     */
    LocationCategoryView findById(Long id);

    /**
     * Finds a Location Category by its name (case-insensitive).
     *
     * @param name category name
     * @return the category view
     * @throws LocationCategoryNotFoundException if the category does not exist
     */
    LocationCategoryView findByName(String name);

    /**
     * Assigns a category to a location idempotently.
     *
     * @param locationId location ID
     * @param categoryId category ID
     */
    void assignCategoryToLocation(Long locationId, Long categoryId);

    /**
     * Finds all categories assigned to the specified location.
     *
     * @param locationId location ID
     * @return list of assigned category views
     */
    List<LocationCategoryView> findCategoriesByLocationId(Long locationId);
}
