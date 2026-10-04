package com.vhvkhangg.personalprivatevault.finance.view;

import com.vhvkhangg.personalprivatevault.finance.enums.DayOfWeek;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurrenceFrequency;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurringPostingMode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * Read model representing an immutable recurring transaction rule with weekdays and entries.
 */
public record RecurringTransactionRuleView(
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
        List<RecurringRuleEntryView> entries,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
    public RecurringTransactionRuleView {
        weekdays = weekdays == null ? Set.of() : Set.copyOf(weekdays);
        entries = entries == null ? List.of() : List.copyOf(entries);
    }
}
