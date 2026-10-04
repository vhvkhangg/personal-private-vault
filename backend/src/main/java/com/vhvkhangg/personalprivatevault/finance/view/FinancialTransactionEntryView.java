package com.vhvkhangg.personalprivatevault.finance.view;

import java.math.BigDecimal;

/**
 * Read model representing an immutable financial transaction entry.
 */
public record FinancialTransactionEntryView(
        Long id,
        Long transactionId,
        Long walletId,
        BigDecimal amountDelta
) {
}
