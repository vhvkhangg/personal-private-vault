package com.vhvkhangg.personalprivatevault.finance.recurring;

import com.vhvkhangg.personalprivatevault.finance.recurring.command.CreateRecurringTransactionRuleCommand;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.UpdateRecurringTransactionRuleCommand;
import com.vhvkhangg.personalprivatevault.finance.view.RecurringTransactionRuleView;

import java.time.Instant;
import java.util.List;

/**
 * Public capability operations for recurring transaction rules.
 */
public interface RecurringTransactionRuleOperations {

    /**
     * Creates a new recurring transaction rule.
     *
     * @param command creation command
     * @return created rule view
     */
    RecurringTransactionRuleView createRule(CreateRecurringTransactionRuleCommand command);

    /**
     * Fully updates an existing recurring transaction rule, replacing schedule fields, weekdays, and entries atomically.
     *
     * @param command update command
     * @return updated rule view
     */
    RecurringTransactionRuleView updateRule(UpdateRecurringTransactionRuleCommand command);

    /**
     * Finds a non-deleted recurring transaction rule by ID.
     *
     * @param id rule identifier
     * @return rule view
     */
    RecurringTransactionRuleView findRuleById(Long id);

    /**
     * Finds due active recurring transaction rules with next_run_at <= cutoff,
     * ordered by next_run_at ASC, id ASC. Side-effect free read.
     *
     * @param cutoff cutoff timestamp (inclusive)
     * @param limit positive maximum count
     * @return list of due rule views
     */
    List<RecurringTransactionRuleView> findDueRules(Instant cutoff, int limit);

    /**
     * Finds non-deleted recurring transaction rules ordered by name ASC, id ASC.
     *
     * @param activeFilter optional active filter
     * @param limit positive maximum count
     * @return list of rule views
     */
    List<RecurringTransactionRuleView> findRules(Boolean activeFilter, int limit);

    /**
     * Soft-deletes a recurring transaction rule. Idempotent.
     *
     * @param id rule identifier
     * @return rule view
     */
    RecurringTransactionRuleView softDeleteRule(Long id);

    /**
     * Restores a soft-deleted recurring transaction rule, revalidating retained schedule and entry shape. Idempotent.
     *
     * @param id rule identifier
     * @return rule view
     */
    RecurringTransactionRuleView restoreRule(Long id);
}
