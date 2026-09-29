package com.vhvkhangg.personalprivatevault.location.location;

import java.math.BigDecimal;

/**
 * Command to update an existing Location.
 */
public record UpdateLocationCommand(
        Long id,
        Long brandId,
        Long addressId,
        String name,
        String imageUrl,
        String description,
        String phone,
        String websiteUrl,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String currencyCode,
        String review
) {
}
