package com.vhvkhangg.personalprivatevault.people.view;

/**
 * Immutable view of a creator group membership referencing a person.
 */
public record CreatorGroupMemberView(
        Long groupId,
        Long personId,
        String personName
) {}
