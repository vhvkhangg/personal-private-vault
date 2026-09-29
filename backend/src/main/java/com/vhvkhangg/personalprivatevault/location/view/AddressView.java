package com.vhvkhangg.personalprivatevault.location.view;

import java.time.Instant;

/**
 * Immutable view of an Address.
 */
public record AddressView(
        Long id,
        String label,
        String addressType,
        String countryCode,
        String administrativeArea,
        String locality,
        String sublocality,
        String streetAddress,
        String postalCode,
        Instant createdAt,
        Instant updatedAt
) {
}
