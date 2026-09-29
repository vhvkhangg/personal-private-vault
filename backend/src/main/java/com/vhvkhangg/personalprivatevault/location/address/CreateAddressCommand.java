package com.vhvkhangg.personalprivatevault.location.address;

/**
 * Command to create a new Address.
 */
public record CreateAddressCommand(
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
