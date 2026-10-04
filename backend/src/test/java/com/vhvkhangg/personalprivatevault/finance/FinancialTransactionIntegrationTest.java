package com.vhvkhangg.personalprivatevault.finance;

import com.vhvkhangg.personalprivatevault.finance.category.TransactionCategoryOperations;
import com.vhvkhangg.personalprivatevault.finance.category.command.CreateTransactionCategoryCommand;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionStatus;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;
import com.vhvkhangg.personalprivatevault.finance.enums.WalletType;
import com.vhvkhangg.personalprivatevault.finance.transaction.FinancialTransactionOperations;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.CreateFinancialTransactionCommand;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.FinancialTransactionEntryInput;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.UpdateFinancialTransactionCommand;
import com.vhvkhangg.personalprivatevault.finance.transaction.exception.FinancialTransactionNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.transaction.exception.InvalidFinancialTransactionException;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.FinancialTransaction;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.FinancialTransactionEntry;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.FinancialTransactionEntryRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.FinancialTransactionRepository;
import com.vhvkhangg.personalprivatevault.finance.view.FinancialTransactionView;
import com.vhvkhangg.personalprivatevault.finance.view.TransactionCategoryView;
import com.vhvkhangg.personalprivatevault.finance.view.WalletView;
import com.vhvkhangg.personalprivatevault.finance.wallet.WalletOperations;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.CreateWalletCommand;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
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
class FinancialTransactionIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FinancialTransactionOperations transactionOperations;

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
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private FinancialTransactionEntryRepository financialTransactionEntryRepository;

    private Long usdWalletId;
    private Long vndWalletId;
    private Long incomeCategoryId;
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
                "USD Wallet", WalletType.BANK_ACCOUNT, "USD", new BigDecimal("1000.0000"), null, true
        ));
        usdWalletId = w1.id();

        WalletView w2 = walletOperations.createWallet(new CreateWalletCommand(
                "VND Wallet", WalletType.E_WALLET, "VND", new BigDecimal("500.0000"), null, true
        ));
        vndWalletId = w2.id();

        TransactionCategoryView incomeCat = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Salary Cat", TransactionCategoryKind.INCOME, null, true
        ));
        incomeCategoryId = incomeCat.id();

        TransactionCategoryView expenseCat = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Food Cat", TransactionCategoryKind.EXPENSE, null, true
        ));
        expenseCategoryId = expenseCat.id();
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM subscriptions");
        jdbcTemplate.execute("DELETE FROM financial_transaction_entries");
        jdbcTemplate.execute("DELETE FROM financial_transactions");
        jdbcTemplate.execute("DELETE FROM recurring_rule_entries");
        jdbcTemplate.execute("DELETE FROM recurring_rule_weekdays");
        jdbcTemplate.execute("DELETE FROM recurring_transaction_rules");
        jdbcTemplate.execute("DELETE FROM transaction_categories");
        jdbcTemplate.execute("DELETE FROM wallets");
        jdbcTemplate.execute("DELETE FROM currencies WHERE code = 'EUR'");
    }

    @Test
    @DisplayName("Creates, updates, and reloads INCOME transaction with exact entry shape and full replacement")
    void incomeTransactionFullReplacement() {
        Instant now = Instant.parse("2026-10-01T12:00:00Z");

        FinancialTransactionView created = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME,
                FinancialTransactionStatus.POSTED,
                incomeCategoryId,
                "Monthly Salary",
                "Direct deposit",
                now,
                null,
                List.of(new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("3500.0000")))
        ));

        assertThat(created.id()).isNotNull();
        assertThat(created.type()).isEqualTo(FinancialTransactionType.INCOME);
        assertThat(created.status()).isEqualTo(FinancialTransactionStatus.POSTED);
        assertThat(created.categoryId()).isEqualTo(incomeCategoryId);
        assertThat(created.description()).isEqualTo("Monthly Salary");
        assertThat(created.notes()).isEqualTo("Direct deposit");
        assertThat(created.occurredAt()).isEqualTo(now);
        assertThat(created.entries()).hasSize(1);
        assertThat(created.entries().getFirst().walletId()).isEqualTo(usdWalletId);
        assertThat(created.entries().getFirst().amountDelta()).isEqualByComparingTo(new BigDecimal("3500.0000"));

        // Derived balance = 1000 + 3500 = 4500
        assertThat(walletOperations.currentBalance(usdWalletId)).isEqualByComparingTo(new BigDecimal("4500.0000"));

        // Full replacement update: change amount to 4000
        FinancialTransactionView updated = transactionOperations.updateTransaction(new UpdateFinancialTransactionCommand(
                created.id(),
                FinancialTransactionType.INCOME,
                FinancialTransactionStatus.POSTED,
                incomeCategoryId,
                "Monthly Salary + Bonus",
                "Updated",
                now,
                null,
                List.of(new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("4000.0000")))
        ));

        assertThat(updated.description()).isEqualTo("Monthly Salary + Bonus");
        assertThat(updated.entries()).hasSize(1);
        assertThat(updated.entries().getFirst().amountDelta()).isEqualByComparingTo(new BigDecimal("4000.0000"));

        // Derived balance = 1000 + 4000 = 5000
        assertThat(walletOperations.currentBalance(usdWalletId)).isEqualByComparingTo(new BigDecimal("5000.0000"));

        // Reload by ID
        FinancialTransactionView reloaded = transactionOperations.findTransactionById(created.id());
        assertThat(reloaded.description()).isEqualTo("Monthly Salary + Bonus");
    }

    @Test
    @DisplayName("Creates cross-currency TRANSFER transaction with 2 distinct wallets and exchange rate without invented zero-sum requirement")
    void crossCurrencyTransferTransaction() {
        BigDecimal exchangeRate = new BigDecimal("1.0850000000"); // 10 decimal digits

        FinancialTransactionView transfer = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.TRANSFER,
                FinancialTransactionStatus.POSTED,
                null, // TRANSFER must have category = null
                "USD to VND Wire",
                null,
                Instant.now(),
                exchangeRate,
                List.of(
                        new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("-1085.0000")),
                        new FinancialTransactionEntryInput(vndWalletId, new BigDecimal("1000.0000"))
                )
        ));

        assertThat(transfer.id()).isNotNull();
        assertThat(transfer.type()).isEqualTo(FinancialTransactionType.TRANSFER);
        assertThat(transfer.exchangeRate()).isEqualByComparingTo(exchangeRate);
        assertThat(transfer.entries()).hasSize(2);

        // USD balance = 1000 - 1085 = -85
        assertThat(walletOperations.currentBalance(usdWalletId)).isEqualByComparingTo(new BigDecimal("-85.0000"));
        // VND balance = 500 + 1000 = 1500
        assertThat(walletOperations.currentBalance(vndWalletId)).isEqualByComparingTo(new BigDecimal("1500.0000"));
    }

    @Test
    @DisplayName("Enforces ledger shape and sign rules for INCOME, EXPENSE, and TRANSFER")
    void enforcesLedgerShapeAndSignRules() {
        // INCOME with negative delta -> rejected
        assertThatThrownBy(() -> transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME, FinancialTransactionStatus.POSTED, incomeCategoryId, "Bad Income", null,
                Instant.now(), null, List.of(new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("-100.0000")))
        )))
                .isInstanceOf(InvalidFinancialTransactionException.class)
                .hasMessageContaining("must be positive");

        // EXPENSE with positive delta -> rejected
        assertThatThrownBy(() -> transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.EXPENSE, FinancialTransactionStatus.POSTED, expenseCategoryId, "Bad Expense", null,
                Instant.now(), null, List.of(new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("100.0000")))
        )))
                .isInstanceOf(InvalidFinancialTransactionException.class)
                .hasMessageContaining("must be negative");

        // TRANSFER with category -> rejected
        assertThatThrownBy(() -> transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.TRANSFER, FinancialTransactionStatus.POSTED, incomeCategoryId, "Bad Transfer", null,
                Instant.now(), null, List.of(
                        new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("-100.0000")),
                        new FinancialTransactionEntryInput(vndWalletId, new BigDecimal("100.0000"))
                )
        )))
                .isInstanceOf(InvalidFinancialTransactionException.class)
                .hasMessageContaining("TRANSFER transactions must not have a category");

        // TRANSFER with same wallet twice -> rejected
        assertThatThrownBy(() -> transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.TRANSFER, FinancialTransactionStatus.POSTED, null, "Self Transfer", null,
                Instant.now(), null, List.of(
                        new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("-100.0000")),
                        new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("100.0000"))
                )
        )))
                .isInstanceOf(InvalidFinancialTransactionException.class)
                .hasMessageContaining("A wallet may only appear once");

        // Zero delta -> rejected
        assertThatThrownBy(() -> transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME, FinancialTransactionStatus.POSTED, incomeCategoryId, "Zero Income", null,
                Instant.now(), null, List.of(new FinancialTransactionEntryInput(usdWalletId, BigDecimal.ZERO))
        )))
                .isInstanceOf(InvalidFinancialTransactionException.class)
                .hasMessageContaining("must not be zero");

        // Deleted wallet assigned to new transaction -> rejected
        walletOperations.softDeleteWallet(vndWalletId);
        assertThatThrownBy(() -> transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME, FinancialTransactionStatus.POSTED, incomeCategoryId, "Deleted Wallet Income", null,
                Instant.now(), null, List.of(new FinancialTransactionEntryInput(vndWalletId, new BigDecimal("50.0000")))
        )))
                .isInstanceOf(InvalidFinancialTransactionException.class)
                .hasMessageContaining("is deleted");
    }

    @Test
    @DisplayName("Deterministic PostgreSQL contention: two concurrent updates on the same transaction serialize under row write guard with preloaded context")
    void contentionSameTransactionMutationSerializesCleanly() throws Exception {
        FinancialTransactionView tx = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME,
                FinancialTransactionStatus.POSTED,
                incomeCategoryId,
                "Original",
                null,
                Instant.now(),
                null,
                List.of(new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("100.0000")))
        ));

        CountDownLatch thread2Preloaded = new CountDownLatch(1);
        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 2: preloads transaction context, then waits for Thread 1 to acquire row lock
            Future<FinancialTransactionView> thread2Future = executor.submit(() -> txTemplate.execute(status -> {
                FinancialTransactionView preloaded = transactionOperations.findTransactionById(tx.id());
                assertThat(preloaded.description()).isEqualTo("Original");
                assertThat(preloaded.entries()).hasSize(1);
                thread2Preloaded.countDown();
                try {
                    boolean awaited = thread1HasLock.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return transactionOperations.updateTransaction(
                        new UpdateFinancialTransactionCommand(
                                tx.id(),
                                FinancialTransactionType.INCOME,
                                FinancialTransactionStatus.POSTED,
                                incomeCategoryId,
                                "Thread 2 Update",
                                null,
                                Instant.now(),
                                null,
                                List.of(new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("300.0000")))
                        )
                );
            }));

            assertThat(thread2Preloaded.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 1: updates transaction to amount 200 in uncommitted transaction (holding transaction row guard)
            Future<FinancialTransactionView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                FinancialTransactionView updated = transactionOperations.updateTransaction(new UpdateFinancialTransactionCommand(
                        tx.id(),
                        FinancialTransactionType.INCOME,
                        FinancialTransactionStatus.POSTED,
                        incomeCategoryId,
                        "Thread 1 Update",
                        null,
                        Instant.now(),
                        null,
                        List.of(new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("200.0000")))
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

            // Observe lock contention on financial_transactions table as Thread 2 attempts update
            awaitCompetingLock("financial_transactions", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            FinancialTransactionView res1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(res1.description()).isEqualTo("Thread 1 Update");

            // Thread 2 unblocks, reloads fresh state under lock, detaches stale entities, and applies its update
            FinancialTransactionView res2 = thread2Future.get(10, TimeUnit.SECONDS);
            assertThat(res2.description()).isEqualTo("Thread 2 Update");

            // Committed final state is Thread 2 Update: exactly 1 transaction, 1 entry of 300
            FinancialTransactionView finalTx = transactionOperations.findTransactionById(tx.id());
            assertThat(finalTx.description()).isEqualTo("Thread 2 Update");
            assertThat(finalTx.entries()).hasSize(1);
            assertThat(finalTx.entries().getFirst().amountDelta()).isEqualByComparingTo(new BigDecimal("300.0000"));

            // Derived balance = 1000 + 300 = 1300
            assertThat(walletOperations.currentBalance(usdWalletId)).isEqualByComparingTo(new BigDecimal("1300.0000"));
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-3 contention: Update vs Delete (Delete wins first, Update loser is rejected and rolled back with zero partial children)")
    void contentionTransactionUpdateVsDeleteWinnerDeleteLeavesNoPartialChildren() throws Exception {
        FinancialTransactionView tx = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME,
                FinancialTransactionStatus.POSTED,
                incomeCategoryId,
                "Original For Delete",
                null,
                Instant.now(),
                null,
                List.of(new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("100.0000")))
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: soft-deletes transaction in uncommitted transaction
            Future<FinancialTransactionView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                FinancialTransactionView deleted = transactionOperations.softDeleteTransaction(tx.id());
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

            // Thread 2: attempts to update transaction with new entry on VND wallet, blocks on row lock
            Future<FinancialTransactionView> thread2Future = executor.submit(() -> transactionOperations.updateTransaction(
                    new UpdateFinancialTransactionCommand(
                            tx.id(),
                            FinancialTransactionType.INCOME,
                            FinancialTransactionStatus.POSTED,
                            incomeCategoryId,
                            "Loser Update Attempt",
                            null,
                            Instant.now(),
                            null,
                            List.of(new FinancialTransactionEntryInput(vndWalletId, new BigDecimal("500.0000")))
                    )
            ));

            // Observe lock contention on financial_transactions table
            awaitCompetingLock("financial_transactions", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            FinancialTransactionView res1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(res1.deletedAt()).isNotNull();

            // Thread 2 unblocks, reloads under lock, detects deleted transaction, and fails
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(FinancialTransactionNotFoundException.class)
                    .hasMessageContaining("Financial transaction not found with id:");

            // Verify zero partial children from Thread 2 persisted in DB
            Integer partialEntries = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM financial_transaction_entries WHERE transaction_id = ? AND wallet_id = ?",
                    Integer.class,
                    tx.id(),
                    vndWalletId
            );
            assertThat(partialEntries).isZero();

            // VND wallet balance unchanged
            assertThat(walletOperations.currentBalance(vndWalletId)).isEqualByComparingTo(new BigDecimal("500.0000"));
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-3 contention: Restore contention with preloaded parent and children reconciles fresh authoritative state")
    void contentionRestoreTransactionWithPreloadedContextReconcilesFreshState() throws Exception {
        FinancialTransactionView tx = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME,
                FinancialTransactionStatus.POSTED,
                incomeCategoryId,
                "To Restore Initial",
                null,
                Instant.now(),
                null,
                List.of(new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("100.0000")))
        ));
        transactionOperations.softDeleteTransaction(tx.id());

        CountDownLatch thread2Preloaded = new CountDownLatch(1);
        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread1CanCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 2: waiting participant with preloaded context
            Future<FinancialTransactionView> thread2Future = executor.submit(() -> txTemplate.execute(status -> {
                // Explicitly preload parent and entries in this persistence context before waiting
                FinancialTransaction preloadedParent = financialTransactionRepository.findById(tx.id()).orElseThrow();
                assertThat(preloadedParent.getDeletedAt()).isNotNull();
                List<FinancialTransactionEntry> preloadedEntries = financialTransactionEntryRepository.findByTransactionIdOrderByIdAsc(tx.id());
                assertThat(preloadedEntries).hasSize(1);
                assertThat(preloadedEntries.getFirst().getAmountDelta()).isEqualByComparingTo(new BigDecimal("100.0000"));
                thread2Preloaded.countDown();

                try {
                    boolean ready = thread1HasLock.await(5, TimeUnit.SECONDS);
                    assertThat(ready).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }

                // Attempts restoreTransaction, blocks on row lock held by Thread 1
                return transactionOperations.restoreTransaction(tx.id());
            }));

            // Main thread waits for Thread 2 to complete preloading
            assertThat(thread2Preloaded.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 1: winner restores transaction and updates scalar/child state in uncommitted transaction
            Future<FinancialTransactionView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                FinancialTransactionView restored = transactionOperations.restoreTransaction(tx.id());
                FinancialTransactionView updated = transactionOperations.updateTransaction(new UpdateFinancialTransactionCommand(
                        tx.id(),
                        FinancialTransactionType.INCOME,
                        FinancialTransactionStatus.POSTED,
                        incomeCategoryId,
                        "Restored And Updated By Winner",
                        null,
                        Instant.now(),
                        null,
                        List.of(new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("250.0000")))
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

            // Ensure Thread 1 has acquired lock
            assertThat(thread1HasLock.await(5, TimeUnit.SECONDS)).isTrue();

            // Observe Thread 2 blocking on PostgreSQL row lock
            awaitCompetingLock("financial_transactions", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread1CanCommit.countDown();

            FinancialTransactionView res1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(res1.description()).isEqualTo("Restored And Updated By Winner");

            FinancialTransactionView res2 = thread2Future.get(10, TimeUnit.SECONDS);
            assertThat(res2.deletedAt()).isNull();
            assertThat(res2.description()).isEqualTo("Restored And Updated By Winner");
            assertThat(res2.entries()).hasSize(1);
            assertThat(res2.entries().getFirst().amountDelta()).isEqualByComparingTo(new BigDecimal("250.0000"));

            // Fresh database reload confirms authoritative state
            FinancialTransactionView fresh = transactionOperations.findTransactionById(tx.id());
            assertThat(fresh.deletedAt()).isNull();
            assertThat(fresh.description()).isEqualTo("Restored And Updated By Winner");
            assertThat(fresh.entries()).hasSize(1);
            assertThat(fresh.entries().getFirst().amountDelta()).isEqualByComparingTo(new BigDecimal("250.0000"));

            // Wallet balance reflects restored and updated contribution (1000 opening + 250 = 1250)
            assertThat(walletOperations.currentBalance(usdWalletId)).isEqualByComparingTo(new BigDecimal("1250.0000"));
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-7: Measured query count proves constant queries for findRecentTransactions and findRecentTransactionsByWallet across small and large limits")
    void measuredQueryCountProvesConstantChildQueriesForTransactions() {
        Instant baseTime = Instant.parse("2026-10-01T00:00:00Z");

        // Create 10 transactions with entries on usdWalletId
        for (int i = 1; i <= 10; i++) {
            transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                    FinancialTransactionType.INCOME,
                    FinancialTransactionStatus.POSTED,
                    incomeCategoryId,
                    "Tx Measured " + String.format("%02d", i),
                    null,
                    baseTime.plus(Duration.ofHours(i)),
                    null,
                    List.of(new FinancialTransactionEntryInput(usdWalletId, new BigDecimal(i + ".0000")))
            ));
        }

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);

        // 1. Global recent: limit 2 vs limit 10
        stats.clear();
        List<FinancialTransactionView> globalSmall = transactionOperations.findRecentTransactions(2);
        long queriesGlobalSmall = stats.getPrepareStatementCount();

        stats.clear();
        List<FinancialTransactionView> globalLarge = transactionOperations.findRecentTransactions(10);
        long queriesGlobalLarge = stats.getPrepareStatementCount();

        // Constant query count: exactly 2 queries (1 for parent transactions batch, 1 for child entries batch)
        assertThat(queriesGlobalSmall).isEqualTo(2);
        assertThat(queriesGlobalLarge).isEqualTo(2);
        assertThat(queriesGlobalSmall).isEqualTo(queriesGlobalLarge);

        // Verify ordering (occurredAt DESC, id DESC) and limit adherence
        assertThat(globalSmall).hasSize(2);
        assertThat(globalLarge).hasSize(10);
        assertThat(globalSmall.get(0).occurredAt()).isAfter(globalSmall.get(1).occurredAt());
        assertThat(globalSmall.get(0).description()).isEqualTo("Tx Measured 10");
        assertThat(globalSmall.get(1).description()).isEqualTo("Tx Measured 09");

        // Verify absence of unrelated child loading: entries belong only to returned parent transactions
        Set<Long> smallTxIds = globalSmall.stream().map(FinancialTransactionView::id).collect(Collectors.toSet());
        for (FinancialTransactionView tx : globalSmall) {
            assertThat(smallTxIds).contains(tx.id());
            for (var entry : tx.entries()) {
                assertThat(entry.transactionId()).isEqualTo(tx.id());
            }
        }

        // 2. Wallet recent: limit 2 vs limit 10
        stats.clear();
        List<FinancialTransactionView> walletSmall = transactionOperations.findRecentTransactionsByWallet(usdWalletId, 2);
        long queriesWalletSmall = stats.getPrepareStatementCount();

        stats.clear();
        List<FinancialTransactionView> walletLarge = transactionOperations.findRecentTransactionsByWallet(usdWalletId, 10);
        long queriesWalletLarge = stats.getPrepareStatementCount();

        // Constant query count: exactly 3 queries (1 for wallet check, 1 for parent transactions batch, 1 for child entries batch)
        assertThat(queriesWalletSmall).isEqualTo(3);
        assertThat(queriesWalletLarge).isEqualTo(3);
        assertThat(queriesWalletSmall).isEqualTo(queriesWalletLarge);

        // Verify ordering (occurredAt DESC, id DESC) and limit adherence
        assertThat(walletSmall).hasSize(2);
        assertThat(walletLarge).hasSize(10);
        assertThat(walletSmall.get(0).occurredAt()).isAfter(walletSmall.get(1).occurredAt());

        // Verify absence of unrelated child loading
        Set<Long> walletSmallTxIds = walletSmall.stream().map(FinancialTransactionView::id).collect(Collectors.toSet());
        for (FinancialTransactionView tx : walletSmall) {
            assertThat(walletSmallTxIds).contains(tx.id());
            for (var entry : tx.entries()) {
                assertThat(entry.transactionId()).isEqualTo(tx.id());
            }
        }
    }

    @Test
    @DisplayName("Bounded recent reads for specific wallet and global transactions ordered by occurred_at DESC, id DESC")
    void boundedRecentReads() {
        Instant t1 = Instant.parse("2026-10-01T10:00:00Z");
        Instant t2 = Instant.parse("2026-10-01T11:00:00Z");
        Instant t3 = Instant.parse("2026-10-01T12:00:00Z");

        FinancialTransactionView tx1 = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME, FinancialTransactionStatus.POSTED, incomeCategoryId, "T1", null,
                t1, null, List.of(new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("10.0000")))
        ));
        FinancialTransactionView tx2 = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME, FinancialTransactionStatus.POSTED, incomeCategoryId, "T2", null,
                t2, null, List.of(new FinancialTransactionEntryInput(vndWalletId, new BigDecimal("20.0000")))
        ));
        FinancialTransactionView tx3 = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME, FinancialTransactionStatus.POSTED, incomeCategoryId, "T3", null,
                t3, null, List.of(new FinancialTransactionEntryInput(usdWalletId, new BigDecimal("30.0000")))
        ));

        // Global recent limit 2
        List<FinancialTransactionView> globalRecent = transactionOperations.findRecentTransactions(2);
        assertThat(globalRecent).hasSize(2);
        assertThat(globalRecent.get(0).id()).isEqualTo(tx3.id());
        assertThat(globalRecent.get(1).id()).isEqualTo(tx2.id());

        // USD wallet recent limit 10
        List<FinancialTransactionView> usdRecent = transactionOperations.findRecentTransactionsByWallet(usdWalletId, 10);
        assertThat(usdRecent).hasSize(2);
        assertThat(usdRecent.get(0).id()).isEqualTo(tx3.id());
        assertThat(usdRecent.get(1).id()).isEqualTo(tx1.id());

        // Non-positive limit
        assertThatThrownBy(() -> transactionOperations.findRecentTransactions(0))
                .isInstanceOf(InvalidFinancialTransactionException.class)
                .hasMessageContaining("Limit must be positive");
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
