package com.vhvkhangg.personalprivatevault.location.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateBrandRequest(
        @NotBlank(message = "Brand name must not be blank")
        @Size(max = 255, message = "Brand name must not exceed 255 characters")
        String name,
        @Size(max = 2048, message = "Logo URL must not exceed 2048 characters")
        String logoUrl,
        @Size(max = 2, message = "Nationality code must not exceed 2 characters")
        String nationalityCode,
        String description,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        @Size(max = 3, message = "Currency code must not exceed 3 characters")
        String currencyCode,
        String review
) {
}
