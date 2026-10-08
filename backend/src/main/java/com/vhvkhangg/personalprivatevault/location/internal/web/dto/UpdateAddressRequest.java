package com.vhvkhangg.personalprivatevault.location.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAddressRequest(
        @Size(max = 255, message = "Label must not exceed 255 characters")
        String label,
        @Size(max = 100, message = "Address type must not exceed 100 characters")
        String addressType,
        @NotBlank(message = "Country code must not be blank")
        @Size(max = 2, message = "Country code must not exceed 2 characters")
        String countryCode,
        @Size(max = 255, message = "Administrative area must not exceed 255 characters")
        String administrativeArea,
        @Size(max = 255, message = "Locality must not exceed 255 characters")
        String locality,
        @Size(max = 255, message = "Sublocality must not exceed 255 characters")
        String sublocality,
        @Size(max = 500, message = "Street address must not exceed 500 characters")
        String streetAddress,
        @Size(max = 32, message = "Postal code must not exceed 32 characters")
        String postalCode
) {
}
