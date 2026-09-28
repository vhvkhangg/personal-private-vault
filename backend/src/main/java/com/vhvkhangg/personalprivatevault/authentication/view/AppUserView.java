package com.vhvkhangg.personalprivatevault.authentication.view;

import java.time.Instant;

/**
 * Public immutable view representing the singleton application user.
 */
public record AppUserView(
        Short id,
        String email,
        String username,
        Instant createdAt,
        Instant updatedAt
) {
}
