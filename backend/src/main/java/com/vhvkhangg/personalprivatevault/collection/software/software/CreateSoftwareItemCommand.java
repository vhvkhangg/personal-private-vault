package com.vhvkhangg.personalprivatevault.collection.software.software;

import com.vhvkhangg.personalprivatevault.collection.software.enums.SoftwareType;

import java.math.BigDecimal;

/**
 * Command to create a new software item.
 */
public record CreateSoftwareItemCommand(
        String name,
        SoftwareType type,
        String logoUrl,
        String description,
        BigDecimal priceAmount,
        String currencyCode,
        String url,
        String review
) {}
