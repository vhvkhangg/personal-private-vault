package com.vhvkhangg.personalprivatevault.finance.transaction.command;

import java.math.BigDecimal;

/**
 * Input entry for financial transactions.
 */
public record FinancialTransactionEntryInput(
        Long walletId,
        BigDecimal amountDelta
) {
}
