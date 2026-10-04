package com.vhvkhangg.personalprivatevault.finance.recurring.command;

import java.math.BigDecimal;

/**
 * Input entry for recurring transaction rules.
 */
public record RecurringRuleEntryInput(
        Long walletId,
        BigDecimal amountDelta
) {
}
