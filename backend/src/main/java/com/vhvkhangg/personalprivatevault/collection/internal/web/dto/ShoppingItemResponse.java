package com.vhvkhangg.personalprivatevault.collection.internal.web.dto;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ShoppingItemResponse(
        Long id,
        String name,
        String avatarUrl,
        String description,
        BigDecimal priceAmount,
        String currencyCode,
        Long platformId,
        CollectionShoppingStatus status,
        String url,
        Instant purchasedAt
) {}
