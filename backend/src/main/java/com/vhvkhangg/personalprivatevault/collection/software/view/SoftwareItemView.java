package com.vhvkhangg.personalprivatevault.collection.software.view;

import com.vhvkhangg.personalprivatevault.collection.software.enums.SoftwareType;

import java.math.BigDecimal;

/**
 * Immutable view of a software item.
 */
public record SoftwareItemView(
        Long id,
        String name,
        SoftwareType type,
        String logoUrl,
        String description,
        BigDecimal priceAmount,
        String currencyCode,
        String url,
        String review
) {}
