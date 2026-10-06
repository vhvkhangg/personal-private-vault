package com.vhvkhangg.personalprivatevault.collection.internal.web.dto;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateShoppingItemRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 2048) String avatarUrl,
        String description,
        @PositiveOrZero BigDecimal priceAmount,
        @Size(min = 3, max = 3) String currencyCode,
        Long platformId,
        CollectionShoppingStatus status,
        @Size(max = 2048) String url,
        Instant purchasedAt
) {}
