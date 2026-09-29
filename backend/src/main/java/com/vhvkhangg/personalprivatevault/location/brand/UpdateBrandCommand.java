package com.vhvkhangg.personalprivatevault.location.brand;

import java.math.BigDecimal;

/**
 * Command to update an existing Brand.
 */
public record UpdateBrandCommand(
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
