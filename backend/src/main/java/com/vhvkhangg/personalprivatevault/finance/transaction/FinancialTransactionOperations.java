package com.vhvkhangg.personalprivatevault.finance.transaction;

import com.vhvkhangg.personalprivatevault.finance.transaction.command.CreateFinancialTransactionCommand;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.UpdateFinancialTransactionCommand;
import com.vhvkhangg.personalprivatevault.finance.view.FinancialTransactionView;

import java.util.List;

/**
 * Public capability operations for financial transactions and their ledger entries.
 */
public interface FinancialTransactionOperations {

    /**
     * Creates a new financial transaction and its associated ledger entries.
     *
     * @param command creation command
     * @return created transaction view
     */
    FinancialTransactionView createTransaction(CreateFinancialTransactionCommand command);

    /**
     * Fully updates an existing financial transaction, replacing its scalar fields and ledger entries atomically.
     *
     * @param command update command
     * @return updated transaction view
     */
    FinancialTransactionView updateTransaction(UpdateFinancialTransactionCommand command);

    /**
     * Finds a non-deleted financial transaction by its ID with all ledger entries.
     *
     * @param id transaction identifier
     * @return transaction view
     */
    FinancialTransactionView findTransactionById(Long id);

    /**
     * Finds recent non-deleted financial transactions referencing a specific wallet,
     * ordered by occurred_at DESC, id DESC.
     *
     * @param walletId wallet identifier
     * @param limit positive maximum count
     * @return list of transaction views
     */
    List<FinancialTransactionView> findRecentTransactionsByWallet(Long walletId, int limit);

    /**
     * Finds recent non-deleted financial transactions across all wallets,
     * ordered by occurred_at DESC, id DESC.
     *
     * @param limit positive maximum count
     * @return list of transaction views
     */
    List<FinancialTransactionView> findRecentTransactions(int limit);

    /**
     * Soft-deletes a financial transaction, immediately excluding posted transactions from derived wallet balance. Idempotent.
     *
     * @param id transaction identifier
     * @return transaction view
     */
    FinancialTransactionView softDeleteTransaction(Long id);

    /**
     * Restores a soft-deleted financial transaction, restoring its balance contribution according to status. Idempotent.
     *
     * @param id transaction identifier
     * @return transaction view
     */
    FinancialTransactionView restoreTransaction(Long id);
}
