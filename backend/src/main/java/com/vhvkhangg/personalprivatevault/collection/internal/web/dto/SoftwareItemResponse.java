package com.vhvkhangg.personalprivatevault.collection.internal.web.dto;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareType;

import java.math.BigDecimal;

public record SoftwareItemResponse(
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
