package com.vhvkhangg.personalprivatevault.people.view;

import java.time.Instant;

/**
 * Immutable view of a creator group.
 */
public record CreatorGroupView(
        Long id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
