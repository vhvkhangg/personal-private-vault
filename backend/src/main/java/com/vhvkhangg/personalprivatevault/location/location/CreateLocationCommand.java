package com.vhvkhangg.personalprivatevault.location.location;

import java.math.BigDecimal;

/**
 * Command to create a new Location backed by a Vault Entry of type LOCATION.
 */
public record CreateLocationCommand(
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
