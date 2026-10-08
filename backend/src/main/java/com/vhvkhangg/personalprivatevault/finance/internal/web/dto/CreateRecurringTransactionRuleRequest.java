package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import com.vhvkhangg.personalprivatevault.finance.enums.DayOfWeek;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurrenceFrequency;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurringPostingMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public record CreateRecurringTransactionRuleRequest(
        @NotBlank @Size(max = 500) String name,
        @Schema(description = "Financial transaction type: INCOME, EXPENSE, or TRANSFER. INCOME requires exactly one entry with positive amountDelta. EXPENSE requires exactly one entry with negative amountDelta. TRANSFER requires exactly two entries with distinct wallets, one negative source and one positive destination, and must have no category.")
        @NotNull FinancialTransactionType transactionType,
        @Schema(description = "Transaction category ID. Optional for INCOME and EXPENSE (must be compatible with category kind). Must be null for TRANSFER.")
        Long categoryId,
        @NotNull RecurringPostingMode postingMode,
        @Schema(description = "Recurrence frequency: DAILY, WEEKLY, MONTHLY, or YEARLY")
        @NotNull RecurrenceFrequency frequency,
        @Schema(description = "Interval count (e.g. 2 for every 2 weeks/months; defaults to 1)")
        @Positive Integer intervalCount,
        @Schema(description = "Day of month (1-31). Required for MONTHLY and YEARLY; must be null for DAILY and WEEKLY.")
        Integer dayOfMonth,
        @Schema(description = "Month of year (1-12). Required for YEARLY; must be null for DAILY, WEEKLY, and MONTHLY.")
        Integer monthOfYear,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        LocalTime postingTime,
        Instant nextRunAt,
        @Size(max = 1000) String description,
        String notes,
        Boolean active,
        @Schema(description = "Days of week. Required for WEEKLY; must be null/empty for DAILY, MONTHLY, and YEARLY.")
        Set<DayOfWeek> weekdays,
        @Schema(description = "Ledger entry list. Each wallet may appear at most once. INCOME/EXPENSE requires exactly one entry with matching sign (+ for income, - for expense). TRANSFER requires exactly two entries with opposite signs.")
        @NotEmpty List<@NotNull @Valid RecurringRuleEntryRequest> entries
) {}
