package com.vhvkhangg.personalprivatevault.finance.internal.application;

import com.vhvkhangg.personalprivatevault.finance.enums.DayOfWeek;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurrenceFrequency;
import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringRuleEntry;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringRuleWeekday;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringRuleWeekdayId;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringTransactionRule;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.TransactionCategory;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.Wallet;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.RecurringRuleEntryRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.RecurringRuleWeekdayRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.RecurringTransactionRuleRepository;
import com.vhvkhangg.personalprivatevault.finance.recurring.RecurringTransactionRuleOperations;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.CreateRecurringTransactionRuleCommand;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.RecurringRuleEntryInput;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.UpdateRecurringTransactionRuleCommand;
import com.vhvkhangg.personalprivatevault.finance.recurring.exception.InvalidRecurringTransactionRuleException;
import com.vhvkhangg.personalprivatevault.finance.recurring.exception.RecurringTransactionRuleNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.view.RecurringRuleEntryView;
import com.vhvkhangg.personalprivatevault.finance.view.RecurringTransactionRuleView;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Application service implementing {@link RecurringTransactionRuleOperations}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecurringTransactionRuleService implements RecurringTransactionRuleOperations {

    private final RecurringTransactionRuleRepository recurringTransactionRuleRepository;
    private final RecurringRuleWeekdayRepository recurringRuleWeekdayRepository;
    private final RecurringRuleEntryRepository recurringRuleEntryRepository;
    private final FinanceLockManager lockManager;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public RecurringTransactionRuleView createRule(CreateRecurringTransactionRuleCommand command) {
        if (command == null) {
            throw new InvalidRecurringTransactionRuleException("Command must not be null");
        }
        int intervalCount = command.intervalCount() != null ? command.intervalCount() : 1;
        validateRuleScalars(command.name(), command.transactionType(), command.postingMode(),
                command.frequency(), intervalCount, command.startDate(), command.endDate(),
                command.description(), command.dayOfMonth(), command.monthOfYear(), command.weekdays());
        validateEntriesShapeAndSign(command.transactionType(), command.entries());

        TransactionCategory category = lockManager.lockAndRefreshCategory(command.categoryId());
        validateCategoryCompatibility(command.transactionType(), category);

        List<Long> walletIds = command.entries().stream().map(RecurringRuleEntryInput::walletId).toList();
        List<Wallet> wallets = lockManager.lockAndRefreshWalletsAscending(walletIds);
        validateWalletsNotDeleted(wallets);

        boolean active = command.active() == null || command.active();
        RecurringTransactionRule rule = new RecurringTransactionRule(
                command.name().trim(),
                command.transactionType(),
                command.categoryId(),
                command.postingMode(),
                command.frequency(),
                intervalCount,
                command.dayOfMonth(),
                command.monthOfYear(),
                command.startDate(),
                command.endDate(),
                command.postingTime(),
                command.nextRunAt(),
                command.description(),
                command.notes(),
                active
        );
        RecurringTransactionRule saved = recurringTransactionRuleRepository.save(rule);

        Set<DayOfWeek> convergedWeekdays = new HashSet<>(command.weekdays());
        for (DayOfWeek day : convergedWeekdays) {
            recurringRuleWeekdayRepository.save(new RecurringRuleWeekday(new RecurringRuleWeekdayId(saved.getId(), day)));
        }

        List<RecurringRuleEntryView> entryViews = new ArrayList<>(command.entries().size());
        for (RecurringRuleEntryInput entryInput : command.entries()) {
            BigDecimal delta = FinanceValidationUtils.normalizeMoney(entryInput.amountDelta(), "amountDelta", InvalidRecurringTransactionRuleException::new);
            RecurringRuleEntry entry = new RecurringRuleEntry(saved.getId(), entryInput.walletId(), delta);
            RecurringRuleEntry savedEntry = recurringRuleEntryRepository.save(entry);
            entryViews.add(toEntryView(savedEntry));
        }
        recurringRuleWeekdayRepository.flush();
        recurringRuleEntryRepository.flush();
        recurringTransactionRuleRepository.flush();

        return toRuleView(saved, convergedWeekdays, entryViews);
    }

    @Override
    @Transactional
    public RecurringTransactionRuleView updateRule(UpdateRecurringTransactionRuleCommand command) {
        if (command == null) {
            throw new InvalidRecurringTransactionRuleException("Command must not be null");
        }
        if (command.id() == null) {
            throw new InvalidRecurringTransactionRuleException("Rule id must not be null");
        }
        int intervalCount = command.intervalCount() != null ? command.intervalCount() : 1;
        validateRuleScalars(command.name(), command.transactionType(), command.postingMode(),
                command.frequency(), intervalCount, command.startDate(), command.endDate(),
                command.description(), command.dayOfMonth(), command.monthOfYear(), command.weekdays());
        validateEntriesShapeAndSign(command.transactionType(), command.entries());

        RecurringTransactionRule rule = lockManager.lockAndRefreshRecurringRule(command.id());
        if (rule.getDeletedAt() != null) {
            throw new InvalidRecurringTransactionRuleException("Cannot update a soft-deleted recurring rule. Restore it first.");
        }

        TransactionCategory category = lockManager.lockAndRefreshCategory(command.categoryId());
        validateCategoryCompatibility(command.transactionType(), category);

        List<Long> walletIds = command.entries().stream().map(RecurringRuleEntryInput::walletId).toList();
        List<Wallet> wallets = lockManager.lockAndRefreshWalletsAscending(walletIds);
        validateWalletsNotDeleted(wallets);

        // Reconcile and detach all candidate weekday identities for this rule to prevent merge into obsolete managed children
        for (DayOfWeek day : DayOfWeek.values()) {
            RecurringRuleWeekdayId candidateId = new RecurringRuleWeekdayId(rule.getId(), day);
            RecurringRuleWeekday managed = entityManager.find(RecurringRuleWeekday.class, candidateId);
            if (managed != null) {
                entityManager.detach(managed);
            }
        }
        recurringRuleWeekdayRepository.deleteByRecurringRuleId(rule.getId());
        recurringRuleWeekdayRepository.flush();
        Set<DayOfWeek> convergedWeekdays = new HashSet<>(command.weekdays());
        for (DayOfWeek day : convergedWeekdays) {
            entityManager.persist(new RecurringRuleWeekday(new RecurringRuleWeekdayId(rule.getId(), day)));
        }
        recurringRuleWeekdayRepository.flush();

        // Replace entries
        List<RecurringRuleEntry> existingEntries = recurringRuleEntryRepository.findByRecurringRuleIdOrderByIdAsc(rule.getId());
        existingEntries.forEach(entityManager::detach);
        recurringRuleEntryRepository.deleteByRecurringRuleId(rule.getId());
        recurringRuleEntryRepository.flush();
        List<RecurringRuleEntryView> entryViews = new ArrayList<>(command.entries().size());
        for (RecurringRuleEntryInput entryInput : command.entries()) {
            BigDecimal delta = FinanceValidationUtils.normalizeMoney(entryInput.amountDelta(), "amountDelta", InvalidRecurringTransactionRuleException::new);
            RecurringRuleEntry entry = new RecurringRuleEntry(rule.getId(), entryInput.walletId(), delta);
            RecurringRuleEntry savedEntry = recurringRuleEntryRepository.save(entry);
            entryViews.add(toEntryView(savedEntry));
        }
        recurringRuleEntryRepository.flush();

        boolean active = command.active() == null || command.active();
        rule.update(
                command.name().trim(),
                command.transactionType(),
                command.categoryId(),
                command.postingMode(),
                command.frequency(),
                intervalCount,
                command.dayOfMonth(),
                command.monthOfYear(),
                command.startDate(),
                command.endDate(),
                command.postingTime(),
                command.nextRunAt(),
                command.description(),
                command.notes(),
                active
        );
        recurringTransactionRuleRepository.flush();

        return toRuleView(rule, convergedWeekdays, entryViews);
    }

    @Override
    public RecurringTransactionRuleView findRuleById(Long id) {
        if (id == null) {
            throw new InvalidRecurringTransactionRuleException("Rule id must not be null");
        }
        RecurringTransactionRule rule = recurringTransactionRuleRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RecurringTransactionRuleNotFoundException(id));
        return loadWithChildren(rule);
    }

    @Override
    public List<RecurringTransactionRuleView> findDueRules(Instant cutoff, int limit) {
        if (cutoff == null) {
            throw new InvalidRecurringTransactionRuleException("cutoff must not be null");
        }
        if (limit <= 0) {
            throw new InvalidRecurringTransactionRuleException("Limit must be positive");
        }
        List<RecurringTransactionRule> rules = recurringTransactionRuleRepository.findDueRules(cutoff, PageRequest.of(0, limit));
        return batchLoadViews(rules);
    }

    @Override
    public List<RecurringTransactionRuleView> findRules(Boolean activeFilter, int limit) {
        if (limit <= 0) {
            throw new InvalidRecurringTransactionRuleException("Limit must be positive");
        }
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<RecurringTransactionRule> rules;
        if (activeFilter != null) {
            rules = recurringTransactionRuleRepository.findByActiveAndDeletedAtIsNullOrderByNameAscIdAsc(activeFilter, pageRequest);
        } else {
            rules = recurringTransactionRuleRepository.findByDeletedAtIsNullOrderByNameAscIdAsc(pageRequest);
        }
        return batchLoadViews(rules);
    }

    @Override
    @Transactional
    public RecurringTransactionRuleView softDeleteRule(Long id) {
        if (id == null) {
            throw new InvalidRecurringTransactionRuleException("Rule id must not be null");
        }
        RecurringTransactionRule rule = lockManager.lockAndRefreshRecurringRule(id);
        rule.softDelete();
        recurringTransactionRuleRepository.flush();
        return loadWithChildren(rule);
    }

    @Override
    @Transactional
    public RecurringTransactionRuleView restoreRule(Long id) {
        if (id == null) {
            throw new InvalidRecurringTransactionRuleException("Rule id must not be null");
        }
        RecurringTransactionRule rule = lockManager.lockAndRefreshRecurringRule(id);
        if (rule.getDeletedAt() == null) {
            return loadWithChildren(rule);
        }

        // Revalidate retained schedule and entry shape from fresh state under the lock
        for (DayOfWeek day : DayOfWeek.values()) {
            RecurringRuleWeekdayId candidateId = new RecurringRuleWeekdayId(rule.getId(), day);
            RecurringRuleWeekday managed = entityManager.find(RecurringRuleWeekday.class, candidateId);
            if (managed != null) {
                entityManager.detach(managed);
            }
        }
        recurringRuleEntryRepository.findByRecurringRuleIdOrderByIdAsc(rule.getId()).forEach(entityManager::detach);

        Set<DayOfWeek> weekdays = recurringRuleWeekdayRepository.findByIdRecurringRuleId(rule.getId())
                .stream()
                .map(w -> w.getId().getWeekday())
                .collect(Collectors.toSet());
        List<RecurringRuleEntry> entries = recurringRuleEntryRepository.findByRecurringRuleIdOrderByIdAsc(rule.getId());
        validateFrequencyCompatibility(rule.getFrequency(), rule.getDayOfMonth(), rule.getMonthOfYear(), weekdays);

        List<RecurringRuleEntryInput> entryInputs = entries.stream()
                .map(e -> new RecurringRuleEntryInput(e.getWalletId(), e.getAmountDelta()))
                .toList();
        validateEntriesShapeAndSign(rule.getTransactionType(), entryInputs);

        rule.restore();
        recurringTransactionRuleRepository.flush();
        return loadWithChildren(rule);
    }

    private List<RecurringTransactionRuleView> batchLoadViews(List<RecurringTransactionRule> rules) {
        if (rules.isEmpty()) {
            return List.of();
        }
        List<Long> ruleIds = rules.stream().map(RecurringTransactionRule::getId).toList();

        List<RecurringRuleWeekday> allWeekdays = recurringRuleWeekdayRepository.findByIdRecurringRuleIdIn(ruleIds);
        Map<Long, Set<DayOfWeek>> weekdaysByRuleId = allWeekdays.stream()
                .collect(Collectors.groupingBy(
                        w -> w.getId().getRecurringRuleId(),
                        Collectors.mapping(w -> w.getId().getWeekday(), Collectors.toSet())
                ));

        List<RecurringRuleEntry> allEntries = recurringRuleEntryRepository.findByRecurringRuleIdInOrderByIdAsc(ruleIds);
        Map<Long, List<RecurringRuleEntryView>> entriesByRuleId = allEntries.stream()
                .map(this::toEntryView)
                .collect(Collectors.groupingBy(
                        RecurringRuleEntryView::recurringRuleId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return rules.stream()
                .map(rule -> toRuleView(
                        rule,
                        weekdaysByRuleId.getOrDefault(rule.getId(), Set.of()),
                        entriesByRuleId.getOrDefault(rule.getId(), List.of())
                ))
                .toList();
    }

    private RecurringTransactionRuleView loadWithChildren(RecurringTransactionRule rule) {
        Set<DayOfWeek> weekdays = recurringRuleWeekdayRepository.findByIdRecurringRuleId(rule.getId())
                .stream()
                .map(w -> w.getId().getWeekday())
                .collect(Collectors.toSet());

        List<RecurringRuleEntryView> entries = recurringRuleEntryRepository.findByRecurringRuleIdOrderByIdAsc(rule.getId())
                .stream()
                .map(this::toEntryView)
                .toList();

        return toRuleView(rule, weekdays, entries);
    }

    private void validateRuleScalars(
            String name,
            FinancialTransactionType transactionType,
            Object postingMode,
            RecurrenceFrequency frequency,
            int intervalCount,
            LocalDate startDate,
            LocalDate endDate,
            String description,
            Integer dayOfMonth,
            Integer monthOfYear,
            Set<DayOfWeek> weekdays
    ) {
        if (name == null || name.isBlank()) {
            throw new InvalidRecurringTransactionRuleException("Rule name must not be blank");
        }
        if (name.length() > 500) {
            throw new InvalidRecurringTransactionRuleException("Rule name must not exceed 500 characters");
        }
        if (transactionType == null) {
            throw new InvalidRecurringTransactionRuleException("transactionType must not be null");
        }
        if (postingMode == null) {
            throw new InvalidRecurringTransactionRuleException("postingMode must not be null");
        }
        if (frequency == null) {
            throw new InvalidRecurringTransactionRuleException("frequency must not be null");
        }
        if (intervalCount <= 0) {
            throw new InvalidRecurringTransactionRuleException("intervalCount must be positive");
        }
        if (startDate == null) {
            throw new InvalidRecurringTransactionRuleException("startDate must not be null");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidRecurringTransactionRuleException("endDate must be on or after startDate");
        }
        if (description != null && description.length() > 1000) {
            throw new InvalidRecurringTransactionRuleException("description must not exceed 1000 characters");
        }
        validateFrequencyCompatibility(frequency, dayOfMonth, monthOfYear, weekdays);
    }

    private void validateFrequencyCompatibility(
            RecurrenceFrequency frequency,
            Integer dayOfMonth,
            Integer monthOfYear,
            Set<DayOfWeek> weekdays
    ) {
        if (frequency == RecurrenceFrequency.DAILY) {
            if (weekdays != null && !weekdays.isEmpty()) {
                throw new InvalidRecurringTransactionRuleException("DAILY frequency must not specify weekdays");
            }
            if (dayOfMonth != null || monthOfYear != null) {
                throw new InvalidRecurringTransactionRuleException("DAILY frequency must not specify dayOfMonth or monthOfYear");
            }
        } else if (frequency == RecurrenceFrequency.WEEKLY) {
            if (weekdays == null || weekdays.isEmpty()) {
                throw new InvalidRecurringTransactionRuleException("WEEKLY frequency must specify at least one weekday");
            }
            if (dayOfMonth != null || monthOfYear != null) {
                throw new InvalidRecurringTransactionRuleException("WEEKLY frequency must not specify dayOfMonth or monthOfYear");
            }
        } else if (frequency == RecurrenceFrequency.MONTHLY) {
            if (weekdays != null && !weekdays.isEmpty()) {
                throw new InvalidRecurringTransactionRuleException("MONTHLY frequency must not specify weekdays");
            }
            if (dayOfMonth == null || dayOfMonth < 1 || dayOfMonth > 31) {
                throw new InvalidRecurringTransactionRuleException("MONTHLY frequency requires dayOfMonth between 1 and 31");
            }
            if (monthOfYear != null) {
                throw new InvalidRecurringTransactionRuleException("MONTHLY frequency must not specify monthOfYear");
            }
        } else if (frequency == RecurrenceFrequency.YEARLY) {
            if (weekdays != null && !weekdays.isEmpty()) {
                throw new InvalidRecurringTransactionRuleException("YEARLY frequency must not specify weekdays");
            }
            if (dayOfMonth == null || dayOfMonth < 1 || dayOfMonth > 31) {
                throw new InvalidRecurringTransactionRuleException("YEARLY frequency requires dayOfMonth between 1 and 31");
            }
            if (monthOfYear == null || monthOfYear < 1 || monthOfYear > 12) {
                throw new InvalidRecurringTransactionRuleException("YEARLY frequency requires monthOfYear between 1 and 12");
            }
        }
    }

    private void validateEntriesShapeAndSign(
            FinancialTransactionType type,
            List<RecurringRuleEntryInput> entries
    ) {
        if (entries == null || entries.isEmpty()) {
            throw new InvalidRecurringTransactionRuleException("Recurring rule must have at least one entry");
        }
        for (RecurringRuleEntryInput entry : entries) {
            if (entry.walletId() == null) {
                throw new InvalidRecurringTransactionRuleException("walletId must not be null");
            }
            BigDecimal normalized = FinanceValidationUtils.normalizeMoney(entry.amountDelta(), "amountDelta", InvalidRecurringTransactionRuleException::new);
            if (normalized.compareTo(BigDecimal.ZERO) == 0) {
                throw new InvalidRecurringTransactionRuleException("amountDelta must not be zero");
            }
        }

        List<Long> distinctWalletIds = entries.stream()
                .map(RecurringRuleEntryInput::walletId)
                .distinct()
                .toList();
        if (distinctWalletIds.size() != entries.size()) {
            throw new InvalidRecurringTransactionRuleException("A wallet may only appear once in a recurring rule's entries");
        }

        if (type == FinancialTransactionType.INCOME) {
            if (entries.size() != 1) {
                throw new InvalidRecurringTransactionRuleException("INCOME recurring rule must have exactly one entry");
            }
            if (entries.getFirst().amountDelta().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidRecurringTransactionRuleException("INCOME entry amountDelta must be positive");
            }
        } else if (type == FinancialTransactionType.EXPENSE) {
            if (entries.size() != 1) {
                throw new InvalidRecurringTransactionRuleException("EXPENSE recurring rule must have exactly one entry");
            }
            if (entries.getFirst().amountDelta().compareTo(BigDecimal.ZERO) >= 0) {
                throw new InvalidRecurringTransactionRuleException("EXPENSE entry amountDelta must be negative");
            }
        } else if (type == FinancialTransactionType.TRANSFER) {
            if (entries.size() != 2) {
                throw new InvalidRecurringTransactionRuleException("TRANSFER recurring rule must have exactly two entries");
            }
            long positiveCount = entries.stream()
                    .filter(e -> e.amountDelta().compareTo(BigDecimal.ZERO) > 0)
                    .count();
            long negativeCount = entries.stream()
                    .filter(e -> e.amountDelta().compareTo(BigDecimal.ZERO) < 0)
                    .count();
            if (positiveCount != 1 || negativeCount != 1) {
                throw new InvalidRecurringTransactionRuleException(
                        "TRANSFER recurring rule must have exactly one source entry (negative delta) and one destination entry (positive delta)"
                );
            }
        }
    }

    private void validateCategoryCompatibility(FinancialTransactionType type, TransactionCategory category) {
        if (type == FinancialTransactionType.TRANSFER) {
            if (category != null) {
                throw new InvalidRecurringTransactionRuleException("TRANSFER recurring rules must not have a category");
            }
            return;
        }
        if (category == null) {
            return;
        }
        if (type == FinancialTransactionType.INCOME) {
            if (category.getKind() != TransactionCategoryKind.INCOME && category.getKind() != TransactionCategoryKind.BOTH) {
                throw new InvalidRecurringTransactionRuleException(
                        "Category kind " + category.getKind() + " is incompatible with INCOME recurring rule"
                );
            }
        } else if (type == FinancialTransactionType.EXPENSE) {
            if (category.getKind() != TransactionCategoryKind.EXPENSE && category.getKind() != TransactionCategoryKind.BOTH) {
                throw new InvalidRecurringTransactionRuleException(
                        "Category kind " + category.getKind() + " is incompatible with EXPENSE recurring rule"
                );
            }
        }
    }

    private void validateWalletsNotDeleted(List<Wallet> wallets) {
        for (Wallet wallet : wallets) {
            if (wallet.getDeletedAt() != null) {
                throw new InvalidRecurringTransactionRuleException("Referenced wallet is deleted: " + wallet.getId());
            }
        }
    }

    private RecurringRuleEntryView toEntryView(RecurringRuleEntry entry) {
        return new RecurringRuleEntryView(
                entry.getId(),
                entry.getRecurringRuleId(),
                entry.getWalletId(),
                entry.getAmountDelta()
        );
    }

    private RecurringTransactionRuleView toRuleView(
            RecurringTransactionRule r,
            Set<DayOfWeek> weekdays,
            List<RecurringRuleEntryView> entries
    ) {
        return new RecurringTransactionRuleView(
                r.getId(),
                r.getName(),
                r.getTransactionType(),
                r.getCategoryId(),
                r.getPostingMode(),
                r.getFrequency(),
                r.getIntervalCount(),
                r.getDayOfMonth(),
                r.getMonthOfYear(),
                r.getStartDate(),
                r.getEndDate(),
                r.getPostingTime(),
                r.getNextRunAt(),
                r.getDescription(),
                r.getNotes(),
                r.isActive(),
                weekdays,
                entries,
                r.getCreatedAt(),
                r.getUpdatedAt(),
                r.getDeletedAt()
        );
    }
}
