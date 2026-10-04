package com.vhvkhangg.personalprivatevault.finance.view;

import com.vhvkhangg.personalprivatevault.finance.enums.WalletType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read model representing an immutable wallet.
 */
public record WalletView(
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
) {
}
