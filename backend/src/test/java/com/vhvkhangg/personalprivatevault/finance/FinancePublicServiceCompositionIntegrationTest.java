package com.vhvkhangg.personalprivatevault.finance;

import com.vhvkhangg.personalprivatevault.finance.category.TransactionCategoryOperations;
import com.vhvkhangg.personalprivatevault.finance.category.command.CreateTransactionCategoryCommand;
import com.vhvkhangg.personalprivatevault.finance.category.command.UpdateTransactionCategoryCommand;
import com.vhvkhangg.personalprivatevault.finance.enums.BillingCycle;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionStatus;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurrenceFrequency;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurringPostingMode;
import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;
import com.vhvkhangg.personalprivatevault.finance.enums.WalletType;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.FinancialTransaction;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.FinancialTransactionEntry;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringTransactionRule;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.Subscription;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.TransactionCategory;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.Wallet;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.FinancialTransactionEntryRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.FinancialTransactionRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.RecurringTransactionRuleRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.SubscriptionRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.TransactionCategoryRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.WalletRepository;
import com.vhvkhangg.personalprivatevault.finance.recurring.RecurringTransactionRuleOperations;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.CreateRecurringTransactionRuleCommand;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.RecurringRuleEntryInput;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.UpdateRecurringTransactionRuleCommand;
import com.vhvkhangg.personalprivatevault.finance.recurring.exception.InvalidRecurringTransactionRuleException;
import com.vhvkhangg.personalprivatevault.finance.subscription.SubscriptionOperations;
import com.vhvkhangg.personalprivatevault.finance.subscription.command.CreateSubscriptionCommand;
import com.vhvkhangg.personalprivatevault.finance.subscription.exception.InvalidSubscriptionException;
import com.vhvkhangg.personalprivatevault.finance.transaction.FinancialTransactionOperations;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.CreateFinancialTransactionCommand;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.FinancialTransactionEntryInput;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.UpdateFinancialTransactionCommand;
import com.vhvkhangg.personalprivatevault.finance.transaction.exception.InvalidFinancialTransactionException;
import com.vhvkhangg.personalprivatevault.finance.view.FinancialTransactionView;
import com.vhvkhangg.personalprivatevault.finance.view.RecurringTransactionRuleView;
import com.vhvkhangg.personalprivatevault.finance.view.SubscriptionView;
import com.vhvkhangg.personalprivatevault.finance.view.TransactionCategoryView;
import com.vhvkhangg.personalprivatevault.finance.view.WalletView;
import com.vhvkhangg.personalprivatevault.finance.wallet.WalletOperations;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.CreateWalletCommand;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.UpdateWalletCommand;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FinancePublicServiceCompositionIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private WalletOperations walletOperations;

    @Autowired
    private FinancialTransactionOperations transactionOperations;

    @Autowired
    private RecurringTransactionRuleOperations ruleOperations;

    @Autowired
    private SubscriptionOperations subscriptionOperations;

    @Autowired
    private TransactionCategoryOperations categoryOperations;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionCategoryRepository categoryRepository;

    @Autowired
    private FinancialTransactionRepository transactionRepository;

    @Autowired
    private FinancialTransactionEntryRepository transactionEntryRepository;

    @Autowired
    private RecurringTransactionRuleRepository ruleRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        cleanUp();
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol) VALUES ('USD', 'US Dollar', '$')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol) VALUES ('VND', 'Vietnamese Dong', '₫')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol) VALUES ('EUR', 'Euro', '€')
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
    }

    @Test
    @DisplayName("FR11-9: Wallet update followed by transaction and rule assignment in same caller tx preserves currency, opening balance, and metadata")
    void walletUpdateFollowedByTransactionAndRuleAssignmentPreservesCurrencyBalanceAndMetadata() {
        // 1. Initial wallet and category committed outside caller transaction
        WalletView initialWallet = walletOperations.createWallet(new CreateWalletCommand(
                "Original Wallet",
                WalletType.BANK_ACCOUNT,
                "USD",
                new BigDecimal("0.0000"),
                "Original notes",
                true
        ));
        TransactionCategoryView category = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "General Category",
                TransactionCategoryKind.BOTH,
                null,
                true
        ));

        // 2. In single caller transaction: update wallet to VND / 123.4500, then create transaction and recurring rule
        transactionTemplate.execute(status -> {
            WalletView updatedWallet = walletOperations.updateWallet(new UpdateWalletCommand(
                    initialWallet.id(),
                    "Updated Wallet",
                    WalletType.CARD,
                    "VND",
                    new BigDecimal("123.4500"),
                    "Updated notes",
                    true
            ));
            assertThat(updatedWallet.name()).isEqualTo("Updated Wallet");
            assertThat(updatedWallet.currencyCode()).isEqualTo("VND");
            assertThat(updatedWallet.openingBalance()).isEqualByComparingTo("123.4500");

            FinancialTransactionView tx = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                    FinancialTransactionType.INCOME,
                    FinancialTransactionStatus.POSTED,
                    category.id(),
                    "Income entry referencing updated wallet",
                    null,
                    Instant.parse("2026-10-04T10:00:00Z"),
                    null,
                    List.of(new FinancialTransactionEntryInput(initialWallet.id(), new BigDecimal("1.0000")))
            ));
            assertThat(tx).isNotNull();

            RecurringTransactionRuleView rule = ruleOperations.createRule(new CreateRecurringTransactionRuleCommand(
                    "Monthly Income Rule",
                    FinancialTransactionType.INCOME,
                    category.id(),
                    RecurringPostingMode.AUTO_POST,
                    RecurrenceFrequency.MONTHLY,
                    1,
                    5,
                    null,
                    LocalDate.parse("2026-10-01"),
                    null,
                    LocalTime.of(9, 0),
                    Instant.parse("2026-10-05T09:00:00Z"),
                    "Rule description",
                    null,
                    true,
                    Set.of(),
                    List.of(new RecurringRuleEntryInput(initialWallet.id(), new BigDecimal("2.0000")))
            ));
            assertThat(rule).isNotNull();
            return null;
        });

        // 3. Fresh verification from database: wallet must NOT revert to Original/USD/0
        Wallet committedWallet = walletRepository.findById(initialWallet.id()).orElseThrow();
        assertThat(committedWallet.getName()).isEqualTo("Updated Wallet");
        assertThat(committedWallet.getType()).isEqualTo(WalletType.CARD);
        assertThat(committedWallet.getCurrencyCode()).isEqualTo("VND");
        assertThat(committedWallet.getOpeningBalance()).isEqualByComparingTo("123.4500");
        assertThat(committedWallet.getNotes()).isEqualTo("Updated notes");

        // Balance calculation = 123.4500 opening + 1.0000 posted transaction = 124.4500
        BigDecimal balance = walletOperations.currentBalance(initialWallet.id());
        assertThat(balance).isEqualByComparingTo("124.4500");
    }

    @Test
    @DisplayName("FR11-9: Wallet soft-delete followed by transaction entry assignment rejects and rolls back atomically")
    void walletSoftDeleteFollowedByTransactionRejectsAndRollsBackAtomically() {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Wallet To Delete",
                WalletType.CASH,
                "USD",
                new BigDecimal("100.0000"),
                null,
                true
        ));
        TransactionCategoryView category = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Cat For Delete Test",
                TransactionCategoryKind.BOTH,
                null,
                true
        ));

        // In single caller transaction: soft-delete wallet, then attempt to create transaction referencing it
        assertThatThrownBy(() -> transactionTemplate.execute(status -> {
            walletOperations.softDeleteWallet(wallet.id());

            // Should reject with InvalidFinancialTransactionException because wallet is deleted
            transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                    FinancialTransactionType.INCOME,
                    FinancialTransactionStatus.POSTED,
                    category.id(),
                    "Invalid income on deleted wallet",
                    null,
                    Instant.parse("2026-10-04T10:00:00Z"),
                    null,
                    List.of(new FinancialTransactionEntryInput(wallet.id(), new BigDecimal("50.0000")))
            ));
            return null;
        })).isInstanceOf(InvalidFinancialTransactionException.class)
                .hasMessageContaining("Referenced wallet is deleted");

        // Fresh verification: entire transaction rolled back; wallet remains active and not deleted
        Wallet rolledBackWallet = walletRepository.findById(wallet.id()).orElseThrow();
        assertThat(rolledBackWallet.getDeletedAt()).isNull();
        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("FR11-9: Wallet soft-delete followed by Subscription assignment rejects and rolls back atomically")
    void walletSoftDeleteFollowedBySubscriptionRejectsAndRollsBackAtomically() {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Subscription Payment Wallet",
                WalletType.BANK_ACCOUNT,
                "USD",
                new BigDecimal("50.0000"),
                null,
                true
        ));

        // In single caller transaction: soft-delete wallet, then attempt to create subscription
        assertThatThrownBy(() -> transactionTemplate.execute(status -> {
            walletOperations.softDeleteWallet(wallet.id());

            subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                    "Cloud Service",
                    "Acme Cloud",
                    new BigDecimal("9.9900"),
                    "USD",
                    BillingCycle.MONTHLY,
                    1,
                    null,
                    LocalDate.parse("2026-11-01"),
                    true,
                    wallet.id(),
                    null,
                    null,
                    null,
                    true
            ));
            return null;
        })).isInstanceOf(InvalidSubscriptionException.class)
                .hasMessageContaining("Payment wallet is deleted");

        // Fresh verification: wallet remains active, no subscription created
        Wallet rolledBackWallet = walletRepository.findById(wallet.id()).orElseThrow();
        assertThat(rolledBackWallet.getDeletedAt()).isNull();
        assertThat(subscriptionRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("FR11-9: Wallet restore sequencing in single caller transaction allows subsequent assignment")
    void walletRestoreSequencingInSingleCallerTransactionAllowsSubsequentAssignment() {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Restorable Wallet",
                WalletType.BANK_ACCOUNT,
                "USD",
                new BigDecimal("500.0000"),
                null,
                true
        ));
        TransactionCategoryView category = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Restore Category",
                TransactionCategoryKind.BOTH,
                null,
                true
        ));

        // Soft delete wallet in committed transaction 1
        walletOperations.softDeleteWallet(wallet.id());
        assertThat(walletRepository.findById(wallet.id()).orElseThrow().getDeletedAt()).isNotNull();

        // In single caller transaction 2: restore wallet, then create transaction and subscription
        transactionTemplate.execute(status -> {
            WalletView restored = walletOperations.restoreWallet(wallet.id());
            assertThat(restored.deletedAt()).isNull();

            FinancialTransactionView tx = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                    FinancialTransactionType.INCOME,
                    FinancialTransactionStatus.POSTED,
                    category.id(),
                    "Restored wallet income",
                    null,
                    Instant.parse("2026-10-04T10:00:00Z"),
                    null,
                    List.of(new FinancialTransactionEntryInput(wallet.id(), new BigDecimal("25.0000")))
            ));
            assertThat(tx).isNotNull();

            SubscriptionView sub = subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                    "Music Streaming",
                    "Audio Co",
                    new BigDecimal("4.9900"),
                    "USD",
                    BillingCycle.MONTHLY,
                    1,
                    null,
                    LocalDate.parse("2026-11-01"),
                    true,
                    wallet.id(),
                    null,
                    null,
                    null,
                    true
            ));
            assertThat(sub).isNotNull();
            return null;
        });

        // Fresh verification: wallet is restored, transaction and subscription committed
        Wallet committedWallet = walletRepository.findById(wallet.id()).orElseThrow();
        assertThat(committedWallet.getDeletedAt()).isNull();
        assertThat(transactionRepository.findAll()).hasSize(1);
        assertThat(subscriptionRepository.findAll()).hasSize(1);
        assertThat(walletOperations.currentBalance(wallet.id())).isEqualByComparingTo("525.0000");
    }

    @Test
    @DisplayName("FR11-9: Category kind update to INCOME followed by incompatible EXPENSE transaction rejects and rolls back atomically")
    void categoryKindUpdateFollowedByIncompatibleExpenseTransactionRejectsAtomicallyAndRollsBack() {
        TransactionCategoryView category = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "General Category Both",
                TransactionCategoryKind.BOTH,
                null,
                true
        ));
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Category Test Wallet",
                WalletType.CASH,
                "USD",
                new BigDecimal("100.0000"),
                null,
                true
        ));

        // In single caller transaction: update category to INCOME, then attempt EXPENSE transaction
        assertThatThrownBy(() -> transactionTemplate.execute(status -> {
            TransactionCategoryView updatedCat = categoryOperations.updateCategory(new UpdateTransactionCategoryCommand(
                    category.id(),
                    "General Category Income Only",
                    TransactionCategoryKind.INCOME,
                    null,
                    true
            ));
            assertThat(updatedCat.kind()).isEqualTo(TransactionCategoryKind.INCOME);

            // Must reject because EXPENSE is incompatible with INCOME category
            transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                    FinancialTransactionType.EXPENSE,
                    FinancialTransactionStatus.POSTED,
                    category.id(),
                    "Incompatible expense",
                    null,
                    Instant.parse("2026-10-04T10:00:00Z"),
                    null,
                    List.of(new FinancialTransactionEntryInput(wallet.id(), new BigDecimal("-1.0000")))
            ));
            return null;
        })).isInstanceOf(InvalidFinancialTransactionException.class)
                .hasMessageContaining("is incompatible with EXPENSE transaction");

        // Fresh verification: category kind remains BOTH in DB, no transaction created
        TransactionCategory committedCat = categoryRepository.findById(category.id()).orElseThrow();
        assertThat(committedCat.getKind()).isEqualTo(TransactionCategoryKind.BOTH);
        assertThat(committedCat.getName()).isEqualTo("General Category Both");
        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("FR11-9: Category kind update to INCOME followed by incompatible EXPENSE rule rejects and rolls back atomically")
    void categoryKindUpdateFollowedByIncompatibleExpenseRuleRejectsAtomicallyAndRollsBack() {
        TransactionCategoryView category = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Rule Category Both",
                TransactionCategoryKind.BOTH,
                null,
                true
        ));
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Rule Category Wallet",
                WalletType.CASH,
                "USD",
                new BigDecimal("100.0000"),
                null,
                true
        ));

        // In single caller transaction: update category to INCOME, then attempt EXPENSE rule
        assertThatThrownBy(() -> transactionTemplate.execute(status -> {
            categoryOperations.updateCategory(new UpdateTransactionCategoryCommand(
                    category.id(),
                    "Rule Category Income Only",
                    TransactionCategoryKind.INCOME,
                    null,
                    true
            ));

            ruleOperations.createRule(new CreateRecurringTransactionRuleCommand(
                    "Incompatible Expense Rule",
                    FinancialTransactionType.EXPENSE,
                    category.id(),
                    RecurringPostingMode.REQUIRE_CONFIRMATION,
                    RecurrenceFrequency.MONTHLY,
                    1,
                    1,
                    null,
                    LocalDate.parse("2026-10-01"),
                    null,
                    LocalTime.of(12, 0),
                    Instant.parse("2026-10-05T12:00:00Z"),
                    "Incompatible expense rule",
                    null,
                    true,
                    Set.of(),
                    List.of(new RecurringRuleEntryInput(wallet.id(), new BigDecimal("-10.0000")))
            ));
            return null;
        })).isInstanceOf(InvalidRecurringTransactionRuleException.class)
                .hasMessageContaining("is incompatible with EXPENSE recurring rule");

        // Fresh verification: category kind remains BOTH, no rule created
        TransactionCategory committedCat = categoryRepository.findById(category.id()).orElseThrow();
        assertThat(committedCat.getKind()).isEqualTo(TransactionCategoryKind.BOTH);
        assertThat(ruleRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("FR11-9: Category kind update followed by compatible assignment preserves new category kind")
    void categoryKindUpdateFollowedByCompatibleAssignmentPreservesNewKind() {
        TransactionCategoryView category = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Flexible Category",
                TransactionCategoryKind.BOTH,
                null,
                true
        ));
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Compatible Test Wallet",
                WalletType.BANK_ACCOUNT,
                "USD",
                new BigDecimal("200.0000"),
                null,
                true
        ));

        // In single caller transaction: update category to INCOME, then create compatible INCOME transaction and rule
        transactionTemplate.execute(status -> {
            categoryOperations.updateCategory(new UpdateTransactionCategoryCommand(
                    category.id(),
                    "Specialized Income Category",
                    TransactionCategoryKind.INCOME,
                    null,
                    true
            ));

            FinancialTransactionView tx = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                    FinancialTransactionType.INCOME,
                    FinancialTransactionStatus.POSTED,
                    category.id(),
                    "Compatible income tx",
                    null,
                    Instant.parse("2026-10-04T10:00:00Z"),
                    null,
                    List.of(new FinancialTransactionEntryInput(wallet.id(), new BigDecimal("10.0000")))
            ));
            assertThat(tx).isNotNull();

            RecurringTransactionRuleView rule = ruleOperations.createRule(new CreateRecurringTransactionRuleCommand(
                    "Compatible Income Rule",
                    FinancialTransactionType.INCOME,
                    category.id(),
                    RecurringPostingMode.REQUIRE_CONFIRMATION,
                    RecurrenceFrequency.MONTHLY,
                    1,
                    1,
                    null,
                    LocalDate.parse("2026-10-01"),
                    null,
                    LocalTime.of(10, 0),
                    Instant.parse("2026-11-01T10:00:00Z"),
                    "Compatible income rule",
                    null,
                    true,
                    Set.of(),
                    List.of(new RecurringRuleEntryInput(wallet.id(), new BigDecimal("20.0000")))
            ));
            assertThat(rule).isNotNull();
            return null;
        });

        // Fresh verification: category kind is INCOME, tx and rule committed
        TransactionCategory committedCat = categoryRepository.findById(category.id()).orElseThrow();
        assertThat(committedCat.getKind()).isEqualTo(TransactionCategoryKind.INCOME);
        assertThat(committedCat.getName()).isEqualTo("Specialized Income Category");
        assertThat(transactionRepository.findAll()).hasSize(1);
        assertThat(ruleRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("FR11-9: Transaction scalar update followed by soft-delete in same caller tx preserves scalar updates")
    void transactionScalarUpdateFollowedBySoftDeletePreservesScalars() {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Tx Lifecycle Wallet",
                WalletType.BANK_ACCOUNT,
                "USD",
                new BigDecimal("500.0000"),
                null,
                true
        ));
        TransactionCategoryView category = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Tx Category",
                TransactionCategoryKind.INCOME,
                null,
                true
        ));
        FinancialTransactionView initialTx = transactionOperations.createTransaction(new CreateFinancialTransactionCommand(
                FinancialTransactionType.INCOME,
                FinancialTransactionStatus.PENDING,
                category.id(),
                "Initial Description",
                "Initial notes",
                Instant.parse("2026-10-04T10:00:00Z"),
                null,
                List.of(new FinancialTransactionEntryInput(wallet.id(), new BigDecimal("50.0000")))
        ));

        // In single caller transaction: update scalars to POSTED with new description, then soft-delete
        transactionTemplate.execute(status -> {
            FinancialTransactionView updatedTx = transactionOperations.updateTransaction(new UpdateFinancialTransactionCommand(
                    initialTx.id(),
                    FinancialTransactionType.INCOME,
                    FinancialTransactionStatus.POSTED,
                    category.id(),
                    "Updated Description Final",
                    "Updated notes final",
                    Instant.parse("2026-10-04T11:00:00Z"),
                    null,
                    List.of(new FinancialTransactionEntryInput(wallet.id(), new BigDecimal("75.0000")))
            ));
            assertThat(updatedTx.description()).isEqualTo("Updated Description Final");
            assertThat(updatedTx.status()).isEqualTo(FinancialTransactionStatus.POSTED);

            // Guarded soft-delete reloads transaction under pessimistic lock
            FinancialTransactionView deletedTx = transactionOperations.softDeleteTransaction(initialTx.id());
            assertThat(deletedTx.deletedAt()).isNotNull();
            return null;
        });

        // Fresh verification: scalars from update are preserved on the soft-deleted transaction
        FinancialTransaction committedTx = transactionRepository.findById(initialTx.id()).orElseThrow();
        assertThat(committedTx.getDescription()).isEqualTo("Updated Description Final");
        assertThat(committedTx.getStatus()).isEqualTo(FinancialTransactionStatus.POSTED);
        assertThat(committedTx.getNotes()).isEqualTo("Updated notes final");
        assertThat(committedTx.getDeletedAt()).isNotNull();

        List<FinancialTransactionEntry> entries = transactionEntryRepository.findByTransactionIdOrderByIdAsc(initialTx.id());
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getAmountDelta()).isEqualByComparingTo("75.0000");
    }

    @Test
    @DisplayName("FR11-9: Recurring rule scalar update followed by Subscription assignment in same caller tx preserves scalars and links")
    void recurringRuleScalarUpdateFollowedBySubscriptionAssignmentPreservesScalarsAndLinks() {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Sub Wallet",
                WalletType.BANK_ACCOUNT,
                "USD",
                new BigDecimal("100.0000"),
                null,
                true
        ));
        TransactionCategoryView category = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Sub Cat",
                TransactionCategoryKind.EXPENSE,
                null,
                true
        ));
        RecurringTransactionRuleView initialRule = ruleOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Original Rule Name",
                FinancialTransactionType.EXPENSE,
                category.id(),
                RecurringPostingMode.REQUIRE_CONFIRMATION,
                RecurrenceFrequency.MONTHLY,
                1,
                10,
                null,
                LocalDate.parse("2026-10-01"),
                null,
                LocalTime.of(8, 0),
                Instant.parse("2026-10-10T08:00:00Z"),
                "Original rule description",
                null,
                true,
                Set.of(),
                List.of(new RecurringRuleEntryInput(wallet.id(), new BigDecimal("-15.0000")))
        ));

        // In single caller transaction: update rule scalars, then link to new subscription
        transactionTemplate.execute(status -> {
            RecurringTransactionRuleView updatedRule = ruleOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                    initialRule.id(),
                    "Mutated Rule Name",
                    FinancialTransactionType.EXPENSE,
                    category.id(),
                    RecurringPostingMode.REQUIRE_CONFIRMATION,
                    RecurrenceFrequency.MONTHLY,
                    2,
                    15,
                    null,
                    LocalDate.parse("2026-10-01"),
                    null,
                    LocalTime.of(8, 0),
                    Instant.parse("2026-10-15T08:00:00Z"),
                    "Mutated rule description",
                    "Mutated notes",
                    true,
                    Set.of(),
                    List.of(new RecurringRuleEntryInput(wallet.id(), new BigDecimal("-30.0000")))
            ));
            assertThat(updatedRule.name()).isEqualTo("Mutated Rule Name");
            assertThat(updatedRule.intervalCount()).isEqualTo(2);

            // Creating subscription locks and refreshes recurring rule
            SubscriptionView sub = subscriptionOperations.createSubscription(new CreateSubscriptionCommand(
                    "VPN Service",
                    "Secure VPN Co",
                    new BigDecimal("30.0000"),
                    "USD",
                    BillingCycle.MONTHLY,
                    1,
                    null,
                    LocalDate.parse("2026-11-01"),
                    true,
                    wallet.id(),
                    initialRule.id(),
                    null,
                    null,
                    true
            ));
            assertThat(sub.recurringRuleId()).isEqualTo(initialRule.id());
            return null;
        });

        // Fresh verification: rule scalars preserved, subscription linked
        RecurringTransactionRule committedRule = ruleRepository.findById(initialRule.id()).orElseThrow();
        assertThat(committedRule.getName()).isEqualTo("Mutated Rule Name");
        assertThat(committedRule.getDescription()).isEqualTo("Mutated rule description");
        assertThat(committedRule.getIntervalCount()).isEqualTo(2);

        Subscription committedSub = subscriptionRepository.findAll().get(0);
        assertThat(committedSub.getRecurringRuleId()).isEqualTo(initialRule.id());
    }

    @Test
    @DisplayName("FR11-9: Recurring rule scalar update followed by soft-delete in same caller tx preserves scalar updates")
    void recurringRuleScalarUpdateFollowedBySoftDeletePreservesScalars() {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand(
                "Rule Lifecycle Wallet",
                WalletType.CASH,
                "USD",
                new BigDecimal("50.0000"),
                null,
                true
        ));
        TransactionCategoryView category = categoryOperations.createCategory(new CreateTransactionCategoryCommand(
                "Lifecycle Cat",
                TransactionCategoryKind.INCOME,
                null,
                true
        ));
        RecurringTransactionRuleView rule = ruleOperations.createRule(new CreateRecurringTransactionRuleCommand(
                "Rule To Delete Later",
                FinancialTransactionType.INCOME,
                category.id(),
                RecurringPostingMode.REQUIRE_CONFIRMATION,
                RecurrenceFrequency.MONTHLY,
                1,
                1,
                null,
                LocalDate.parse("2026-10-01"),
                null,
                LocalTime.of(9, 0),
                Instant.parse("2026-11-01T09:00:00Z"),
                "Initial note",
                null,
                true,
                Set.of(),
                List.of(new RecurringRuleEntryInput(wallet.id(), new BigDecimal("10.0000")))
        ));

        // In single caller transaction: update rule name and description, then soft delete
        transactionTemplate.execute(status -> {
            ruleOperations.updateRule(new UpdateRecurringTransactionRuleCommand(
                    rule.id(),
                    "Modified Rule Before Delete",
                    FinancialTransactionType.INCOME,
                    category.id(),
                    RecurringPostingMode.REQUIRE_CONFIRMATION,
                    RecurrenceFrequency.MONTHLY,
                    1,
                    1,
                    null,
                    LocalDate.parse("2026-10-01"),
                    null,
                    LocalTime.of(9, 0),
                    Instant.parse("2026-11-01T09:00:00Z"),
                    "Updated description before delete",
                    "Updated note before delete",
                    true,
                    Set.of(),
                    List.of(new RecurringRuleEntryInput(wallet.id(), new BigDecimal("10.0000")))
            ));

            // Soft-delete locks and refreshes the rule
            RecurringTransactionRuleView deleted = ruleOperations.softDeleteRule(rule.id());
            assertThat(deleted.deletedAt()).isNotNull();
            return null;
        });

        // Fresh verification: scalars preserved on soft-deleted rule
        RecurringTransactionRule committedRule = ruleRepository.findById(rule.id()).orElseThrow();
        assertThat(committedRule.getName()).isEqualTo("Modified Rule Before Delete");
        assertThat(committedRule.getDescription()).isEqualTo("Updated description before delete");
        assertThat(committedRule.getNotes()).isEqualTo("Updated note before delete");
        assertThat(committedRule.getDeletedAt()).isNotNull();
    }
}
