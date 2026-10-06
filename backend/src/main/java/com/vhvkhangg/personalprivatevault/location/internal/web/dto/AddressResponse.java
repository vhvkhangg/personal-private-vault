package com.vhvkhangg.personalprivatevault.location.internal.web.dto;

import java.time.Instant;

public record AddressResponse(
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
