package com.vhvkhangg.personalprivatevault.collection.api;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Public parent collection command to update a shopping item with full scalar replacement semantics.
 */
public record UpdateCollectionShoppingItemCommand(
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
