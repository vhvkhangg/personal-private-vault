package com.vhvkhangg.personalprivatevault.finance;

import com.vhvkhangg.personalprivatevault.finance.category.TransactionCategoryOperations;
import com.vhvkhangg.personalprivatevault.finance.category.command.CreateTransactionCategoryCommand;
import com.vhvkhangg.personalprivatevault.finance.category.command.UpdateTransactionCategoryCommand;
import com.vhvkhangg.personalprivatevault.finance.category.exception.InvalidTransactionCategoryException;
import com.vhvkhangg.personalprivatevault.finance.category.exception.TransactionCategoryConflictException;
import com.vhvkhangg.personalprivatevault.finance.category.exception.TransactionCategoryNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionStatus;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;
import com.vhvkhangg.personalprivatevault.finance.enums.WalletType;
import com.vhvkhangg.personalprivatevault.finance.transaction.FinancialTransactionOperations;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.CreateFinancialTransactionCommand;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.FinancialTransactionEntryInput;
import com.vhvkhangg.personalprivatevault.finance.transaction.exception.InvalidFinancialTransactionException;
import com.vhvkhangg.personalprivatevault.finance.view.FinancialTransactionView;
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
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
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
class TransactionCategoryIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private TransactionCategoryOperations categoryOperations;

    @Autowired
    private WalletOperations walletOperations;

    @Autowired
    private FinancialTransactionOperations transactionOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long testWalletId;

    @BeforeEach
    void setUp() {
        cleanUp();
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol) VALUES ('USD', 'US Dollar', '$')
                ON CONFLICT (code) DO NOTHING
                """);
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Test Wallet", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));
        testWalletId = wallet.id();
    }

    @AfterEach
    void cleanUp() {
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
    @DisplayName("Creates, updates, and reloads category with parent hierarchy")
    void createUpdateAndReloadCategory() {
        TransactionCategoryView parent = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Food & Dining", TransactionCategoryKind.EXPENSE, null, true
        ));

        assertThat(parent.id()).isNotNull();
        assertThat(parent.name()).isEqualTo("Food & Dining");
        assertThat(parent.kind()).isEqualTo(TransactionCategoryKind.EXPENSE);
        assertThat(parent.parentCategoryId()).isNull();
        assertThat(parent.active()).isTrue();

        TransactionCategoryView child = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Restaurants", TransactionCategoryKind.EXPENSE, parent.id(), true
        ));

        assertThat(child.parentCategoryId()).isEqualTo(parent.id());

        // Update child
        TransactionCategoryView updated = categoryOperations.updateCategory(new UpdateTransactionCategoryCommand(
                child.id(), "Fine Dining", TransactionCategoryKind.EXPENSE, parent.id(), true
        ));
        assertThat(updated.name()).isEqualTo("Fine Dining");

        TransactionCategoryView reloaded = categoryOperations.findCategoryById(child.id());
        assertThat(reloaded.name()).isEqualTo("Fine Dining");
    }

    @Test
    @DisplayName("Validates category constraints: non-blank name, self-parent rejection, and parent existence")
    void validatesCategoryConstraints() {
        // Blank name
        assertThatThrownBy(() -> categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "   ", TransactionCategoryKind.INCOME, null, true
        )))
                .isInstanceOf(InvalidTransactionCategoryException.class)
                .hasMessageContaining("name must not be blank");

        // Non-existent parent
        assertThatThrownBy(() -> categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Orphan", TransactionCategoryKind.INCOME, 999999L, true
        )))
                .isInstanceOf(InvalidTransactionCategoryException.class)
                .hasMessageContaining("Parent category does not exist: 999999");

        // Self-parent rejection
        TransactionCategoryView cat = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Self Cat", TransactionCategoryKind.BOTH, null, true
        ));
        assertThatThrownBy(() -> categoryOperations.updateCategory(new UpdateTransactionCategoryCommand(
                cat.id(), "Self Cat", TransactionCategoryKind.BOTH, cat.id(), true
        )))
                .isInstanceOf(InvalidTransactionCategoryException.class)
                .hasMessageContaining("cannot directly parent itself");

        // Not found update
        assertThatThrownBy(() -> categoryOperations.updateCategory(new UpdateTransactionCategoryCommand(
                999999L, "Name", TransactionCategoryKind.BOTH, null, true
        )))
                .isInstanceOf(TransactionCategoryNotFoundException.class);
    }

    @Test
    @DisplayName("Category kind mutation policy rejects historical incompatibility")
    void categoryKindMutationRejectsIncompatibleHistoricalReferences() {
        // Category with kind BOTH
        TransactionCategoryView cat = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "General", TransactionCategoryKind.BOTH, null, true
        ));

        // Create an EXPENSE transaction referencing cat
        transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.EXPENSE,
                FinancialTransactionStatus.POSTED,
                cat.id(),
                "Expense 1",
                null,
                Instant.now(),
                null,
                List.of(new FinancialTransactionEntryInput(testWalletId, new BigDecimal("-30.0000")))
        ));

        // Changing kind to INCOME must be rejected because EXPENSE reference exists
        assertThatThrownBy(() -> categoryOperations.updateCategory(new UpdateTransactionCategoryCommand(
                cat.id(), "General", TransactionCategoryKind.INCOME, null, true
        )))
                .isInstanceOf(TransactionCategoryConflictException.class)
                .hasMessageContaining("Cannot change category kind to INCOME");

        // Changing kind to EXPENSE is allowed because all existing references are EXPENSE
        TransactionCategoryView updatedToExpense = categoryOperations.updateCategory(new UpdateTransactionCategoryCommand(
                cat.id(), "General", TransactionCategoryKind.EXPENSE, null, true
        ));
        assertThat(updatedToExpense.kind()).isEqualTo(TransactionCategoryKind.EXPENSE);

        // Changing kind back to BOTH is always allowed
        TransactionCategoryView updatedToBoth = categoryOperations.updateCategory(new UpdateTransactionCategoryCommand(
                cat.id(), "General", TransactionCategoryKind.BOTH, null, true
        ));
        assertThat(updatedToBoth.kind()).isEqualTo(TransactionCategoryKind.BOTH);
    }

    @Test
    @DisplayName("Contention between category kind update and entry assignment under shared category guard")
    void contentionCategoryKindUpdateVsEntryAssignment() throws Exception {
        TransactionCategoryView cat = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Contention Category", TransactionCategoryKind.BOTH, null, true
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: updates category kind to INCOME in uncommitted transaction (holds category lock)
            Future<TransactionCategoryView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                TransactionCategoryView updated = categoryOperations.updateCategory(new UpdateTransactionCategoryCommand(
                        cat.id(), "Contention Category", TransactionCategoryKind.INCOME, null, true
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

            // Thread 2: attempts to create an EXPENSE transaction assigning this category.
            // Blocks on the category row lock held by Thread 1.
            Future<FinancialTransactionView> thread2Future = executor.submit(() -> transactionOperations.createTransaction(
                    new CreateFinancialTransactionCommand(
                            FinancialTransactionType.EXPENSE,
                            FinancialTransactionStatus.POSTED,
                            cat.id(),
                            "Contention Expense",
                            null,
                            Instant.now(),
                            null,
                            List.of(new FinancialTransactionEntryInput(testWalletId, new BigDecimal("-20.0000")))
                    )
            ));

            // Observe lock contention on transaction_categories table
            awaitCompetingLock("transaction_categories", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            TransactionCategoryView updatedCat = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(updatedCat.kind()).isEqualTo(TransactionCategoryKind.INCOME);

            // Thread 2 wakes up, reloads fresh category state under the category lock,
            // detects that category kind is now INCOME (incompatible with EXPENSE), and throws InvalidFinancialTransactionException.
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(InvalidFinancialTransactionException.class)
                    .hasMessageContaining("is incompatible with EXPENSE transaction");

            // Zero partial writes for Thread 2's transaction
            Integer txCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM financial_transactions WHERE description = 'Contention Expense'",
                    Integer.class
            );
            assertThat(txCount).isZero();
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Orders categories deterministically by name ASC, id ASC with positive limit")
    void boundedReadsAndOrdering() {
        categoryOperations.createCategory(new CreateTransactionCategoryCommand("Utilities", TransactionCategoryKind.EXPENSE, null, true));
        categoryOperations.createCategory(new CreateTransactionCategoryCommand("Entertainment", TransactionCategoryKind.EXPENSE, null, true));
        categoryOperations.createCategory(new CreateTransactionCategoryCommand("Groceries", TransactionCategoryKind.EXPENSE, null, true));

        List<TransactionCategoryView> categories = categoryOperations.findCategories(true, 2);
        assertThat(categories).hasSize(2);
        assertThat(categories.get(0).name()).isEqualTo("Entertainment");
        assertThat(categories.get(1).name()).isEqualTo("Groceries");

        // Limit validation
        assertThatThrownBy(() -> categoryOperations.findCategories(true, 0))
                .isInstanceOf(InvalidTransactionCategoryException.class)
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
