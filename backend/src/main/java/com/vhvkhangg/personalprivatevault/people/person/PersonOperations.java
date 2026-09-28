package com.vhvkhangg.personalprivatevault.people.person;

import com.vhvkhangg.personalprivatevault.people.enums.PersonRole;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;

import java.util.Optional;
import java.util.Set;

/**
 * Public synchronous capability contract for person profiles and role assignments.
 */
public interface PersonOperations {

    /**
     * Creates a new person backed by a newly created Vault Entry with type PERSON.
     *
     * @param command creation command containing person profile attributes
     * @return immutable view of the created person
     * @throws InvalidPersonException if attributes violate domain validation rules
     */
    PersonView create(CreatePersonCommand command);

    /**
     * Finds an existing person by their ID.
     *
     * @param personId person ID (same as vault entry ID)
     * @return optional containing the person view if found, or empty
     */
    Optional<PersonView> find(Long personId);

    /**
     * Updates an existing person's profile attributes.
     *
     * @param command update command
     * @return immutable view of the updated person
     * @throws PersonNotFoundException if the person does not exist
     * @throws InvalidPersonException if updated attributes violate domain validation rules
     */
    PersonView update(UpdatePersonCommand command);

    /**
     * Idempotently adds a role to a person.
     *
     * @param personId person ID
     * @param role role to assign
     * @throws PersonNotFoundException if the person does not exist
     * @throws InvalidPersonException if inputs are invalid
     */
    void addRole(Long personId, PersonRole role);

    /**
     * Retrieves the set of assigned roles for a person.
     *
     * @param personId person ID
     * @return immutable set of roles
     * @throws PersonNotFoundException if the person does not exist
     * @throws InvalidPersonException if inputs are invalid
     */
    Set<PersonRole> getRoles(Long personId);
}
