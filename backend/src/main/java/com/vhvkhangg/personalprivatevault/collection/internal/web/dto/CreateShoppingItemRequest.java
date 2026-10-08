package com.vhvkhangg.personalprivatevault.collection.internal.web.dto;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateShoppingItemRequest(
        @NotBlank @Size(max = 500) String name,
        @Size(max = 2048) String avatarUrl,
        String description,
        @PositiveOrZero BigDecimal priceAmount,
        @Size(max = 3) String currencyCode,
        Long platformId,
        CollectionShoppingStatus status,
        @Size(max = 2048) String url,
        Instant purchasedAt
) {
    public CreateShoppingItemRequest {
        name = name != null ? name.trim() : null;
        currencyCode = trimOrNull(currencyCode);
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
