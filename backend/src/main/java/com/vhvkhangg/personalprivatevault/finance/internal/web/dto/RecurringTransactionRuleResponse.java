package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import com.vhvkhangg.personalprivatevault.finance.enums.DayOfWeek;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurrenceFrequency;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurringPostingMode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public record RecurringTransactionRuleResponse(
        Long id,
        String name,
        FinancialTransactionType transactionType,
        Long categoryId,
        RecurringPostingMode postingMode,
        RecurrenceFrequency frequency,
        int intervalCount,
        Integer dayOfMonth,
        Integer monthOfYear,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime postingTime,
        Instant nextRunAt,
        String description,
        String notes,
        boolean active,
        Set<DayOfWeek> weekdays,
        List<RecurringRuleEntryResponse> entries,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {}
