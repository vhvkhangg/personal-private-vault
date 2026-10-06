package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionStatus;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CreateFinancialTransactionRequest(
        @NotNull FinancialTransactionType type,
        FinancialTransactionStatus status,
        Long categoryId,
        @NotBlank @Size(max = 255) String description,
        String notes,
        @NotNull Instant occurredAt,
        @Positive BigDecimal exchangeRate,
        @NotEmpty @Valid List<FinancialTransactionEntryRequest> entries
) {}
