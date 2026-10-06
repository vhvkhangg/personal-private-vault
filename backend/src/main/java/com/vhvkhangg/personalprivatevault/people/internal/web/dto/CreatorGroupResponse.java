package com.vhvkhangg.personalprivatevault.people.internal.web.dto;

import java.time.Instant;

public record CreatorGroupResponse(
        Long id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
