package com.vhvkhangg.personalprivatevault.people.group.command;

/**
 * Command to update an existing creator group.
 */
public record UpdateCreatorGroupCommand(
        Long id,
        String name,
        String description
) {}
