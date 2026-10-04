package com.vhvkhangg.personalprivatevault.finance.wallet.command;

import com.vhvkhangg.personalprivatevault.finance.enums.WalletType;

import java.math.BigDecimal;

/**
 * Command for creating a wallet.
 */
public record CreateWalletCommand(
        String name,
        WalletType type,
        String currencyCode,
        BigDecimal openingBalance,
        String notes,
        Boolean active
) {
}
