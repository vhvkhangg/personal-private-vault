package com.vhvkhangg.personalprivatevault.finance;

import com.vhvkhangg.personalprivatevault.finance.category.TransactionCategoryOperations;
import com.vhvkhangg.personalprivatevault.finance.category.command.CreateTransactionCategoryCommand;
import com.vhvkhangg.personalprivatevault.finance.enums.BillingCycle;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurrenceFrequency;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurringPostingMode;
import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;
import com.vhvkhangg.personalprivatevault.finance.enums.WalletType;
import com.vhvkhangg.personalprivatevault.finance.recurring.RecurringTransactionRuleOperations;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.CreateRecurringTransactionRuleCommand;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.RecurringRuleEntryInput;
import com.vhvkhangg.personalprivatevault.finance.subscription.SubscriptionOperations;
import com.vhvkhangg.personalprivatevault.finance.subscription.command.CreateSubscriptionCommand;
import com.vhvkhangg.personalprivatevault.finance.subscription.command.UpdateSubscriptionCommand;
import com.vhvkhangg.personalprivatevault.finance.subscription.exception.InvalidSubscriptionException;
import com.vhvkhangg.personalprivatevault.finance.subscription.exception.SubscriptionConflictException;
import com.vhvkhangg.personalprivatevault.finance.subscription.exception.SubscriptionNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.view.RecurringTransactionRuleView;
import com.vhvkhangg.personalprivatevault.finance.view.SubscriptionView;
import com.vhvkhangg.personalprivatevault.finance.view.TransactionCategoryView;
import com.vhvkhangg.personalprivatevault.finance.view.WalletView;
import com.vhvkhangg.personalprivatevault.finance.wallet.WalletOperations;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.CreateWalletCommand;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class SubscriptionIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private SubscriptionOperations subscriptionOperations;

    @Autowired
    private WalletOperations walletOperations;

    @Autowired
    private RecurringTransactionRuleOperations recurringOperations;

    @Autowired
    private TransactionCategoryOperations categoryOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long usdWalletId;
    private Long recurringRuleId;

    @BeforeEach
    void setUp() {
        cleanUp();
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol) VALUES ('USD', 'US Dollar', '$')
                ON CONFLICT (code) DO NOTHING
                """);

        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Sub Wallet", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));
        usdWalletId = wallet.id();

        TransactionCategoryView cat = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Sub Category", TransactionCategoryKind.EXPENSE, null, true
        ));

        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Netflix Rule", FinancialTransactionType.EXPENSE, cat.id(), RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.MONTHLY, 1, 1, null, LocalDate.now(), null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-15.9900")))
        ));
        recurringRuleId = rule.id();
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
    @DisplayName("Creates, updates, and reloads subscription with Reference currency and wallet/rule validation")
    void createUpdateAndReloadSubscription() {
        LocalDate nextBilling = LocalDate.of(2026, 11, 1);

        CreateSubscriptionCommand cmd = new CreateSubscriptionCommand(
                "Netflix Standard",
                "Netflix Inc",
                new BigDecimal("15.9900"),
                "USD",
                BillingCycle.MONTHLY,
                1,
                null,
                nextBilling,
                true,
                usdWalletId,
                recurringRuleId,
                "https://netflix.com",
                "Standard streaming plan",
                true
        );

        SubscriptionView sub = subscriptionOperations.createSubscription(cmd);

        assertThat(sub.id()).isNotNull();
        assertThat(sub.name()).isEqualTo("Netflix Standard");
        assertThat(sub.provider()).isEqualTo("Netflix Inc");
        assertThat(sub.priceAmount()).isEqualByComparingTo(new BigDecimal("15.9900"));
        assertThat(sub.currencyCode()).isEqualTo("USD");
        assertThat(sub.billingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(sub.billingInterval()).isEqualTo(1);
        assertThat(sub.customCycleDays()).isNull();
        assertThat(sub.nextBillingDate()).isEqualTo(nextBilling);
        assertThat(sub.autoRenew()).isTrue();
        assertThat(sub.paymentWalletId()).isEqualTo(usdWalletId);
        assertThat(sub.recurringRuleId()).isEqualTo(recurringRuleId);
        assertThat(sub.url()).isEqualTo("https://netflix.com");
        assertThat(sub.notes()).isEqualTo("Standard streaming plan");
        assertThat(sub.active()).isTrue();

        // Update
        SubscriptionView updated = subscriptionOperations.updateSubscription(new UpdateSubscriptionCommand(
                sub.id(),
                "Netflix Premium",
                "Netflix Inc",
                new BigDecimal("22.9900"),
                "USD",
                BillingCycle.MONTHLY,
                1,
                null,
                nextBilling,
                true,
                usdWalletId,
                recurringRuleId,
                "https://netflix.com",
                "Upgraded to 4K",
                true
        ));

        assertThat(updated.name()).isEqualTo("Netflix Premium");
        assertThat(updated.priceAmount()).isEqualByComparingTo(new BigDecimal("22.9900"));
        assertThat(updated.notes()).isEqualTo("Upgraded to 4K");

        // Reload
        SubscriptionView reloaded = subscriptionOperations.findSubscriptionById(sub.id());
        assertThat(reloaded.name()).isEqualTo("Netflix Premium");
    }

    @Test
    @DisplayName("Validates custom cycle days: required for CUSTOM, prohibited for non-CUSTOM")
    void validatesCustomCycleDays() {
        // CUSTOM without customCycleDays -> rejected
        assertThatThrownBy(() -> subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Custom Sub", null, new BigDecimal("10.0000"), "USD", BillingCycle.CUSTOM,
                1, null, null, true, null, null, null, null, true
        )))
                .isInstanceOf(InvalidSubscriptionException.class)
                .hasMessageContaining("customCycleDays must be positive");

        // CUSTOM with customCycleDays <= 0 -> rejected
        assertThatThrownBy(() -> subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Custom Sub", null, new BigDecimal("10.0000"), "USD", BillingCycle.CUSTOM,
                1, 0, null, true, null, null, null, null, true
        )))
                .isInstanceOf(InvalidSubscriptionException.class)
                .hasMessageContaining("customCycleDays must be positive");

        // Non-CUSTOM with customCycleDays -> rejected
        assertThatThrownBy(() -> subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Monthly Sub", null, new BigDecimal("10.0000"), "USD", BillingCycle.MONTHLY,
                1, 30, null, true, null, null, null, null, true
        )))
                .isInstanceOf(InvalidSubscriptionException.class)
                .hasMessageContaining("customCycleDays must be null for non-CUSTOM billing cycle");
    }

    @Test
    @DisplayName("Sequential duplicate recurring_rule_id throws SubscriptionConflictException")
    void sequentialDuplicateRecurringRuleThrowsConflict() {
        subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Sub 1", null, new BigDecimal("10.0000"), "USD", BillingCycle.MONTHLY,
                1, null, null, true, usdWalletId, recurringRuleId, null, null, true
        ));

        assertThatThrownBy(() -> subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Sub 2", null, new BigDecimal("20.0000"), "USD", BillingCycle.MONTHLY,
                1, null, null, true, usdWalletId, recurringRuleId, null, null, true
        )))
                .isInstanceOf(SubscriptionConflictException.class)
                .hasMessageContaining("already linked to another subscription");
    }

    @Test
    @DisplayName("Concurrent duplicate recurring_rule_id competition relies on PostgreSQL unique constraint with safe translation")
    void concurrentDuplicateRecurringRuleConflictReliesOnPostgresUniqueConstraint(CapturedOutput output) throws Exception {
        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1 inserts subscription linked to recurringRuleId in uncommitted transaction
            Future<Long> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                Long id = jdbcTemplate.queryForObject(
                        "INSERT INTO subscriptions (name, price_amount, currency_code, billing_cycle, billing_interval, recurring_rule_id) " +
                        "VALUES ('Thread 1 Sub', 10.0000, 'USD', 'MONTHLY'::billing_cycle, 1, ?) RETURNING id",
                        Long.class, recurringRuleId
                );
                thread1Inserted.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return id;
            }));

            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2 calls createSubscription with same recurringRuleId; pre-check passes because Thread 1 is uncommitted,
            // then Thread 2 blocks on uq_subscriptions_recurring_rule_id unique constraint.
            Future<SubscriptionView> thread2Future = executor.submit(() -> subscriptionOperations.createSubscription(
                    new CreateSubscriptionCommand(
                            "Thread 2 Sub", null, new BigDecimal("15.0000"), "USD", BillingCycle.MONTHLY,
                            1, null, null, true, usdWalletId, recurringRuleId, null, null, true
                    )
            ));

            // Observe lock contention on subscriptions table
            awaitCompetingLock("subscriptions", Duration.ofSeconds(5));

            // Let thread 1 commit
            thread2ReadyToCommit.countDown();

            Long id1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(id1).isNotNull();

            // Thread 2 must fail with SubscriptionConflictException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(SubscriptionConflictException.class)
                    .hasMessageContaining("already linked to another subscription");

            // Exactly 1 subscription exists in DB
            Integer subCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM subscriptions WHERE recurring_rule_id = ?",
                    Integer.class, recurringRuleId
            );
            assertThat(subCount).isEqualTo(1);

            // Privacy verification: no raw database details in log
            assertThat(output.getAll())
                    .doesNotContain("Detail: Key (recurring_rule_id)=(")
                    .doesNotContain("subscriptions_recurring_rule_id_key");
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-5 contention: Subscription assignment vs Wallet delete (Winner Order A: Assignment wins first, Delete waits and soft-deletes)")
    void contentionAssignmentVsWalletDeleteWinnerAssignment() throws Exception {
        WalletView w = walletOperations.createWallet(new CreateWalletCommand(
                "Sub Wallet Winner A", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: creates subscription assigning w.id() in uncommitted transaction
            Future<SubscriptionView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                SubscriptionView sub = subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                        "Sub Winner A", null, new BigDecimal("12.0000"), "USD", BillingCycle.MONTHLY,
                        1, null, null, true, w.id(), null, null, null, true
                ));
                thread1HasLock.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return sub;
            }));

            assertThat(thread1HasLock.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: attempts to soft-delete wallet w.id(), blocks on wallet row lock
            Future<WalletView> thread2Future = executor.submit(() -> walletOperations.softDeleteWallet(w.id()));

            // Observe lock contention on wallets table
            awaitCompetingLock("wallets", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            SubscriptionView sub = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(sub.paymentWalletId()).isEqualTo(w.id());

            WalletView deletedWallet = thread2Future.get(10, TimeUnit.SECONDS);
            assertThat(deletedWallet.deletedAt()).isNotNull();

            // Historical link is retained without cascading or deletion
            SubscriptionView reloaded = subscriptionOperations.findSubscriptionById(sub.id());
            assertThat(reloaded.paymentWalletId()).isEqualTo(w.id());
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-5 contention: Subscription assignment vs Wallet delete (Winner Order B: Delete wins first, Assignment waits and is rejected)")
    void contentionAssignmentVsWalletDeleteWinnerDelete() throws Exception {
        WalletView w = walletOperations.createWallet(new CreateWalletCommand(
                "Sub Wallet Winner B", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: soft-deletes wallet w.id() in uncommitted transaction
            Future<WalletView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                WalletView deleted = walletOperations.softDeleteWallet(w.id());
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

            // Thread 2: attempts to create subscription assigning w.id(), blocks on wallet row lock
            Future<SubscriptionView> thread2Future = executor.submit(() -> subscriptionOperations.createSubscription(
                    new CreateSubscriptionCommand(
                            "Sub Winner B Attempt", null, new BigDecimal("12.0000"), "USD", BillingCycle.MONTHLY,
                            1, null, null, true, w.id(), null, null, null, true
                    )
            ));

            // Observe lock contention on wallets table
            awaitCompetingLock("wallets", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            WalletView deletedWallet = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(deletedWallet.deletedAt()).isNotNull();

            // Thread 2 unblocks, reloads wallet under lock, finds deleted, and fails
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(InvalidSubscriptionException.class)
                    .hasMessageContaining("Payment wallet is deleted");

            // Verify no subscription was created
            Integer subCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM subscriptions WHERE name = 'Sub Winner B Attempt'",
                    Integer.class
            );
            assertThat(subCount).isZero();
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-5 contention: Subscription assignment vs Recurring Rule delete (Winner Order A: Assignment wins first, Delete waits and soft-deletes)")
    void contentionAssignmentVsRecurringRuleDeleteWinnerAssignment() throws Exception {
        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Rule Winner A", FinancialTransactionType.EXPENSE, categoryOperations.createCategory(
                        new CreateTransactionCategoryCommand("Cat A", TransactionCategoryKind.EXPENSE, null, true)
                ).id(),
                RecurringPostingMode.AUTO_POST, RecurrenceFrequency.MONTHLY, 1, 1, null,
                LocalDate.now(), null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-5.0000")))
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: creates subscription assigning rule.id() in uncommitted transaction
            Future<SubscriptionView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                SubscriptionView sub = subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                        "Sub Rule Winner A", null, new BigDecimal("5.0000"), "USD", BillingCycle.MONTHLY,
                        1, null, null, true, null, rule.id(), null, null, true
                ));
                thread1HasLock.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return sub;
            }));

            assertThat(thread1HasLock.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: attempts to soft-delete recurring rule, blocks on recurring rule row lock
            Future<RecurringTransactionRuleView> thread2Future = executor.submit(() -> recurringOperations.softDeleteRule(rule.id()));

            // Observe lock contention on recurring_transaction_rules table
            awaitCompetingLock("recurring_transaction_rules", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            SubscriptionView sub = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(sub.recurringRuleId()).isEqualTo(rule.id());

            RecurringTransactionRuleView deletedRule = thread2Future.get(10, TimeUnit.SECONDS);
            assertThat(deletedRule.deletedAt()).isNotNull();

            // Historical link is retained
            SubscriptionView reloaded = subscriptionOperations.findSubscriptionById(sub.id());
            assertThat(reloaded.recurringRuleId()).isEqualTo(rule.id());
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-5 contention: Subscription assignment vs Recurring Rule delete (Winner Order B: Delete wins first, Assignment waits and is rejected)")
    void contentionAssignmentVsRecurringRuleDeleteWinnerDelete() throws Exception {
        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Rule Winner B", FinancialTransactionType.EXPENSE, categoryOperations.createCategory(
                        new CreateTransactionCategoryCommand("Cat B", TransactionCategoryKind.EXPENSE, null, true)
                ).id(),
                RecurringPostingMode.AUTO_POST, RecurrenceFrequency.MONTHLY, 1, 1, null,
                LocalDate.now(), null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-5.0000")))
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: soft-deletes rule in uncommitted transaction
            Future<RecurringTransactionRuleView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
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

            // Thread 2: attempts to create subscription assigning rule.id(), blocks on recurring rule row lock
            Future<SubscriptionView> thread2Future = executor.submit(() -> subscriptionOperations.createSubscription(
                    new CreateSubscriptionCommand(
                            "Sub Rule Winner B Attempt", null, new BigDecimal("5.0000"), "USD", BillingCycle.MONTHLY,
                            1, null, null, true, null, rule.id(), null, null, true
                    )
            ));

            // Observe lock contention on recurring_transaction_rules table
            awaitCompetingLock("recurring_transaction_rules", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            RecurringTransactionRuleView deletedRule = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(deletedRule.deletedAt()).isNotNull();

            // Thread 2 unblocks, reloads rule under lock, finds deleted, and fails
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(InvalidSubscriptionException.class)
                    .hasMessageContaining("Recurring rule is deleted");

            // Verify no subscription was created
            Integer subCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM subscriptions WHERE name = 'Sub Rule Winner B Attempt'",
                    Integer.class
            );
            assertThat(subCount).isZero();
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-5 contention: Subscription update changed wallet vs Wallet delete (Winner Order A: Update wins first, Delete waits and soft-deletes)")
    void contentionUpdateAssignmentVsWalletDeleteWinnerUpdate() throws Exception {
        WalletView wOld = walletOperations.createWallet(new CreateWalletCommand(
                "Sub Update W Old", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));
        WalletView wNew = walletOperations.createWallet(new CreateWalletCommand(
                "Sub Update W New A", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));

        SubscriptionView sub = subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Sub Update Winner A", null, new BigDecimal("10.0000"), "USD", BillingCycle.MONTHLY,
                1, null, null, true, wOld.id(), null, null, null, true
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: updates subscription changing wallet to wNew in uncommitted transaction
            Future<SubscriptionView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                SubscriptionView updated = subscriptionOperations.updateSubscription(new UpdateSubscriptionCommand(
                        sub.id(), "Sub Update Winner A Updated", "Provider A", new BigDecimal("25.0000"), "USD",
                        BillingCycle.MONTHLY, 1, null, null, true, wNew.id(), null, null, null, true
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

            // Thread 2: attempts to soft-delete wNew, blocks on wallet row lock
            Future<WalletView> thread2Future = executor.submit(() -> walletOperations.softDeleteWallet(wNew.id()));

            // Observe lock contention on wallets table
            awaitCompetingLock("wallets", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            SubscriptionView updatedSub = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(updatedSub.paymentWalletId()).isEqualTo(wNew.id());
            assertThat(updatedSub.name()).isEqualTo("Sub Update Winner A Updated");
            assertThat(updatedSub.priceAmount()).isEqualByComparingTo(new BigDecimal("25.0000"));

            WalletView deletedWallet = thread2Future.get(10, TimeUnit.SECONDS);
            assertThat(deletedWallet.deletedAt()).isNotNull();

            // Historical link is retained without cascading or deletion
            SubscriptionView reloaded = subscriptionOperations.findSubscriptionById(sub.id());
            assertThat(reloaded.paymentWalletId()).isEqualTo(wNew.id());
            assertThat(reloaded.name()).isEqualTo("Sub Update Winner A Updated");
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-5 contention: Subscription update changed wallet vs Wallet delete (Winner Order B: Delete wins first, Update waits and is rejected)")
    void contentionUpdateAssignmentVsWalletDeleteWinnerDelete() throws Exception {
        WalletView wOld = walletOperations.createWallet(new CreateWalletCommand(
                "Sub Update W Old B", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));
        WalletView wNew = walletOperations.createWallet(new CreateWalletCommand(
                "Sub Update W New B", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));

        SubscriptionView sub = subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Sub Update Winner B Initial", "Provider B", new BigDecimal("10.0000"), "USD", BillingCycle.MONTHLY,
                1, null, null, true, wOld.id(), null, null, null, true
        ));

        int initialSubCount = jdbcTemplate.queryForObject("SELECT count(*) FROM subscriptions", Integer.class);

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: soft-deletes wNew in uncommitted transaction holding wallet lock
            Future<WalletView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                WalletView deleted = walletOperations.softDeleteWallet(wNew.id());
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

            // Thread 2: attempts to update subscription assigning wNew, blocks on wallet lock
            Future<SubscriptionView> thread2Future = executor.submit(() -> txTemplate.execute(status ->
                    subscriptionOperations.updateSubscription(new UpdateSubscriptionCommand(
                            sub.id(), "Sub Update Winner B Attempted", "Provider B Updated", new BigDecimal("99.0000"), "USD",
                            BillingCycle.MONTHLY, 1, null, null, true, wNew.id(), null, null, null, true
                    ))
            ));

            // Observe lock contention on wallets table
            awaitCompetingLock("wallets", Duration.ofSeconds(5));

            // Release Thread 1 to commit soft-delete
            thread2ReadyToCommit.countDown();

            WalletView deletedWallet = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(deletedWallet.deletedAt()).isNotNull();

            // Thread 2 unblocks, reloads wallet under lock, finds deleted, and throws InvalidSubscriptionException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(InvalidSubscriptionException.class)
                    .hasMessageContaining("Payment wallet is deleted");

            // Verify full rollback: scalar fields and links on sub remain at initial values
            SubscriptionView reloaded = subscriptionOperations.findSubscriptionById(sub.id());
            assertThat(reloaded.name()).isEqualTo("Sub Update Winner B Initial");
            assertThat(reloaded.provider()).isEqualTo("Provider B");
            assertThat(reloaded.priceAmount()).isEqualByComparingTo(new BigDecimal("10.0000"));
            assertThat(reloaded.paymentWalletId()).isEqualTo(wOld.id());

            // Subscription count is completely unchanged
            int finalSubCount = jdbcTemplate.queryForObject("SELECT count(*) FROM subscriptions", Integer.class);
            assertThat(finalSubCount).isEqualTo(initialSubCount);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-5 contention: Subscription update changed rule vs Rule delete (Winner Order A: Update wins first, Delete waits and soft-deletes)")
    void contentionUpdateAssignmentVsRecurringRuleDeleteWinnerUpdate() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        RecurringTransactionRuleView rNew = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Rule Winner A New", FinancialTransactionType.EXPENSE, categoryOperations.createCategory(
                        new CreateTransactionCategoryCommand("Cat Winner A", TransactionCategoryKind.EXPENSE, null, true)
                ).id(),
                RecurringPostingMode.AUTO_POST, RecurrenceFrequency.MONTHLY, 1, 1, null,
                start, null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
        ));

        SubscriptionView sub = subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Sub Rule Winner A", null, new BigDecimal("10.0000"), "USD", BillingCycle.MONTHLY,
                1, null, null, true, usdWalletId, null, null, null, true
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: updates subscription changing rule to rNew in uncommitted transaction
            Future<SubscriptionView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                SubscriptionView updated = subscriptionOperations.updateSubscription(new UpdateSubscriptionCommand(
                        sub.id(), "Sub Rule Winner A Updated", "Provider RA", new BigDecimal("35.0000"), "USD",
                        BillingCycle.MONTHLY, 1, null, null, true, usdWalletId, rNew.id(), null, null, true
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

            // Thread 2: attempts to soft-delete rNew, blocks on recurring rule row lock
            Future<RecurringTransactionRuleView> thread2Future = executor.submit(() -> recurringOperations.softDeleteRule(rNew.id()));

            // Observe lock contention on recurring_transaction_rules table
            awaitCompetingLock("recurring_transaction_rules", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            SubscriptionView updatedSub = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(updatedSub.recurringRuleId()).isEqualTo(rNew.id());
            assertThat(updatedSub.name()).isEqualTo("Sub Rule Winner A Updated");
            assertThat(updatedSub.priceAmount()).isEqualByComparingTo(new BigDecimal("35.0000"));

            RecurringTransactionRuleView deletedRule = thread2Future.get(10, TimeUnit.SECONDS);
            assertThat(deletedRule.deletedAt()).isNotNull();

            // Historical link is retained cleanly
            SubscriptionView reloaded = subscriptionOperations.findSubscriptionById(sub.id());
            assertThat(reloaded.recurringRuleId()).isEqualTo(rNew.id());
            assertThat(reloaded.name()).isEqualTo("Sub Rule Winner A Updated");
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-5 contention: Subscription update changed rule vs Rule delete (Winner Order B: Delete wins first, Update waits and is rejected)")
    void contentionUpdateAssignmentVsRecurringRuleDeleteWinnerDelete() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        RecurringTransactionRuleView rNew = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Rule Winner B New", FinancialTransactionType.EXPENSE, categoryOperations.createCategory(
                        new CreateTransactionCategoryCommand("Cat Winner B", TransactionCategoryKind.EXPENSE, null, true)
                ).id(),
                RecurringPostingMode.AUTO_POST, RecurrenceFrequency.MONTHLY, 1, 1, null,
                start, null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
        ));

        SubscriptionView sub = subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Sub Rule Winner B Initial", "Provider RB", new BigDecimal("10.0000"), "USD", BillingCycle.MONTHLY,
                1, null, null, true, usdWalletId, null, null, null, true
        ));

        int initialSubCount = jdbcTemplate.queryForObject("SELECT count(*) FROM subscriptions", Integer.class);

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: soft-deletes rNew in uncommitted transaction holding rule lock
            Future<RecurringTransactionRuleView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                RecurringTransactionRuleView deleted = recurringOperations.softDeleteRule(rNew.id());
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

            // Thread 2: attempts to update subscription assigning rNew, blocks on rule lock
            Future<SubscriptionView> thread2Future = executor.submit(() -> txTemplate.execute(status ->
                    subscriptionOperations.updateSubscription(new UpdateSubscriptionCommand(
                            sub.id(), "Sub Rule Winner B Attempted", "Provider RB Updated", new BigDecimal("99.0000"), "USD",
                            BillingCycle.MONTHLY, 1, null, null, true, usdWalletId, rNew.id(), null, null, true
                    ))
            ));

            // Observe lock contention on recurring_transaction_rules table
            awaitCompetingLock("recurring_transaction_rules", Duration.ofSeconds(5));

            // Release Thread 1 to commit soft-delete
            thread2ReadyToCommit.countDown();

            RecurringTransactionRuleView deletedRule = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(deletedRule.deletedAt()).isNotNull();

            // Thread 2 unblocks, discovers rule is deleted, throws InvalidSubscriptionException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(InvalidSubscriptionException.class)
                    .hasMessageContaining("Recurring rule is deleted");

            // Verify full rollback: scalar fields and links on sub remain at initial values
            SubscriptionView reloaded = subscriptionOperations.findSubscriptionById(sub.id());
            assertThat(reloaded.name()).isEqualTo("Sub Rule Winner B Initial");
            assertThat(reloaded.provider()).isEqualTo("Provider RB");
            assertThat(reloaded.priceAmount()).isEqualByComparingTo(new BigDecimal("10.0000"));
            assertThat(reloaded.recurringRuleId()).isNull();

            // Subscription count is completely unchanged
            int finalSubCount = jdbcTemplate.queryForObject("SELECT count(*) FROM subscriptions", Integer.class);
            assertThat(finalSubCount).isEqualTo(initialSubCount);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-5 contention: Subscription update changing both links observes canonical lock acquisition order (Subscription -> RecurringRule -> Wallet)")
    void contentionUpdateAssignmentBothLinksChangeCanonicalGuardOrder() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        RecurringTransactionRuleView rNew = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Rule Guard Order", FinancialTransactionType.EXPENSE, categoryOperations.createCategory(
                        new CreateTransactionCategoryCommand("Cat Guard Order", TransactionCategoryKind.EXPENSE, null, true)
                ).id(),
                RecurringPostingMode.AUTO_POST, RecurrenceFrequency.MONTHLY, 1, 1, null,
                start, null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-10.0000")))
        ));
        WalletView wNew = walletOperations.createWallet(new CreateWalletCommand(
                "Wallet Guard Order", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));

        SubscriptionView sub = subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Sub Both Initial", "Provider Both", new BigDecimal("10.0000"), "USD", BillingCycle.MONTHLY,
                1, null, null, true, usdWalletId, null, null, null, true
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: locks and soft-deletes rNew in uncommitted transaction
            Future<RecurringTransactionRuleView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                RecurringTransactionRuleView deleted = recurringOperations.softDeleteRule(rNew.id());
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

            // Thread 2: attempts to update subscription changing both rNew AND wNew.
            // Following canonical lock order (Subscription -> RecurringRule -> Wallet), it blocks on rNew FIRST.
            Future<SubscriptionView> thread2Future = executor.submit(() -> txTemplate.execute(status ->
                    subscriptionOperations.updateSubscription(new UpdateSubscriptionCommand(
                            sub.id(), "Sub Both Attempted", "Provider Both Updated", new BigDecimal("50.0000"), "USD",
                            BillingCycle.MONTHLY, 1, null, null, true, wNew.id(), rNew.id(), null, null, true
                    ))
            ));

            // Observe lock contention specifically on recurring_transaction_rules table
            awaitCompetingLock("recurring_transaction_rules", Duration.ofSeconds(5));

            // Release Thread 1 to commit soft-delete
            thread2ReadyToCommit.countDown();

            RecurringTransactionRuleView deletedRule = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(deletedRule.deletedAt()).isNotNull();

            // Thread 2 unblocks, discovers rule is deleted, throws InvalidSubscriptionException without mutating wallet
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(InvalidSubscriptionException.class)
                    .hasMessageContaining("Recurring rule is deleted");

            // Verify full rollback: sub fields and links remain initial
            SubscriptionView reloaded = subscriptionOperations.findSubscriptionById(sub.id());
            assertThat(reloaded.name()).isEqualTo("Sub Both Initial");
            assertThat(reloaded.priceAmount()).isEqualByComparingTo(new BigDecimal("10.0000"));
            assertThat(reloaded.recurringRuleId()).isNull();
            assertThat(reloaded.paymentWalletId()).isEqualTo(usdWalletId);

            // wNew was not deleted or locked permanently
            WalletView wNewReloaded = walletOperations.findWalletById(wNew.id());
            assertThat(wNewReloaded.deletedAt()).isNull();
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-5: Subscription update retains historical soft-deleted owners but rejects assigning new soft-deleted owners")
    void updateRetainsHistoricalDeletedOwnersButRejectsNewDeletedOwners() {
        WalletView w = walletOperations.createWallet(new CreateWalletCommand(
                "Sub Wallet Hist", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));
        RecurringTransactionRuleView r = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Rule Hist", FinancialTransactionType.EXPENSE, categoryOperations.createCategory(
                        new CreateTransactionCategoryCommand("Cat Hist", TransactionCategoryKind.EXPENSE, null, true)
                ).id(),
                RecurringPostingMode.AUTO_POST, RecurrenceFrequency.MONTHLY, 1, 1, null,
                LocalDate.now(), null, null, null, null, null, true,
                null, List.of(new RecurringRuleEntryInput(usdWalletId, new BigDecimal("-5.0000")))
        ));

        SubscriptionView sub = subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Sub Hist", null, new BigDecimal("10.0000"), "USD", BillingCycle.MONTHLY,
                1, null, null, true, w.id(), r.id(), null, null, true
        ));

        // Soft-delete wallet and recurring rule
        walletOperations.softDeleteWallet(w.id());
        recurringOperations.softDeleteRule(r.id());

        // Update name and price while retaining existing wallet and rule -> SUCCEEDS!
        SubscriptionView updated = subscriptionOperations.updateSubscription(new UpdateSubscriptionCommand(
                sub.id(), "Sub Hist Updated", "Provider", new BigDecimal("15.0000"), "USD",
                BillingCycle.MONTHLY, 1, null, null, true, w.id(), r.id(), null, null, true
        ));
        assertThat(updated.name()).isEqualTo("Sub Hist Updated");
        assertThat(updated.paymentWalletId()).isEqualTo(w.id());
        assertThat(updated.recurringRuleId()).isEqualTo(r.id());

        // Create another deleted wallet
        WalletView w2 = walletOperations.createWallet(new CreateWalletCommand(
                "Sub Wallet 2", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));
        walletOperations.softDeleteWallet(w2.id());

        // Attempting to change paymentWalletId to the newly deleted wallet w2 -> REJECTED
        assertThatThrownBy(() -> subscriptionOperations.updateSubscription(new UpdateSubscriptionCommand(
                sub.id(), "Sub Hist Updated", "Provider", new BigDecimal("15.0000"), "USD",
                BillingCycle.MONTHLY, 1, null, null, true, w2.id(), r.id(), null, null, true
        )))
                .isInstanceOf(InvalidSubscriptionException.class)
                .hasMessageContaining("Payment wallet is deleted");
    }

    @Test
    @DisplayName("FR11-6: Non-uniqueness errors with recurringRuleId are not misdiagnosed as duplicate rule conflicts")
    void nonUniquenessErrorsWithRecurringRuleIdNotMisdiagnosed() {
        // Unknown currency code on command with recurringRuleId -> InvalidSubscriptionException, NOT SubscriptionConflictException
        assertThatThrownBy(() -> subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Sub Invalid", null, new BigDecimal("10.0000"), "INVALID", BillingCycle.MONTHLY,
                1, null, null, true, null, recurringRuleId, null, null, true
        )))
                .isInstanceOf(InvalidSubscriptionException.class)
                .hasMessageContaining("Unknown currency code: INVALID")
                .isNotInstanceOf(SubscriptionConflictException.class);

        // Non-existent recurring rule ID -> InvalidSubscriptionException, NOT SubscriptionConflictException
        assertThatThrownBy(() -> subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Sub Invalid Rule", null, new BigDecimal("10.0000"), "USD", BillingCycle.MONTHLY,
                1, null, null, true, null, 9999999L, null, null, true
        )))
                .isInstanceOf(InvalidSubscriptionException.class)
                .hasMessageContaining("Recurring rule does not exist: 9999999")
                .isNotInstanceOf(SubscriptionConflictException.class);
    }

    @Test
    @DisplayName("Active subscriptions ordered by next_billing_date ASC NULLS LAST, id ASC with positive limit")
    void activeSubscriptionsBoundedRead() {
        LocalDate d1 = LocalDate.of(2026, 10, 15);
        LocalDate d2 = LocalDate.of(2026, 11, 1);

        subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Sub Later", null, new BigDecimal("10.0000"), "USD", BillingCycle.MONTHLY,
                1, null, d2, true, null, null, null, null, true
        ));
        subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Sub Sooner", null, new BigDecimal("10.0000"), "USD", BillingCycle.MONTHLY,
                1, null, d1, true, null, null, null, null, true
        ));
        subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Sub Null Date", null, new BigDecimal("10.0000"), "USD", BillingCycle.MONTHLY,
                1, null, null, true, null, null, null, null, true
        ));

        List<SubscriptionView> active = subscriptionOperations.findActiveSubscriptions(10);
        assertThat(active).hasSize(3);
        assertThat(active.get(0).name()).isEqualTo("Sub Sooner"); // d1
        assertThat(active.get(1).name()).isEqualTo("Sub Later");  // d2
        assertThat(active.get(2).name()).isEqualTo("Sub Null Date"); // null last
    }

    @Test
    @DisplayName("Soft delete excludes from default active list and findSubscriptionById, restore returns it")
    void softDeleteAndRestoreLifecycle() {
        SubscriptionView sub = subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                "Temp Sub", null, new BigDecimal("5.0000"), "USD", BillingCycle.MONTHLY,
                1, null, null, true, null, null, null, null, true
        ));

        // Soft delete
        subscriptionOperations.softDeleteSubscription(sub.id());

        assertThatThrownBy(() -> subscriptionOperations.findSubscriptionById(sub.id()))
                .isInstanceOf(SubscriptionNotFoundException.class);
        assertThat(subscriptionOperations.findActiveSubscriptions(10)).isEmpty();

        // Restore
        SubscriptionView restored = subscriptionOperations.restoreSubscription(sub.id());
        assertThat(restored.deletedAt()).isNull();

        SubscriptionView reloaded = subscriptionOperations.findSubscriptionById(sub.id());
        assertThat(reloaded).isNotNull();
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
