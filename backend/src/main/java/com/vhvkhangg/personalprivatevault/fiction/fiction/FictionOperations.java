package com.vhvkhangg.personalprivatevault.fiction.fiction;

import com.vhvkhangg.personalprivatevault.fiction.fiction.command.CreateFictionCommand;
import com.vhvkhangg.personalprivatevault.fiction.fiction.command.UpdateFictionCommand;
import com.vhvkhangg.personalprivatevault.fiction.fiction.exception.FictionNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.fiction.exception.InvalidFictionException;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionClassificationsView;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionView;

import java.util.Optional;
import java.util.Set;

/**
 * Public synchronous capability contract for fiction works and narrative classifications.
 */
public interface FictionOperations {

    /**
     * Creates a new fiction work backed by a newly created Vault Entry with type FICTION.
     *
     * @param command creation command containing fiction attributes
     * @return immutable view of the created fiction
     * @throws InvalidFictionException if attributes or references violate domain validation rules
     */
    FictionView create(CreateFictionCommand command);

    /**
     * Finds an existing fiction by its ID.
     *
     * @param fictionId fiction ID (identical to vault entry ID)
     * @return optional containing the fiction view if found, or empty
     */
    Optional<FictionView> find(Long fictionId);

    /**
     * Updates an existing fiction's attributes.
     *
     * @param command update command
     * @return immutable view of the updated fiction
     * @throws FictionNotFoundException if the fiction does not exist
     * @throws InvalidFictionException if updated attributes violate domain validation rules
     */
    FictionView update(UpdateFictionCommand command);

    /**
     * Idempotently assigns a story archetype to a fiction.
     *
     * @param fictionId fiction ID
     * @param storyArchetypeId story archetype ID from the reference module
     * @throws FictionNotFoundException if the fiction does not exist
     * @throws InvalidFictionException if the reference ID does not exist or inputs are invalid
     */
    void addStoryArchetype(Long fictionId, Long storyArchetypeId);

    /**
     * Retrieves the set of assigned story archetype IDs for a fiction.
     *
     * @param fictionId fiction ID
     * @return immutable set of story archetype IDs
     * @throws FictionNotFoundException if the fiction does not exist
     * @throws InvalidFictionException if inputs are invalid
     */
    Set<Long> getStoryArchetypes(Long fictionId);

    /**
     * Idempotently assigns a world setting to a fiction.
     *
     * @param fictionId fiction ID
     * @param worldSettingId world setting ID from the reference module
     * @throws FictionNotFoundException if the fiction does not exist
     * @throws InvalidFictionException if the reference ID does not exist or inputs are invalid
     */
    void addWorldSetting(Long fictionId, Long worldSettingId);

    /**
     * Retrieves the set of assigned world setting IDs for a fiction.
     *
     * @param fictionId fiction ID
     * @return immutable set of world setting IDs
     * @throws FictionNotFoundException if the fiction does not exist
     * @throws InvalidFictionException if inputs are invalid
     */
    Set<Long> getWorldSettings(Long fictionId);

    /**
     * Retrieves all narrative classifications (story archetypes and world settings) for a fiction.
     *
     * @param fictionId fiction ID
     * @return immutable view of the fiction's classifications
     * @throws FictionNotFoundException if the fiction does not exist
     * @throws InvalidFictionException if inputs are invalid
     */
    FictionClassificationsView getClassifications(Long fictionId);
}
