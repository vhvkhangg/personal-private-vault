package com.vhvkhangg.personalprivatevault.location.view;

import java.math.BigDecimal;

/**
 * Immutable view of a Brand.
 */
public record BrandView(
        Long id,
        String name,
        String logoUrl,
        String nationalityCode,
        String description,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String currencyCode,
        String review
) {
}
