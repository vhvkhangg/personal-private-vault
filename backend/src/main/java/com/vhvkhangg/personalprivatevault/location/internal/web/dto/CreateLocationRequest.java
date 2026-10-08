package com.vhvkhangg.personalprivatevault.location.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateLocationRequest(
        Long brandId,
        Long addressId,
        @NotBlank(message = "Location name must not be blank")
        @Size(max = 500, message = "Location name must not exceed 500 characters")
        String name,
        @Size(max = 2048, message = "Image URL must not exceed 2048 characters")
        String imageUrl,
        String description,
        @Size(max = 64, message = "Phone must not exceed 64 characters")
        String phone,
        @Size(max = 2048, message = "Website URL must not exceed 2048 characters")
        String websiteUrl,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        @Size(max = 3, message = "Currency code must not exceed 3 characters")
        String currencyCode,
        String review
) {
    public CreateLocationRequest {
        name = name != null ? name.trim() : null;
        phone = (phone == null || phone.isBlank()) ? null : phone.trim();
    }
}
