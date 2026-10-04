package com.vhvkhangg.personalprivatevault.finance.internal.application;

import com.vhvkhangg.personalprivatevault.finance.category.exception.TransactionCategoryNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.FinancialTransaction;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringTransactionRule;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.Subscription;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.TransactionCategory;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.Wallet;
import com.vhvkhangg.personalprivatevault.finance.recurring.exception.RecurringTransactionRuleNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.subscription.exception.SubscriptionNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.transaction.exception.FinancialTransactionNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.wallet.exception.WalletNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Coordinates pessimistic write locks and fresh-state reloading across finance aggregate boundaries.
 */
@Component
@RequiredArgsConstructor
public class FinanceLockManager {

    private final EntityManager entityManager;

    public Wallet lockAndRefreshWallet(Long walletId) {
        if (walletId == null) {
            throw new WalletNotFoundException(null);
        }
        Wallet wallet = entityManager.find(Wallet.class, walletId, LockModeType.PESSIMISTIC_WRITE);
        if (wallet == null) {
            throw new WalletNotFoundException(walletId);
        }
        entityManager.refresh(wallet, LockModeType.PESSIMISTIC_WRITE);
        return wallet;
    }

    public List<Wallet> lockAndRefreshWalletsAscending(Collection<Long> walletIds) {
        if (walletIds == null || walletIds.isEmpty()) {
            return List.of();
        }
        List<Long> sortedIds = walletIds.stream().distinct().sorted().toList();
        List<Wallet> wallets = new ArrayList<>(sortedIds.size());
        for (Long id : sortedIds) {
            wallets.add(lockAndRefreshWallet(id));
        }
        return wallets;
    }

    public TransactionCategory lockAndRefreshCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        TransactionCategory category = entityManager.find(TransactionCategory.class, categoryId, LockModeType.PESSIMISTIC_WRITE);
        if (category == null) {
            throw new TransactionCategoryNotFoundException(categoryId);
        }
        entityManager.refresh(category, LockModeType.PESSIMISTIC_WRITE);
        return category;
    }

    public FinancialTransaction lockAndRefreshTransaction(Long transactionId) {
        if (transactionId == null) {
            throw new FinancialTransactionNotFoundException(null);
        }
        FinancialTransaction transaction = entityManager.find(FinancialTransaction.class, transactionId, LockModeType.PESSIMISTIC_WRITE);
        if (transaction == null) {
            throw new FinancialTransactionNotFoundException(transactionId);
        }
        entityManager.refresh(transaction, LockModeType.PESSIMISTIC_WRITE);
        return transaction;
    }

    public RecurringTransactionRule lockAndRefreshRecurringRule(Long ruleId) {
        if (ruleId == null) {
            throw new RecurringTransactionRuleNotFoundException(null);
        }
        RecurringTransactionRule rule = entityManager.find(RecurringTransactionRule.class, ruleId, LockModeType.PESSIMISTIC_WRITE);
        if (rule == null) {
            throw new RecurringTransactionRuleNotFoundException(ruleId);
        }
        entityManager.refresh(rule, LockModeType.PESSIMISTIC_WRITE);
        return rule;
    }

    public Subscription lockAndRefreshSubscription(Long subscriptionId) {
        if (subscriptionId == null) {
            throw new SubscriptionNotFoundException(null);
        }
        Subscription subscription = entityManager.find(Subscription.class, subscriptionId, LockModeType.PESSIMISTIC_WRITE);
        if (subscription == null) {
            throw new SubscriptionNotFoundException(subscriptionId);
        }
        entityManager.refresh(subscription, LockModeType.PESSIMISTIC_WRITE);
        return subscription;
    }
}
