package com.vhvkhangg.personalprivatevault.collection.internal.web.dto;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Full replacement update for a shopping item. Nullable fields set to null or omitted will be cleared.")
public record UpdateShoppingItemRequest(
        @NotBlank @Size(max = 500) String name,
        @Size(max = 2048) String avatarUrl,
        String description,
        @PositiveOrZero BigDecimal priceAmount,
        @Size(max = 3) String currencyCode,
        Long platformId,
        @Schema(description = "Shopping status: WISHLIST or PURCHASED. Defaults to WISHLIST if omitted. If changed to WISHLIST, purchasedAt must be null.")
        CollectionShoppingStatus status,
        @Size(max = 2048) String url,
        @Schema(description = "Purchase timestamp. Must be null if status is WISHLIST. If status is PURCHASED, nullable timestamp with no default.")
        Instant purchasedAt
) {
    public UpdateShoppingItemRequest {
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
