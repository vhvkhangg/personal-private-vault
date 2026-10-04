package com.vhvkhangg.personalprivatevault.finance.transaction.command;

import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionStatus;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Command for updating an existing financial transaction.
 */
public record UpdateFinancialTransactionCommand(
        Long id,
        FinancialTransactionType type,
        FinancialTransactionStatus status,
        Long categoryId,
        String description,
        String notes,
        Instant occurredAt,
        BigDecimal exchangeRate,
        List<FinancialTransactionEntryInput> entries
) {
    public UpdateFinancialTransactionCommand {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }
}
