package com.vhvkhangg.personalprivatevault.finance;

import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionStatus;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurrenceFrequency;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurringPostingMode;
import com.vhvkhangg.personalprivatevault.finance.enums.WalletType;
import com.vhvkhangg.personalprivatevault.finance.recurring.RecurringTransactionRuleOperations;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.CreateRecurringTransactionRuleCommand;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.RecurringRuleEntryInput;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.UpdateRecurringTransactionRuleCommand;
import com.vhvkhangg.personalprivatevault.finance.transaction.FinancialTransactionOperations;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.CreateFinancialTransactionCommand;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.FinancialTransactionEntryInput;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.UpdateFinancialTransactionCommand;
import com.vhvkhangg.personalprivatevault.finance.view.FinancialTransactionView;
import com.vhvkhangg.personalprivatevault.finance.view.RecurringTransactionRuleView;
import com.vhvkhangg.personalprivatevault.finance.view.WalletView;
import com.vhvkhangg.personalprivatevault.finance.wallet.WalletOperations;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.CreateWalletCommand;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.UpdateWalletCommand;
import com.vhvkhangg.personalprivatevault.finance.wallet.exception.InvalidWalletException;
import com.vhvkhangg.personalprivatevault.finance.wallet.exception.WalletConflictException;
import com.vhvkhangg.personalprivatevault.finance.wallet.exception.WalletNotFoundException;
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
class WalletIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private WalletOperations walletOperations;

    @Autowired
    private FinancialTransactionOperations transactionOperations;

    @Autowired
    private RecurringTransactionRuleOperations recurringOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

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
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM subscriptions");
        jdbcTemplate.execute("DELETE FROM recurring_rule_entries");
        jdbcTemplate.execute("DELETE FROM recurring_rule_weekdays");
        jdbcTemplate.execute("DELETE FROM recurring_transaction_rules");
        jdbcTemplate.execute("DELETE FROM financial_transaction_entries");
        jdbcTemplate.execute("DELETE FROM financial_transactions");
        jdbcTemplate.execute("DELETE FROM wallets");
        jdbcTemplate.execute("DELETE FROM transaction_categories");
        jdbcTemplate.execute("DELETE FROM currencies WHERE code = 'EUR'");
    }

    @Test
    @DisplayName("Creates, updates, and reloads wallet with exact decimal precision and Reference currency validation")
    void createUpdateAndReloadWallet() {
        CreateWalletCommand cmd = new CreateWalletCommand(
                "Checking Account",
                WalletType.BANK_ACCOUNT,
                "USD",
                new BigDecimal("1000.5000"),
                "Primary checking",
                true
        );

        WalletView wallet = walletOperations.createWallet(cmd);

        assertThat(wallet.id()).isNotNull();
        assertThat(wallet.name()).isEqualTo("Checking Account");
        assertThat(wallet.type()).isEqualTo(WalletType.BANK_ACCOUNT);
        assertThat(wallet.currencyCode()).isEqualTo("USD");
        assertThat(wallet.openingBalance()).isEqualByComparingTo(new BigDecimal("1000.5000"));
        assertThat(wallet.notes()).isEqualTo("Primary checking");
        assertThat(wallet.active()).isTrue();
        assertThat(wallet.createdAt()).isNotNull();
        assertThat(wallet.updatedAt()).isNotNull();
        assertThat(wallet.deletedAt()).isNull();

        // Derived balance equals opening balance when no transactions exist
        BigDecimal balance = walletOperations.currentBalance(wallet.id());
        assertThat(balance).isEqualByComparingTo(new BigDecimal("1000.5000"));

        // Update wallet metadata and currency (no transactions exist yet)
        UpdateWalletCommand updateCmd = new UpdateWalletCommand(
                wallet.id(),
                "Primary Checking",
                WalletType.BANK_ACCOUNT,
                "VND",
                new BigDecimal("1000.5000"),
                "Updated notes",
                true
        );
        WalletView updated = walletOperations.updateWallet(updateCmd);
        assertThat(updated.name()).isEqualTo("Primary Checking");
        assertThat(updated.currencyCode()).isEqualTo("VND");
        assertThat(updated.notes()).isEqualTo("Updated notes");

        WalletView reloaded = walletOperations.findWalletById(wallet.id());
        assertThat(reloaded.name()).isEqualTo("Primary Checking");
        assertThat(reloaded.currencyCode()).isEqualTo("VND");
    }

    @Test
    @DisplayName("Validates decimal scale and rejects scale > 4 without silent rounding")
    void validatesDecimalScale() {
        // Scale 5 rejected
        assertThatThrownBy(() -> walletOperations.createWallet(new CreateWalletCommand(
                "Scale Test", WalletType.CASH, "USD", new BigDecimal("100.12345"), null, true
        )))
                .isInstanceOf(InvalidWalletException.class)
                .hasMessageContaining("scale must not exceed 4");

        // Scale 4 accepted
        WalletView w = walletOperations.createWallet(new CreateWalletCommand(
                "Scale Test 4", WalletType.CASH, "USD", new BigDecimal("100.1234"), null, true
        ));
        assertThat(w.openingBalance()).isEqualByComparingTo(new BigDecimal("100.1234"));

        // Reference currency validation
        assertThatThrownBy(() -> walletOperations.createWallet(new CreateWalletCommand(
                "Unknown Currency", WalletType.CASH, "XYZ", BigDecimal.ZERO, null, true
        )))
                .isInstanceOf(InvalidWalletException.class)
                .hasMessageContaining("Unknown currency code: XYZ");
    }

    @Test
    @DisplayName("Derives current balance from opening balance and POSTED non-deleted entries only")
    void derivedCurrentBalanceCalculation() {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Income Wallet", WalletType.BANK_ACCOUNT, "USD", new BigDecimal("500.0000"), null, true
        ));

        // 1. POSTED Income: +200
        FinancialTransactionView tx1 = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME,
                FinancialTransactionStatus.POSTED,
                null,
                "Salary",
                null,
                Instant.now(),
                null,
                List.of(new FinancialTransactionEntryInput(wallet.id(), new BigDecimal("200.0000")))
        ));

        // Derived balance = 500 + 200 = 700
        assertThat(walletOperations.currentBalance(wallet.id()))
                .isEqualByComparingTo(new BigDecimal("700.0000"));

        // 2. PENDING Expense: -50 (must NOT affect current balance)
        transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.EXPENSE,
                FinancialTransactionStatus.PENDING,
                null,
                "Pending Charge",
                null,
                Instant.now(),
                null,
                List.of(new FinancialTransactionEntryInput(wallet.id(), new BigDecimal("-50.0000")))
        ));
        assertThat(walletOperations.currentBalance(wallet.id()))
                .isEqualByComparingTo(new BigDecimal("700.0000"));

        // 3. CANCELLED Income: +1000 (must NOT affect current balance)
        transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME,
                FinancialTransactionStatus.CANCELLED,
                null,
                "Cancelled Bonus",
                null,
                Instant.now(),
                null,
                List.of(new FinancialTransactionEntryInput(wallet.id(), new BigDecimal("1000.0000")))
        ));
        assertThat(walletOperations.currentBalance(wallet.id()))
                .isEqualByComparingTo(new BigDecimal("700.0000"));

        // 4. POSTED Expense: -100
        transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.EXPENSE,
                FinancialTransactionStatus.POSTED,
                null,
                "Groceries",
                null,
                Instant.now(),
                null,
                List.of(new FinancialTransactionEntryInput(wallet.id(), new BigDecimal("-100.0000")))
        ));
        // Derived balance = 700 - 100 = 600
        assertThat(walletOperations.currentBalance(wallet.id()))
                .isEqualByComparingTo(new BigDecimal("600.0000"));

        // 5. Soft-delete tx1 (+200): immediately removes it from balance
        transactionOperations.softDeleteTransaction(tx1.id());
        // Balance = 500 - 100 = 400
        assertThat(walletOperations.currentBalance(wallet.id()))
                .isEqualByComparingTo(new BigDecimal("400.0000"));

        // 6. Restore tx1: immediately restores it to balance
        transactionOperations.restoreTransaction(tx1.id());
        // Balance = 400 + 200 = 600
        assertThat(walletOperations.currentBalance(wallet.id()))
                .isEqualByComparingTo(new BigDecimal("600.0000"));
    }

    @Test
    @DisplayName("Wallet currency change is rejected while retained financial transaction entries reference the wallet")
    void walletCurrencyChangeRejectedWhileTransactionEntriesExist() {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Wallet Locked", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));

        // Create transaction referencing wallet
        FinancialTransactionView tx = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME,
                FinancialTransactionStatus.POSTED,
                null,
                "Initial",
                null,
                Instant.now(),
                null,
                List.of(new FinancialTransactionEntryInput(wallet.id(), new BigDecimal("100.0000")))
        ));

        // Attempt currency change from USD to VND -> rejected
        assertThatThrownBy(() -> walletOperations.updateWallet(new UpdateWalletCommand(
                wallet.id(), "Wallet Locked", WalletType.BANK_ACCOUNT, "VND", BigDecimal.ZERO, null, true
        )))
                .isInstanceOf(WalletConflictException.class)
                .hasMessageContaining("Cannot change currency of wallet");

        // Verify currency and balance remain untouched
        WalletView reloaded = walletOperations.findWalletById(wallet.id());
        assertThat(reloaded.currencyCode()).isEqualTo("USD");
        assertThat(walletOperations.currentBalance(wallet.id())).isEqualByComparingTo(new BigDecimal("100.0000"));

        // Soft-delete transaction: entry row is still retained, so currency update is STILL rejected
        transactionOperations.softDeleteTransaction(tx.id());
        assertThatThrownBy(() -> walletOperations.updateWallet(new UpdateWalletCommand(
                wallet.id(), "Wallet Locked", WalletType.BANK_ACCOUNT, "VND", BigDecimal.ZERO, null, true
        )))
                .isInstanceOf(WalletConflictException.class)
                .hasMessageContaining("Cannot change currency of wallet");
    }

    @Test
    @DisplayName("Wallet currency change is rejected while retained recurring rule entries reference the wallet")
    void walletCurrencyChangeRejectedWhileRecurringEntriesExist() {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Recurring Locked", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));

        // Create recurring rule referencing wallet
        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Monthly Rent",
                FinancialTransactionType.EXPENSE,
                null,
                RecurringPostingMode.REQUIRE_CONFIRMATION,
                RecurrenceFrequency.MONTHLY,
                1,
                1,
                null,
                LocalDate.now(),
                null,
                null,
                null,
                null,
                null,
                true,
                null,
                List.of(new RecurringRuleEntryInput(wallet.id(), new BigDecimal("-500.0000")))
        ));

        // Attempt currency change -> rejected
        assertThatThrownBy(() -> walletOperations.updateWallet(new UpdateWalletCommand(
                wallet.id(), "Recurring Locked", WalletType.BANK_ACCOUNT, "VND", BigDecimal.ZERO, null, true
        )))
                .isInstanceOf(WalletConflictException.class)
                .hasMessageContaining("Cannot change currency of wallet");

        // Soft delete rule: entry row is still retained, so currency update is still rejected
        recurringOperations.softDeleteRule(rule.id());
        assertThatThrownBy(() -> walletOperations.updateWallet(new UpdateWalletCommand(
                wallet.id(), "Recurring Locked", WalletType.BANK_ACCOUNT, "VND", BigDecimal.ZERO, null, true
        )))
                .isInstanceOf(WalletConflictException.class)
                .hasMessageContaining("Cannot change currency of wallet");
    }

    @Test
    @DisplayName("Regression sequence: create on A, replace with B, commit, reload, verify no retained A reference, update A currency succeeds for transaction entries")
    void transactionEntryFullReplacementFreesWalletCurrencyChange() {
        WalletView walletA = walletOperations.createWallet(new CreateWalletCommand(
                "Wallet A", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));
        WalletView walletB = walletOperations.createWallet(new CreateWalletCommand(
                "Wallet B", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));

        // 1. Create entry referencing wallet A
        FinancialTransactionView tx = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME,
                FinancialTransactionStatus.POSTED,
                null,
                "Income A",
                null,
                Instant.now(),
                null,
                List.of(new FinancialTransactionEntryInput(walletA.id(), new BigDecimal("100.0000")))
        ));

        // Currency change on A rejected
        assertThatThrownBy(() -> walletOperations.updateWallet(new UpdateWalletCommand(
                walletA.id(), "Wallet A", WalletType.BANK_ACCOUNT, "VND", BigDecimal.ZERO, null, true
        ))).isInstanceOf(WalletConflictException.class);

        // 2. Full-replace child set so only wallet B remains
        transactionOperations.updateTransaction(new UpdateFinancialTransactionCommand(
                tx.id(),
                FinancialTransactionType.INCOME,
                FinancialTransactionStatus.POSTED,
                null,
                "Income B",
                null,
                Instant.now(),
                null,
                List.of(new FinancialTransactionEntryInput(walletB.id(), new BigDecimal("100.0000")))
        ));

        // 3. Verify no retained entry references wallet A
        Integer countA = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM financial_transaction_entries WHERE wallet_id = ?",
                Integer.class,
                walletA.id()
        );
        assertThat(countA).isZero();

        // 4. Update wallet A currency to VND succeeds
        WalletView updatedA = walletOperations.updateWallet(new UpdateWalletCommand(
                walletA.id(), "Wallet A", WalletType.BANK_ACCOUNT, "VND", BigDecimal.ZERO, null, true
        ));
        assertThat(updatedA.currencyCode()).isEqualTo("VND");
    }

    @Test
    @DisplayName("Regression sequence: create on A, replace with B, commit, reload, verify no retained A reference, update A currency succeeds for recurring rule entries")
    void recurringEntryFullReplacementFreesWalletCurrencyChange() {
        WalletView walletA = walletOperations.createWallet(new CreateWalletCommand(
                "Rule Wallet A", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));
        WalletView walletB = walletOperations.createWallet(new CreateWalletCommand(
                "Rule Wallet B", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));

        // 1. Create recurring rule referencing wallet A
        RecurringTransactionRuleView rule = recurringOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Daily Tip A",
                FinancialTransactionType.EXPENSE,
                null,
                RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY,
                1,
                null,
                null,
                LocalDate.now(),
                null,
                null,
                null,
                null,
                null,
                true,
                null,
                List.of(new RecurringRuleEntryInput(walletA.id(), new BigDecimal("-5.0000")))
        ));

        // Currency change on A rejected
        assertThatThrownBy(() -> walletOperations.updateWallet(new UpdateWalletCommand(
                walletA.id(), "Rule Wallet A", WalletType.BANK_ACCOUNT, "VND", BigDecimal.ZERO, null, true
        ))).isInstanceOf(WalletConflictException.class);

        // 2. Full-replace child set so only wallet B remains
        recurringOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                rule.id(),
                "Daily Tip B",
                FinancialTransactionType.EXPENSE,
                null,
                RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY,
                1,
                null,
                null,
                LocalDate.now(),
                null,
                null,
                null,
                null,
                null,
                true,
                null,
                List.of(new RecurringRuleEntryInput(walletB.id(), new BigDecimal("-5.0000")))
        ));

        // 3. Verify no retained recurring entry references wallet A
        Integer countA = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM recurring_rule_entries WHERE wallet_id = ?",
                Integer.class,
                walletA.id()
        );
        assertThat(countA).isZero();

        // 4. Update wallet A currency to VND succeeds
        WalletView updatedA = walletOperations.updateWallet(new UpdateWalletCommand(
                walletA.id(), "Rule Wallet A", WalletType.BANK_ACCOUNT, "VND", BigDecimal.ZERO, null, true
        ));
        assertThat(updatedA.currencyCode()).isEqualTo("VND");
    }

    @Test
    @DisplayName("Deterministic PostgreSQL contention: entry assignment wins, waiting currency change rejects safely")
    void contentionEntryAssignmentWinsWaitingCurrencyChangeRejects() throws Exception {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Contention Wallet", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: creates transaction referencing wallet in uncommitted transaction (acquiring wallet lock)
            Future<FinancialTransactionView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                FinancialTransactionView tx = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                        FinancialTransactionType.INCOME,
                        FinancialTransactionStatus.POSTED,
                        null,
                        "Contention Income",
                        null,
                        Instant.now(),
                        null,
                        List.of(new FinancialTransactionEntryInput(wallet.id(), new BigDecimal("50.0000")))
                ));
                thread1HasLock.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return tx;
            }));

            assertThat(thread1HasLock.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: attempts to update wallet currency from USD to VND.
            // Blocks on the wallet row lock held by Thread 1.
            Future<WalletView> thread2Future = executor.submit(() -> walletOperations.updateWallet(
                    new UpdateWalletCommand(wallet.id(), "Contention Wallet", WalletType.BANK_ACCOUNT, "VND", BigDecimal.ZERO, null, true)
            ));

            // Observe lock contention on wallets table
            awaitCompetingLock("wallets", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            FinancialTransactionView tx = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(tx).isNotNull();

            // Thread 2 wakes up, inspects fresh state under the wallet lock, detects retained entry, and throws WalletConflictException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(WalletConflictException.class)
                    .hasMessageContaining("Cannot change currency of wallet");

            // Final state: wallet currency is still USD
            WalletView finalWallet = walletOperations.findWalletById(wallet.id());
            assertThat(finalWallet.currencyCode()).isEqualTo("USD");
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Deterministic PostgreSQL contention: currency change wins, waiting entry assignment validates with fresh currency")
    void contentionCurrencyChangeWinsWaitingEntryAssignmentValidatesFreshState() throws Exception {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Fresh Currency Wallet", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true
        ));

        CountDownLatch thread1HasLock = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: updates wallet currency to VND in uncommitted transaction (holds wallet lock)
            Future<WalletView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                WalletView updated = walletOperations.updateWallet(new UpdateWalletCommand(
                        wallet.id(), "Fresh Currency Wallet", WalletType.BANK_ACCOUNT, "VND", BigDecimal.ZERO, null, true
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

            // Thread 2: attempts to create transaction on wallet. Blocks on wallet lock.
            Future<FinancialTransactionView> thread2Future = executor.submit(() -> transactionOperations.createTransaction(
                    new CreateFinancialTransactionCommand(
                            FinancialTransactionType.INCOME,
                            FinancialTransactionStatus.POSTED,
                            null,
                            "VND Income",
                            null,
                            Instant.now(),
                            null,
                            List.of(new FinancialTransactionEntryInput(wallet.id(), new BigDecimal("75.0000")))
                    )
            ));

            // Observe lock contention on wallets table
            awaitCompetingLock("wallets", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            WalletView updatedWallet = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(updatedWallet.currencyCode()).isEqualTo("VND");

            FinancialTransactionView tx = thread2Future.get(10, TimeUnit.SECONDS);
            assertThat(tx).isNotNull();

            // Entry was assigned to wallet whose currency is now VND
            WalletView finalWallet = walletOperations.findWalletById(wallet.id());
            assertThat(finalWallet.currencyCode()).isEqualTo("VND");
            assertThat(walletOperations.currentBalance(wallet.id())).isEqualByComparingTo(new BigDecimal("75.0000"));
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Orders wallets deterministically by name ASC, id ASC with positive limit")
    void boundedReadsAndOrdering() {
        walletOperations.createWallet(new CreateWalletCommand("Charlie", WalletType.CASH, "USD", BigDecimal.ZERO, null, true));
        walletOperations.createWallet(new CreateWalletCommand("Alice", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true));
        walletOperations.createWallet(new CreateWalletCommand("Bob", WalletType.E_WALLET, "USD", BigDecimal.ZERO, null, true));

        List<WalletView> wallets = walletOperations.findWallets(true, 2);
        assertThat(wallets).hasSize(2);
        assertThat(wallets.get(0).name()).isEqualTo("Alice");
        assertThat(wallets.get(1).name()).isEqualTo("Bob");

        // Limit validation
        assertThatThrownBy(() -> walletOperations.findWallets(true, 0))
                .isInstanceOf(InvalidWalletException.class)
                .hasMessageContaining("Limit must be positive");
    }

    @Test
    @DisplayName("Soft delete excludes from default list and findWalletById, restore returns it")
    void softDeleteAndRestoreLifecycle() {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Delete Me", WalletType.CASH, "USD", new BigDecimal("10.0000"), null, true
        ));

        // Soft delete
        walletOperations.softDeleteWallet(wallet.id());

        assertThatThrownBy(() -> walletOperations.findWalletById(wallet.id()))
                .isInstanceOf(WalletNotFoundException.class);
        assertThat(walletOperations.findWallets(null, 10)).isEmpty();

        // Restore
        WalletView restored = walletOperations.restoreWallet(wallet.id());
        assertThat(restored.deletedAt()).isNull();

        WalletView reloaded = walletOperations.findWalletById(wallet.id());
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
