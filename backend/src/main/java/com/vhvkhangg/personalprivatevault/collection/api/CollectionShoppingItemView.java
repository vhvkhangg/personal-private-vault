package com.vhvkhangg.personalprivatevault.collection.api;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Public parent collection view of a shopping item.
 */
public record CollectionShoppingItemView(
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
