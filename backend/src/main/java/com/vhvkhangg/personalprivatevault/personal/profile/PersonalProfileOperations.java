package com.vhvkhangg.personalprivatevault.personal.profile;

import com.vhvkhangg.personalprivatevault.personal.profile.command.CreatePersonalProfileCommand;
import com.vhvkhangg.personalprivatevault.personal.profile.command.UpdatePersonalProfileCommand;
import com.vhvkhangg.personalprivatevault.personal.view.PersonalProfileView;

import java.util.List;
import java.util.Optional;

/**
 * Public capability operations for personal profiles.
 */
public interface PersonalProfileOperations {

    /**
     * Creates a new personal profile.
     *
     * @param command creation command
     * @return created profile view
     */
    PersonalProfileView createProfile(CreatePersonalProfileCommand command);

    /**
     * Fully updates an existing personal profile.
     *
     * @param command update command
     * @return updated profile view
     */
    PersonalProfileView updateProfile(UpdatePersonalProfileCommand command);

    /**
     * Finds a non-deleted personal profile by ID.
     *
     * @param id profile identifier
     * @return profile view
     */
    PersonalProfileView findProfileById(Long id);

    /**
     * Finds the sole active self profile if present.
     *
     * @return optional profile view
     */
    Optional<PersonalProfileView> findSelfProfile();

    /**
     * Finds non-deleted personal profiles ordered by name ASC, id ASC.
     *
     * @param limit positive maximum count
     * @return list of profile views
     */
    List<PersonalProfileView> findProfiles(int limit);

    /**
     * Soft-deletes a personal profile. Idempotent.
     *
     * @param id profile identifier
     * @return profile view
     */
    PersonalProfileView softDeleteProfile(Long id);

    /**
     * Restores a soft-deleted personal profile. Idempotent.
     *
     * @param id profile identifier
     * @return profile view
     */
    PersonalProfileView restoreProfile(Long id);
}
