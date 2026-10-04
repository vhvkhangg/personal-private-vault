package com.vhvkhangg.personalprivatevault.finance.wallet;

import com.vhvkhangg.personalprivatevault.finance.wallet.command.CreateWalletCommand;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.UpdateWalletCommand;
import com.vhvkhangg.personalprivatevault.finance.view.WalletView;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public capability operations for wallets.
 */
public interface WalletOperations {

    /**
     * Creates a new wallet.
     *
     * @param command creation command
     * @return created wallet view
     */
    WalletView createWallet(CreateWalletCommand command);

    /**
     * Updates an existing wallet.
     *
     * @param command update command
     * @return updated wallet view
     */
    WalletView updateWallet(UpdateWalletCommand command);

    /**
     * Finds a non-deleted wallet by its ID.
     *
     * @param id wallet identifier
     * @return wallet view
     */
    WalletView findWalletById(Long id);

    /**
     * Finds non-deleted wallets ordered by name ASC, id ASC.
     *
     * @param activeFilter optional active filter
     * @param limit positive maximum count
     * @return list of wallet views
     */
    List<WalletView> findWallets(Boolean activeFilter, int limit);

    /**
     * Derives the current balance of a non-deleted wallet from its opening balance
     * and posted non-deleted transaction ledger entries.
     *
     * @param walletId wallet identifier
     * @return derived current balance
     */
    BigDecimal currentBalance(Long walletId);

    /**
     * Soft-deletes a wallet. Idempotent.
     *
     * @param id wallet identifier
     * @return wallet view
     */
    WalletView softDeleteWallet(Long id);

    /**
     * Restores a soft-deleted wallet. Idempotent.
     *
     * @param id wallet identifier
     * @return wallet view
     */
    WalletView restoreWallet(Long id);
}
