package com.vhvkhangg.personalprivatevault.finance.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.finance.category.exception.InvalidTransactionCategoryException;
import com.vhvkhangg.personalprivatevault.finance.category.exception.TransactionCategoryConflictException;
import com.vhvkhangg.personalprivatevault.finance.category.exception.TransactionCategoryNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.recurring.exception.InvalidRecurringTransactionRuleException;
import com.vhvkhangg.personalprivatevault.finance.recurring.exception.RecurringTransactionRuleNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.subscription.exception.InvalidSubscriptionException;
import com.vhvkhangg.personalprivatevault.finance.subscription.exception.SubscriptionConflictException;
import com.vhvkhangg.personalprivatevault.finance.subscription.exception.SubscriptionNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.transaction.exception.FinancialTransactionNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.transaction.exception.InvalidFinancialTransactionException;
import com.vhvkhangg.personalprivatevault.finance.wallet.exception.InvalidWalletException;
import com.vhvkhangg.personalprivatevault.finance.wallet.exception.WalletConflictException;
import com.vhvkhangg.personalprivatevault.finance.wallet.exception.WalletNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.finance.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FinanceExceptionAdvice {

    // --- Not Found (404) ---

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleWalletNotFound(WalletNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND", "Wallet not found");
    }

    @ExceptionHandler(TransactionCategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryNotFound(TransactionCategoryNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "TRANSACTION_CATEGORY_NOT_FOUND", "Transaction category not found");
    }

    @ExceptionHandler(FinancialTransactionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTxNotFound(FinancialTransactionNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "FINANCIAL_TRANSACTION_NOT_FOUND", "Financial transaction not found");
    }

    @ExceptionHandler(RecurringTransactionRuleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuleNotFound(RecurringTransactionRuleNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "RECURRING_RULE_NOT_FOUND", "Recurring transaction rule not found");
    }

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSubNotFound(SubscriptionNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "SUBSCRIPTION_NOT_FOUND", "Subscription not found");
    }

    // --- Conflict (409) ---

    @ExceptionHandler(WalletConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleWalletConflict(WalletConflictException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "WALLET_CONFLICT", "Wallet conflict");
    }

    @ExceptionHandler(TransactionCategoryConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryConflict(TransactionCategoryConflictException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "TRANSACTION_CATEGORY_CONFLICT", "Transaction category conflict");
    }

    @ExceptionHandler(SubscriptionConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleSubConflict(SubscriptionConflictException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "SUBSCRIPTION_CONFLICT", "Subscription conflict");
    }

    // --- Unprocessable Content (422) ---

    @ExceptionHandler(InvalidWalletException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidWallet(InvalidWalletException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_WALLET", "Invalid wallet data");
    }

    @ExceptionHandler(InvalidTransactionCategoryException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCategory(InvalidTransactionCategoryException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_TRANSACTION_CATEGORY", "Invalid transaction category data");
    }

    @ExceptionHandler(InvalidFinancialTransactionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTx(InvalidFinancialTransactionException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_FINANCIAL_TRANSACTION", "Invalid financial transaction data");
    }

    @ExceptionHandler(InvalidRecurringTransactionRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRule(InvalidRecurringTransactionRuleException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_RECURRING_RULE", "Invalid recurring transaction rule data");
    }

    @ExceptionHandler(InvalidSubscriptionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSub(InvalidSubscriptionException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_SUBSCRIPTION", "Invalid subscription data");
    }
}
