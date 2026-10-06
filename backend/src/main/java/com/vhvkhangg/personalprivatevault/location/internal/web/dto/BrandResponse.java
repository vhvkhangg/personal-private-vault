package com.vhvkhangg.personalprivatevault.location.internal.web.dto;

import java.math.BigDecimal;

public record BrandResponse(
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
