package com.vhvkhangg.personalprivatevault.finance;

import com.vhvkhangg.personalprivatevault.finance.category.TransactionCategoryOperations;
import com.vhvkhangg.personalprivatevault.finance.category.command.CreateTransactionCategoryCommand;
import com.vhvkhangg.personalprivatevault.finance.enums.DayOfWeek;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurrenceFrequency;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurringPostingMode;
import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;
import com.vhvkhangg.personalprivatevault.finance.enums.WalletType;
import com.vhvkhangg.personalprivatevault.finance.recurring.RecurringTransactionRuleOperations;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.CreateRecurringTransactionRuleCommand;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.RecurringRuleEntryInput;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.UpdateRecurringTransactionRuleCommand;
import com.vhvkhangg.personalprivatevault.finance.recurring.exception.InvalidRecurringTransactionRuleException;
import com.vhvkhangg.personalprivatevault.finance.recurring.exception.RecurringTransactionRuleNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.view.RecurringTransactionRuleView;
import com.vhvkhangg.personalprivatevault.finance.view.TransactionCategoryView;
import com.vhvkhangg.personalprivatevault.finance.view.WalletView;
import com.vhvkhangg.personalprivatevault.finance.wallet.WalletOperations;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.CreateWalletCommand;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringRuleEntry;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringRuleWeekday;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringTransactionRule;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.RecurringRuleEntryRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.RecurringRuleWeekdayRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.RecurringTransactionRuleRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class RecurringTransactionRuleIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private RecurringTransactionRuleOperations recurringOperations;

    @Autowired
    private WalletOperations walletOperations;

    @Autowired
    private TransactionCategoryOperations categoryOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private RecurringTransactionRuleRepository recurringTransactionRuleRepository;

    @Autowired
    private RecurringRuleWeekdayRepository recurringRuleWeekdayRepository;

    @Autowired
    private RecurringRuleEntryRepository recurringRuleEntryRepository;

    private Long usdWalletId;
    private Long vndWalletId;
    private Long expenseCategoryId;

    @BeforeEach
    void setUp() {
        cleanUp();
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol) VALUES ('USD', 'US Dollar', '$')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol) VALUES ('VND', 'Vietnamese Dong', '₫')
                ON CONFLICT (code) DO NOTHING
                """);

        WalletView w1 = walletOperations.createWallet(new CreateWalletCommand(
                "USD Wallet", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));
        usdWalletId = w1.id();

        WalletView w2 = walletOperations.createWallet(new CreateWalletCommand(
                "VND Wallet", WalletType.BANK_ACCOUNT, "VND", BigDecimal.ZERO, null, true
        ));
        vndWalletId = w2.id();

        TransactionCategoryView cat = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Expense Cat", TransactionCategoryKind.EXPENSE, null, true
        ));
        expenseCategoryId = cat.id();
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM subscriptions");
        jdbcTemplate.execute("DELETE FROM recurring_rule_entries");
        jdbcTemplate.execute("DELETE FROM recurring_rule_weekdays");
        jdbcTemplate.execute("DELETE FROM recurring_transaction_rules");
        jdbcTemplate.execute("DELETE FROM financial_transaction_entries");
        jdbcTemplate.execute("DELETE FROM financial_transactions");
        jdbcTemplate.execute("DELETE FROM transaction_categories");
        jdbcTemplate.execute("DELETE FROM wallets");
        jdbcTemplate.execute("DELETE FROM currencies WHERE code = 'EUR'");
    }

    @Test
    @DisplayName("Validates schedule field compatibility across DAILY, WEEKLY, MONTHLY, and YEARLY frequencies")
    void validatesScheduleFieldCompatibility() {
        LocalDate start = LocalDate.of(2026, 1, 1);

        // DAILY: valid with no day/month/weekdays
        RecurringTransactionRuleView dailyRule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Daily Standup",
                FinancialTransactionType.EXPENSE,
                expenseCategoryId,
                RecurringPostingMode.REQUIRE_CONFIRMATION,
                RecurrenceFrequency.DAILY,
                1,
                null,
                null,
                start,
                null,
                null,
                null,
                null,
                null,
                true,
                null,
                List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
        ));
        assertThat(dailyRule.id()).isNotNull();

        // DAILY: invalid if weekdays provided
        assertThatThrownBy(() -> recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Bad Daily", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, 1, null, null, start, null, null, null, null, null, true,
                Set.of(DayOfWeek.MONDAY), List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
        )))
                .isInstanceOf(InvalidRecurringTransactionRuleException.class)
                .hasMessageContaining("must not specify weekdays");

        // WEEKLY: valid with weekdays
        RecurringTransactionRuleView weeklyRule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Weekly Gym", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-25.0000")))
        ));
        assertThat(weeklyRule.weekdays()).containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);

        // WEEKLY: invalid without weekdays
        assertThatThrownBy(() -> recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Bad Weekly", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                Set.of(), List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-25.0000")))
        )))
                .isInstanceOf(InvalidRecurringTransactionRuleException.class)
                .hasMessageContaining("at least one weekday");

        // MONTHLY: valid with dayOfMonth
        RecurringTransactionRuleView monthlyRule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Monthly Rent", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.REQUIRE_CONFIRMATION,
                RecurrenceFrequency.MONTHLY, 1, 15, null, start, null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-1200.0000")))
        ));
        assertThat(monthlyRule.dayOfMonth()).isEqualTo(15);

        // MONTHLY: invalid dayOfMonth > 31
        assertThatThrownBy(() -> recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Bad Monthly", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.MONTHLY, 1, 32, null, start, null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-1200.0000")))
        )))
                .isInstanceOf(InvalidRecurringTransactionRuleException.class)
                .hasMessageContaining("dayOfMonth between 1 and 31");

        // YEARLY: valid with dayOfMonth and monthOfYear
        RecurringTransactionRuleView yearlyRule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Yearly Insurance", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.REQUIRE_CONFIRMATION,
                RecurrenceFrequency.YEARLY, 1, 10, 6, start, null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-800.0000")))
        ));
        assertThat(yearlyRule.dayOfMonth()).isEqualTo(10);
        assertThat(yearlyRule.monthOfYear()).isEqualTo(6);

        // YEARLY: invalid without monthOfYear
        assertThatThrownBy(() -> recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Bad Yearly", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.YEARLY, 1, 10, null, start, null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-800.0000")))
        )))
                .isInstanceOf(InvalidRecurringTransactionRuleException.class)
                .hasMessageContaining("monthOfYear between 1 and 12");
    }

    @Test
    @DisplayName("Updating a soft-deleted rule is rejected until restored")
    void updateSoftDeletedRuleRejectedUntilRestored() {
        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "To Delete", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, 1, null, null, LocalDate.now(), null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-5.0000")))
        ));

        // Soft delete
        recurringOperations.softDeleteRule(rule.id());

        // Update rejected
        assertThatThrownBy(() -> recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                rule.id(), "To Delete", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, 1, null, null, LocalDate.now(), null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-5.0000")))
        )))
                .isInstanceOf(InvalidRecurringTransactionRuleException.class)
                .hasMessageContaining("Cannot update a soft-deleted recurring rule");

        // Restore
        recurringOperations.restoreRule(rule.id());

        // Update succeeds
        RecurringTransactionRuleView updated = recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                rule.id(), "Restored and Updated", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, 1, null, null, LocalDate.now(), null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-5.0000")))
        ));
        assertThat(updated.name()).isEqualTo("Restored and Updated");
    }

    @Test
    @DisplayName("Deterministic PostgreSQL contention: replacement vs replacement on same rule preserves one coherent configuration")
    void contentionReplacementVsReplacementPreservesCoherentConfiguration() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Initial Rule", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, 1, null, null, start, null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: updates to WEEKLY schedule with MONDAY, WEDNESDAY and USD entry -20
            Future<RecurringTransactionRuleView> f1 = executor.submit(() -> txTemplate.execute(status -> {
                RecurringTransactionRuleView updated = recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                        rule.id(), "Weekly Schedule 1", FinancialTransactionType.EXPENSE, expenseCategoryId,
                        RecurringPostingMode.REQUIRE_CONFIRMATION, RecurrenceFrequency.WEEKLY, 1, null, null, start,
                        null, null, null, null, null, true,
                        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                        List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-20.0000")))
                ));
                thread1HasLock.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return updated;
            }));

            assertThat(thread1HasLock.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: attempts to update to MONTHLY schedule with day 25 and VND entry -50.
            // Blocks on the recurring rule row lock held by Thread 1.
            Future<RecurringTransactionRuleView> f2 = executor.submit(() -> recurringOperations.updateRule(
                    new UpdateRecurringTransactionRuleCommand(
                            rule.id(), "Monthly Schedule 2", FinancialTransactionType.EXPENSE, expenseCategoryId,
                            RecurringPostingMode.AUTO_POST, RecurrenceFrequency.MONTHLY, 2, 25, null, start,
                            null, null, null, null, null, true,
                            null,
                            List.of(new RecurringRuleEntryInput(vndWalletId, new BigDecimal("-50.0000")))
                    )
            ));

            // Observe lock contention on recurring_transaction_rules table
            awaitCompetingLock("recurring_transaction_rules", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            RecurringTransactionRuleView r1 = f1.get(10, TimeUnit.SECONDS);
            assertThat(r1.frequency()).isEqualTo(RecurrenceFrequency.WEEKLY);

            RecurringTransactionRuleView r2 = f2.get(10, TimeUnit.SECONDS);
            assertThat(r2.frequency()).isEqualTo(RecurrenceFrequency.MONTHLY);

            // Final state in DB must be fully coherent from Thread 2 (never mixed child sets)
            RecurringTransactionRuleView finalRule = recurringOperations.findRuleById(rule.id());
            assertThat(finalRule.frequency()).isEqualTo(RecurrenceFrequency.MONTHLY);
            assertThat(finalRule.dayOfMonth()).isEqualTo(25);
            assertThat(finalRule.weekdays()).isEmpty(); // No leftover weekdays from Thread 1!
            assertThat(finalRule.entries()).hasSize(1);
            assertThat(finalRule.entries().getFirst().walletId()).isEqualTo(vndWalletId);
            assertThat(finalRule.entries().getFirst().amountDelta()).isEqualByComparingTo(new BigDecimal("-50.0000"));

            // Database count check: exactly 0 weekdays, exactly 1 entry
            Integer weekdayCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM recurring_rule_weekdays WHERE recurring_rule_id = ?",
                    Integer.class,
                    rule.id()
            );
            assertThat(weekdayCount).isZero();

            Integer entryCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM recurring_rule_entries WHERE recurring_rule_id = ?",
                    Integer.class,
                    rule.id()
            );
            assertThat(entryCount).isEqualTo(1);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Side-effect-free findDueRules retrieves active non-deleted rules with next_run_at <= cutoff ordered next_run_at ASC, id ASC")
    void dueRulesReadSideEffectFree() {
        Instant cutoff = Instant.parse("2026-10-01T12:00:00Z");
        LocalDate start = LocalDate.of(2026, 1, 1);

        // Rule 1: due (next_run_at = cutoff - 1h)
        RecurringTransactionRuleView r1 = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Due 1", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, 1, null, null, start, null, LocalTime.NOON,
                cutoff.minus(Duration.ofHours(1)), null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
        ));

        // Rule 2: due (next_run_at = cutoff - 2h) -> earlier, should be first
        RecurringTransactionRuleView r2 = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Due 2", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, 1, null, null, start, null, LocalTime.NOON,
                cutoff.minus(Duration.ofHours(2)), null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-20.0000")))
        ));

        // Rule 3: not due (next_run_at = cutoff + 1h)
        recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Future", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, 1, null, null, start, null, LocalTime.NOON,
                cutoff.plus(Duration.ofHours(1)), null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-30.0000")))
        ));

        // Rule 4: due but inactive (must be excluded)
        recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Inactive Due", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, 1, null, null, start, null, LocalTime.NOON,
                cutoff.minus(Duration.ofHours(1)), null, null, false,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-40.0000")))
        ));

        // Query due rules
        List<RecurringTransactionRuleView> due = recurringOperations.findDueRules(cutoff, 10);
        assertThat(due).hasSize(2);
        assertThat(due.get(0).id()).isEqualTo(r2.id()); // earlier first
        assertThat(due.get(1).id()).isEqualTo(r1.id());

        // Verify read was completely side-effect free: next_run_at unchanged in DB
        Instant r1NextRun = jdbcTemplate.queryForObject(
                "SELECT next_run_at FROM recurring_transaction_rules WHERE id = ?",
                Instant.class,
                r1.id()
        );
        assertThat(r1NextRun).isEqualTo(cutoff.minus(Duration.ofHours(1)));
    }

    @Test
    @DisplayName("FR11-1: Preloaded recurring weekday replacement correctly preserves weekdays across same, overlapping, disjoint, and rollback")
    void preloadedWeekdayReplacementPreservesWeekdaysAcrossSameOverlappingDisjointAndRollback() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Preload Test Rule", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                Set.of(DayOfWeek.MONDAY),
                List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
        ));

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // 1. Within one transaction: preload rule, full-replace with SAME weekday (MONDAY), then OVERLAPPING (MONDAY, WEDNESDAY), then DISJOINT (FRIDAY)
        txTemplate.executeWithoutResult(status -> {
            RecurringTransactionRuleView preloaded = recurringOperations.findRuleById(rule.id());
            assertThat(preloaded.weekdays()).containsExactly(DayOfWeek.MONDAY);

            // Same weekday replacement after preload
            RecurringTransactionRuleView same = recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                    rule.id(), "Preload Test Rule", FinancialTransactionType.EXPENSE, expenseCategoryId,
                    RecurringPostingMode.AUTO_POST, RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                    Set.of(DayOfWeek.MONDAY),
                    List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
            ));
            assertThat(same.weekdays()).containsExactly(DayOfWeek.MONDAY);

            // Overlapping weekdays replacement in same transaction
            RecurringTransactionRuleView overlapping = recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                    rule.id(), "Preload Test Rule", FinancialTransactionType.EXPENSE, expenseCategoryId,
                    RecurringPostingMode.AUTO_POST, RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                    List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
            ));
            assertThat(overlapping.weekdays()).containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);

            // Disjoint weekday replacement in same transaction
            RecurringTransactionRuleView disjoint = recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                    rule.id(), "Preload Test Rule", FinancialTransactionType.EXPENSE, expenseCategoryId,
                    RecurringPostingMode.AUTO_POST, RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                    Set.of(DayOfWeek.FRIDAY),
                    List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
            ));
            assertThat(disjoint.weekdays()).containsExactly(DayOfWeek.FRIDAY);
        });

        // Verify committed state in DB: exactly 1 weekday (FRIDAY)
        RecurringTransactionRuleView committed = recurringOperations.findRuleById(rule.id());
        assertThat(committed.weekdays()).containsExactly(DayOfWeek.FRIDAY);
        Integer dbWeekdayCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM recurring_rule_weekdays WHERE recurring_rule_id = ?",
                Integer.class, rule.id()
        );
        assertThat(dbWeekdayCount).isEqualTo(1);

        // 2. Rollback preservation: preload, update to SUNDAY, roll back -> original FRIDAY preserved
        txTemplate.executeWithoutResult(status -> {
            recurringOperations.findRuleById(rule.id());
            recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                    rule.id(), "Preload Test Rule", FinancialTransactionType.EXPENSE, expenseCategoryId,
                    RecurringPostingMode.AUTO_POST, RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                    Set.of(DayOfWeek.SUNDAY),
                    List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
            ));
            status.setRollbackOnly();
        });

        RecurringTransactionRuleView afterRollback = recurringOperations.findRuleById(rule.id());
        assertThat(afterRollback.weekdays()).containsExactly(DayOfWeek.FRIDAY);
    }

    @Test
    @DisplayName("FR11-3 contention: Replacement vs Delete (Winner Order A: Replacement wins first, Delete waits)")
    void contentionReplacementVsDeleteWinnerReplacement() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Rule A", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                Set.of(DayOfWeek.MONDAY),
                List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: updates weekdays to TUESDAY and amount to -25 in uncommitted transaction
            Future<RecurringTransactionRuleView> f1 = executor.submit(() -> txTemplate.execute(status -> {
                RecurringTransactionRuleView updated = recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                        rule.id(), "Rule A Replaced", FinancialTransactionType.EXPENSE, expenseCategoryId,
                        RecurringPostingMode.AUTO_POST, RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                        Set.of(DayOfWeek.TUESDAY),
                        List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-25.0000")))
                ));
                thread1HasLock.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return updated;
            }));

            assertThat(thread1HasLock.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: attempts to soft-delete rule, blocks on recurring rule row write lock
            Future<RecurringTransactionRuleView> f2 = executor.submit(() -> recurringOperations.softDeleteRule(rule.id()));

            awaitCompetingLock("recurring_transaction_rules", Duration.ofSeconds(5));
            thread2ReadyToCommit.countDown();

            RecurringTransactionRuleView r1 = f1.get(10, TimeUnit.SECONDS);
            assertThat(r1.weekdays()).containsExactly(DayOfWeek.TUESDAY);

            RecurringTransactionRuleView r2 = f2.get(10, TimeUnit.SECONDS);
            assertThat(r2.deletedAt()).isNotNull();

            // Replaced children from Thread 1 are retained in DB
            Integer weekdayCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM recurring_rule_weekdays WHERE recurring_rule_id = ? AND weekday = 'TUESDAY'",
                    Integer.class, rule.id()
            );
            assertThat(weekdayCount).isEqualTo(1);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-3 contention: Replacement vs Delete (Winner Order B: Delete wins first, Replacement waits and is rejected)")
    void contentionReplacementVsDeleteWinnerDelete() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Rule B", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                Set.of(DayOfWeek.MONDAY),
                List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: soft-deletes rule in uncommitted transaction
            Future<RecurringTransactionRuleView> f1 = executor.submit(() -> txTemplate.execute(status -> {
                RecurringTransactionRuleView deleted = recurringOperations.softDeleteRule(rule.id());
                thread1HasLock.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return deleted;
            }));

            assertThat(thread1HasLock.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: attempts to update rule, blocks on recurring rule row write lock
            Future<RecurringTransactionRuleView> f2 = executor.submit(() -> recurringOperations.updateRule(
                    new UpdateRecurringTransactionRuleCommand(
                            rule.id(), "Rule B Update Attempt", FinancialTransactionType.EXPENSE, expenseCategoryId,
                            RecurringPostingMode.AUTO_POST, RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                            Set.of(DayOfWeek.WEDNESDAY),
                            List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-30.0000")))
                    )
            ));

            awaitCompetingLock("recurring_transaction_rules", Duration.ofSeconds(5));
            thread2ReadyToCommit.countDown();

            RecurringTransactionRuleView r1 = f1.get(10, TimeUnit.SECONDS);
            assertThat(r1.deletedAt()).isNotNull();

            // Thread 2 must fail after unblocking because rule is soft-deleted
            assertThatThrownBy(() -> {
                try {
                    f2.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(InvalidRecurringTransactionRuleException.class)
                    .hasMessageContaining("Cannot update a soft-deleted recurring rule. Restore it first.");

            // Verify no partial writes from Thread 2
            Integer wednesdayCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM recurring_rule_weekdays WHERE recurring_rule_id = ? AND weekday = 'WEDNESDAY'",
                    Integer.class, rule.id()
            );
            assertThat(wednesdayCount).isZero();
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-1: Stale managed weekday reintroduced after external deletion persists cleanly without merge drop")
    void staleManagedWeekdayReaddedAfterExternalRemovalPersistsCleanly() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Stale Weekday Sequential", FinancialTransactionType.INCOME, null, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                Set.of(DayOfWeek.MONDAY),
                List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("10.0000")))
        ));

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            txTemplate.executeWithoutResult(status -> {
                // Step 2: Tx A loads rule through public findRuleById, managing MONDAY composite-key child
                RecurringTransactionRuleView loadedA = recurringOperations.findRuleById(rule.id());
                assertThat(loadedA.weekdays()).containsExactly(DayOfWeek.MONDAY);

                // Step 3: Independent transaction B replaces rule with TUESDAY and commits
                try {
                    executor.submit(() -> recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                            rule.id(), "Updated By Tx B", FinancialTransactionType.INCOME, null,
                            RecurringPostingMode.AUTO_POST, RecurrenceFrequency.WEEKLY, 1, null, null, start,
                            null, null, null, null, null, true,
                            Set.of(DayOfWeek.TUESDAY),
                            List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("10.0000")))
                    ))).get(10, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                // Step 4: Tx A full-replaces the rule with MONDAY again and commits
                recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                        rule.id(), "Re-updated By Tx A", FinancialTransactionType.INCOME, null,
                        RecurringPostingMode.AUTO_POST, RecurrenceFrequency.WEEKLY, 1, null, null, start,
                        null, null, null, null, null, true,
                        Set.of(DayOfWeek.MONDAY),
                        List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("15.0000")))
                ));
            });

            // Step 5: Direct SQL assertion on PostgreSQL table proves MONDAY is physically persisted
            Integer weekdayCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM recurring_rule_weekdays WHERE recurring_rule_id = ?",
                    Integer.class, rule.id()
            );
            assertThat(weekdayCount).isEqualTo(1);

            String savedWeekday = jdbcTemplate.queryForObject(
                    "SELECT weekday FROM recurring_rule_weekdays WHERE recurring_rule_id = ?",
                    String.class, rule.id()
            );
            assertThat(savedWeekday).isEqualTo("MONDAY");

            // Direct entry assertion
            Integer entryCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM recurring_rule_entries WHERE recurring_rule_id = ?",
                    Integer.class, rule.id()
            );
            assertThat(entryCount).isEqualTo(1);

            // Fresh reload verification
            RecurringTransactionRuleView fresh = recurringOperations.findRuleById(rule.id());
            assertThat(fresh.name()).isEqualTo("Re-updated By Tx A");
            assertThat(fresh.weekdays()).containsExactly(DayOfWeek.MONDAY);
            assertThat(fresh.entries()).hasSize(1);
            assertThat(fresh.entries().getFirst().amountDelta()).isEqualByComparingTo(new BigDecimal("15.0000"));
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-1 contention: Stale managed weekday re-add under lock contention reconciles preloaded context and persists cleanly")
    void staleManagedWeekdayContentionWithPreloadedContextReconcilesFreshState() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Stale Contention Rule", FinancialTransactionType.INCOME, null, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("10.0000")))
        ));

        CountDownLatch thread2Preloaded = new CountDownLatch(1);
        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread1CanCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 2 (waiter): preloads MONDAY, WEDNESDAY in its context before waiting
            Future<RecurringTransactionRuleView> thread2Future = executor.submit(() -> txTemplate.execute(status -> {
                RecurringTransactionRuleView preloaded = recurringOperations.findRuleById(rule.id());
                assertThat(preloaded.weekdays()).containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
                thread2Preloaded.countDown();

                try {
                    boolean ready = thread1HasLock.await(5, TimeUnit.SECONDS);
                    assertThat(ready).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }

                // Attempts update back to overlapping/disjoint set (MONDAY, FRIDAY), blocks on lock
                return recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                        rule.id(), "Re-updated With Overlap", FinancialTransactionType.INCOME, null,
                        RecurringPostingMode.AUTO_POST, RecurrenceFrequency.WEEKLY, 1, null, null, start,
                        null, null, null, null, null, true,
                        Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                        List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("30.0000")))
                ));
            }));

            assertThat(thread2Preloaded.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 1 (winner): replaces with disjoint set (TUESDAY, THURSDAY) in uncommitted transaction
            Future<RecurringTransactionRuleView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                RecurringTransactionRuleView updated1 = recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                        rule.id(), "Winner Tx 1 Disjoint", FinancialTransactionType.INCOME, null,
                        RecurringPostingMode.AUTO_POST, RecurrenceFrequency.WEEKLY, 1, null, null, start,
                        null, null, null, null, null, true,
                        Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                        List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("20.0000")))
                ));
                thread1HasLock.countDown();
                try {
                    boolean awaited = thread1CanCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return updated1;
            }));

            assertThat(thread1HasLock.await(5, TimeUnit.SECONDS)).isTrue();

            // Observe lock contention in PostgreSQL
            awaitCompetingLock("recurring_transaction_rules", Duration.ofSeconds(5));

            // Allow Thread 1 to commit
            thread1CanCommit.countDown();

            RecurringTransactionRuleView r1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(r1.name()).isEqualTo("Winner Tx 1 Disjoint");

            RecurringTransactionRuleView r2 = thread2Future.get(10, TimeUnit.SECONDS);
            assertThat(r2.name()).isEqualTo("Re-updated With Overlap");
            assertThat(r2.weekdays()).containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);

            // Direct SQL check on PostgreSQL table
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM recurring_rule_weekdays WHERE recurring_rule_id = ?",
                    Integer.class, rule.id()
            );
            assertThat(count).isEqualTo(2);

            List<String> weekdays = jdbcTemplate.queryForList(
                    "SELECT weekday FROM recurring_rule_weekdays WHERE recurring_rule_id = ? ORDER BY weekday ASC",
                    String.class, rule.id()
            );
            assertThat(weekdays).containsExactlyInAnyOrder("FRIDAY", "MONDAY");

            // Fresh reload verification
            RecurringTransactionRuleView fresh = recurringOperations.findRuleById(rule.id());
            assertThat(fresh.weekdays()).containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
            assertThat(fresh.entries()).hasSize(1);
            assertThat(fresh.entries().getFirst().amountDelta()).isEqualByComparingTo(new BigDecimal("30.0000"));
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-1 contention rollback: Waiting update rollback after contention preserves winner's committed weekdays")
    void staleManagedWeekdayContentionRollbackPreservesWinnerState() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Stale Rollback Rule", FinancialTransactionType.INCOME, null, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                Set.of(DayOfWeek.MONDAY),
                List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("10.0000")))
        ));

        CountDownLatch thread2Preloaded = new CountDownLatch(1);
        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread1CanCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 2 (waiter): preloads MONDAY, attempts update that subsequently rolls back
            Future<?> thread2Future = executor.submit(() -> txTemplate.execute(status -> {
                RecurringTransactionRuleView preloaded = recurringOperations.findRuleById(rule.id());
                assertThat(preloaded.weekdays()).containsExactly(DayOfWeek.MONDAY);
                thread2Preloaded.countDown();

                try {
                    boolean ready = thread1HasLock.await(5, TimeUnit.SECONDS);
                    assertThat(ready).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }

                // Attempts update with MONDAY, blocks on lock
                recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                        rule.id(), "Attempted By Waiter", FinancialTransactionType.INCOME, null,
                        RecurringPostingMode.AUTO_POST, RecurrenceFrequency.WEEKLY, 1, null, null, start,
                        null, null, null, null, null, true,
                        Set.of(DayOfWeek.MONDAY),
                        List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("99.0000")))
                ));
                // Simulate business rule failure / forced rollback
                status.setRollbackOnly();
                return null;
            }));

            assertThat(thread2Preloaded.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 1 (winner): updates to TUESDAY in uncommitted transaction
            Future<RecurringTransactionRuleView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                RecurringTransactionRuleView updated1 = recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                        rule.id(), "Winner Committed", FinancialTransactionType.INCOME, null,
                        RecurringPostingMode.AUTO_POST, RecurrenceFrequency.WEEKLY, 1, null, null, start,
                        null, null, null, null, null, true,
                        Set.of(DayOfWeek.TUESDAY),
                        List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("20.0000")))
                ));
                thread1HasLock.countDown();
                try {
                    boolean awaited = thread1CanCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return updated1;
            }));

            assertThat(thread1HasLock.await(5, TimeUnit.SECONDS)).isTrue();

            awaitCompetingLock("recurring_transaction_rules", Duration.ofSeconds(5));
            thread1CanCommit.countDown();

            thread1Future.get(10, TimeUnit.SECONDS);
            thread2Future.get(10, TimeUnit.SECONDS);

            // Winner state preserved, no partial writes from rolled back waiter
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM recurring_rule_weekdays WHERE recurring_rule_id = ?",
                    Integer.class, rule.id()
            );
            assertThat(count).isEqualTo(1);

            String savedWeekday = jdbcTemplate.queryForObject(
                    "SELECT weekday FROM recurring_rule_weekdays WHERE recurring_rule_id = ?",
                    String.class, rule.id()
            );
            assertThat(savedWeekday).isEqualTo("TUESDAY");

            RecurringTransactionRuleView fresh = recurringOperations.findRuleById(rule.id());
            assertThat(fresh.name()).isEqualTo("Winner Committed");
            assertThat(fresh.weekdays()).containsExactly(DayOfWeek.TUESDAY);
            assertThat(fresh.entries().getFirst().amountDelta()).isEqualByComparingTo(new BigDecimal("20.0000"));
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-3 contention: Restore contention with preloaded parent and children reconciles fresh authoritative state")
    void contentionRestoreWithPreloadedParentAndChildren() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Rule Restore Initial", FinancialTransactionType.EXPENSE, expenseCategoryId, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.WEEKLY, 1, null, null, start, null, null, null, null, null, true,
                Set.of(DayOfWeek.MONDAY),
                List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
        ));
        recurringOperations.softDeleteRule(rule.id());

        CountDownLatch thread2Preloaded = new CountDownLatch(1);
        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread1CanCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 2: waiting participant with preloaded context
            Future<RecurringTransactionRuleView> thread2Future = executor.submit(() -> txTemplate.execute(status -> {
                // Explicitly preload parent and children (weekdays and entries) into this persistence context
                RecurringTransactionRule preloadedParent = recurringTransactionRuleRepository.findById(rule.id()).orElseThrow();
                assertThat(preloadedParent.getDeletedAt()).isNotNull();
                List<RecurringRuleWeekday> preloadedWeekdays = recurringRuleWeekdayRepository.findByIdRecurringRuleId(rule.id());
                assertThat(preloadedWeekdays).hasSize(1);
                List<RecurringRuleEntry> preloadedEntries = recurringRuleEntryRepository.findByRecurringRuleIdOrderByIdAsc(rule.id());
                assertThat(preloadedEntries).hasSize(1);
                thread2Preloaded.countDown();

                try {
                    boolean ready = thread1HasLock.await(5, TimeUnit.SECONDS);
                    assertThat(ready).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }

                // Attempts restoreRule, blocks on row lock held by Thread 1
                return recurringOperations.restoreRule(rule.id());
            }));

            // Main thread waits for Thread 2 to complete preloading
            assertThat(thread2Preloaded.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 1: winner acquires lock, restores, and updates to fresh authoritative state (different weekdays & entries)
            Future<RecurringTransactionRuleView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                RecurringTransactionRuleView restored = recurringOperations.restoreRule(rule.id());
                RecurringTransactionRuleView updated = recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                        rule.id(),
                        "Restored And Updated By Winner",
                        FinancialTransactionType.EXPENSE,
                        expenseCategoryId,
                        RecurringPostingMode.AUTO_POST,
                        RecurrenceFrequency.WEEKLY,
                        1,
                        null,
                        null,
                        start,
                        null,
                        null,
                        null,
                        "Updated Description",
                        null,
                        true,
                        Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                        List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-25.0000")))
                ));
                thread1HasLock.countDown();
                try {
                    boolean awaited = thread1CanCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return updated;
            }));

            assertThat(thread1HasLock.await(5, TimeUnit.SECONDS)).isTrue();

            // Observe PostgreSQL row lock blocking Thread 2
            awaitCompetingLock("recurring_transaction_rules", Duration.ofSeconds(5));

            // Allow Thread 1 to commit
            thread1CanCommit.countDown();

            RecurringTransactionRuleView r1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(r1.name()).isEqualTo("Restored And Updated By Winner");
            assertThat(r1.weekdays()).containsExactlyInAnyOrder(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);

            RecurringTransactionRuleView r2 = thread2Future.get(10, TimeUnit.SECONDS);
            assertThat(r2.deletedAt()).isNull();
            assertThat(r2.name()).isEqualTo("Restored And Updated By Winner");
            assertThat(r2.weekdays()).containsExactlyInAnyOrder(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
            assertThat(r2.entries()).hasSize(1);
            assertThat(r2.entries().getFirst().amountDelta()).isEqualByComparingTo(new BigDecimal("-25.0000"));

            // Fresh database reload confirms authoritative state
            RecurringTransactionRuleView fresh = recurringOperations.findRuleById(rule.id());
            assertThat(fresh.deletedAt()).isNull();
            assertThat(fresh.name()).isEqualTo("Restored And Updated By Winner");
            assertThat(fresh.weekdays()).containsExactlyInAnyOrder(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
            assertThat(fresh.entries().getFirst().amountDelta()).isEqualByComparingTo(new BigDecimal("-25.0000"));
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-7: Measured query count proves constant queries for findRules and findDueRules across small and large limits")
    void measuredQueryCountProvesConstantChildQueriesForRulesAndDueRules() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        Instant cutoff = Instant.now().plus(Duration.ofDays(10));
        Instant baseTime = Instant.now().minus(Duration.ofHours(20));

        // Create 10 rules with weekdays and entries
        for (int i = 1; i <= 10; i++) {
            recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                    "Measured Rule " + String.format("%02d", i),
                    FinancialTransactionType.EXPENSE,
                    expenseCategoryId,
                    RecurringPostingMode.AUTO_POST,
                    RecurrenceFrequency.WEEKLY,
                    1,
                    null,
                    null,
                    start,
                    null,
                    LocalTime.of(10, 0),
                    baseTime.plus(Duration.ofHours(i)),
                    "Description " + i,
                    null,
                    true,
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                    List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-" + i + ".0000")))
            ));
        }

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);

        // 1. Measure findRules with limit 2 vs limit 10
        stats.clear();
        List<RecurringTransactionRuleView> rulesSmall = recurringOperations.findRules(null, 2);
        long queriesRulesSmall = stats.getPrepareStatementCount();

        stats.clear();
        List<RecurringTransactionRuleView> rulesLarge = recurringOperations.findRules(null, 10);
        long queriesRulesLarge = stats.getPrepareStatementCount();

        // Constant query count: exactly 3 queries (1 for parent rules, 1 for batch weekdays, 1 for batch entries)
        assertThat(queriesRulesSmall).isEqualTo(3);
        assertThat(queriesRulesLarge).isEqualTo(3);
        assertThat(queriesRulesSmall).isEqualTo(queriesRulesLarge);

        // Verify ordering (name ASC, id ASC) and limit adherence
        assertThat(rulesSmall).hasSize(2);
        assertThat(rulesLarge).hasSize(10);
        assertThat(rulesSmall.get(0).name()).isEqualTo("Measured Rule 01");
        assertThat(rulesSmall.get(1).name()).isEqualTo("Measured Rule 02");
        assertThat(rulesLarge.get(9).name()).isEqualTo("Measured Rule 10");

        // Verify absence of unrelated child loading: weekdays and entries belong only to returned rules
        Set<Long> smallIds = rulesSmall.stream().map(RecurringTransactionRuleView::id).collect(Collectors.toSet());
        for (RecurringTransactionRuleView r : rulesSmall) {
            assertThat(smallIds).contains(r.id());
            for (var entry : r.entries()) {
                assertThat(entry.recurringRuleId()).isEqualTo(r.id());
            }
        }

        // 2. Measure findDueRules with limit 2 vs limit 10
        stats.clear();
        List<RecurringTransactionRuleView> dueSmall = recurringOperations.findDueRules(cutoff, 2);
        long queriesDueSmall = stats.getPrepareStatementCount();

        stats.clear();
        List<RecurringTransactionRuleView> dueLarge = recurringOperations.findDueRules(cutoff, 10);
        long queriesDueLarge = stats.getPrepareStatementCount();

        // Constant query count: exactly 3 queries
        assertThat(queriesDueSmall).isEqualTo(3);
        assertThat(queriesDueLarge).isEqualTo(3);
        assertThat(queriesDueSmall).isEqualTo(queriesDueLarge);

        // Verify ordering (nextRunAt ASC, id ASC) and limit adherence
        assertThat(dueSmall).hasSize(2);
        assertThat(dueLarge).hasSize(10);
        assertThat(dueSmall.get(0).nextRunAt()).isBeforeOrEqualTo(dueSmall.get(1).nextRunAt());

        // Absence of unrelated child loading
        Set<Long> dueSmallIds = dueSmall.stream().map(RecurringTransactionRuleView::id).collect(Collectors.toSet());
        for (RecurringTransactionRuleView r : dueSmall) {
            assertThat(dueSmallIds).contains(r.id());
            for (var entry : r.entries()) {
                assertThat(entry.recurringRuleId()).isEqualTo(r.id());
            }
        }
    }

    private void awaitCompetingLock(String tablePattern, Duration timeout) throws Exception {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM pg_locks l " +
                    "JOIN pg_stat_activity a ON l.pid = a.pid " +
                    "WHERE NOT l.granted " +
                    "  AND a.pid != pg_backend_pid() " +
                    "  AND a.query ILIKE ?",
                    Integer.class,
                    "%" + tablePattern + "%"
            );
            if (count != null && count > 0) {
                return;
            }
            Thread.sleep(10);
        }
        throw new AssertionError("Timed out waiting for competing transaction to reach PostgreSQL lock on " + tablePattern);
    }
}
