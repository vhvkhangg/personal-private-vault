package com.vhvkhangg.personalprivatevault.collection.shopping.shopping;

import com.vhvkhangg.personalprivatevault.collection.shopping.enums.ShoppingStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Command to create a new shopping item.
 */
public record CreateShoppingItemCommand(
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
