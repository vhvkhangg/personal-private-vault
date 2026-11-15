package com.vhvkhangg.personalprivatevault.vault.entry;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import com.vhvkhangg.personalprivatevault.vault.view.VaultTrashPageView;

import java.util.Optional;

/** Public synchronous API for shared vault identity and recycle-bin operations. */
public interface VaultEntryOperations extends VaultSourceGuard {
    VaultEntryView create(VaultEntryType entryType);
    Optional<VaultEntryView> find(Long vaultEntryId);
    java.util.List<VaultEntryView> findAll(java.util.Collection<Long> vaultEntryIds);
    VaultEntryView moveToTrash(Long vaultEntryId);
    VaultEntryView restore(Long vaultEntryId);
    VaultTrashPageView listTrash(int limit, String cursor);

    final class InvalidTrashInputException extends IllegalArgumentException {
        public InvalidTrashInputException(String message) {
            super(message);
        }
    }
}
