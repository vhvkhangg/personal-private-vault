package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionStatus;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record UpdateFinancialTransactionRequest(
        @Schema(description = "Transaction type: EXPENSE (negative delta entry, category optional), INCOME (positive delta entry, category optional), or TRANSFER (must not have category; two distinct wallets with opposite signs)")
        @NotNull FinancialTransactionType type,

        @Schema(description = "Transaction lifecycle status: PENDING, POSTED, or CANCELLED (defaults to POSTED if omitted)")
        FinancialTransactionStatus status,

        @Schema(description = "Category ID (optional for EXPENSE and INCOME; must be null for TRANSFER)")
        Long categoryId,

        @Size(max = 1000) String description,
        String notes,
        @NotNull Instant occurredAt,
        @Positive BigDecimal exchangeRate,

        @Schema(description = "List of wallet entries. EXPENSE and INCOME require exactly 1 entry. TRANSFER requires exactly 2 entries for distinct wallets (one negative source, one positive destination).")
        @NotEmpty List<@NotNull @Valid FinancialTransactionEntryRequest> entries
) {}
