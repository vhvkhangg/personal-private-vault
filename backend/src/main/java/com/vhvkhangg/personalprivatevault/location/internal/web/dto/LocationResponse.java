package com.vhvkhangg.personalprivatevault.location.internal.web.dto;

import java.math.BigDecimal;

public record LocationResponse(
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
