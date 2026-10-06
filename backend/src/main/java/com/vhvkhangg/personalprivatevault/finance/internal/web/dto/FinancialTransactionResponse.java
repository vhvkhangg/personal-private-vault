package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionStatus;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record FinancialTransactionResponse(
        Long id,
        FinancialTransactionType type,
        FinancialTransactionStatus status,
        Long categoryId,
        Long generatedByRecurringRuleId,
        String description,
        String notes,
        Instant occurredAt,
        BigDecimal exchangeRate,
        List<FinancialTransactionEntryResponse> entries,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {}
