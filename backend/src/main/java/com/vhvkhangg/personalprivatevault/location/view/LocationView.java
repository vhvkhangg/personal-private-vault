package com.vhvkhangg.personalprivatevault.location.view;

import java.math.BigDecimal;

/**
 * Immutable view of a Location.
 */
public record LocationView(
        Long id,
        Long brandId,
        Long addressId,
        String name,
        String imageUrl,
        String description,
        String phone,
        String websiteUrl,
        boolean businessHoursKnown,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String currencyCode,
        String review
) {
}
