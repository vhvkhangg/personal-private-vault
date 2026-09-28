package com.vhvkhangg.personalprivatevault.people.group;

import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupMemberView;
import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupView;

import java.util.List;
import java.util.Optional;

/**
 * Public synchronous capability contract for creator groups and memberships.
 */
public interface CreatorGroupOperations {

    /**
     * Creates a new creator group with an exact-case unique name.
     *
     * @param command creation command
     * @return immutable view of the created group
     * @throws CreatorGroupNameAlreadyExistsException if a group with the same stored name already exists
     * @throws InvalidCreatorGroupException if attributes violate domain validation rules
     */
    CreatorGroupView create(CreateCreatorGroupCommand command);

    /**
     * Finds an existing creator group by ID.
     *
     * @param groupId creator group ID
     * @return optional containing the group view if found, or empty
     */
    Optional<CreatorGroupView> find(Long groupId);

    /**
     * Updates an existing creator group.
     *
     * @param command update command
     * @return immutable view of the updated group
     * @throws CreatorGroupNotFoundException if the group does not exist
     * @throws CreatorGroupNameAlreadyExistsException if the new name is already taken by another group
     * @throws InvalidCreatorGroupException if attributes violate domain validation rules
     */
    CreatorGroupView update(UpdateCreatorGroupCommand command);

    /**
     * Idempotently adds a person as a member of a creator group.
     *
     * @param groupId creator group ID
     * @param personId person ID
     * @throws CreatorGroupNotFoundException if the creator group does not exist
     * @throws com.vhvkhangg.personalprivatevault.people.person.PersonNotFoundException if the person does not exist
     * @throws InvalidCreatorGroupException if inputs are invalid
     */
    void addMember(Long groupId, Long personId);

    /**
     * Retrieves the members of a creator group.
     *
     * @param groupId creator group ID
     * @return immutable list of member views
     * @throws CreatorGroupNotFoundException if the creator group does not exist
     * @throws InvalidCreatorGroupException if inputs are invalid
     */
    List<CreatorGroupMemberView> getMembers(Long groupId);
}
