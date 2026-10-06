package com.vhvkhangg.personalprivatevault.collection.internal.web.dto;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateSoftwareItemRequest(
        @NotBlank @Size(max = 255) String name,
        CollectionSoftwareType type,
        @Size(max = 2048) String logoUrl,
        String description,
        @PositiveOrZero BigDecimal priceAmount,
        @Size(min = 3, max = 3) String currencyCode,
        @Size(max = 2048) String url,
        String review
) {}
