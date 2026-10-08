package com.vhvkhangg.personalprivatevault.collection.internal.web.dto;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateSoftwareItemRequest(
        @NotBlank @Size(max = 500) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Software type: APPLICATION or EXTENSION")
        CollectionSoftwareType type,
        @Size(max = 2048) String logoUrl,
        String description,
        @PositiveOrZero BigDecimal priceAmount,
        @Size(max = 3) String currencyCode,
        @Size(max = 2048) String url,
        String review
) {
    public CreateSoftwareItemRequest {
        name = name != null ? name.trim() : null;
        currencyCode = trimOrNull(currencyCode);
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
