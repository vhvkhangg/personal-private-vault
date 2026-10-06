package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import com.vhvkhangg.personalprivatevault.finance.enums.DayOfWeek;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurrenceFrequency;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurringPostingMode;
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

public record UpdateRecurringTransactionRuleRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull FinancialTransactionType transactionType,
        Long categoryId,
        @NotNull RecurringPostingMode postingMode,
        @NotNull RecurrenceFrequency frequency,
        @Positive Integer intervalCount,
        Integer dayOfMonth,
        Integer monthOfYear,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        LocalTime postingTime,
        Instant nextRunAt,
        String description,
        String notes,
        Boolean active,
        Set<DayOfWeek> weekdays,
        @NotEmpty @Valid List<RecurringRuleEntryRequest> entries
) {}
