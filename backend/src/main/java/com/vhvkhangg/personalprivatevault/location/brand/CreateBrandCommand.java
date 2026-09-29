package com.vhvkhangg.personalprivatevault.location.brand;

import java.math.BigDecimal;

/**
 * Command to create a new Brand backed by a Vault Entry of type BRAND.
 */
public record CreateBrandCommand(
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
