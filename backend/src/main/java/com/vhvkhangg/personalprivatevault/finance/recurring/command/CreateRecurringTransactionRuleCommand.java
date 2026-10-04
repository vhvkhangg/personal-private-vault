package com.vhvkhangg.personalprivatevault.finance.recurring.command;

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
 * Command for creating a recurring transaction rule.
 */
public record CreateRecurringTransactionRuleCommand(
        String name,
        FinancialTransactionType transactionType,
        Long categoryId,
        RecurringPostingMode postingMode,
        RecurrenceFrequency frequency,
        Integer intervalCount,
        Integer dayOfMonth,
        Integer monthOfYear,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime postingTime,
        Instant nextRunAt,
        String description,
        String notes,
        Boolean active,
        Set<DayOfWeek> weekdays,
        List<RecurringRuleEntryInput> entries
) {
    public CreateRecurringTransactionRuleCommand {
        weekdays = weekdays == null ? Set.of() : Set.copyOf(weekdays);
        entries = entries == null ? List.of() : List.copyOf(entries);
    }
}
