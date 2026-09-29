package com.vhvkhangg.personalprivatevault.location.location;

import com.vhvkhangg.personalprivatevault.location.enums.DiningServiceStyle;
import com.vhvkhangg.personalprivatevault.location.view.LocationView;

import java.util.Set;

/**
 * Public capability-oriented operations for physical Locations and dining service styles.
 */
public interface LocationOperations {

    /**
     * Creates a new Location, backed by a Vault Entry of type LOCATION.
     *
     * @param command creation command
     * @return the created location view
     * @throws InvalidLocationException if input parameters are invalid
     */
    LocationView create(CreateLocationCommand command);

    /**
     * Updates an existing Location.
     *
     * @param command update command
     * @return the updated location view
     * @throws LocationNotFoundException if the location does not exist
     * @throws InvalidLocationException if input parameters are invalid
     */
    LocationView update(UpdateLocationCommand command);

    /**
     * Finds a Location by its ID.
     *
     * @param id location ID
     * @return the location view
     * @throws LocationNotFoundException if the location does not exist
     */
    LocationView findById(Long id);

    /**
     * Assigns a dining service style to a location idempotently.
     *
     * @param locationId location ID
     * @param style dining service style
     */
    void assignDiningServiceStyle(Long locationId, DiningServiceStyle style);

    /**
     * Finds all dining service styles assigned to the specified location.
     *
     * @param locationId location ID
     * @return set of assigned dining service styles
     */
    Set<DiningServiceStyle> findDiningServiceStylesByLocationId(Long locationId);
}
