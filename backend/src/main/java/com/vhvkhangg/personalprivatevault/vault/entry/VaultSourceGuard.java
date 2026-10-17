package com.vhvkhangg.personalprivatevault.vault.entry;

import java.util.Collection;

/**
 * Public capability interface for acquiring transaction-scoped pessimistic write locks on vault_entries rows.
 */
public interface VaultSourceGuard {

    /**
     * Acquires a pessimistic write lock on the specified vault entry row and authoritatively
     * refreshes its lifecycle state in the current transaction.
     *
     * @param vaultEntryId the vault entry ID
     */
    void acquireSourceGuard(Long vaultEntryId);

    /**
     * Acquires pessimistic write locks on the specified vault entry rows in globally sorted
     * ascending numeric ID order to prevent deadlocks.
     *
     * @param vaultEntryIds collection of vault entry IDs
     */
    void acquireSourceGuards(Collection<Long> vaultEntryIds);
}
