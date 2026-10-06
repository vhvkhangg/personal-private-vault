package com.vhvkhangg.personalprivatevault.finance.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.finance.category.command.CreateTransactionCategoryCommand;
import com.vhvkhangg.personalprivatevault.finance.category.command.UpdateTransactionCategoryCommand;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateFinancialTransactionRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateRecurringTransactionRuleRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateSubscriptionRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateTransactionCategoryRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateWalletRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.FinancialTransactionEntryRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.FinancialTransactionEntryResponse;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.FinancialTransactionResponse;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.RecurringRuleEntryRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.RecurringRuleEntryResponse;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.RecurringTransactionRuleResponse;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.SubscriptionResponse;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.TransactionCategoryResponse;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateFinancialTransactionRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateRecurringTransactionRuleRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateSubscriptionRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateTransactionCategoryRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateWalletRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.WalletResponse;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.CreateRecurringTransactionRuleCommand;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.RecurringRuleEntryInput;
import com.vhvkhangg.personalprivatevault.finance.recurring.command.UpdateRecurringTransactionRuleCommand;
import com.vhvkhangg.personalprivatevault.finance.subscription.command.CreateSubscriptionCommand;
import com.vhvkhangg.personalprivatevault.finance.subscription.command.UpdateSubscriptionCommand;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.CreateFinancialTransactionCommand;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.FinancialTransactionEntryInput;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.UpdateFinancialTransactionCommand;
import com.vhvkhangg.personalprivatevault.finance.view.FinancialTransactionEntryView;
import com.vhvkhangg.personalprivatevault.finance.view.FinancialTransactionView;
import com.vhvkhangg.personalprivatevault.finance.view.RecurringRuleEntryView;
import com.vhvkhangg.personalprivatevault.finance.view.RecurringTransactionRuleView;
import com.vhvkhangg.personalprivatevault.finance.view.SubscriptionView;
import com.vhvkhangg.personalprivatevault.finance.view.TransactionCategoryView;
import com.vhvkhangg.personalprivatevault.finance.view.WalletView;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.CreateWalletCommand;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.UpdateWalletCommand;

import java.util.List;

public final class FinanceWebMapper {

    private FinanceWebMapper() {}

    // --- Wallet ---

    public static CreateWalletCommand toCommand(CreateWalletRequest request) {
        return new CreateWalletCommand(
                request.name(),
                request.type(),
                request.currencyCode(),
                request.openingBalance(),
                request.notes(),
                request.active()
        );
    }

    public static UpdateWalletCommand toCommand(Long id, UpdateWalletRequest request) {
        return new UpdateWalletCommand(
                id,
                request.name(),
                request.type(),
                request.currencyCode(),
                request.openingBalance(),
                request.notes(),
                request.active()
        );
    }

    public static WalletResponse toResponse(WalletView view) {
        return new WalletResponse(
                view.id(),
                view.name(),
                view.type(),
                view.currencyCode(),
                view.openingBalance(),
                view.notes(),
                view.active(),
                view.createdAt(),
                view.updatedAt(),
                view.deletedAt()
        );
    }

    // --- Category ---

    public static CreateTransactionCategoryCommand toCommand(CreateTransactionCategoryRequest request) {
        return new CreateTransactionCategoryCommand(
                request.name(),
                request.kind(),
                request.parentCategoryId(),
                request.active()
        );
    }

    public static UpdateTransactionCategoryCommand toCommand(Long id, UpdateTransactionCategoryRequest request) {
        return new UpdateTransactionCategoryCommand(
                id,
                request.name(),
                request.kind(),
                request.parentCategoryId(),
                request.active()
        );
    }

    public static TransactionCategoryResponse toResponse(TransactionCategoryView view) {
        return new TransactionCategoryResponse(
                view.id(),
                view.name(),
                view.kind(),
                view.parentCategoryId(),
                view.active(),
                view.createdAt()
        );
    }

    // --- Transaction ---

    public static CreateFinancialTransactionCommand toCommand(CreateFinancialTransactionRequest request) {
        List<FinancialTransactionEntryInput> entries = request.entries() != null
                ? request.entries().stream().map(FinanceWebMapper::toEntryInput).toList()
                : List.of();
        return new CreateFinancialTransactionCommand(
                request.type(),
                request.status(),
                request.categoryId(),
                request.description(),
                request.notes(),
                request.occurredAt(),
                request.exchangeRate(),
                entries
        );
    }

    public static UpdateFinancialTransactionCommand toCommand(Long id, UpdateFinancialTransactionRequest request) {
        List<FinancialTransactionEntryInput> entries = request.entries() != null
                ? request.entries().stream().map(FinanceWebMapper::toEntryInput).toList()
                : List.of();
        return new UpdateFinancialTransactionCommand(
                id,
                request.type(),
                request.status(),
                request.categoryId(),
                request.description(),
                request.notes(),
                request.occurredAt(),
                request.exchangeRate(),
                entries
        );
    }

    public static FinancialTransactionEntryInput toEntryInput(FinancialTransactionEntryRequest request) {
        return new FinancialTransactionEntryInput(
                request.walletId(),
                request.amountDelta()
        );
    }

    public static FinancialTransactionEntryResponse toResponse(FinancialTransactionEntryView view) {
        return new FinancialTransactionEntryResponse(
                view.id(),
                view.transactionId(),
                view.walletId(),
                view.amountDelta()
        );
    }

    public static FinancialTransactionResponse toResponse(FinancialTransactionView view) {
        List<FinancialTransactionEntryResponse> entries = view.entries() != null
                ? view.entries().stream().map(FinanceWebMapper::toResponse).toList()
                : List.of();
        return new FinancialTransactionResponse(
                view.id(),
                view.type(),
                view.status(),
                view.categoryId(),
                view.generatedByRecurringRuleId(),
                view.description(),
                view.notes(),
                view.occurredAt(),
                view.exchangeRate(),
                entries,
                view.createdAt(),
                view.updatedAt(),
                view.deletedAt()
        );
    }

    // --- Recurring Rule ---

    public static CreateRecurringTransactionRuleCommand toCommand(CreateRecurringTransactionRuleRequest request) {
        List<RecurringRuleEntryInput> entries = request.entries() != null
                ? request.entries().stream().map(FinanceWebMapper::toEntryInput).toList()
                : List.of();
        return new CreateRecurringTransactionRuleCommand(
                request.name(),
                request.transactionType(),
                request.categoryId(),
                request.postingMode(),
                request.frequency(),
                request.intervalCount(),
                request.dayOfMonth(),
                request.monthOfYear(),
                request.startDate(),
                request.endDate(),
                request.postingTime(),
                request.nextRunAt(),
                request.description(),
                request.notes(),
                request.active(),
                request.weekdays(),
                entries
        );
    }

    public static UpdateRecurringTransactionRuleCommand toCommand(Long id, UpdateRecurringTransactionRuleRequest request) {
        List<RecurringRuleEntryInput> entries = request.entries() != null
                ? request.entries().stream().map(FinanceWebMapper::toEntryInput).toList()
                : List.of();
        return new UpdateRecurringTransactionRuleCommand(
                id,
                request.name(),
                request.transactionType(),
                request.categoryId(),
                request.postingMode(),
                request.frequency(),
                request.intervalCount(),
                request.dayOfMonth(),
                request.monthOfYear(),
                request.startDate(),
                request.endDate(),
                request.postingTime(),
                request.nextRunAt(),
                request.description(),
                request.notes(),
                request.active(),
                request.weekdays(),
                entries
        );
    }

    public static RecurringRuleEntryInput toEntryInput(RecurringRuleEntryRequest request) {
        return new RecurringRuleEntryInput(
                request.walletId(),
                request.amountDelta()
        );
    }

    public static RecurringRuleEntryResponse toResponse(RecurringRuleEntryView view) {
        return new RecurringRuleEntryResponse(
                view.id(),
                view.recurringRuleId(),
                view.walletId(),
                view.amountDelta()
        );
    }

    public static RecurringTransactionRuleResponse toResponse(RecurringTransactionRuleView view) {
        List<RecurringRuleEntryResponse> entries = view.entries() != null
                ? view.entries().stream().map(FinanceWebMapper::toResponse).toList()
                : List.of();
        return new RecurringTransactionRuleResponse(
                view.id(),
                view.name(),
                view.transactionType(),
                view.categoryId(),
                view.postingMode(),
                view.frequency(),
                view.intervalCount(),
                view.dayOfMonth(),
                view.monthOfYear(),
                view.startDate(),
                view.endDate(),
                view.postingTime(),
                view.nextRunAt(),
                view.description(),
                view.notes(),
                view.active(),
                view.weekdays(),
                entries,
                view.createdAt(),
                view.updatedAt(),
                view.deletedAt()
        );
    }

    // --- Subscription ---

    public static CreateSubscriptionCommand toCommand(CreateSubscriptionRequest request) {
        return new CreateSubscriptionCommand(
                request.name(),
                request.provider(),
                request.priceAmount(),
                request.currencyCode(),
                request.billingCycle(),
                request.billingInterval(),
                request.customCycleDays(),
                request.nextBillingDate(),
                request.autoRenew(),
                request.paymentWalletId(),
                request.recurringRuleId(),
                request.url(),
                request.notes(),
                request.active()
        );
    }

    public static UpdateSubscriptionCommand toCommand(Long id, UpdateSubscriptionRequest request) {
        return new UpdateSubscriptionCommand(
                id,
                request.name(),
                request.provider(),
                request.priceAmount(),
                request.currencyCode(),
                request.billingCycle(),
                request.billingInterval(),
                request.customCycleDays(),
                request.nextBillingDate(),
                request.autoRenew(),
                request.paymentWalletId(),
                request.recurringRuleId(),
                request.url(),
                request.notes(),
                request.active()
        );
    }

    public static SubscriptionResponse toResponse(SubscriptionView view) {
        return new SubscriptionResponse(
                view.id(),
                view.name(),
                view.provider(),
                view.priceAmount(),
                view.currencyCode(),
                view.billingCycle(),
                view.billingInterval(),
                view.customCycleDays(),
                view.nextBillingDate(),
                view.autoRenew(),
                view.paymentWalletId(),
                view.recurringRuleId(),
                view.url(),
                view.notes(),
                view.active(),
                view.createdAt(),
                view.updatedAt(),
                view.deletedAt()
        );
    }
}
