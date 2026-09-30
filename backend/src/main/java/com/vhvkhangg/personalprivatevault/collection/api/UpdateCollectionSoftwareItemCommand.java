package com.vhvkhangg.personalprivatevault.collection.api;

import java.math.BigDecimal;

/**
 * Public parent collection command to update a software item with full scalar replacement semantics.
 */
public record UpdateCollectionSoftwareItemCommand(
        String name,
        CollectionSoftwareType type,
        String logoUrl,
        String description,
        BigDecimal priceAmount,
        String currencyCode,
        String url,
        String review
) {}
