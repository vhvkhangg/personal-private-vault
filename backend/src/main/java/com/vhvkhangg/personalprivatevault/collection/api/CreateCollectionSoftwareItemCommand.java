package com.vhvkhangg.personalprivatevault.collection.api;

import java.math.BigDecimal;

/**
 * Public parent collection command to create a software item.
 */
public record CreateCollectionSoftwareItemCommand(
        String name,
        CollectionSoftwareType type,
        String logoUrl,
        String description,
        BigDecimal priceAmount,
        String currencyCode,
        String url,
        String review
) {}
