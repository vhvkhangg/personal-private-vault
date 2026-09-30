package com.vhvkhangg.personalprivatevault.collection.software.software;

import com.vhvkhangg.personalprivatevault.collection.software.enums.SoftwareType;

import java.math.BigDecimal;

/**
 * Command to update an existing software item with full scalar replacement semantics.
 */
public record UpdateSoftwareItemCommand(
        String name,
        SoftwareType type,
        String logoUrl,
        String description,
        BigDecimal priceAmount,
        String currencyCode,
        String url,
        String review
) {}
