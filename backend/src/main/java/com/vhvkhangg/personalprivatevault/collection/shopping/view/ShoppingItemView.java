package com.vhvkhangg.personalprivatevault.collection.shopping.view;

import com.vhvkhangg.personalprivatevault.collection.shopping.enums.ShoppingStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable view of a shopping item.
 */
public record ShoppingItemView(
        Long id,
        String name,
        String avatarUrl,
        String description,
        BigDecimal priceAmount,
        String currencyCode,
        Long platformId,
        ShoppingStatus status,
        String url,
        Instant purchasedAt
) {}
