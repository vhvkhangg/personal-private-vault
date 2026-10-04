package com.vhvkhangg.personalprivatevault.finance.view;

import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionStatus;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Read model representing an immutable financial transaction with its ledger entries.
 */
public record FinancialTransactionView(
        Long id,
        FinancialTransactionType type,
        FinancialTransactionStatus status,
        Long categoryId,
        Long generatedByRecurringRuleId,
        String description,
        String notes,
        Instant occurredAt,
        BigDecimal exchangeRate,
        List<FinancialTransactionEntryView> entries,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
    public FinancialTransactionView {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }
}
