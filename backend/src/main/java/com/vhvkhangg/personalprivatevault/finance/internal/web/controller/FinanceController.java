package com.vhvkhangg.personalprivatevault.finance.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.finance.category.TransactionCategoryOperations;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateFinancialTransactionRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateRecurringTransactionRuleRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateSubscriptionRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateTransactionCategoryRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateWalletRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.FinancialTransactionResponse;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.RecurringTransactionRuleResponse;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.SubscriptionResponse;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.TransactionCategoryResponse;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateFinancialTransactionRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateRecurringTransactionRuleRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateSubscriptionRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateTransactionCategoryRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateWalletRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.WalletBalanceResponse;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.WalletResponse;
import com.vhvkhangg.personalprivatevault.finance.internal.web.mapper.FinanceWebMapper;
import com.vhvkhangg.personalprivatevault.finance.recurring.RecurringTransactionRuleOperations;
import com.vhvkhangg.personalprivatevault.finance.subscription.SubscriptionOperations;
import com.vhvkhangg.personalprivatevault.finance.transaction.FinancialTransactionOperations;
import com.vhvkhangg.personalprivatevault.finance.view.FinancialTransactionView;
import com.vhvkhangg.personalprivatevault.finance.view.RecurringTransactionRuleView;
import com.vhvkhangg.personalprivatevault.finance.view.SubscriptionView;
import com.vhvkhangg.personalprivatevault.finance.view.TransactionCategoryView;
import com.vhvkhangg.personalprivatevault.finance.view.WalletView;
import com.vhvkhangg.personalprivatevault.finance.wallet.WalletOperations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
@Validated
@Tag(name = "Finance", description = "Finance ledger: wallets, categories, transactions, recurring rules, and subscriptions")
public class FinanceController {

    private final WalletOperations walletOperations;
    private final TransactionCategoryOperations categoryOperations;
    private final FinancialTransactionOperations transactionOperations;
    private final RecurringTransactionRuleOperations recurringRuleOperations;
    private final SubscriptionOperations subscriptionOperations;

    // --- Wallets ---

    @PostMapping("/wallets")
    @Operation(summary = "Create wallet", operationId = "createWallet")
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        WalletView created = walletOperations.createWallet(FinanceWebMapper.toCommand(request));
        return ApiResponses.created(FinanceWebMapper.toResponse(created));
    }

    @GetMapping("/wallets/{id}")
    @Operation(summary = "Get wallet by ID", operationId = "getWallet")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(@PathVariable Long id) {
        WalletView view = walletOperations.findWalletById(id);
        return ApiResponses.ok(FinanceWebMapper.toResponse(view));
    }

    @PutMapping("/wallets/{id}")
    @Operation(summary = "Update wallet", operationId = "updateWallet")
    public ResponseEntity<ApiResponse<WalletResponse>> updateWallet(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWalletRequest request) {
        WalletView updated = walletOperations.updateWallet(FinanceWebMapper.toCommand(id, request));
        return ApiResponses.ok(FinanceWebMapper.toResponse(updated));
    }

    @GetMapping("/wallets")
    @Operation(summary = "Find wallets", operationId = "findWallets")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> findWallets(
            @RequestParam(required = false) Boolean activeFilter,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<WalletView> wallets = walletOperations.findWallets(activeFilter, limit);
        return ApiResponses.ok(wallets.stream().map(FinanceWebMapper::toResponse).toList());
    }

    @GetMapping("/wallets/{id}/balance")
    @Operation(summary = "Get derived wallet balance", operationId = "getWalletBalance")
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> getWalletBalance(@PathVariable Long id) {
        BigDecimal balance = walletOperations.currentBalance(id);
        return ApiResponses.ok(new WalletBalanceResponse(id, balance));
    }

    @DeleteMapping("/wallets/{id}")
    @Operation(summary = "Soft-delete wallet", operationId = "softDeleteWallet")
    public ResponseEntity<ApiResponse<WalletResponse>> softDeleteWallet(@PathVariable Long id) {
        WalletView view = walletOperations.softDeleteWallet(id);
        return ApiResponses.ok(FinanceWebMapper.toResponse(view));
    }

    @PostMapping("/wallets/{id}/restore")
    @Operation(summary = "Restore soft-deleted wallet", operationId = "restoreWallet")
    public ResponseEntity<ApiResponse<WalletResponse>> restoreWallet(@PathVariable Long id) {
        WalletView view = walletOperations.restoreWallet(id);
        return ApiResponses.ok(FinanceWebMapper.toResponse(view));
    }

    // --- Categories ---

    @PostMapping("/categories")
    @Operation(summary = "Create transaction category", operationId = "createTransactionCategory")
    public ResponseEntity<ApiResponse<TransactionCategoryResponse>> createCategory(
            @Valid @RequestBody CreateTransactionCategoryRequest request) {
        TransactionCategoryView created = categoryOperations.createCategory(FinanceWebMapper.toCommand(request));
        return ApiResponses.created(FinanceWebMapper.toResponse(created));
    }

    @GetMapping("/categories/{id}")
    @Operation(summary = "Get transaction category by ID", operationId = "getTransactionCategory")
    public ResponseEntity<ApiResponse<TransactionCategoryResponse>> getCategory(@PathVariable Long id) {
        TransactionCategoryView view = categoryOperations.findCategoryById(id);
        return ApiResponses.ok(FinanceWebMapper.toResponse(view));
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update transaction category", operationId = "updateTransactionCategory")
    public ResponseEntity<ApiResponse<TransactionCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionCategoryRequest request) {
        TransactionCategoryView updated = categoryOperations.updateCategory(FinanceWebMapper.toCommand(id, request));
        return ApiResponses.ok(FinanceWebMapper.toResponse(updated));
    }

    @GetMapping("/categories")
    @Operation(summary = "Find transaction categories", operationId = "findTransactionCategories")
    public ResponseEntity<ApiResponse<List<TransactionCategoryResponse>>> findCategories(
            @RequestParam(required = false) Boolean activeFilter,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<TransactionCategoryView> categories = categoryOperations.findCategories(activeFilter, limit);
        return ApiResponses.ok(categories.stream().map(FinanceWebMapper::toResponse).toList());
    }

    // --- Financial Transactions ---

    @PostMapping("/transactions")
    @Operation(summary = "Create financial transaction", operationId = "createFinancialTransaction")
    public ResponseEntity<ApiResponse<FinancialTransactionResponse>> createTransaction(
            @Valid @RequestBody CreateFinancialTransactionRequest request) {
        FinancialTransactionView created = transactionOperations.createTransaction(FinanceWebMapper.toCommand(request));
        return ApiResponses.created(FinanceWebMapper.toResponse(created));
    }

    @GetMapping("/transactions/{id}")
    @Operation(summary = "Get financial transaction by ID", operationId = "getFinancialTransaction")
    public ResponseEntity<ApiResponse<FinancialTransactionResponse>> getTransaction(@PathVariable Long id) {
        FinancialTransactionView view = transactionOperations.findTransactionById(id);
        return ApiResponses.ok(FinanceWebMapper.toResponse(view));
    }

    @PutMapping("/transactions/{id}")
    @Operation(summary = "Update financial transaction", operationId = "updateFinancialTransaction")
    public ResponseEntity<ApiResponse<FinancialTransactionResponse>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFinancialTransactionRequest request) {
        FinancialTransactionView updated = transactionOperations.updateTransaction(FinanceWebMapper.toCommand(id, request));
        return ApiResponses.ok(FinanceWebMapper.toResponse(updated));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Find financial transactions", operationId = "findFinancialTransactions")
    public ResponseEntity<ApiResponse<List<FinancialTransactionResponse>>> findTransactions(
            @RequestParam(required = false) Long walletId,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<FinancialTransactionView> transactions = walletId != null
                ? transactionOperations.findRecentTransactionsByWallet(walletId, limit)
                : transactionOperations.findRecentTransactions(limit);
        return ApiResponses.ok(transactions.stream().map(FinanceWebMapper::toResponse).toList());
    }

    @DeleteMapping("/transactions/{id}")
    @Operation(summary = "Soft-delete financial transaction", operationId = "softDeleteFinancialTransaction")
    public ResponseEntity<ApiResponse<FinancialTransactionResponse>> softDeleteTransaction(@PathVariable Long id) {
        FinancialTransactionView view = transactionOperations.softDeleteTransaction(id);
        return ApiResponses.ok(FinanceWebMapper.toResponse(view));
    }

    @PostMapping("/transactions/{id}/restore")
    @Operation(summary = "Restore soft-deleted financial transaction", operationId = "restoreFinancialTransaction")
    public ResponseEntity<ApiResponse<FinancialTransactionResponse>> restoreTransaction(@PathVariable Long id) {
        FinancialTransactionView view = transactionOperations.restoreTransaction(id);
        return ApiResponses.ok(FinanceWebMapper.toResponse(view));
    }

    // --- Recurring Rules ---

    @PostMapping("/recurring-rules")
    @Operation(summary = "Create recurring transaction rule", operationId = "createRecurringRule")
    public ResponseEntity<ApiResponse<RecurringTransactionRuleResponse>> createRule(
            @Valid @RequestBody CreateRecurringTransactionRuleRequest request) {
        RecurringTransactionRuleView created = recurringRuleOperations.createRule(FinanceWebMapper.toCommand(request));
        return ApiResponses.created(FinanceWebMapper.toResponse(created));
    }

    @GetMapping("/recurring-rules/{id}")
    @Operation(summary = "Get recurring transaction rule by ID", operationId = "getRecurringRule")
    public ResponseEntity<ApiResponse<RecurringTransactionRuleResponse>> getRule(@PathVariable Long id) {
        RecurringTransactionRuleView view = recurringRuleOperations.findRuleById(id);
        return ApiResponses.ok(FinanceWebMapper.toResponse(view));
    }

    @PutMapping("/recurring-rules/{id}")
    @Operation(summary = "Update recurring transaction rule", operationId = "updateRecurringRule")
    public ResponseEntity<ApiResponse<RecurringTransactionRuleResponse>> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecurringTransactionRuleRequest request) {
        RecurringTransactionRuleView updated = recurringRuleOperations.updateRule(FinanceWebMapper.toCommand(id, request));
        return ApiResponses.ok(FinanceWebMapper.toResponse(updated));
    }

    @GetMapping("/recurring-rules")
    @Operation(summary = "Find recurring transaction rules", operationId = "findRecurringRules")
    public ResponseEntity<ApiResponse<List<RecurringTransactionRuleResponse>>> findRules(
            @RequestParam(required = false) Boolean activeFilter,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<RecurringTransactionRuleView> rules = recurringRuleOperations.findRules(activeFilter, limit);
        return ApiResponses.ok(rules.stream().map(FinanceWebMapper::toResponse).toList());
    }

    @GetMapping("/recurring-rules/due")
    @Operation(summary = "Find due recurring transaction rules", operationId = "findDueRecurringRules")
    public ResponseEntity<ApiResponse<List<RecurringTransactionRuleResponse>>> findDueRules(
            @RequestParam Instant cutoff,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<RecurringTransactionRuleView> rules = recurringRuleOperations.findDueRules(cutoff, limit);
        return ApiResponses.ok(rules.stream().map(FinanceWebMapper::toResponse).toList());
    }

    @DeleteMapping("/recurring-rules/{id}")
    @Operation(summary = "Soft-delete recurring transaction rule", operationId = "softDeleteRecurringRule")
    public ResponseEntity<ApiResponse<RecurringTransactionRuleResponse>> softDeleteRule(@PathVariable Long id) {
        RecurringTransactionRuleView view = recurringRuleOperations.softDeleteRule(id);
        return ApiResponses.ok(FinanceWebMapper.toResponse(view));
    }

    @PostMapping("/recurring-rules/{id}/restore")
    @Operation(summary = "Restore soft-deleted recurring transaction rule", operationId = "restoreRecurringRule")
    public ResponseEntity<ApiResponse<RecurringTransactionRuleResponse>> restoreRule(@PathVariable Long id) {
        RecurringTransactionRuleView view = recurringRuleOperations.restoreRule(id);
        return ApiResponses.ok(FinanceWebMapper.toResponse(view));
    }

    // --- Subscriptions ---

    @PostMapping("/subscriptions")
    @Operation(summary = "Create subscription", operationId = "createSubscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        SubscriptionView created = subscriptionOperations.createSubscription(FinanceWebMapper.toCommand(request));
        return ApiResponses.created(FinanceWebMapper.toResponse(created));
    }

    @GetMapping("/subscriptions/{id}")
    @Operation(summary = "Get subscription by ID", operationId = "getSubscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(@PathVariable Long id) {
        SubscriptionView view = subscriptionOperations.findSubscriptionById(id);
        return ApiResponses.ok(FinanceWebMapper.toResponse(view));
    }

    @PutMapping("/subscriptions/{id}")
    @Operation(summary = "Update subscription", operationId = "updateSubscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubscriptionRequest request) {
        SubscriptionView updated = subscriptionOperations.updateSubscription(FinanceWebMapper.toCommand(id, request));
        return ApiResponses.ok(FinanceWebMapper.toResponse(updated));
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "Find active subscriptions", operationId = "findActiveSubscriptions")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> findSubscriptions(
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<SubscriptionView> subscriptions = subscriptionOperations.findActiveSubscriptions(limit);
        return ApiResponses.ok(subscriptions.stream().map(FinanceWebMapper::toResponse).toList());
    }

    @DeleteMapping("/subscriptions/{id}")
    @Operation(summary = "Soft-delete subscription", operationId = "softDeleteSubscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> softDeleteSubscription(@PathVariable Long id) {
        SubscriptionView view = subscriptionOperations.softDeleteSubscription(id);
        return ApiResponses.ok(FinanceWebMapper.toResponse(view));
    }

    @PostMapping("/subscriptions/{id}/restore")
    @Operation(summary = "Restore soft-deleted subscription", operationId = "restoreSubscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> restoreSubscription(@PathVariable Long id) {
        SubscriptionView view = subscriptionOperations.restoreSubscription(id);
        return ApiResponses.ok(FinanceWebMapper.toResponse(view));
    }
}
