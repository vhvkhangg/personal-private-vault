package com.vhvkhangg.personalprivatevault.people.group;

/**
 * Command to create a new creator group.
 */
public record CreateCreatorGroupCommand(
        String name,
        String description
) {}
