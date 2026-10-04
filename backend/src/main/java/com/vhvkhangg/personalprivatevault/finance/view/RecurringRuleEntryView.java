package com.vhvkhangg.personalprivatevault.finance.view;

import java.math.BigDecimal;

/**
 * Read model representing an immutable recurring rule entry.
 */
public record RecurringRuleEntryView(
        Long id,
        Long recurringRuleId,
        Long walletId,
        BigDecimal amountDelta
) {
}
