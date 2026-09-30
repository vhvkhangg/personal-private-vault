package com.vhvkhangg.personalprivatevault.collection.api;

import java.math.BigDecimal;

/**
 * Public parent collection view of a software item.
 */
public record CollectionSoftwareItemView(
        Long id,
        String name,
        CollectionSoftwareType type,
        String logoUrl,
        String description,
        BigDecimal priceAmount,
        String currencyCode,
        String url,
        String review
) {}
