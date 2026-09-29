package com.vhvkhangg.personalprivatevault.location.address;

/**
 * Command to update an existing Address.
 */
public record UpdateAddressCommand(
        Long id,
        String label,
        String addressType,
        String countryCode,
        String administrativeArea,
        String locality,
        String sublocality,
        String streetAddress,
        String postalCode
) {
}
