package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import com.vhvkhangg.personalprivatevault.finance.enums.WalletType;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletResponse(
        Long id,
        String name,
        WalletType type,
        String currencyCode,
        BigDecimal openingBalance,
        String notes,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {}
